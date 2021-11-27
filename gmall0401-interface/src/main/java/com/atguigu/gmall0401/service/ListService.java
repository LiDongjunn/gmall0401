package com.atguigu.gmall0401.service;

import com.atguigu.gmall0401.bean.SkuLsInfo;
import com.atguigu.gmall0401.bean.SkuLsParams;
import com.atguigu.gmall0401.bean.SkuLsResult;

public interface ListService {
    //保存至es
    public void saveSkuLsInfo(SkuLsInfo skuLsInfo);
    //根据用户所选参数，返回符合参数结果
    public SkuLsResult getSkuLsInfoList(SkuLsParams skuLsParams);
    //点击每个sku商品增加其hotScore值
    public void incrHotScore(String skuId);
}
