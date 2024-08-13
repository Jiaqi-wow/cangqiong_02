package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonObject;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.LoginMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.LoginService;
import com.sky.utils.HttpClientUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class LoginServiceImpl implements LoginService {

    private static final String WX_LOGIN_URL = "https://api.weixin.qq.com/sns/jscode2session";

    @Autowired
    private WeChatProperties weChatProperties;
    @Autowired
    private LoginMapper loginMapper;

    @Override
    public User login(UserLoginDTO userLoginDTO) {

        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("appid", weChatProperties.getAppid());
        paramMap.put("secret", weChatProperties.getSecret());
        paramMap.put("grant_type", "authorization_code");
        paramMap.put("js_code", userLoginDTO.getCode());
        String s = HttpClientUtil.doGet(WX_LOGIN_URL, paramMap);

        JSONObject jsonObject = JSONObject.parseObject(s);
        String openid = jsonObject.getString("openid");

        if(openid == null){
            throw new LoginFailedException("openid获取失败");
        }

        //查看数据库是否已经存在该用户
        User user = loginMapper.selectByOpenid(openid);

        //如果未存在则创建
        if (user == null) {
            user = new User();
            user.setOpenid(openid);
            user.setCreateTime(LocalDateTime.now());
            loginMapper.insertOne(user);
        }
        return user;

    }
}
