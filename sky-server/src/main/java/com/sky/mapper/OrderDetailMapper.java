package com.sky.mapper;

import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderDetailMapper {

    /**
     * 插入订单明细
     * @param orderDetailList
     */
    void insertBatch(List<OrderDetail> orderDetailList);

    /**
     * 通过订单id查询订单细节
     * @param order
     * @return
     */
    @Select("select * from order_detail where order_id = #{id};")
    List<OrderDetail> getByOrderId(Orders order);
}
