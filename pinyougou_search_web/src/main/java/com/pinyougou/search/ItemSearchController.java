package com.pinyougou.search;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/itemSearch")
public class ItemSearchController {
    @Reference
    private ItemSearchService itemSearchService;

    @RequestMapping("/search")
    public Map<String, Object> search(@RequestBody Map searchMap) {

        Map<String, Object> search = itemSearchService.search(searchMap);
        return search;
    }
}

/*
* @RestController
@RequestMapping("/itemsearch")
public class ItemSearchController {
	@Reference
	private ItemSearchService itemSearchService;

	@RequestMapping("/search")
	public Map<String, Object> search(@RequestBody Map searchMap ){
		return  itemSearchService.search(searchMap);
	}
}
* */