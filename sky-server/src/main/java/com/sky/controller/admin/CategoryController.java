package com.sky.controller.admin;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.License;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/category")
@Slf4j
@Api
public class CategoryController {

    @Autowired
    private CategoryService categoryService;


    /**
     * 新增分类
     * @param categoryDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增分类")
    public Result save(@RequestBody CategoryDTO categoryDTO) {
        log.info("新增分类");
        categoryService.save(categoryDTO);
        return Result.success();
    }

    /**
     * 分页查询
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("分类分页查询")
    public Result<PageResult> page(CategoryPageQueryDTO categoryPageQueryDTO){
        log.info("分类分页查询");
        PageResult pageResult = categoryService.page(categoryPageQueryDTO);
        return Result.success(pageResult);
    }

//    @GetMapping
//    @ApiOperation("根据id查询分类")
//    public Result<Category> queryById(@RequestParam Integer id){
//        log.info("根据id查询分类");
//        return null;
//    }

    @PostMapping("/status/{status}")
    @ApiOperation("启用禁用")
    public Result stopAndStart(Long id, @PathVariable Integer status){
        log.info("启用禁用");
        categoryService.stopAndStart(id, status);
        return Result.success();
    }

    @PutMapping
    @ApiOperation("修改分类")
    public Result update(@RequestBody CategoryDTO categoryDTO){
        log.info("修改分类");
        categoryService.update(categoryDTO);
        return Result.success();
    }

    /**
     * 单条删除
     * @param id
     * @return
     */
    @DeleteMapping
    @ApiOperation("根据id删除分类")
    public Result delete(Long id){
        log.info("删除id");
        categoryService.delete(id);
        return Result.success();
    }

    /**
     * 根据类型查询分类
     * @param type
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据类型查询分类")
    public Result<List> requryByType(Integer type){
        List<Category> categoryList=categoryService.requryByType(type);

        return Result.success(categoryList);
    }

}
