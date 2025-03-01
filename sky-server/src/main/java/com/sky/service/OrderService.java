package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

public interface OrderService {

    /**
     * 用户下单
     * @param order
     * @return
     */
    OrderSubmitVO submit(OrdersSubmitDTO order);

    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 支付成功，修改订单状态
     * @param outTradeNo
     */
    void paySuccess(String outTradeNo);

    /**
     * 历史订单查询
     * @param ordersPageQueryDTO
     * @return
     */
    PageResult historyOrders(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 查询订单详情
     * @param id
     * @return
     */
    OrderVO getById(Integer id);

    /**
     * 取消订单
     * @param id
     */
    void cancel(Integer id) throws Exception;

    /**
     * 再来一单
     * @param id
     */
    void repetition(Integer id);

    /**
     * 管理端的订单搜索
     *
     * @param ordersPageQueryDTO
     * @return
     */
    PageResult ordersSearch(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 各个状态的订单数量统计
     * @return
     */
    OrderStatisticsVO getStatusNum();

    /**
     * 管理端接单操作
     * @param ordersDTO
     * @return
     */
    void confirm(OrdersDTO ordersDTO);

    /**
     * 管理端拒单操作
     * @param ordersRejectionDTO
     */
    void rejection(OrdersRejectionDTO ordersRejectionDTO);
}
