package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.mapper.DishMapper;
import com.sky.service.ShoppingCartService;
import com.sky.vo.DishVO;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private DishMapper dishMapper;

    @Override
    public void add(ShoppingCartDTO shoppingCartDTO) {
        //查询数据库是否有该购物车记录
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setUserId(BaseContext.getCurrentId());

        List<ShoppingCart> shoppingCartList = shoppingCartMapper.select(shoppingCart);

        if (shoppingCartList != null && shoppingCartList.size() > 0) {
            //如果有，则更新该购物车记录的number字段
            ShoppingCart shoppingCartOne = shoppingCartList.get(0);
            shoppingCartOne.setNumber(shoppingCartOne.getNumber()+1);

            shoppingCartMapper.updateNumber(shoppingCartOne);
        }else {
            //如果没有，则插入。判断是菜品还是套餐
            if(shoppingCart.getDishId() == null){
                //是套餐
                SetmealVO setmealVO = setmealMapper.selectById(shoppingCart.getSetmealId());
                shoppingCart.setName(setmealVO.getName());
                shoppingCart.setNumber(1);
                shoppingCart.setAmount(setmealVO.getPrice());
                shoppingCart.setImage(setmealVO.getImage());
                shoppingCart.setCreateTime(LocalDateTime.now());


            }else{
                //是菜品
                DishVO dishVO = dishMapper.getById(shoppingCart.getDishId());
                shoppingCart.setName(dishVO.getName());
                shoppingCart.setNumber(1);
                shoppingCart.setAmount(dishVO.getPrice());
                shoppingCart.setImage(dishVO.getImage());
                shoppingCart.setCreateTime(LocalDateTime.now());
            }
            shoppingCartMapper.insert(shoppingCart);

        }

    }

    @Override
    public List<ShoppingCart> showShoppingCart() {

        ShoppingCart build = ShoppingCart.builder()
                .userId(BaseContext.getCurrentId())
                .build();
        List<ShoppingCart> select = shoppingCartMapper.select(build);
        return select;

    }

    @Override
    public void cleanShoppingCart() {
        shoppingCartMapper.deleteByUserid(BaseContext.getCurrentId());
    }

    @Override
    public void subShoppingCart(ShoppingCartDTO shoppingCartDTO) {

        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setUserId(BaseContext.getCurrentId());
        ShoppingCart shoppingCart1 = shoppingCartMapper.select(shoppingCart).get(0);

        if (shoppingCart1.getNumber() > 1){
            shoppingCart1.setNumber(shoppingCart1.getNumber()-1);
            shoppingCartMapper.updateNumber(shoppingCart1);
        }else {
            shoppingCartMapper.deleteById(shoppingCart1.getId());
        }

    }
}
