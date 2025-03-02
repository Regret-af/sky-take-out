package com.sky.task;

import com.sky.constant.MessageConstant;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务类，定时处理订单状态
 */
@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 处理超时未支付订单
     */
    // @Scheduled(cron = "0 * * * * *")
    public void processTimeoutOrder() {
        log.info("定时处理超时订单:{}", LocalDateTime.now());

        // 获取超时订单
        LocalDateTime orderTime = LocalDateTime.now().minusMinutes(15);
        List<Orders> timeoutOrders = orderMapper.getByStatusAndOrderTime(Orders.PENDING_PAYMENT, orderTime);

        // 更改订单状态
        for (Orders order : timeoutOrders) {
            order.setStatus(Orders.CANCELLED);
            order.setCancelTime(LocalDateTime.now());
            order.setCancelReason(MessageConstant.ORDER_TIME_OUT);
            orderMapper.update(order);
        }
    }

    /**
     * 处理配送超时订单
     */
    @Scheduled(cron = "0 0 1 * * *")
    public void processDeliveryOrder() {
        log.info("定时处理配送超时订单:{}", LocalDateTime.now());

        LocalDateTime orderTime = LocalDateTime.now().minusMinutes(60);
        // 获取订单
        List<Orders> deliveryOrders = orderMapper.getByStatusAndOrderTime(Orders.DELIVERY_IN_PROGRESS, orderTime);

        for (Orders order : deliveryOrders) {
            order.setStatus(Orders.COMPLETED);
            order.setDeliveryTime(LocalDateTime.now());
            orderMapper.update(order);
        }
    }
}
