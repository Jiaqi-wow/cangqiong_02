package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.OrderStatistics;
import com.sky.entity.Orders;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {


    void insertOrder(Orders orders);

    void update(Orders orders);

    Page<OrderVO> selectPageOrders(OrdersPageQueryDTO ordersPageQueryDTO);

    @Select("select * from orders where id = #{orderId}")
    Orders selectById(Long orderId);

    @Update("update orders set status = 6,pay_status = 2 where id = #{orderId}")
    void updateStatusToCancel(Long orderId);

    Page<Orders> selectPageOrdersAdmin(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 订单状态统计
     * @return
     */
    @Select("select status, count(status) as statusNumber from orders group by status ")
    List<OrderStatistics> countOrderStatistics();

    void updateById(Orders orders);

    @Select("select * from orders where status = #{status} and order_time < #{time}")
    List<Orders> selectByStatusAndTimeLT(Integer status, LocalDateTime time);

    Double sumByStatusAndTimeBe(Map map);

    Integer orderCountByStatusAndTime(Map map);

    @Select("select count(*) from orders")
    Integer selectTotalCount();

}
