package com.amzics.service;

import com.amzics.model.domain.SysUser;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户表 服务类
 * </p>
 */
public interface SysUserService extends IService<SysUser> {

    void register(SysUser user);

    SysUser active(String email, String sid);

    SysUser login(SysUser user);

    SysUser rename(SysUser user);


    void updatePassword(SysUser user, String passwordNew);

    void updatePasswordByToken(String email, String sid, String passwordNew);

    String test();
}
