package com.zxh.mybatis.mapper;

import com.zxh.mybatis.po.ProductBase;
import com.zxh.mybatis.po.ProductBaseExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ProductBaseMapper {
    int countByExample(ProductBaseExample example);

    int deleteByExample(ProductBaseExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(ProductBase record);

    int insertSelective(ProductBase record);

    List<ProductBase> selectByExample(ProductBaseExample example);

    ProductBase selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") ProductBase record, @Param("example") ProductBaseExample example);

    int updateByExample(@Param("record") ProductBase record, @Param("example") ProductBaseExample example);

    int updateByPrimaryKeySelective(ProductBase record);

    int updateByPrimaryKey(ProductBase record);
}