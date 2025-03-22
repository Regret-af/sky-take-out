package com.sky.trade.controller.admin;

import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.trade.domain.dto.OrdersCancelDTO;
import com.sky.trade.domain.dto.OrdersDTO;
import com.sky.trade.domain.dto.OrdersPageQueryDTO;
import com.sky.trade.domain.dto.OrdersRejectionDTO;
import com.sky.trade.domain.vo.OrderStatisticsVO;
import com.sky.trade.domain.vo.OrderVO;
import com.sky.trade.service.OrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("adminOrderController")
@RequestMapping("/admin/order")
@Slf4j
@Api(tags = "订单管理接口")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 管理端的订单搜索
     * @param ordersPageQueryDTO
     * @return
     */
    @GetMapping("conditionSearch")
    @ApiOperation("订单搜索")
    public Result<PageResult> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("管理端开始进行订单搜索：{}", ordersPageQueryDTO);
        PageResult pageResult = orderService.ordersSearch(ordersPageQueryDTO);

        return Result.success(pageResult);
    }

    /**
     * 各个状态的订单数量统计
     * @return
     */
    @GetMapping("/statistics")
    @ApiOperation("各个状态的订单数量统计")
    public Result<OrderStatisticsVO> statistics() {
        log.info("管理端开始进行各个状态的订单数量统计~");
        OrderStatisticsVO statisticsVO = orderService.getStatusNum();

        return Result.success(statisticsVO);
    }

    /**
     * 查询订单详情
     * @param id
     * @return
     */
    @GetMapping("/details/{id}")
    @ApiOperation("查询订单详情")
    public Result<OrderVO> getOrderDetail(@PathVariable Long id) {
        log.info("管理端开始查询订单详情，订单id为：{}", id);
        OrderVO orderVO = orderService.getById(id);

        return Result.success(orderVO);
    }

    /**
     * 管理端接单操作
     * @param ordersDTO
     * @return
     */
    @PutMapping("/confirm")
    @ApiOperation("接单")
    public Result confirm(@RequestBody OrdersDTO ordersDTO) {
        log.info("管理端进行接单，订单id为:{}", ordersDTO.getId());
        orderService.confirm(ordersDTO);

        return Result.success();
    }

    /**
     * 管理端拒单操作
     * @param ordersRejectionDTO
     * @return
     */
    @PutMapping("/rejection")
    @ApiOperation("拒单")
    public Result rejection(@RequestBody OrdersRejectionDTO ordersRejectionDTO) {
        log.info("管理端进行拒单操作:{}", ordersRejectionDTO);
        orderService.rejection(ordersRejectionDTO);

        return Result.success();
    }

    /**
     * 管理端取消订单操作
     * @param ordersCancelDTO
     * @return
     */
    @PutMapping("/cancel")
    @ApiOperation("取消订单")
    public Result cancel(@RequestBody OrdersCancelDTO ordersCancelDTO) {
        log.info("管理端进行拒单操作:{}", ordersCancelDTO);
        orderService.adminCancel(ordersCancelDTO);

        return Result.success();
    }

    /**
     * 派送订单
     * @param id
     * @return
     */
    @PutMapping("/delivery/{id}")
    @ApiOperation("派送订单")
    public Result delivery(@PathVariable Long id) {
        log.info("开始派送订单,id为:{}", id);
        orderService.delivery(id);

        return Result.success();
    }

    /**
     * 完成订单
     * @param id
     * @return
     */
    @PutMapping("/complete/{id}")
    @ApiOperation("完成订单")
    public Result complete(@PathVariable Long id) {
        log.info("id为:{}的订单完成~", id);
        orderService.complete(id);

        return Result.success();
    }
}
