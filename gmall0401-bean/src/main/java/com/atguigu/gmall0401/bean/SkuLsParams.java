package com.atguigu.gmall0401.bean;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
public class SkuLsParams implements Serializable {
    //用户输入商品名
    String  keyword;
    
    String catalog3Id;

    String[] valueId;

    int pageNo=1;

    int pageSize=20;
}