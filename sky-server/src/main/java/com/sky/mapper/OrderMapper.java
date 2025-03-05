package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {

    /**
     * 插入订单数据
     * @param orders
     * @return
     */
    void insert(Orders orders);

    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    /**
     * 分页查询订单信息
     * @param ordersPageQueryDTO
     * @return
     */
    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 根据订单id查询
     * @param id
     * @return
     */
    @Select("select * from orders where id = #{id};")
    Orders getById(Long id);

    /**
     * 根据订单状态查询订单数量
     * @param toBeConfirmed
     * @return
     */
    @Select("select count(*) from orders where status = #{toBeConfirmed}")
    Integer countStatus(Integer toBeConfirmed);

    /**
     * 查询超时订单
     * @param status
     * @param orderTime
     * @return
     */
    @Select("select * from orders where status = #{status} and order_time <= #{orderTime};")
    List<Orders> getByStatusAndOrderTime(Integer status, LocalDateTime orderTime);

    /**
     * 查找营业额
     * @param map
     * @return
     */
    @Select("select sum(amount) from orders where status = #{status} and order_time > #{begin} and order_time < #{end};")
    Double sumMoneyByMap(Map map);

    /**
     * 根据 map查找表中的数据
     * @param map
     * @return
     */
    Integer countByMap(Map map);
}
