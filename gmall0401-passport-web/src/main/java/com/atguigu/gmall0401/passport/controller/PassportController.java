package com.atguigu.gmall0401.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall0401.bean.UserInfo;
import com.atguigu.gmall0401.service.UserService;
import com.atguigu.gmall0401.utill.JwtUtil;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class PassportController {

    @Reference
    UserService userService;

    public final String JWT_KEY = "atguigu";


    @GetMapping("index")
    public String index(){
        return "index";
    }


    @PostMapping("login")
    @ResponseBody
    public String login(UserInfo userInfo, HttpServletRequest request){
        //根据用户输入,获取用户信息
        UserInfo userInfoExist = userService.login(userInfo);

        if (userInfoExist != null){
            Map<String, Object> map = new HashMap<>();
            map.put("userId",userInfoExist.getId());
            map.put("nickName",userInfoExist.getNickName());

//            request.getRemoteAddr(); 如果加了nginx，传回的nginx地址
            System.out.println("========================\n"+request.getRemoteAddr()+"========================\n");

            String ipAddr = request.getHeader("X-forwarded-for");
            String token = JwtUtil.encode(JWT_KEY, map, ipAddr);
            return token;
        }


        return "fail";


    }

    @GetMapping("verify")
    @ResponseBody
    public String verify(@RequestParam(value = "token") String token, @RequestParam(value = "currentIp") String currentIp){
        Map<String, Object> userMap = JwtUtil.decode(token, JWT_KEY, currentIp);

        //token是验证中心发的,是否存在缓存用户
        if (userMap != null){
            String userId = (String) userMap.get("userId");
            Boolean verify = userService.verify(userId);
            if (verify)
                return "success";

        }

        return "fail";
    }


}
