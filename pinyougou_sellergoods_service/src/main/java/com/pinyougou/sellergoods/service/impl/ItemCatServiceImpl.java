package com.pinyougou.sellergoods.service.impl;
import java.util.List;

import com.pinyougou.sellergoods.service.IItemCatService;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.pojo.TbItemCat;
import com.pinyougou.pojo.TbItemCatExample;
import com.pinyougou.pojo.TbItemCatExample.Criteria;


import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Transactional
@Service
public class ItemCatServiceImpl implements IItemCatService {

	@Autowired
	private TbItemCatMapper itemCatMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbItemCat> findAll() {
		return itemCatMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbItemCat> page=   (Page<TbItemCat>) itemCatMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbItemCat itemCat) {
		itemCatMapper.insert(itemCat);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbItemCat itemCat){
		itemCatMapper.updateByPrimaryKey(itemCat);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbItemCat findOne(Long id){
		return itemCatMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			itemCatMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbItemCat itemCat, int pageNum, int pageSize) {

		
		TbItemCatExample example=new TbItemCatExample();
		Criteria criteria = example.createCriteria();
		
		if(itemCat!=null){			
						if(itemCat.getName()!=null && itemCat.getName().length()>0){
				criteria.andNameLike("%"+itemCat.getName()+"%");
			}

			criteria.andParentIdEqualTo(itemCat.getParentId());
	
		}
			PageHelper.startPage(pageNum, pageSize);
		Page<TbItemCat> page= (Page<TbItemCat>)itemCatMapper.selectByExample(example);

			//投注  缓存

			//每次执行查询的时候，一次性读取缓存进行存储 (因为每次增删改都要执行此方法)
			List<TbItemCat> list = findAll();
			for(TbItemCat itemCat1:list){
				redisTemplate.boundHashOps("itemCat").put(
						itemCat1.getName(), itemCat1.getTypeId()
				);
			}
			System.out.println("更新缓存:商品分类表");
			//投注  缓存

			return new PageResult(page.getTotal(), page.getResult());

	}
	@Autowired
	private RedisTemplate redisTemplate;

	@Override
	public List<TbItemCat> findByParentId(Long pid) {
		TbItemCatExample example=new TbItemCatExample();
		Criteria criteria = example.createCriteria();

		criteria.andParentIdEqualTo(pid);

		//投注  缓存


		//每次执行查询的时候，一次性读取缓存进行存储 (因为每次增删改都要执行此方法)
		List<TbItemCat> list = findAll();
		for(TbItemCat itemCat:list){
			redisTemplate.boundHashOps("itemCat").put(
					itemCat.getName(), itemCat.getTypeId()
			);
		}
		System.out.println("更新缓存:商品分类表");
		//投注  缓存

		return itemCatMapper.selectByExample(example);
		}



	}

