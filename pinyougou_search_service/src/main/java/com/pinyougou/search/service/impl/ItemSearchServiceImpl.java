package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
        //查询·商品列表
        map.putAll(searchList(searchMap));

        //查询商品 分类

        List<String> categoryList = searchCategory(searchMap);
        map.put("categoryList",categoryList );

        //获取 品牌类别 和 规格列表
       /* if (categoryList.size() > 0) {
            map.putAll(searchSpecAndBrand(categoryList.get(0)));//获取到 的是【规格1,2】，list每个元素都一样
        }
*/
        //3.查询品牌和规格列表
        String categoryName=(String)searchMap.get("category");
        if(!"".equals(categoryName)){//如果有分类名称
            map.putAll(searchSpecAndBrand(categoryName));
        }else{//如果没有分类名称，按照第一个查询
            if(categoryList.size()>0){    //此处：caList是回流列表 中 的 数据，而上的 判断则是对指 searchMap的数据
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

     Criteria filterCriteria=new Criteria("item_brand").is(searchMap.get("brand"));
         //通过 不同的专用实现 类，实现上下 统属关系

            FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);

            highlightQuery.addFilterQuery(filterQuery);
        }

        if (searchMap.get("spec") != null) {
            Map<String,String> specMap= (Map) searchMap.get("spec");
            for(String key:specMap.keySet() ){
                Criteria filterCriteria=new Criteria("item_spec_"+key);
                filterCriteria.is( specMap.get(key) );

                FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
                highlightQuery.addFilterQuery(filterQuery);
            }
        }



        //执行 查询索引库
        HighlightPage<TbItem> pageItems = solrTemplate.queryForHighlightPage(highlightQuery, TbItem.class);

        // 选取高亮 模式 的回流 质点，加工
        List<HighlightEntry<TbItem>> highlightEntryList = pageItems.getHighlighted();

        for (HighlightEntry<TbItem> entry : highlightEntryList) {
            //获取 样式单体  sns :获取 域单体
              /*  for (HighlightEntry.Highlight highlight:entry.getHighlights()
                         ) {// 继续 遍历sns  获取 单体
                        List<String> sns = highlight.getSnipplets();
                    }*/
            if (entry.getHighlights().size() > 0 && entry.getHighlights().get(0).getSnipplets().size() > 0) {
                //如果递进的两个列表 都 有值
                String s = entry.getHighlights().get(0).getSnipplets().get(0);
                //获取回流质点 entity
                entry.getEntity().setTitle(s);
            }

        }
        //填充到运载容器中
        map.put("rows", pageItems.getContent());
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
         Long typeTemId = (long)redisTemplate.boundHashOps("itemCat").get(categoryId);
        if (typeTemId !=null) {

            //从缓存中 获取  品牌列表 和 规格列表
            List brandList = (List) redisTemplate.boundHashOps("brandList").get(typeTemId);
            List specList = (List) redisTemplate.boundHashOps("specList").get(typeTemId);

            map.put("brandList", brandList);
            map.put("specList", specList);
        }

        return map;
    }
}
