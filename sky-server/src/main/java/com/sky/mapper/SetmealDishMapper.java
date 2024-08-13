package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {
    /**
     * 批量插入套餐菜品
     * @param setmealDishes
     */
    void saveBatch(List<SetmealDish> setmealDishes);

    /**
     * 批量删除套餐菜品表
     * @param ids
     */
    void delete(List<Long> setmeal_ids);

    /**
     * 根据id获取套餐菜品列表
     * @param id
     * @return
     */
    @Select("select * from setmeal_dish where setmeal_id = #{setmealId}")
    List<SetmealDish> selectById(Long setmealId);

    @Select("select dish_id from setmeal_dish where setmeal_id =#{setmealId}")
    List<Long> selectDishIdBySetmealId(Long setmealId);
}
