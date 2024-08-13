package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderStatusTask {

    @Autowired
    private OrderMapper orderMapper;

    @Scheduled(cron = "0 * * * * ?")
    public void orderOutTimeTask(){
        Integer status = Orders.UN_PAID;
        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);
        List<Orders> ordersList = orderMapper.selectByStatusAndTimeLT(status, time);
        if(ordersList != null && ordersList.size() > 0){
            for(Orders order : ordersList){
                order.setStatus(Orders.CANCELLED);
                order.setCancelTime(time.plusMinutes(15));
                order.setCancelReason("订单超时，已取消");
                log.info("订单{}超时，已取消",order.getId());
                orderMapper.updateById(order);
            }
        }
    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void orderDeliveryTask(){
        Integer status = Orders.DELIVERY_IN_PROGRESS;
        LocalDateTime time = LocalDateTime.now().plusMinutes(-60);
        List<Orders> ordersList = orderMapper.selectByStatusAndTimeLT(status, time);
        if(ordersList != null && ordersList.size() > 0){
            for(Orders order : ordersList){
                order.setStatus(Orders.COMPLETED);
                log.info("订单{}派送完成",order.getId());
                orderMapper.updateById(order);
            }
        }
    }
}
