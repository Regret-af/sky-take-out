package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {

    /**
     * 通过openid查询用户
     * @param openid
     * @return
     */
    @Select("select * from user where openid = #{openid}")
    User getByOpenid(String openid);

    /**
     * 新增用户
     * @param user
     */
    void insert(User user);

    /**
     * 通过id查找用户
     * @param userId
     * @return
     */
    @Select("select * from user where id = #{userId};")
    User getById(Long userId);
}
