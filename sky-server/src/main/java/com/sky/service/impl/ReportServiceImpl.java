package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.entity.Top10;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.*;
import io.swagger.models.auth.In;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private OrderDetailMapper  orderDetailMapper;

    @Override
    public TurnoverReportVO turnoverReport(LocalDate begin, LocalDate end) {

        List<LocalDate> localDateList = getDateList(begin, end);

        //生成营业额列表
        List<Double> doubleList = new ArrayList<>();
        for (LocalDate localDate : localDateList) {

            LocalDateTime startTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);

            Map map = new HashMap();
            map.put("status", Orders.COMPLETED);
            map.put("startTime", startTime);
            map.put("endTime", endTime);

            Double sum = orderMapper.sumByStatusAndTimeBe(map);
            sum = sum == null ? 0 : sum;
            doubleList.add(sum);

        }
        //构造VO
        return TurnoverReportVO.builder()
                .turnoverList(StringUtils.join(doubleList,","))
                .dateList(StringUtils.join(localDateList,","))
                .build();
    }

    @Override
    public UserReportVO userReport(LocalDate begin, LocalDate end) {

        List<LocalDate> localDateList = getDateList(begin, end);
        //生成用户总数列表和新增用户数量列表
        List<Integer> userTotalList = new ArrayList<>();
        List<Integer> newUserList = new ArrayList<>();
        for (LocalDate localDate : localDateList) {

            LocalDateTime startTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);

            Map map = new HashMap();
            map.put("endTime", endTime);

            Integer totalUser = userMapper.countByTime(map);
            userTotalList.add(totalUser);

            map.put("startTime", startTime);
            Integer newUser = userMapper.countByTime(map);
            newUserList.add(newUser);


        }
        //构造VO
        return UserReportVO.builder()
                .totalUserList(StringUtils.join(userTotalList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .dateList(StringUtils.join(localDateList, ","))
                .build();
    }

    @Override
    public OrderReportVO ordersReport(LocalDate begin, LocalDate end) {
        //生成时间列表
        List<LocalDate> localDateList = getDateList(begin, end);

        //订单数列表
        List<Integer> orderNumberList = new ArrayList<>();
        //有效订单数列表
        List<Integer> effectOrderNumberList = new ArrayList<>();

        for (LocalDate localDate : localDateList) {

            LocalDateTime startTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);

            Map map = new HashMap();
            map.put("startTime", startTime);
            map.put("endTime", endTime);

            Integer orderNumber = orderMapper.orderCountByStatusAndTime(map);
            orderNumberList.add(orderNumber);

            map.put("status", Orders.COMPLETED);
            Integer effectOrderNumber = orderMapper.orderCountByStatusAndTime(map);
            effectOrderNumberList.add(effectOrderNumber);
        }
//        LocalDateTime startTime = LocalDateTime.of(begin, LocalTime.MIN);
//        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
//
//        Map map = new HashMap();
//        map.put("startTime", startTime);
//        map.put("endTime", endTime);
//        Integer totalOrderCount = orderMapper.orderCountByStatusAndTime(map);
//
//        map.put("status", Orders.COMPLETED);
//        Integer  validOrderCount = orderMapper.orderCountByStatusAndTime(map);

        Integer totalOrderCount = orderNumberList.stream().reduce(0,Integer::sum);
        Integer validOrderCount = effectOrderNumberList.stream().reduce(0,Integer::sum);

        Double orderCompletionRate = validOrderCount * 1.0 / totalOrderCount;



        return OrderReportVO.builder()
                .dateList(StringUtils.join(localDateList,","))
                .orderCountList(StringUtils.join(orderNumberList, ","))
                .validOrderCountList(StringUtils.join(effectOrderNumberList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    @Override
    public SalesTop10ReportVO top10(LocalDate begin, LocalDate end) {
//        String[] nameList = new String[10];
//        Integer[] numberList = new Integer[10];

        LocalDateTime startTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        Map map = new HashMap();
        map.put("startTime", startTime);
        map.put("endTime", endTime);
        map.put("status", Orders.COMPLETED);
        List<Top10> countList = orderDetailMapper.selectCountByNameAndTime(map);

//        int index = 0;
//        for (Top10 top10 : countList) {
//            nameList[index] = top10.getName();
//            numberList[index] = top10.getSum();
//            index++;
//            if(index == 10){
//                break;
//            }
//        }

        List<String> nameList = countList.stream().map(Top10::getName).collect(Collectors.toList());
        List<Integer> numberList = countList.stream().map(Top10::getSum).collect(Collectors.toList());

        return SalesTop10ReportVO
                .builder()
                .nameList(StringUtils.join(nameList,","))
                .numberList(StringUtils.join(numberList, ","))
                .build();

    }

    private List<LocalDate> getDateList(LocalDate begin, LocalDate end) {
        //生成时间列表
        List<LocalDate> localDateList = new ArrayList<>();
        localDateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            localDateList.add(begin);
        }
        return localDateList;
    }
}
