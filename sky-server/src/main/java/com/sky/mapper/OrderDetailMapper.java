package com.sky.mapper;

import com.sky.entity.OrderDetail;
import com.sky.entity.Top10;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface OrderDetailMapper {


    void insertBatch(List<OrderDetail> orderDetails);

    @Select("select * from order_detail where order_id = #{id}")
    List<OrderDetail> selectByOrderId(Long id);

    List<Top10> selectCountByNameAndTime(Map map);

//    @Select("select sum(number) as sum from order_detail group by name order by sum")
//    List<Integer> selectCountByName();


}
