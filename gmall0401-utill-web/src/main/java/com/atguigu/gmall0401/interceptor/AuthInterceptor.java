package com.atguigu.gmall0401.interceptor;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0401.config.LoginRequire;
import com.atguigu.gmall0401.constants.WebConst;
import com.atguigu.gmall0401.utill.CookieUtil;
import com.atguigu.gmall0401.utill.HttpClientUtil;
import io.jsonwebtoken.impl.Base64UrlCodec;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import static com.atguigu.gmall0401.constants.WebConst.VERIFY_URL;

@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getParameter("newToken");
        //把token保存到cookie
        if(token!=null){
            CookieUtil.setCookie(request,response,"token",token, WebConst.cookieMaxAge,false);
        }else{
            token = CookieUtil.getCookieValue(request, "token", false);
        }

        if(token!=null) {
            //读取token
            Map userMap = getUserMapByToken(token);
            String nickName = (String) userMap.get("nickName");
            request.setAttribute("nickName", nickName);
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        LoginRequire loginRequire = handlerMethod.getMethodAnnotation(LoginRequire.class);
        if (loginRequire != null){
            if (token != null){
                //发送token认证中心认证
                String currentIP = request.getHeader("X-forwarded-for");
                String isLogin = HttpClientUtil.doGet(VERIFY_URL + "?token=" + token + "&currentIP=" + currentIP);

                if ("success".equals(isLogin)){
                    Map userMap = getUserMapByToken(token);
                    String userId = (String) userMap.get("userId");
                    request.setAttribute("userId", userId);
                    return true;

                }else if (!loginRequire.autoRedirect()){
                    return true;
                }else {
                    //token 失效，重新登录
                    String requestURL = request.getRequestURL().toString();
                    String encodeURL = URLEncoder.encode(requestURL, "UTF-8");
                    response.sendRedirect(WebConst.LOGIN_URL+"?originUrl="+encodeURL);
                    return false;

                }
            }else {
                //没有token，重新登录
                String requestURL = request.getRequestURL().toString();
                String encodeURL = URLEncoder.encode(requestURL, "UTF-8");
                response.sendRedirect(WebConst.LOGIN_URL+"?originUrl="+encodeURL);
                return false;
            }
        }


        return true;
    }

    private  Map getUserMapByToken(String  token){
        String tokenUserInfo = StringUtils.substringBetween(token, ".");
        Base64UrlCodec base64UrlCodec = new Base64UrlCodec();
        byte[] userBytes = base64UrlCodec.decode(tokenUserInfo);
        String userJson = null;
        try {
            userJson = new String(userBytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Map userMap = JSON.parseObject(userJson, Map.class);
        return userMap;
    }
}
