package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {

    /**
     * 动态查询购物车
     * @param shoppingCartDTO
     * @return
     */
    List<ShoppingCart> list(ShoppingCart shoppingCartDTO);

    /**
     * 更新购物车数据
     * @param shoppingCart
     */
    @Update("update shopping_cart set number = #{number} where user_id = #{userId}")
    void updateNumById(ShoppingCart shoppingCart);

    /**
     * 插入购物车数据
     * @param shoppingCart
     */
    @Insert("insert into shopping_cart (name, image, user_id, dish_id, setmeal_id, dish_flavor, amount, create_time) " +
            "value (#{name}, #{image}, #{userId}, #{dishId}, #{setmealId}, #{dishFlavor}, #{amount}, #{createTime});")
    void insert(ShoppingCart shoppingCart);

    /**
     * 通过用户id清空购物车
     * @param userId
     */
    @Delete("delete from shopping_cart where user_id = #{userId};")
    void deleteByUserId(Long userId);

    /**
     * 删除购物车中一条记录
     * @param id
     */
    @Delete("delete from shopping_cart where id = #{id};")
    void deleteById(Long id);
}
