package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 添加购物车
     * @param shoppingCartDTO
     * @return
     */
    @Override
    public void add(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);

        // 填入用户id
        shoppingCart.setUserId(BaseContext.getCurrentId());

        // 首先判断该商品是否存在
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

        // 若已经存在，则将商品数量加一
        if (list != null && list.size() > 0) {
            ShoppingCart cart = list.get(0);
            cart.setNumber(cart.getNumber() + 1);
            shoppingCartMapper.updateNumById(cart);
        } else {
            // 若不存在，则插入该数据

            // 判断本次添加的是菜品还是套餐
            Long dishId = shoppingCartDTO.getDishId();

            if (dishId != null) {
                // 本次添加的是菜品
                shoppingCart.setDishId(dishId);

                Dish dish = dishMapper.seleteById(dishId);
                shoppingCart.setName(dish.getName());   // 填入名称
                shoppingCart.setAmount(dish.getPrice());    // 填入价格
                shoppingCart.setImage(dish.getImage()); // 填入图片路径
            } else {
                // 本次添加的是套餐
                Long setmealId = shoppingCart.getSetmealId();

                Setmeal setmeal = setmealMapper.getById(setmealId);
                shoppingCart.setName(setmeal.getName());    // 填入名称
                shoppingCart.setAmount(setmeal.getPrice()); // 填入价格
                shoppingCart.setImage(setmeal.getImage());  // 填入图片路径
            }

            shoppingCart.setCreateTime(LocalDateTime.now());

            // 插入数据库
            shoppingCartMapper.insert(shoppingCart);
        }
    }

    /**
     * 查看购物车
     * @return
     */
    @Override
    public List<ShoppingCart> list() {
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(BaseContext.getCurrentId());

        return shoppingCartMapper.list(shoppingCart);
    }

    /**
     * 清空购物车
     */
    @Override
    public void clean() {
        shoppingCartMapper.deleteByUserId(BaseContext.getCurrentId());
    }

    /**
     * 删除购物车中一个商品
     * @param shoppingCartDTO
     */
    @Override
    public void sub(ShoppingCartDTO shoppingCartDTO) {
        // 首先这个商品在购物车中肯定有，所以直接使数量-1，如果为0，直接删除
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setUserId(BaseContext.getCurrentId());

        // 查看商品数量
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        shoppingCart = list.get(0);
        Integer number = shoppingCart.getNumber();

        if (number == 1) shoppingCartMapper.deleteById(shoppingCart.getId());
        else {
            shoppingCart.setNumber(number - 1);
            shoppingCartMapper.updateNumById(shoppingCart);
        }
    }
}
