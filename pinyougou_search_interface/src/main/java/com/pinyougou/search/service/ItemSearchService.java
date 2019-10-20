package com.pinyougou.search.service;

import java.util.List;
import java.util.Map;

public interface ItemSearchService {

    public Map<String,Object> search(Map map);

    /**
     * 导入数据
     * @param list
     */
    public void importList(List list);

    /**
     * 批量删除 索引库 记录
     * @param list
     */
    public void deleteList(List goodsIds);
}
