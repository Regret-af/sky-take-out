package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/dish")
@Slf4j
@Api(tags = "菜品相关接口")
public class DishController {

    @Autowired
    DishService dishService;

    /**
     * 新增菜品
     * @param dish
     * @return
     */
    @PostMapping
    @ApiOperation("新增菜品")
    public Result save(@RequestBody DishDTO dish) {
        log.info("开始新增菜品：{}", dish);
        dishService.saveWithFlavor(dish);

        return Result.success();
    }

    /**
     * 菜品分页查询
     * @param queryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    public Result page(DishPageQueryDTO queryDTO) {
        log.info("开始菜品分页查询：{}", queryDTO);
        PageResult pageResult = dishService.pageQuery(queryDTO);

        return Result.success(pageResult);
    }

    /**
     * 批量删除菜品
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("批量删除菜品")
    public Result delete(@RequestParam List<Long> ids) {
        log.info("开始批量删除菜品：{}", ids);
        dishService.delete(ids);

        return  Result.success();
    }

    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    public Result<DishVO> seleteById(@PathVariable Long id) {
        log.info("开始根据id查询菜品：{}", id);
        DishVO dish = dishService.seleteById(id);

        return Result.success(dish);
    }

    /**
     * 修改菜品
     * @param dish
     * @return
     */
    @PutMapping
    @ApiOperation("修改菜品")
    public Result update(@RequestBody DishDTO dish) {
        log.info("开始修改菜品：{}", dish);
        dishService.updateWithFlavor(dish);

        return Result.success();
    }

    /**
     * 根据分类查询菜品
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result seleteByCategoryId(@RequestParam Long categoryId) {
        log.info("开始根据分类id查询菜品：{}", categoryId);
        List<Dish> list = dishService.getByCategoryId(categoryId);

        return Result.success(list);
    }

    /**
     * 起售、停售菜品
     * @param status
     * @param id
     * @return
     */
    @PostMapping("status/{status}")
    @ApiOperation("起售、停售菜品")
    public Result startOrStop(@PathVariable Integer status, Long id) {
        log.info("起售、停售菜品");
        dishService.startOrStop(id, status);

        return Result.success();
    }
}
