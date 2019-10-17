package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.result.ScoredPage;

import java.util.HashMap;
import java.util.Map;

@Service(timeout = 10000)
public class ItemSearchServiceImpl implements ItemSearchService {
@Autowired
private SolrTemplate solrTemplate;

    @Override
    public Map<String, Object> search(Map searchMap) {
        //运载以 map  {keyWord:{}，key2:{}}  ，  将传入参数 在 索引库中 查询 ，运载回流
        Map<String, Object> map = new HashMap<>();
        Query query = new SimpleQuery("*:*");

        //构建条件
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));

        //追加进入
        query.addCriteria(criteria);

        //索引库分页查询
        ScoredPage<TbItem> pageItems = solrTemplate.queryForPage(query, TbItem.class);

        //填充到运载容器中
        map.put("rows", pageItems.getContent());
        return map;
    }
}
