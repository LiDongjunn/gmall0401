package com.atgui.gmall0401.list.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0401.bean.BaseAttrInfo;
import com.atguigu.gmall0401.bean.BaseAttrValue;
import com.atguigu.gmall0401.bean.SkuLsParams;
import com.atguigu.gmall0401.bean.SkuLsResult;
import com.atguigu.gmall0401.service.ListService;
import com.atguigu.gmall0401.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


@Controller
public class ListController {
    @Reference
    ListService listService;
    @Reference
    ManageService manageService;

    @GetMapping("list.html")
    public String list(SkuLsParams skuLsParams, Model model){
        skuLsParams.setPageSize(4);
        //根据用户输入查询参数keyword;catalog3Id;valueId;获取sku清单,并返回前端
        SkuLsResult skuLsResult= listService.getSkuLsInfoList(skuLsParams);
        model.addAttribute("skuLsInfoList",skuLsResult);

        //获取平台属性列表,并返回前端
        List<String> attrValueIdList = skuLsResult.getAttrValueIdList();
        List<BaseAttrInfo> attrList = manageService.getAttrList(attrValueIdList);

        //初始化平台属性面包屑
        List<BaseAttrValue> selectedValueList = new ArrayList<>();

        //删除已选择的平台属性
        if (skuLsParams.getValueId() != null && skuLsParams.getValueId().length > 0){
            for (Iterator<BaseAttrInfo> iterator = attrList.iterator(); iterator.hasNext(); ) {
                BaseAttrInfo baseAttrInfo =  iterator.next();
                List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();

                for (BaseAttrValue attrValue : attrValueList) {

                    for (int i = 0; i < skuLsParams.getValueId().length; i++) {
                        String selectedValueId = skuLsParams.getValueId()[i];

                        if (selectedValueId.equals(attrValue.getId())){
                            //保存  取消已经选择平台属性的路径
                            attrValue.setParamUrl(makeParamUrl(skuLsParams,selectedValueId));
                            //保存平台属性那一行到面包屑
                            selectedValueList.add(attrValue);
                            //删除 平台属性那一行
                            iterator.remove();

                        }

                    }

                }
            }
        }

        //历史查询条件
        String paramUrl = makeParamUrl(skuLsParams);
        model.addAttribute("paramUrl",paramUrl);

        //平台属性面包屑
        model.addAttribute("selectedValueList",selectedValueList);

        //前端添加关键字
        model.addAttribute("keyword",skuLsParams.getKeyword());

        //获取平台属性列表,并返回前端,包含 返回路径URL
        model.addAttribute("attrList",attrList);

        //向前端添加分页
        model.addAttribute("pageNo",skuLsParams.getPageNo());
        model.addAttribute("totalPage",skuLsResult.getTotalPages());


        return "list";
    }

    public String makeParamUrl(SkuLsParams skuLsParams ,String... excludeValueId){
        String paramUrl = "";

        if (skuLsParams.getKeyword() != null){
            paramUrl += "keyword=" + skuLsParams.getKeyword();
        }else if (skuLsParams.getCatalog3Id() != null){
            paramUrl += "catalog3Id=" + skuLsParams.getCatalog3Id();
        }

        if (skuLsParams.getValueId() != null){
            String[] valueIds = skuLsParams.getValueId();
            for (String  valueId :valueIds
            ) {
                if (excludeValueId != null && excludeValueId.length > 0){
                    String exValueId = excludeValueId[0];
                    if (exValueId.equals(valueId)){
                        continue;
                    }
                }

                if (paramUrl != null && paramUrl.length() >0){
                    paramUrl += "&valueId=" + valueId;
                }


            }
        }
        return paramUrl;
    }

}
