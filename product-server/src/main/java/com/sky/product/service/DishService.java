package com.sky.product.service;

import com.sky.product.domain.dto.DishDTO;
import com.sky.product.domain.dto.DishPageQueryDTO;
import com.sky.product.domain.entity.Dish;
import com.sky.product.domain.vo.DishVO;
import com.sky.result.PageResult;

import java.util.List;

public interface DishService {

    /**
     * 新增菜品
     * @param dish
     */
    void saveWithFlavor(DishDTO dish);

    /**
     * 菜品分页查询
     *
     * @param queryDTO
     * @return
     */
    PageResult pageQuery(DishPageQueryDTO queryDTO);

    /**
     * 批量删除菜品
     * @param ids
     */
    void delete(List<Long> ids);

    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    DishVO seleteById(Long id);

    /**
     * 修改菜品
     * @param dish
     */
    void updateWithFlavor(DishDTO dish);

    /**
     * 根据分类查询菜品
     * @param categoryId
     * @return
     */
    List<Dish> getByCategoryId(Long categoryId);

    /**
     * 起售、停售菜品
     * @param status
     * @param id
     * @return
     */
    void startOrStop(Long id, Integer status);

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    List<DishVO> listWithFlavor(Dish dish);
}
