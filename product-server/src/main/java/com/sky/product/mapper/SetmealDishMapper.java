package com.sky.product.mapper;

import com.sky.product.domain.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    /**
     * 根据菜品id，查询所属套餐
     * @param ids
     * @return
     */
    List<Long> getSetmealIdsByDishIds(List<Long> ids);

    /**
     * 插入套餐与菜品关系
     * @param setmealDishes
     */
    void insert(List<SetmealDish> setmealDishes);

    /**
     * 查询套餐中包含的菜品信息
     * @param id
     * @return
     */
    @Select("select * from setmeal_dish where setmeal_id = #{id}")
    List<SetmealDish> getSetmealDishes(Long id);

    /**
     * 删除套餐中包含的菜品信息
     * @param ids
     */
    void deleteBySetmealId(List<Long> ids);
}
