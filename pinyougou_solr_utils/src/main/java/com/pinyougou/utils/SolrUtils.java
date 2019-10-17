package com.pinyougou.utils;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.TbItemMapper;

import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SolrUtils {
    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private SolrTemplate solrTemplate;

    public static void main(String[] args) {
        //1，加载配置文件，spring工产 实例化工具类，调用 方法，实现索引导入数据
        ApplicationContext context =
                new ClassPathXmlApplicationContext("classpath*:spring/applicationContext*.xml");
        SolrUtils solrUtils = (SolrUtils) context.getBean("solrUtils");//由配置 加载 ，触发 注解扫描，注解标志生效，默认首字母小写

        //调用工具类 方法 ,触发 实例 执行
        solrUtils.importItemData();

        //删除清空索引库
//     solrUtils.deleSolr();


    }

    // 从数据库 查询  ，导入 索引库
    public void importItemData() {
        //从数据库查询
        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo("1");//已审核
        List<TbItem> itemList = itemMapper.selectByExample(example);

        for (TbItem item : itemList) {
            Map specMap= JSON.parseObject(item.getSpec(),Map.class);//将spec字段中的json字符串转换为map
//            item.       //给带注解的字段赋值
            System.out.println(item.getSpec());
//            System.out.println(item.getTitle());
        }
        //对指itemList,批量导入 索引库
        solrTemplate.saveBeans(itemList);
        solrTemplate.commit();
    }
        // 从数据库 查询  ，导入 索引库
    public void deleSolr() {
        Query query =new SimpleQuery("*:*");
        solrTemplate.delete(query);
        solrTemplate.commit();
    }


}
