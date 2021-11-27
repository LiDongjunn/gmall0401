package com.atguigu.gmall0401.user.controller;


import com.atguigu.gmall0401.bean.UserInfo;
import com.atguigu.gmall0401.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;


@RestController
public class UserInfoController {

    @Autowired
    UserService userService;


    @GetMapping("allUser")
    public List<UserInfo> allUser(){
        return userService.getUserInfoListAll();
    }


    @PostMapping("addUser")
    public String addUser(UserInfo userInfo){
        userService.addUser(userInfo);
        return "success";
    }

    @PostMapping("updateUser")
    public String updateUser(UserInfo userInfo) {
        userService.updateUser(userInfo);
        return "success";
    }

    @PostMapping("updateUserByName")
    public void updateUserByName(UserInfo userInfo) {
        userService.updateUserByName(userInfo.getName(),userInfo);
    }
    @PostMapping("delUser")
    public void delUser(UserInfo userInfo) {
        userService.delUser(userInfo);
    }

    @PostMapping("getUserInfoById")
    public UserInfo getUserInfoById(String id) {
        return userService.getUserInfoById(id);
    }



}
