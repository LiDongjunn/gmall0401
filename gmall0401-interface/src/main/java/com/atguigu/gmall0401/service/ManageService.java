package com.atguigu.gmall0401.service;


import com.atguigu.gmall0401.bean.*;

import java.util.List;
import java.util.Map;

public interface ManageService {

    public List<BaseCatalog1> getCatalog1();

    public List<BaseCatalog2> getCatalog2(String catalog1Id);

    public List<BaseCatalog3> getCatalog3(String catalog2Id);
    //根据三级分类（手机）查平台属性列表
    public List<BaseAttrInfo> getAttrList(String catalog3Id);

    public void saveAttrInfo(BaseAttrInfo baseAttrInfo);

    public BaseAttrInfo getBaseAttrInfo(String attrId);

    public List<BaseSaleAttr> getBaseSaleAttrList();

    public void saveSpuInfo(SpuInfo spuInfo);

    public List<SpuInfo> getSpuList(String catalog3Id);

    public List<SpuImage> getSpuImageList(String spuId);

    //获取销售属性
    public List<SpuSaleAttr> getSpuSaleAttrList(String spuId);
    //保存Sku
    public void saveSkuInfo(SkuInfo skuInfo);
    //查询Sku
    public SkuInfo getSkuInfo(String skuId);

    //根据spuId获取销售属性，并根据sku检查是否选中属性
    public List<SpuSaleAttr> getSpuSaleAttrListCheckSku(String skuId,String spuId);

    //根据spuId,查询已有的sku涉及的销售属性清单
    public Map getSkuValueIdsMap(String spuId);

    //根据多个属性值查平台属性列表
    public List<BaseAttrInfo> getAttrList(List attrValueIdList);



}
