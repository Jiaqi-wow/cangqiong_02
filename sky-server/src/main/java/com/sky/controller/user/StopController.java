package com.sky.controller.user;

import com.sky.result.Result;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("userStopController")
@RequestMapping("/user/shop")
public class StopController {

    @Autowired
    private RedisTemplate redisTemplate;

    public static final String STATUS = "status";




    @GetMapping("/status")
    @ApiOperation("获取营业状态")
    public Result<Integer> getStatus(){
        Integer o = (Integer) redisTemplate.opsForValue().get(STATUS);
        return Result.success(o);
    }
}
