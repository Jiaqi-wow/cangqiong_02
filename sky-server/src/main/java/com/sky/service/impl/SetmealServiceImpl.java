package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.DishMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import io.swagger.models.auth.In;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private DishMapper dishMapper;



    @Transactional
    public void save(SetmealDTO setmealDTO) {
        //套餐表插入
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.save(setmeal);

        //套餐-菜品表插入
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.stream().forEach(setmealDish -> setmealDish.setSetmealId(setmeal.getId()));

        setmealDishMapper.saveBatch(setmealDishes);
    }

    @Transactional
    public PageResult page(SetmealPageQueryDTO setmealPageQueryDTO) {

        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());

        Page<SetmealVO> setmealPage = setmealMapper.pageQuery(setmealPageQueryDTO);

        return new PageResult(setmealPage.getTotal(),setmealPage.getResult());

    }


    public void deleteBatch(List<Long> ids) {
        //如果起售不可以删除

        for (Long id : ids) {
            Integer status = setmealMapper.selectStatusById(id);
            if (status == StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }
        //删除套餐
        setmealMapper.delete(ids);
        //删除套餐菜品关联表
        setmealDishMapper.delete(ids);

    }

    @Transactional
    public SetmealVO getById(Long id) {
        SetmealVO setmealVO = setmealMapper.selectById(id);
        List<SetmealDish> setmealDishList = setmealDishMapper.selectById(id);
        if(setmealDishList != null && setmealDishList.size() > 0) {
            setmealVO.setSetmealDishes(setmealDishList);
        }
        return setmealVO;

    }

    @Transactional
    public void update(SetmealDTO setmealDTO) {
        //修改套餐
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.update(setmeal);

        //删除套餐菜品
        List<Long> setmealIds = new ArrayList<>();
        setmealIds.add(setmeal.getId());
        setmealDishMapper.delete(setmealIds);
        //新增套餐菜品
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.stream().forEach(setmealDish -> setmealDish.setSetmealId(setmeal.getId()));
        setmealDishMapper.saveBatch(setmealDishes);
    }

//    @Override
//    public void stopAndStart(Integer status Long id) {
//        if(status == StatusConstant.ENABLE) {
//            // 判断包含的菜品是否都起售
//
//
//        }else if(status == StatusConstant.DISABLE) {
//            Setmeal setmeal  = new Setmeal();
//            setmeal.setStatus(status);
//            setmealMapper.update(setmeal);
//        }
//    }

    @Transactional
    public void stopAndStart(Integer status, Long setmealId) {
        Setmeal setmeal = new Setmeal();
        setmeal.setStatus(status);
        setmeal.setId(setmealId);
        if(status == StatusConstant.ENABLE) {
            // 判断包含的菜品是否都起售
            List<Long> dishIds = setmealDishMapper.selectDishIdBySetmealId(setmealId);
            for(Long dishId : dishIds) {
                Integer dishStatus = dishMapper.selectById(dishId);
                if(dishStatus == StatusConstant.DISABLE){
                    throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                }
            }
            setmealMapper.update(setmeal);



        }else if(status == StatusConstant.DISABLE) {

            setmealMapper.update(setmeal);
        }
    }

    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }
}
