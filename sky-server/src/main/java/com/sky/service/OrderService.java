package com.sky.service;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.Orders;
import com.sky.result.PageResult;
import com.sky.vo.*;

import java.time.LocalDate;

public interface OrderService {
    OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO);

    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 查询历史订单
     * @param ordersPageQueryDTO
     * @return
     */
    PageResult  getHistoryOrders(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 根据订单id查看订单详情
     * @param orderId
     * @return
     */
    OrderVO getOrderDetailByOrderId(Long orderId);

    /**
     * 取消订单
     * @param orderId
     */
    void cancelOrder(Long orderId);

    void repetition(Long orderId);

    /**
     * 管理端获取订单
     * @param ordersPageQueryDTO
     * @return
     */
    PageResult getOrdersAdmin(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 订单状态统计
     * @return
     */
    OrderStatisticsVO getOrderStatistics();

    void confirmed(Orders orders);

    /**
     * 拒单
     * @param orders
     */
    void rejection(Orders orders);

    /**
     * 派送
     * @param id
     */
    void delivery(Long id);

    /**
     * 商户取消订单
     * @param orders
     */
    void cancelOrderAdmin(Orders orders);

    /**
     * 完成订单
     * @param id
     */
    void complete(Long id);

    void reminder(Long id);



}
