package com.sky.trade.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.result.PageResult;
import com.sky.trade.domain.dto.*;
import com.sky.trade.domain.entity.OrderDetail;
import com.sky.trade.domain.entity.Orders;
import com.sky.trade.domain.entity.ShoppingCart;
import com.sky.trade.domain.vo.OrderPaymentVO;
import com.sky.trade.domain.vo.OrderStatisticsVO;
import com.sky.trade.domain.vo.OrderSubmitVO;
import com.sky.trade.domain.vo.OrderVO;
import com.sky.trade.mapper.OrderDetailMapper;
import com.sky.trade.mapper.OrderMapper;
import com.sky.trade.mapper.ShoppingCartMapper;
import com.sky.trade.service.OrderService;
import com.sky.trade.websocket.WebSocketServer;
import com.sky.utils.HttpClientUtil;
import com.sky.utils.WeChatPayUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
 /*   @Autowired
    private AddressBookMapper addressBookMapper;*/
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    /*@Autowired
    private UserMapper userMapper;*/
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private WebSocketServer webSocketServer;

    @Value("${sky.baidu.ak}")
    private String ak;
    @Value("${sky.shop.address}")
    private String shopAddress;

    /**
     * 用户下单
     * @param order
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
  /* TODO
    public OrderSubmitVO submit(OrdersSubmitDTO order) {
        Long userId = BaseContext.getCurrentId();

        // 处理各种业务异常
        // 地址簿为空
        AddressBook addressBook = addressBookMapper.getById(order.getAddressBookId());

        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        // 购物车为空
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(userId)
                .build();
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

        if (list == null || list.size() == 0) {
            throw new AddressBookBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        checkOutOfRange(addressBook.getProvinceName() + addressBook.getCityName() + addressBook.getDistrictName() + addressBook.getDetail());

        // 拷贝数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(order, orders);

        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setUserId(userId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setPhone(addressBook.getPhone());
        orders.setAddress(addressBook.getProvinceName() + addressBook.getCityName() + addressBook.getDistrictName() + addressBook.getDetail());
        orders.setConsignee(addressBook.getConsignee());
        // 存入用户名
        orders.setUserName(userMapper.getById(userId).getName());

        // 先将订单存入订单表
        orderMapper.insert(orders);

        // 获取订单id
        Long orderId = orders.getId();

        // 再将订单明细存入订单明细表
        List<OrderDetail> orderDetailList = new ArrayList<OrderDetail>();
        for (ShoppingCart cart : list) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetailList.add(orderDetail);
        }

        orderDetailMapper.insertBatch(orderDetailList);

        // 最后清空购物车
        shoppingCartMapper.deleteByUserId(userId);

        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orderId)
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .orderTime(orders.getOrderTime())
                .build();

        return orderSubmitVO;
    }

   */

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
  /* TODO  public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    } */

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);

        // 通过 WebSocket向客户端浏览器推送消息，提醒商家来单
        Map map = new HashMap();
        map.put("type", 1); // 1 来单提醒 2 催单提醒
        map.put("orderId", orders.getId());
        map.put("content", "订单号:" + outTradeNo);

        String json = JSONObject.toJSONString(map);
        webSocketServer.sendToAllClient(json);
    }

    /**
     * 历史订单查询
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult historyOrders(OrdersPageQueryDTO ordersPageQueryDTO) {
        // 设置分页查询参数
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());

        // 设置用户id
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());

        // 开始进行查询
        Page<Orders> orders = orderMapper.pageQuery(ordersPageQueryDTO);

        // 封装返回数据
        List<OrderVO> list = new ArrayList<>();

        if (orders != null && orders.size() > 0) {
            for (Orders order : orders) {
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(order, orderVO);

                // 通过订单id查询订单细节
                List<OrderDetail> details = orderDetailMapper.getByOrderId(order);
                orderVO.setOrderDetailList(details);

                list.add(orderVO);
            }
        }

        return new PageResult(orders.getTotal(), list);
    }

    /**
     * 查询订单详情
     * @param id
     * @return
     */
    @Override
    public OrderVO getById(Long id) {
        OrderVO orderVO = new OrderVO();

        // 查询订单信息
        Orders orders = orderMapper.getById(id);

        // 查询订单明细表
        List<OrderDetail> details = orderDetailMapper.getByOrderId(orders);

        // 封装返回数据
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(details);

        return orderVO;
    }

    /**
     * 取消订单
     * @param id
     * @return
     */
    @Override
    public void cancel(Long id) throws Exception {
        // 判断订单是否存在
        Orders ordersDB = orderMapper.getById(id);

        if (ordersDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        // 判断订单状态
        if (ordersDB.getStatus() > 2) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.CANCELLED)
                .cancelTime(LocalDateTime.now())
                .cancelReason("用户取消订单")
                .build();

        // 如果订单已经付款，需要进行退款操作
        if (ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            /*
            // 进行退款操作
            weChatPayUtil.refund(
                    ordersDB.getNumber(),   //商户订单号
                    ordersDB.getNumber(),   //商户退款单号
                    new BigDecimal(String.valueOf(ordersDB.getAmount())),   //退款金额，单位 元
                    new BigDecimal(String.valueOf(ordersDB.getAmount())));  //原订单金额
             */

            // 更新数据信息
            orders.setPayStatus(Orders.REFUND);
        }

        orderMapper.update(orders);
    }

    /**
     * 再来一单
     * @param id
     * @return
     */
    @Override
    public void repetition(Long id) {
        // 首先查看订单是否存在
        Orders ordersDB = orderMapper.getById(id);

        if (ordersDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        // 如果存在，将订单商品加入购物车
        // 当前用户id
        Long currentId = BaseContext.getCurrentId();

        // 获取订单明细
        List<OrderDetail> details = orderDetailMapper.getByOrderId(ordersDB);

        List<ShoppingCart> list = details.stream().map(orderDetail -> {
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(orderDetail, shoppingCart);

            shoppingCart.setUserId(currentId);
            shoppingCart.setCreateTime(LocalDateTime.now());

            return shoppingCart;
        }).collect(Collectors.toList());

        // 将购物车数据批量加入数据库
        shoppingCartMapper.insertBatch(list);
    }

    /**
     * 管理端的订单搜索
     *
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult ordersSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        // 设置分页查询参数
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());

        // 开始进行查询
        Page<Orders> orders = orderMapper.pageQuery(ordersPageQueryDTO);

        // 开始封装数据
        List<OrderVO> list = new ArrayList<>();
        if (orders != null && orders.size() > 0) {
            for (Orders order : orders) {
                OrderVO orderVO = new OrderVO();
                // 将共同字段复制
                BeanUtils.copyProperties(order, orderVO);
                // 将菜品信息封装为字符串
                orderVO.setOrderDishes(getOrderDishesStr(order));

                list.add(orderVO);
            }
        }

        return new PageResult(orders.getTotal(), list);
    }

    /**
     * 各个状态的订单数量统计
     * @return
     */
    @Override
    public OrderStatisticsVO getStatusNum() {
        // 查询各个状态的订单数量
        Integer toBeConfirmed = orderMapper.countStatus(Orders.TO_BE_CONFIRMED);
        Integer confirmed = orderMapper.countStatus(Orders.CONFIRMED);
        Integer deliveryInProgress = orderMapper.countStatus(Orders.DELIVERY_IN_PROGRESS);

        // 封装返回数据
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);

        return orderStatisticsVO;
    }

    /**
     * 管理端接单操作
     * @param ordersDTO
     * @return
     */
    @Override
    public void confirm(OrdersDTO ordersDTO) {
        Orders orders = Orders.builder()
                .id(ordersDTO.getId())
                .status(Orders.CONFIRMED)
                .build();

        // 进行数据库的更新操作
        orderMapper.update(orders);
    }

    /**
     * 管理端拒单操作
     * @param ordersRejectionDTO
     */
    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        // 先判断订单是否存在
        Orders ordersDB = orderMapper.getById(ordersRejectionDTO.getId());

        if (ordersDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        } else if (!ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        // 若存在，先进行退款操作
        /*
            // 进行退款操作
            weChatPayUtil.refund(
                    ordersDB.getNumber(),   //商户订单号
                    ordersDB.getNumber(),   //商户退款单号
                    new BigDecimal(String.valueOf(ordersDB.getAmount())),   //退款金额，单位 元
                    new BigDecimal(String.valueOf(ordersDB.getAmount())));  //原订单金额
        */

        // 再将订单状态更改
        Orders orders = Orders.builder()
                .id(ordersRejectionDTO.getId())
                .status(Orders.CANCELLED)
                .payStatus(Orders.REFUND)
                .rejectionReason(ordersRejectionDTO.getRejectionReason())
                .cancelTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    /**
     * 管理端取消订单操作
     * @param ordersCancelDTO
     */
    @Override
    public void adminCancel(OrdersCancelDTO ordersCancelDTO) {
        // 判断订单是否存在
        Orders ordersDB = orderMapper.getById(ordersCancelDTO.getId());

        // 如果不存在，抛出异常
        if (ordersDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        // 封装更新对象
        Orders orders = Orders.builder()
                .id(ordersCancelDTO.getId())
                .status(Orders.CANCELLED)
                .cancelTime(LocalDateTime.now())
                .cancelReason(ordersCancelDTO.getCancelReason())
                .build();

        // 判断是否需要退款
        if (ordersDB.getPayStatus() == Orders.PAID) {
            orders.setPayStatus(Orders.REFUND);
            /*
            // 进行退款操作
            weChatPayUtil.refund(
                    ordersDB.getNumber(),   //商户订单号
                    ordersDB.getNumber(),   //商户退款单号
                    new BigDecimal(String.valueOf(ordersDB.getAmount())),   //退款金额，单位 元
                    new BigDecimal(String.valueOf(ordersDB.getAmount())));  //原订单金额
            */
        }

        // 更新数据库
        orderMapper.update(orders);
    }

    /**
     * 派送订单
     * @param id
     */
    @Override
    public void delivery(Long id) {
        // 查询订单是否存在
        Orders ordersDB = orderMapper.getById(id);

        // 若不存在，抛出异常
        if (ordersDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        // 查看订单状态和订单支付状态
        if (!ordersDB.getStatus().equals(Orders.CONFIRMED) || !ordersDB.getPayStatus().equals(Orders.PAID)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        // 更新订单状态
        Orders orders = Orders.builder().status(Orders.DELIVERY_IN_PROGRESS).id(id).build();
        orderMapper.update(orders);
    }

    /**
     * 完成订单
     * @param id
     */
    @Override
    public void complete(Long id) {
        // 查询订单是否存在
        Orders ordersDB = orderMapper.getById(id);

        // 若不存在，直接抛出异常
        if (ordersDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        // 查看订单状态和订单支付状态
        if (!ordersDB.getStatus().equals(Orders.DELIVERY_IN_PROGRESS) || !ordersDB.getPayStatus().equals(Orders.PAID)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        // 更新数据库中订单信息
        Orders orders = Orders.builder()
                .id(id)
                .status(Orders.COMPLETED)
                .deliveryTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    /**
     * 用户催单
     * @param id
     */
    @Override
    public void reminder(Long id) {
        // 根据id查询订单号
        Orders ordersDB = orderMapper.getById(id);

        // 校验订单是否存在
        if (ordersDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        // 通过 WebSocket向客户端浏览器发送消息，通知商家用户催单
        Map map = new HashMap();
        map.put("type", 2);
        map.put("orderId", id);
        map.put("content", "订单号:" + ordersDB.getNumber());
        String json =   JSONObject.toJSONString(map);

        webSocketServer.sendToAllClient(json);
    }

    /**
     * 根据订单id获取菜品信息字符串
     * @param orders
     * @return
     */
    private String getOrderDishesStr(Orders orders) {
        // 查询订单菜品详情信息(菜品和数量)
        List<OrderDetail> details = orderDetailMapper.getByOrderId(orders);

        // 将每一条信息都转化为字符串
        List<String> orderStrList = details.stream().map(orderDetail -> {
            String orderStr = orderDetail.getName() + "*" + orderDetail.getNumber() + ";";
            return orderStr;
        }).collect(Collectors.toList());

        // 进行拼接
        return String.join("", orderStrList);
    }

    // 查询是否超过配送范围
    private void checkOutOfRange(String address) {
        Map map = new HashMap();
        map.put("address", address);
        map.put("output", "json");
        map.put("city", "北京市");
        map.put("ak", ak);

        // 获取用户的经纬度坐标
        String userCoordinate = HttpClientUtil.doGet("https://api.map.baidu.com/geocoding/v3/", map);

        // 数据解析
        JSONObject jsonObject = JSONObject.parseObject(userCoordinate);
        if (!jsonObject.getString("status").equals("0")) {
            throw new OrderBusinessException(MessageConstant.ADDRESS_RESOLUTION_FAILED);
        }

        // 获取用户地址的经纬度
        JSONObject location = jsonObject.getJSONObject("result").getJSONObject("location");
        String lng = location.getString("lng"); // 经度值
        String lat = location.getString("lat"); // 纬度值
        String userLngLat = lat + "," + lng;

        // 获取商家的经纬度坐标
        map.put("address", shopAddress);
        String adminCoordinate = HttpClientUtil.doGet("https://api.map.baidu.com/geocoding/v3/", map);

        // 数据解析
        jsonObject = JSONObject.parseObject(adminCoordinate);
        if (!jsonObject.getString("status").equals("0")) {
            throw new OrderBusinessException(MessageConstant.ADDRESS_RESOLUTION_FAILED);
        }

        // 获取用户地址的经纬度
        location = jsonObject.getJSONObject("result").getJSONObject("location");
        lng = location.getString("lng");
        lat = location.getString("lat");
        String shopLngLat = lat + "," + lng;

        // 添加请求参数
        map.put("origins", shopLngLat);
        map.put("destinations", userLngLat);
        map.put("riding_type", "1");
        map.remove("address");
        map.remove("city");

        // 发送请求
        String res = HttpClientUtil.doGet("https://api.map.baidu.com/routematrix/v2/riding", map);

        // 数据解析
        JSONObject resObject = JSONObject.parseObject(res);
        if (!resObject.getString("status").equals("0")) {
            throw new OrderBusinessException(MessageConstant.ADDRESS_RESOLUTION_FAILED);
        }

        Integer distance = resObject.getJSONArray("result")
                .getJSONObject(0)
                .getJSONObject("distance")
                .getInteger("value");

        if (distance > 5000) {
            throw new OrderBusinessException(MessageConstant.OUT_OF_DELIVERY);
        }
    }
}
