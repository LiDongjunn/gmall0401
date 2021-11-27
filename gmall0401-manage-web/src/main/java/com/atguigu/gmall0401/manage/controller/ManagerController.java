package com.atguigu.gmall0401.manage.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.support.Parameter;
import com.atguigu.gmall0401.bean.*;
import com.atguigu.gmall0401.service.ListService;
import com.atguigu.gmall0401.service.ManageService;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.http.HttpRequest;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

@RestController
@CrossOrigin
public class ManagerController {

//    http://localhost:8082/getCatalog1

    @Reference
    ManageService manageService;
    @Reference
    ListService listService;
    @PostMapping("getCatalog1")
    public List<BaseCatalog1>  getBaseCatalog1 (){
        return manageService.getCatalog1();
    }

    @PostMapping("getCatalog2")
    public List<BaseCatalog2>  getBaseCatalog2 (String catalog1Id){
        return manageService.getCatalog2(catalog1Id);
    }

    @PostMapping("getCatalog3")
    public List<BaseCatalog3>  getBaseCatalog3 (String catalog2Id){
        return manageService.getCatalog3(catalog2Id);
    }

    @GetMapping("attrInfoList")
    public List<BaseAttrInfo> getBaseAttrInfo(String catalog3Id){
        System.out.println(catalog3Id);
        return manageService.getAttrList(catalog3Id);
    }


    @PostMapping("saveAttrInfo")
    public String saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo){
        manageService.saveAttrInfo(baseAttrInfo);
        System.out.println("success");
        return "success";
    }

    @PostMapping ("getAttrValueList")
    public List<BaseAttrValue> getAttrValueList(String attrId){
        BaseAttrInfo baseAttrInfo = manageService.getBaseAttrInfo(attrId);
        return baseAttrInfo.getAttrValueList();
    }

    @PostMapping("baseSaleAttrList")
    public List<BaseSaleAttr> baseSaleAttrList(String catalog3Id){
        return manageService.getBaseSaleAttrList();
    }


    @PostMapping("saveSpuInfo")
    public String saveSpuInfo(@RequestBody SpuInfo spuInfo){
        manageService.saveSpuInfo(spuInfo);
        return "success";
    }

    //spuList?catalog3Id=61
    @GetMapping("spuList")
    public List<SpuInfo> SpuList(String catalog3Id){


        List<SpuInfo> spuList = manageService.getSpuList(catalog3Id);
//        System.out.println(spuList);
        return spuList;
    }


//    http://manage.gmall.com/spuImageList?spuId=57
    @GetMapping("spuImageList")
    public List<SpuImage> getSpuImageList(String spuId){
        List<SpuImage> spuImageList = manageService.getSpuImageList(spuId);
        return spuImageList;
    }

//    http://manage.gmall.com/spuSaleAttrList?spuId=59

    @GetMapping("spuSaleAttrList")
    public List<SpuSaleAttr> getSpuSaleAttrList(String spuId){
        List<SpuSaleAttr> spuSaleAttrList = manageService.getSpuSaleAttrList(spuId);
        return spuSaleAttrList;
    }

    @GetMapping()
    public String onSaleBySpu(String spuId){
        return null;
    }

    @GetMapping("onSale")
    public String onSaleBySku(@RequestParam("skuId") String skuId){
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        SkuLsInfo skuLsInfo = new SkuLsInfo();
        // 属性拷贝
        try {
            BeanUtils.copyProperties(skuLsInfo,skuInfo);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        listService.saveSkuLsInfo(skuLsInfo);
        return "success";
    }


}


