package com.sky.service;

import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;

public interface LoginService {
    /**
     * 用户登录
     * @param userLoginDTO
     * @return
     */
    User login(UserLoginDTO userLoginDTO);
}
