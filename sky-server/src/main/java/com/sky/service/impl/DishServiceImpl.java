package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class DishServiceImpl implements DishService {

    @Autowired
    DishMapper dishMapper;

    @Autowired
    DishFlavorMapper dishFlavorMapper;

    @Autowired
    SetmealDishMapper setmealDishMapper;

    @Autowired
    SetmealMapper setmealMapper;

    /**
     * 新增菜品
     * @param dish
     */
    @Override
    @Transactional
    public void saveWithFlavor(DishDTO dish) {
        Dish dishEntity = new Dish();
        BeanUtils.copyProperties(dish, dishEntity);

        // 向菜品表插入一条数据
        dishMapper.insert(dishEntity);

        // 获取菜品ID
        Long id = dishEntity.getId();

        List<DishFlavor> flavors = dish.getFlavors();

        if (flavors != null && flavors.size() > 0) {
            flavors.forEach(flavor -> {
                flavor.setDishId(id);
            });

            // 向口味表插入n条数据
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 菜品分页查询
     *
     * @param queryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO queryDTO) {
        // 设置分页查询参数
        PageHelper.startPage(queryDTO.getPage(), queryDTO.getPageSize());

        // 开始查询
        Page<DishVO> page = dishMapper.pageQuery(queryDTO);

        // 返回结果
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 批量删除菜品
     * @param ids
     */
    @Override
    @Transactional
    public void delete(List<Long> ids) {
        // 判断菜品状态是否为起售中
        for (Long id : ids) {
            Dish dish = dishMapper.seleteById(id);

            // 如果菜品为起售中，抛出异常
            if (dish.getStatus() == StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }

        // 判断套餐中是否包含要删除的菜品
        List<Long> dishIds = setmealDishMapper.getSetmealIdsByDishIds(ids);

        if (dishIds != null && dishIds.size() > 0) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        // 删除菜品关联的口味
        dishFlavorMapper.deleteBatch(ids);

        // 在菜品表中删除菜品
        dishMapper.deleteBatch(ids);
    }

    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    @Override
    public DishVO seleteById(Long id) {
        // 查询菜品基本信息
        Dish dish = dishMapper.seleteById(id);

        // 查询相关口味信息
        List<DishFlavor> flavors = dishFlavorMapper.seleteByDishId(id);

        // 将数据封装到VO对象中
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(flavors);

        return dishVO;
    }

    /**
     * 更新菜品
     * @param dish
     */
    @Override
    @Transactional
    public void updateWithFlavor(DishDTO dish) {
        // 进行数据迁移
        Dish dishEntity = new Dish();
        BeanUtils.copyProperties(dish, dishEntity);

        // 先更新菜品信息
        dishMapper.update(dishEntity);

        // 再更新口味信息（全删除，重新添加）
        List<DishFlavor> flavors = dish.getFlavors();
        dishFlavorMapper.deleteById(dishEntity.getId());
        if (flavors != null && flavors.size() > 0) {
            flavors.forEach(flavor -> {
                flavor.setDishId(dishEntity.getId());
            });

            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 根据分类查询菜品
     * @param categoryId
     * @return
     */
    @Override
    public List<Dish> getByCategoryId(Long categoryId) {
        Dish dish = Dish.builder().categoryId(categoryId).build();
        return dishMapper.list(dish);
    }

    /**
     * 起售、停售菜品
     * @param status
     * @param id
     * @return
     */
    @Override
    public void startOrStop(Long id, Integer status) {
        // 如果需要将菜品状态更改为停售，则需要先将包含该菜品的套餐均改为停售状态
        if (status == StatusConstant.DISABLE) {
            List<Long> ids = new ArrayList<>();
            ids.add(id);

            List<Long> list = setmealDishMapper.getSetmealIdsByDishIds(ids);
            Setmeal setmeal = Setmeal.builder()
                    .status(status)
                    .build();

            if (list != null && list.size() > 0) {
                list.forEach(setmealId -> {
                    setmeal.setId(setmealId);
                    setmealMapper.update(setmeal);
                });
            }
        }

        Dish dish = Dish.builder()
                .id(id)
                .status(status)
                .build();

        // 最后改变菜品的状态
        dishMapper.update(dish);
    }

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.seleteByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }
}
