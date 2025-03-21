package com.sky.product.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.product.domain.dto.SetmealDTO;
import com.sky.product.domain.dto.SetmealPageQueryDTO;
import com.sky.product.domain.entity.Dish;
import com.sky.product.domain.entity.Setmeal;
import com.sky.product.domain.entity.SetmealDish;
import com.sky.product.domain.vo.DishItemVO;
import com.sky.product.domain.vo.SetmealVO;
import com.sky.product.mapper.DishMapper;
import com.sky.product.mapper.SetmealDishMapper;
import com.sky.product.mapper.SetmealMapper;
import com.sky.product.service.SetmealService;
import com.sky.result.PageResult;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private DishMapper dishMapper;

    /**
     * 新增套餐
     * @param setmealDTO
     */
    @Override
    @Transactional
    public void save(SetmealDTO setmealDTO) {
        // 将套餐信息添加到套餐表
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.insert(setmeal);

        // 将套餐与菜品的对应关系添加到表中
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        // 将套餐id填入套餐与菜品关系
        for (SetmealDish setmealDish : setmealDishes) {
            setmealDish.setSetmealId(setmeal.getId());
        }

        setmealDishMapper.insert(setmealDishes);
    }

    /**
     * 套餐的分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    @Override
    public PageResult page(SetmealPageQueryDTO setmealPageQueryDTO) {
        // 设置分页查询参数
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());

        // 拷贝数据
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealPageQueryDTO, setmeal);

        // 开始进行查询
        Page<SetmealVO> page = setmealMapper.getByPage(setmeal);

        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 批量删除套餐
     * @param ids
     */
    @Override
    public void deleteBatch(List<Long> ids) {
        // 先判断该套餐是否在售，若在售，则不能删除
        ids.forEach(id -> {
            Setmeal setmeal = setmealMapper.getById(id);
            if (setmeal.getStatus() == StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        });

        // 若状态为停售，直接删除
        setmealMapper.deleteBatch(ids);

        // 删除套餐中包含的菜品信息
        setmealDishMapper.deleteBySetmealId(ids);
    }

    /**
     * 根据id查询套餐
     *
     * @param id
     * @return
     */
    @Override
    public SetmealVO getById(Long id) {
        // 查询套餐基本信息
        Setmeal setmeal = setmealMapper.getById(id);
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal, setmealVO);

        // 查询套餐中包含的菜品
        List<SetmealDish> dishes = setmealDishMapper.getSetmealDishes(setmeal.getId());
        setmealVO.setSetmealDishes(dishes);

        return setmealVO;
    }

    /**
     * 修改套餐信息
     * @param setmealDTO
     */
    @Override
    public void update(SetmealDTO setmealDTO) {
        // 先修改套餐信息
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.update(setmeal);

        List<Long> list = new ArrayList<>();
        list.add(setmeal.getId());

        // 再修改套餐和菜品的关系，先全部删除，再新增
        setmealDishMapper.deleteBySetmealId(list);
        setmealDishMapper.insert(setmealDTO.getSetmealDishes());
    }

    /**
     * 起售、停售套餐
     * @param id
     * @param status
     */
    @Override
    public void startOrStop(Long id, Integer status) {
        // 创建套餐对象
        Setmeal setmeal = Setmeal.builder()
                .id(id)
                .status(status)
                .build();

        // 如果需要停售，不需要判断，直接停售
        if (Objects.equals(status, StatusConstant.DISABLE)) {
            setmealMapper.update(setmeal);
        }

        // 如果需要起售，则需要判断套餐内菜品是否均起售
        List<Dish> dishes = dishMapper.getBySetmealId(id);
        dishes.forEach(dish -> {
            if (Objects.equals(dish.getStatus(), StatusConstant.DISABLE)) {
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ENABLE_FAILED);
            }
        });

        setmealMapper.update(setmeal);
    }

    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }
}
