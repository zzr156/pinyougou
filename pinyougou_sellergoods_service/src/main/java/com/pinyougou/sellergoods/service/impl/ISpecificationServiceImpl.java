package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbSpecificationMapper;
import com.pinyougou.mapper.TbSpecificationOptionMapper;
import com.pinyougou.pojo.TbSpecification;
import com.pinyougou.pojo.TbSpecificationExample;
import com.pinyougou.pojo.TbSpecificationOption;
import com.pinyougou.pojo.TbSpecificationOptionExample;
import com.pinyougou.pojogroup.Specification;
import com.pinyougou.sellergoods.service.ISpecificationService;

import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Transactional
@Service
public class ISpecificationServiceImpl implements ISpecificationService {


    @Autowired
    private TbSpecificationMapper specificationMapper;
    @Autowired
    private TbSpecificationOptionMapper specificationOptionMapper;

    /**
     * 查询全部
     */
    @Override
    public List<TbSpecification> findAll() {
        return specificationMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbSpecification> page = (Page<TbSpecification>) specificationMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void addWithOption(Specification specification) {

        specificationMapper.insert(specification.getSpecification());//插入规格
        //循环插入规格选项
        for (TbSpecificationOption specificationOption : specification.getSpecificationOptionList()) {
            specificationOption.setSpecId(specification.getSpecification().getId());
            //设置规格ID
            specificationOptionMapper.insert(specificationOption);
        }
    }


    /**
     * 修改
     */
    @Override
    public void update(Specification specification) {
        TbSpecificationOptionExample oExample = new TbSpecificationOptionExample();
        TbSpecificationOptionExample.Criteria oCriteria = oExample.createCriteria();

        TbSpecification specification1 = specification.getSpecification();
        List<TbSpecificationOption> specificationOptionList = specification.getSpecificationOptionList();

        specificationMapper.updateByPrimaryKey(specification1);

        oCriteria.andSpecIdEqualTo(specification1.getId());
        specificationOptionMapper.deleteByExample(oExample);

        for (TbSpecificationOption specificationOption : specificationOptionList) {
            specificationOption.setSpecId(specification1.getId());
            specificationOptionMapper.insert(specificationOption);
        }

//        specificationMapper.updateByPrimaryKey(specification);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public Specification findOne(Long id) {

        Specification specification = new Specification();

        TbSpecificationOptionExample OExample = new TbSpecificationOptionExample();

        TbSpecificationOptionExample.Criteria criteria = OExample.createCriteria();

        criteria.andSpecIdEqualTo(id);
        //填充超类 的属性规格
        specification.setSpecification(specificationMapper.selectByPrimaryKey(id));
        //填充 超类的 属性 规格选项列表
        specification.setSpecificationOptionList(specificationOptionMapper.selectByExample(OExample));
        return specification;
//        return specificationMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            specificationMapper.deleteByPrimaryKey(id);
            TbSpecificationOptionExample oExample = new TbSpecificationOptionExample();
            TbSpecificationOptionExample.Criteria oCriteria = oExample.createCriteria();
            oCriteria.andSpecIdEqualTo(id);

            specificationOptionMapper.deleteByExample(oExample);
        }
    }


    @Override
    public PageResult findPage(TbSpecification specification, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbSpecificationExample example = new TbSpecificationExample();
        TbSpecificationExample.Criteria criteria = example.createCriteria();

        if (specification != null) {
            if (specification.getSpecName() != null && specification.getSpecName().length() > 0) {
                criteria.andSpecNameLike("%" + specification.getSpecName() + "%");
            }

        }

        Page<TbSpecification> page = (Page<TbSpecification>) specificationMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 模板编辑 规格下拉
     */
    @Override
    public List<Map> selectOptionList() {

        return specificationMapper.selectOptionList();
    }
}
