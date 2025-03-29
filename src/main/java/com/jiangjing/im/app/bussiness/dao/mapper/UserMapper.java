package com.jiangjing.im.app.bussiness.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jiangjing.im.app.bussiness.dao.UserEntity;
import com.jiangjing.im.app.bussiness.model.req.SearchUserReq;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author Admin
 */
@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {

     List<String> searchUser(SearchUserReq req);

}
