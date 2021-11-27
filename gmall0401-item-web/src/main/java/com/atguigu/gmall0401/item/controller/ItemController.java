package com.atguigu.gmall0401.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0401.bean.SkuInfo;
import com.atguigu.gmall0401.bean.SpuSaleAttr;
import com.atguigu.gmall0401.config.LoginRequire;
import com.atguigu.gmall0401.service.ListService;
import com.atguigu.gmall0401.service.ManageService;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Controller
@CrossOrigin
public class ItemController {
    @Reference
    ManageService manageService;
    @Reference
    ListService listService;

    @LoginRequire
    @GetMapping("{skuId}.html")
    public String item(@PathVariable("skuId") String skuId, HttpServletRequest request){
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);

        if (skuInfo == null){
            return "item";
        }
        List<SpuSaleAttr> spuSaleAttrList = manageService.getSpuSaleAttrListCheckSku(skuId, skuInfo.getSpuId());


        request.setAttribute("skuInfo",skuInfo);
        request.setAttribute("spuSaleAttrList",spuSaleAttrList);

//        request.setAttribute("gname","<span style=\"color:green\">宝强</span>");

        //得到属性组合与skuid的映射关系，用于页面根据属性组合进行跳转
        Map skuValueIdsMap = manageService.getSkuValueIdsMap(skuInfo.getSpuId());
        String valuesSkuJson = JSON.toJSONString(skuValueIdsMap);
        request.setAttribute("valuesSkuJson",valuesSkuJson);

        listService.incrHotScore(skuId);

        return "item";
    }

}
