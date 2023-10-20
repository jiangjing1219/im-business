package com.jiangjing.im.app.bussiness.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jiangjing.im.app.bussiness.dao.UserEntity;
import com.jiangjing.im.app.bussiness.model.req.SearchUserReq;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author Admin
 */
@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {


    @Select("<script>" +
            " select user_id from app_user  " +
            "<if test = 'searchType == 1'> " +
            " where mobile = #{keyWord} " +
            " </if>" +
            " <if test = 'searchType == 2'> " +
            "  where user_name = #{keyWord} " +
            " </if> " +
            " </script> ")
    public List<String> searchUser(SearchUserReq req);

}
