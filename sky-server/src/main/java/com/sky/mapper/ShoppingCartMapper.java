package com.sky.mapper;

import com.sky.entity.ShoppingCart;
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
    @Update("update shopping_cart set number = number + 1 where user_id = #{userId}")
    void updateNumById(ShoppingCart shoppingCart);

    /**
     * 插入购物车数据
     * @param shoppingCart
     */
    @Insert("insert into shopping_cart (name, image, user_id, dish_id, setmeal_id, dish_flavor, amount, create_time) " +
            "value (#{name}, #{image}, #{userId}, #{dishId}, #{setmealId}, #{dishFlavor}, #{amount}, #{createTime});")
    void insert(ShoppingCart shoppingCart);
}
