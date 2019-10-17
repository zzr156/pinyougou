package com.pinyougou.content.service.impl;
import java.util.List;

import com.pinyougou.content.service.ContentService;
import com.pinyougou.mapper.TbContentMapper;
import com.pinyougou.pojo.TbContent;
import com.pinyougou.pojo.TbContentExample;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class ContentServiceImpl implements ContentService {

	@Autowired
	private TbContentMapper contentMapper;

	/**
	 * 查询全部
	 */
	@Override
	public List<TbContent> findAll() {
		return contentMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {

		PageHelper.startPage(pageNum, pageSize);
		Page<TbContent> page=   (Page<TbContent>) contentMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbContent content) {
		//增加前 清空 缓存
		redisTemplate.boundHashOps("content").delete(content.getCategoryId());

		contentMapper.insert(content);
	}


	/**
	 * 修改
	 */
	@Override
	public void update(TbContent content){
		//修改 后 需要  清空原来的容器，也要 清理 当前的 容器
		TbContent beforeOne= contentMapper.selectByPrimaryKey(content.getId());
		redisTemplate.boundHashOps("content").delete(beforeOne.getCategoryId());

		//插入后,清空当前 容器，等待第一次被访问(此时的 categoryId 是期望值)
		contentMapper.updateByPrimaryKey(content);
		redisTemplate.boundHashOps("content").delete(content.getCategoryId());
	}

	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbContent findOne(Long id){
		return contentMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {

		for(Long id:ids){//要删除 主体，先 解绑 专属的 缓存容器
			Long categoryId = contentMapper.selectByPrimaryKey(id).getCategoryId();
			redisTemplate.boundHashOps("content").delete(categoryId);
			contentMapper.deleteByPrimaryKey(id);
		}
	}


		@Override
	public PageResult findPage(TbContent content, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);

		TbContentExample example=new TbContentExample();
		TbContentExample.Criteria criteria = example.createCriteria();

		if(content!=null){
						if(content.getTitle()!=null && content.getTitle().length()>0){
				criteria.andTitleLike("%"+content.getTitle()+"%");
			}
			if(content.getUrl()!=null && content.getUrl().length()>0){
				criteria.andUrlLike("%"+content.getUrl()+"%");
			}
			if(content.getPic()!=null && content.getPic().length()>0){
				criteria.andPicLike("%"+content.getPic()+"%");
			}
			if(content.getStatus()!=null && content.getStatus().length()>0){
				criteria.andStatusLike("%"+content.getStatus()+"%");
			}

		}

		Page<TbContent> page= (Page<TbContent>)contentMapper.selectByExample(example);
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Autowired
	private RedisTemplate redisTemplate;
	/**
	 *根据 分类 查询 广告图片
	 */
	@Override
	public List<TbContent> findByCategoryId(long categoryId) {
		//外流脉络----缓存关卡（先从 缓存中 获取）-----数据库

		//获取 专属key  的  缓存 接收容器  ,且返回 操作单体   {content:{categoryId:[list]}}
		List<TbContent> contentList=(List<TbContent>) redisTemplate.boundHashOps("content").get(categoryId);

		if (contentList == null) {
			TbContentExample contentExample = new TbContentExample();
			TbContentExample.Criteria criteria = contentExample.createCriteria();


			criteria.andCategoryIdEqualTo(categoryId);

			criteria.andStatusEqualTo("1");//仅仅查询 启用状态的 广告

			contentExample.setOrderByClause("sort_order");//排序

			 contentList = contentMapper.selectByExample(contentExample);

			 //第一次进数据库查询，且 将结果单体【】 填充到缓存中
			redisTemplate.boundHashOps("content").put(categoryId,contentList);
			return contentList;

		}else {
			System.out.println("缓存中获取");

		}
		return contentList;
        /*
        * 	//根据广告分类ID查询广告列表
		TbContentExample contentExample=new TbContentExample();
		Criteria criteria2 = contentExample.createCriteria();
		criteria2.andCategoryIdEqualTo(categoryId);
		criteria2.andStatusEqualTo("1");//开启状态
		contentExample.setOrderByClause("sort_order");//排序
		return  contentMapper.selectByExample(contentExample);*/
		
	}
}
