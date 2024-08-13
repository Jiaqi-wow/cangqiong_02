package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.FlavorMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class DishServiceImpl implements DishService {


    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private FlavorMapper flavorMapper;
    @Autowired
    private RedisTemplate redisTemplate;


    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {

        cleanCache("dish_"  + dishDTO.getCategoryId());

        //添加dish
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        dishMapper.save(dish);

        //批量添加flavor
        List<DishFlavor> flavors = dishDTO.getFlavors();
        flavors.stream().forEach(flavor->flavor.setDishId(dish.getId()));
        flavorMapper.insertBatch(flavors);

    }

    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {

        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());

    }

    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {

        cleanCache("dish_*");

        //判断菜品是否起售
        for (Long id : ids) {
            Integer status = dishMapper.selectById(id);
            if(status == StatusConstant.ENABLE)
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
        }
        //判断菜品是否和套餐关联

        List<Long> setmealIds = dishMapper.dishWithSetmeal(ids);
        if(setmealIds != null && setmealIds.size() > 0)
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        //批量删除菜品

        dishMapper.deleteBatch(ids);
        //批量删除口味

        flavorMapper.deleteBatch(ids);
    }

    @Override
    public DishVO getById(Long id) {
        DishVO dishVO = dishMapper.getById(id);
        List<DishFlavor> dishFlavorList = dishMapper.getFlavorsByDishId(id);
        dishVO.setFlavors(dishFlavorList);
        return dishVO;
    }

    @Override
    @Transactional
    public void update(DishDTO dishDTO) {
        cleanCache("dish_*");
        // 修改菜品
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.update(dish);
        //删除该菜品的口味

        flavorMapper.deleteById(dishDTO.getId());

        //新增口味
        if(dishDTO.getFlavors() != null && dishDTO.getFlavors().size() > 0){
            for (DishFlavor dishFlavor: dishDTO.getFlavors()) {
                dishFlavor.setDishId(dish.getId());
            }
            flavorMapper.insertBatch(dishDTO.getFlavors());
        }

    }

    @Override
    public List<Dish> getByCategoryId(Long categoryId) {

        List<Dish> dishList = dishMapper.selectByCategoryId(categoryId);
        return dishList;
    }

    @Override
    public void stopAndStart(Integer status, Long id) {
        cleanCache("dish_*");
        Dish dish = new Dish();
        dish.setId(id);
        dish.setStatus(status);
        dishMapper.update(dish);
    }

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.selectByCategoryIdAndStatus(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = flavorMapper.getByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }

    private void cleanCache(String key){
        Set keys = redisTemplate.keys(key);
        redisTemplate.delete(keys);
    }
}
