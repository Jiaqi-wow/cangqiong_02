package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.*;
import com.sky.ws.WebSocketServer;
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

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
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WebSocketServer webSocketServer;

    @Transactional
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        //1. 处理业务异常，地址为空和购物车为空时抛出业务异常
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(BaseContext.getCurrentId());
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.select(shoppingCart);
        if(shoppingCartList == null || shoppingCartList.size() == 0) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        //2. 插入order表中
        Orders orders = new Orders();
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setStatus(1);
        orders.setUserId(BaseContext.getCurrentId());
        orders.setAddressBookId(addressBook.getId());
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayMethod(ordersSubmitDTO.getPayMethod());
        orders.setPayStatus(0);
        orders.setAmount(ordersSubmitDTO.getAmount());
        orders.setRemark(ordersSubmitDTO.getRemark());
        orders.setAddress(addressBook.getDetail());
        orders.setConsignee(addressBook.getConsignee());
        orders.setEstimatedDeliveryTime(ordersSubmitDTO.getEstimatedDeliveryTime());
        orders.setDeliveryStatus(ordersSubmitDTO.getDeliveryStatus());
        orders.setPackAmount(ordersSubmitDTO.getPackAmount());
        orders.setTablewareNumber(ordersSubmitDTO.getTablewareNumber());
        orders.setTablewareStatus(ordersSubmitDTO.getTablewareStatus());

        orderMapper.insertOrder(orders);

        //3. 批量插入订单详情表中
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (ShoppingCart cart : shoppingCartList) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetailList.add(orderDetail);
        }

        orderDetailMapper.insertBatch(orderDetailList);

        //4. 删除购物车
        shoppingCartMapper.deleteByUserid(BaseContext.getCurrentId());

        //5. 构造VO数据
        OrderSubmitVO orderSubmitVO = new OrderSubmitVO();
        orderSubmitVO.setOrderNumber(orders.getNumber());
        orderSubmitVO.setId(orders.getId());
        orderSubmitVO.setOrderAmount(orders.getAmount());
        orderSubmitVO.setOrderTime(orders.getOrderTime());

        //6.返回
        return orderSubmitVO;
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */

    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        JSONObject jsonObject = new JSONObject();
        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );
//
//        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
//            throw new OrderBusinessException("该订单已支付");
//        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        //修改订单状态为已支付
        Orders orders = new Orders();
//        orders.setUserId(userId);
        orders.setNumber(ordersPaymentDTO.getOrderNumber());
        orders.setStatus(Orders.TO_BE_CONFIRMED);
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setPayStatus(Orders.PAID);

        orderMapper.update(orders);

        //向客户端推送来单提醒
        Map map = new HashMap();
        map.put("type",1);
        map.put("orderId",orders.getId());
        map.put("content","订单号" + orders.getNumber());

        String jsonString = JSON.toJSONString(map);

        webSocketServer.sendToAllClient(jsonString);

        return vo;
    }

    @Override
    public PageResult getHistoryOrders(OrdersPageQueryDTO ordersPageQueryDTO) {
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());

        //查出order
        Page<OrderVO> page =  orderMapper.selectPageOrders(ordersPageQueryDTO);

        for (OrderVO orderVO : page.getResult()) {
            //查订单详情
            List<OrderDetail> orderDetailList = orderDetailMapper.selectByOrderId(orderVO.getId());
            orderVO.setOrderDetailList(orderDetailList);
        }
        return new PageResult(page.getTotal(),page.getResult());

    }

    @Override
    public OrderVO getOrderDetailByOrderId(Long orderId) {
        //获取订单信息
        Orders orders = orderMapper.selectById(orderId);

        //获取订单详情信息
        List<OrderDetail> orderDetailList = orderDetailMapper.selectByOrderId(orderId);

        //构造VO
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(orderDetailList);

        return orderVO;
    }

    @Override
    public void cancelOrder(Long orderId) {
        //取消订单的同时要退款
        orderMapper.updateStatusToCancel(orderId);
    }

    @Override
    public void repetition(Long orderId) {
        //查出订单详情
        List<OrderDetail> orderDetailList = orderDetailMapper.selectByOrderId(orderId);
        //添加到购物车中
        List<ShoppingCart> shoppingCartList = new ArrayList<>();
        for (OrderDetail orderDetail : orderDetailList) {
                orderDetail.setId(null);

                ShoppingCart shoppingCart = new ShoppingCart();
                BeanUtils.copyProperties(orderDetail, shoppingCart);
                shoppingCart.setUserId(BaseContext.getCurrentId());
                shoppingCart.setId(null);
                shoppingCart.setCreateTime(LocalDateTime.now());

                shoppingCartList.add(shoppingCart);
        }
        shoppingCartMapper.insertBatch(shoppingCartList);
    }

    @Override
    public PageResult getOrdersAdmin(OrdersPageQueryDTO ordersPageQueryDTO) {

        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());
        Page<Orders> page = orderMapper.selectPageOrdersAdmin(ordersPageQueryDTO);
//        if(page.getTotal() == 0){
//            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
//        }
//        return new PageResult(page.getTotal(),page.getResult());
        // 部分订单状态，需要额外返回订单菜品信息，将Orders转化为OrderVO
        List<OrderVO> orderVOList = getOrderVOList(page);

        return new PageResult(page.getTotal(), orderVOList);
    }

    private List<OrderVO> getOrderVOList(Page<Orders> page) {
        // 需要返回订单菜品信息，自定义OrderVO响应结果
        List<OrderVO> orderVOList = new ArrayList<>();

        List<Orders> ordersList = page.getResult();
        if (!CollectionUtils.isEmpty(ordersList)) {
            for (Orders orders : ordersList) {
                // 将共同字段复制到OrderVO
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                String orderDishes = getOrderDishesStr(orders);

                // 将订单菜品信息封装到orderVO中，并添加到orderVOList
                orderVO.setOrderDishes(orderDishes);
                orderVOList.add(orderVO);
            }
        }
        return orderVOList;
    }

    /**
     * 根据订单id获取菜品信息字符串
     *
     * @param orders
     * @return
     */
    private String getOrderDishesStr(Orders orders) {
        // 查询订单菜品详情信息（订单中的菜品和数量）
        List<OrderDetail> orderDetailList = orderDetailMapper.selectByOrderId(orders.getId());

        // 将每一条订单菜品信息拼接为字符串（格式：宫保鸡丁*3；）
        List<String> orderDishList = orderDetailList.stream().map(x -> {
            String orderDish = x.getName() + "*" + x.getNumber() + ";";
            return orderDish;
        }).collect(Collectors.toList());

        // 将该订单对应的所有菜品信息拼接在一起
        return String.join("", orderDishList);
    }

    @Override
    public OrderStatisticsVO getOrderStatistics() {
        List<OrderStatistics> list = new ArrayList<>();
        list = orderMapper.countOrderStatistics();

        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        for (OrderStatistics orderStatistics : list) {
            if(orderStatistics.getStatus() == Orders.TO_BE_CONFIRMED){
                orderStatisticsVO.setToBeConfirmed(orderStatistics.getStatusNUmber());
            }else if(orderStatistics.getStatus() == Orders.CONFIRMED){
                orderStatisticsVO.setConfirmed(orderStatistics.getStatusNUmber());
            }else if(orderStatistics.getStatus() == Orders.DELIVERY_IN_PROGRESS){
                orderStatisticsVO.setDeliveryInProgress(orderStatistics.getStatusNUmber());
            }
        }
        return orderStatisticsVO;
    }

    @Override
    public void confirmed(Orders orders) {


        orders.setStatus(Orders.CONFIRMED);
        orderMapper.updateById(orders);
    }

    @Override
    public void rejection(Orders orders) {

        //不提醒用户单被拒绝了吗？

        //退款

        //订单状态变为已取消，支付状态变为退款，增加拒单原因
        orders.setPayStatus(Orders.REFUND);
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.updateById(orders);

    }

    @Override
    public void delivery(Long id) {
        Orders orders = new Orders();
        orders.setId(id);
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);
        orderMapper.updateById(orders);
    }

    @Override
    public void cancelOrderAdmin(Orders orders) {
        //退款
        //修改状态
        orders.setStatus(Orders.CANCELLED);
        orders.setPayStatus(Orders.REFUND);

        orderMapper.updateById(orders);
    }

    @Override
    public void complete(Long id) {
        Orders orders = new Orders();
        orders.setId(id);
        orders.setStatus(Orders.COMPLETED);

        orderMapper.updateById(orders);
    }

    @Override
    public void reminder(Long id) {
        Orders orders = orderMapper.selectById(id);
        if(orders == null){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Map map = new HashMap();
        map.put("type", 2);
        map.put("orderId", id);
        map.put("content", "订单号" + orders.getNumber());

        String jsonString = JSON.toJSONString(map);

        webSocketServer.sendToAllClient(jsonString);
    }


}
