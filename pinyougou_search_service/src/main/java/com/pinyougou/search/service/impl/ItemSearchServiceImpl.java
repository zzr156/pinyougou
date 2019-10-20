package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(timeout = 10000)
public class ItemSearchServiceImpl implements ItemSearchService {
    @Autowired
    private SolrTemplate solrTemplate;


    @Override
    public Map search(Map searchMap) {
        Map map = new HashMap();

        String keywords = (String) searchMap.get("keywords");
        keywords.replace(" ","");
        searchMap.put("keywords", keywords);

        //查询·商品列表
        map.putAll(searchList(searchMap));

        //查询商品 分类

        List<String> categoryList = searchCategory(searchMap);
        map.put("categoryList", categoryList);

        //获取 品牌类别 和 规格列表
       /* if (categoryList.size() > 0) {
            map.putAll(searchSpecAndBrand(categoryList.get(0)));//获取到 的是【规格1,2】，list每个元素都一样
        }
*/
        //3.查询品牌和规格列表
        String categoryName = (String) searchMap.get("category");
        if (!"".equals(categoryName)) {//如果有分类名称
            map.putAll(searchSpecAndBrand(categoryName));
        } else {//如果没有分类名称，按照第一个查询
            if (categoryList.size() > 0) {    //此处：caList是回流列表 中 的 数据，而上的 判断则是对指 searchMap的数据
                map.putAll(searchSpecAndBrand(categoryList.get(0)));
            }
        }

        return map;  //        return map.putAll(searchList(searchMap));  没有返回值 所以不能这样返回
    }


    /**
     * 根据 searchMap中的 查询 分类列表
     */
    private List<String> searchCategory(Map searchMap) {
        List list = new ArrayList();
        Query query = new SimpleQuery("*:*");

        //fieldname=查询的是  分类字段，保障：有域 定义，有实体类字段映射绑定
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));

        //围堵深入，继续 设置 分组细节：
        GroupOptions groupOptions = new GroupOptions();
        groupOptions.addGroupByField("item_category");

        //设置 分组 模式查询
        query.setGroupOptions(groupOptions);
        //拼接条件
        query.addCriteria(criteria);
        GroupPage<TbItem> tbItemGroupPage = solrTemplate.queryForGroupPage(query, TbItem.class);//具有多个域范畴的分组页

        //回流：
        GroupResult<TbItem> groupResult = tbItemGroupPage.getGroupResult("item_category");//单个域的分组页

        Page<GroupEntry<TbItem>> groupEntry = groupResult.getGroupEntries();//单体分组页  转换获取， 分口页

        List<GroupEntry<TbItem>> content = groupEntry.getContent();//一个页有  一个  集合，获取 入口集合

        for (GroupEntry<TbItem> entry : content) {//集合 获取 入口单体，单体获取期望的数据
            //获取 分组 结果，填充到回流输入口list中   ————不需要进行 取出加工，所以直接 entryllst  批量获取
            String groupValue = entry.getGroupValue();
            list.add(groupValue);
        }

        return list;
    }


    //疑问：如何 将筛选 投注到列表中？后端过滤？还是前端隐藏？----还没做关联，此时仅仅将 按钮的字符串信息回流回显。
    //如果关联，应当是，在  原来的商品列表 脉络上拼接 上 分类 字段的条件

    /**
     * 根据 searchMap中的 关键字，查询 SKU商品列表
     *
     * @param searchMap
     * @return
     */
    private Map searchList(Map searchMap) {

        //运载以 map  {keyWord:{}，key2:{}}  ，  将传入参数 在 索引库中 查询 ，运载回流
        Map map = new HashMap();
        //高亮模式：
        HighlightQuery highlightQuery = new SimpleHighlightQuery();

        //投注  条件模型
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        highlightQuery.addCriteria(criteria);

        // 设置 高亮模式
        HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");
        highlightOptions.setSimplePrefix("<em style='color:red'>");
        highlightOptions.setSimplePostfix("</em>");

        highlightQuery.setHighlightOptions(highlightOptions);


        //投注过滤 效果  : 分类 不为空，有 筛选-过滤
        if (!"".equals(searchMap.get("category"))) {
            //过滤用 脉络
            FilterQuery filterQuery = new SimpleQuery();
            Criteria categoryCriteria = new Criteria("item_category").is(searchMap.get("category"));


            //填充：
            filterQuery.addCriteria(categoryCriteria);
            highlightQuery.addFilterQuery(filterQuery);
        }

        if (!"".equals(searchMap.get("brand"))) {

            Criteria filterCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
            //通过 不同的专用实现 类，实现上下 统属关系

            FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);

            highlightQuery.addFilterQuery(filterQuery);
        }

        if (searchMap.get("spec") != null) {
            Map<String, String> specMap = (Map) searchMap.get("spec");
            for (String key : specMap.keySet()) {
                Criteria filterCriteria = new Criteria("item_spec_" + key);
                filterCriteria.is(specMap.get(key));

                FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                highlightQuery.addFilterQuery(filterQuery);
            }
        }

        //1.5.过滤价格
        if (!"".equals(searchMap.get("price"))) {

            Map map1 = searchPrice((String) searchMap.get("price"));
            highlightQuery.addFilterQuery((FilterQuery) map1.get("less"));

            if ((FilterQuery) map1.get("greater") != null) {

                highlightQuery.addFilterQuery((FilterQuery) map1.get("greater"));
                //如果 是* 则 此 值为null,此操作空指针

            }
        }

        //1.6  分页
        Integer pageNo = (Integer) searchMap.get("pageNo");
        if (pageNo==null) {
            pageNo=1;
        }
        Integer pageSize = (Integer) searchMap.get("pageSize");
        if (pageSize==null){
            pageSize=20;
        }
        highlightQuery.setOffset(pageNo);
        highlightQuery.setRows(pageSize);


        //1.7 排序
          //获取 前台传入 参数， 判断分支 排序设置
        String sort = (String)searchMap.get("sort");
        String sortField = (String) searchMap.get("sortField");

        if (sortField != null && sortField != "") {
            if("ASC".equals(sort)){
                //枚举  ，Sort类模型  （solr的 排序模型 ）
                highlightQuery.addSort(new Sort(Sort.Direction.ASC,"item_"+sortField));
            }
            if ("DESC".equals(sort)) {
                highlightQuery.addSort(new Sort(Sort.Direction.DESC, "item_" + sortField));

            }
        }

        //执行 查询索引库
        HighlightPage<TbItem> pageItems = solrTemplate.queryForHighlightPage(highlightQuery, TbItem.class);

        // 选取高亮 模式 的回流 质点，加工
        List<HighlightEntry<TbItem>> highlightEntryList = pageItems.getHighlighted();

        for (HighlightEntry<TbItem> entry : highlightEntryList) {
            //获取 样式单体  sns :获取 域单体
            if (entry.getHighlights().size() > 0 && entry.getHighlights().get(0).getSnipplets().size() > 0) {
                //如果递进的两个列表 都 有值
                String s = entry.getHighlights().get(0).getSnipplets().get(0);
                //获取回流质点 entity
                entry.getEntity().setTitle(s);
            }

        }
        //填充到运载容器中
        map.put("rows", pageItems.getContent());

        //填充 分页 回流数据
        map.put("totalPages", pageItems.getTotalPages());
        map.put("total", pageItems.getTotalElements());
        return map;
    }

    /**
     * 根据 商品分类  获取 商品 品牌列表 和 规格列表
     */
    @Autowired
    private RedisTemplate redisTemplate;

    private Map searchSpecAndBrand(String categoryId) {
        Map map = new HashMap();
        //从缓存中 获取 模板id  以 分类名--模板ID
        Long typeTemId = (long) redisTemplate.boundHashOps("itemCat").get(categoryId);
        if (typeTemId != null) {

            //从缓存中 获取  品牌列表 和 规格列表
            List brandList = (List) redisTemplate.boundHashOps("brandList").get(typeTemId);
            List specList = (List) redisTemplate.boundHashOps("specList").get(typeTemId);

            map.put("brandList", brandList);
            map.put("specList", specList);
        }

        return map;
    }


    /**
     * 过滤 价格
     */
    private Map searchPrice(String priceStr) {
        Map map = new HashMap();
        //字符处理 获取 数据

        String[] price = priceStr.split("-");

        if (!"0".equals(price[0])) {//如果  大于零，即此数值作  查询条件 的 区间 下限(双向限制)
            FilterQuery filterQuery1 = new SimpleQuery();
            Criteria item_price = new Criteria("item_price");
            item_price.greaterThanEqual(price[0]);
            filterQuery1.addCriteria(item_price);
            map.put("less", filterQuery1);
        }
        System.out.println(price[1]);
        if (!"*".equals(price[1])) {// 如果   是*  ，即上限无线 （单向区间）
            FilterQuery filterQuery2 = new SimpleQuery();
            Criteria item_price = new Criteria("item_price");
            item_price.lessThanEqual(price[1]);

            filterQuery2.addCriteria(item_price);
            map.put("greater", filterQuery2);
        }


        return map;

    }
}

