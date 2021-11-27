package com.atguigu.gmall0401.user.service.impl;




import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0401.bean.UserInfo;
import com.atguigu.gmall0401.service.UserService;
import com.atguigu.gmall0401.user.mapper.UserMapper;

import com.atguigu.gmall0401.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    RedisUtil redisUtil;

    public final String USER_INFO_KEY_PREFIX = "user:";
    public final String USER_INFO_KEY_SUFFIX = ":info";
    public final int USER_INFO_KEY_TIMEOUT = 60*60*24;

    @Override
    public List<UserInfo> getUserInfoListAll() {
        return userMapper.selectAll();
    }

    @Override
    public void addUser(UserInfo userInfo) {
        userMapper.insertSelective(userInfo);
    }

    @Override
    public void updateUser(UserInfo userInfo) {
        userMapper.updateByPrimaryKeySelective(userInfo);
    }

    @Override
    public void updateUserByName(String name, UserInfo userInfo) {
        Example example = new Example(UserInfo.class);
        example.createCriteria().andEqualTo("name",name);

        userMapper.updateByExampleSelective(userInfo,example);
    }

    @Override
    public void delUser(UserInfo userInfo) {
        userMapper.deleteByPrimaryKey(userInfo.getId());
    }

    @Override
    public UserInfo getUserInfoById(String id) {
        return userMapper.selectByPrimaryKey(id);
    }

    @Override
    public UserInfo login(UserInfo userInfo) {

        String passwd = userInfo.getPasswd();
        String passwdMD5 = DigestUtils.md5DigestAsHex(passwd.getBytes());
        userInfo.setPasswd(passwdMD5);

        UserInfo userInfoExists = userMapper.selectOne(userInfo);

        if (userInfoExists != null){
            //Redis缓存
            Jedis jedis = redisUtil.getJedis();
            String userInfoJson = JSON.toJSONString(userInfoExists);

            String userKey = USER_INFO_KEY_PREFIX + userInfoExists.getId() + USER_INFO_KEY_SUFFIX;
            jedis.setex(userKey,USER_INFO_KEY_TIMEOUT,userInfoJson);

            jedis.close();
            return userInfoExists;
        }



        return null;
    }
    //根据token里的userId ，查询缓存中用户信息
    @Override
    public Boolean verify(String userId) {
        Jedis jedis = redisUtil.getJedis();
        String userKey = USER_INFO_KEY_PREFIX + userId + USER_INFO_KEY_SUFFIX;
        Boolean isLogin = jedis.exists(userKey);
        if (isLogin){
            jedis.expire(userKey,USER_INFO_KEY_TIMEOUT);
        }
        jedis.close();


        return isLogin;
    }
}
