package com.sky.controller.admin;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("adminOrderController")
@RequestMapping("/admin/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/conditionSearch")
    @ApiOperation("条件分页查询")
    public Result<PageResult> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageResult historyOrders = orderService.getOrdersAdmin(ordersPageQueryDTO);

        return Result.success(historyOrders);
    }

    @GetMapping("/statistics")
    @ApiOperation("订单状态统计")
    public Result<OrderStatisticsVO> getOrderStatistics() {
        OrderStatisticsVO statisticsVO = orderService.getOrderStatistics();
        return Result.success(statisticsVO);
    }

    @GetMapping("/details/{id}")
    @ApiOperation("查看订单详情")
    public Result<OrderVO> getOrdersDetail(@PathVariable Long id){
        OrderVO orderDetailByOrderId = orderService.getOrderDetailByOrderId(id);
        return Result.success(orderDetailByOrderId);
    }

    @PutMapping("/confirm")
    @ApiOperation("接单")
    public Result confirmed(@RequestBody Orders orders){
        orderService.confirmed(orders);
        return Result.success();
    }

    @PutMapping("/rejection")
    @ApiOperation("拒单")
    public Result rejection(@RequestBody Orders orders){
        orderService.rejection(orders);
        return Result.success();
    }

    @PutMapping("/delivery/{id}")
    @ApiOperation("派送")
    public Result delivery(@PathVariable Long id){
        orderService.delivery(id);
        return Result.success();
    }

    @PutMapping("/cancel")
    @ApiOperation("取消")
    public Result cancel(@RequestBody Orders orders){
        orderService.cancelOrderAdmin(orders);
        return Result.success();
    }

    @PutMapping("/complete/{id}")
    public Result complete(@PathVariable Long id){
        orderService.complete(id);
        return Result.success();
    }
}
