package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FlavorMapper {
    /**
     * 批量添加口味
     * @param flavors
     */
    void insertBatch(List<DishFlavor> flavors);

    void deleteBatch(List<Long> ids);

    @Delete("delete from dish_flavor where dish_id = #{dishId}")
    void deleteById(Long dishId);

    @Select("select * from dish_flavor where dish_id = #{id}")
    List<DishFlavor> getByDishId(Long id);
}
