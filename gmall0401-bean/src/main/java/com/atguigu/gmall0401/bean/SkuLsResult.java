package com.atguigu.gmall0401.bean;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
public class SkuLsResult implements Serializable {
    //查询出的商品
    List<SkuLsInfo> skuLsInfoList;

    //查询出的商品总个数
    long total;
    //查询出的商品总页数
    long totalPages;
    //查询出的平台属性列表
    List<String> attrValueIdList;
}