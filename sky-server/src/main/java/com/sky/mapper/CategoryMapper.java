package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CategoryMapper {
    @Insert("insert into category(type, name, sort, status, create_time, update_time, create_user, update_user) " +
            "values (#{type}, #{name}, #{sort}, #{status}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    void insert(Category category);

    /**
     * 分页分类查询
     * @param categoryPageQueryDTO
     * @return
     */
    Page<Category> selectPageRequry(CategoryPageQueryDTO categoryPageQueryDTO);

    /**
     * 分类启用禁用
     * @param category
     */
    void update(Category category);

    /**
     * 单条删除
     * @param id
     */
    @Delete("delete from category where id=#{id}")
    void deleteById(Long id);

    /**
     * 根据类型查询分类
     * @param type
     * @return
     */
    @Select("select * from category where type=#{type}")
    List<Category> selectByType(Integer type);
}
