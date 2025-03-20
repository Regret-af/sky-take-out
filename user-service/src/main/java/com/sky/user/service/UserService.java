package com.sky.user.service;

import com.sky.user.domain.entity.User;
import com.sky.user.domain.dto.UserLoginDTO;

public interface UserService {

    /**
     * 微信登录
     * @param loginDTO
     * @return
     */
    User wxLogin(UserLoginDTO loginDTO);
}
