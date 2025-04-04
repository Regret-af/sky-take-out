package com.sky.product.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.enumeration.OperationType;
import com.sky.product.domain.dto.DishPageQueryDTO;
import com.sky.product.domain.entity.Dish;
import com.sky.product.domain.vo.DishVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    /**
     * 添加菜品
     * @param dishEntity
     */
    @AutoFill(OperationType.INSERT)
    void insert(Dish dishEntity);

    /**
     * 分页查询
     * @param queryDTO
     * @return
     */
    Page<DishVO> pageQuery(DishPageQueryDTO queryDTO);

    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    @Select("select * from dish where id = #{id}")
    Dish seleteById(Long id);

    /**
     * 批量删除菜品
     * @param ids
     */
    void deleteBatch(List<Long> ids);

    /**
     * 更新菜品信息
     * @param dishEntity
     */
    @AutoFill(OperationType.UPDATE)
    void update(Dish dishEntity);

    /**
     * 根据分类查询菜品
     * @param dish
     * @return
     */
    List<Dish> list(Dish dish);

    /**
     * 根据套餐id查询套餐中菜品
     * @param id
     * @return
     */
    @Select("select d.* from setmeal_dish s left join dish d on s.dish_id = d.id where s.setmeal_id = #{id};")
    List<Dish> getBySetmealId(Long id);

    /**
     * 根据条件统计菜品数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);
}
