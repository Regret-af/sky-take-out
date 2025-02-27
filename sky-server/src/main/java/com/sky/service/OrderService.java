package com.sky.service;

import com.sky.dto.OrdersSubmitDTO;
import com.sky.vo.OrderSubmitVO;

public interface OrderService {

    /**
     * 用户下单
     * @param order
     * @return
     */
    OrderSubmitVO submit(OrdersSubmitDTO order);
}
