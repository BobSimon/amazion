package com.amzics.service.impl;

import com.amzics.common.consts.RedisKey;
import com.amzics.common.utils.EncryptUtils;
import com.amzics.mapper.SysUserMapper;
import com.amzics.model.domain.SysUser;
import com.amzics.model.exception.BusinessException;
import com.amzics.service.SysUserService;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 注册用户
     */
    @Override
    public void register(SysUser user) {
        //获取用户名或者邮箱相同的user
        Wrapper<SysUser> wrapper = new QueryWrapper<SysUser>().lambda()
                .eq(SysUser::getUsername, user.getUsername())
                .or(w -> w.eq(SysUser::getEmail, user.getEmail()));

        SysUser sysUser = baseMapper.selectOne(wrapper);
        //如果用户已经超时了
        if (null != sysUser
                && !sysUser.getIsValid()
                && null != sysUser.getCreateTime()) {
            long timespan = new Date().getTime() - sysUser.getCreateTime().getTime();
            //两个小时没有激活了
            if (timespan >= 2 * 60 * 60 * 1000) {
                //用户已经超时激活时间了,删除
                this.removeById(sysUser.getId());
                sysUser = null;
            }
        }
        //判断用户名还是邮箱相同
        if (null != sysUser) {
            if (user.getEmail().equals(sysUser.getEmail())) {
                //邮箱相同
                throw new BusinessException("邮箱已存在！");
            }
            if (user.getUsername().equals(sysUser.getUsername())) {
                //用户名相同
                throw new BusinessException("用户名已存在！");
            }
        }
        //region 炳哥原逻辑
        user.setRoleid(false);
        user.setUserGrade(false);
        //endregion
        user.setIsValid(false);
        user.setVerifyToken(UUID.randomUUID().toString());
        user.setPassword(EncryptUtils.md5Encrypt(user.getPassword()));
        user.setCreateTime(new Date());
        int rows = baseMapper.insert(user);
        if (rows < 1) {
            throw new BusinessException("注册失败！");
        }
    }

    /**
     * 激活用户
     */
    @Override
    public SysUser active(String email, String sid) {
        LambdaQueryWrapper<SysUser> eq = new QueryWrapper<SysUser>().lambda().eq(SysUser::getEmail, email).and(w -> w.eq(SysUser::getVerifyToken, sid));
        SysUser sysUser = baseMapper.selectOne(eq);
        if (null == sysUser || sysUser.getIsValid()) {
            throw new BusinessException("无效的链接");
        }
        //是否超过验证验证时间
        long timespan = new Date().getTime() - sysUser.getCreateTime().getTime();
        if (timespan >= 2 * 60 * 60 * 1000) {
            throw new BusinessException("链接已经失效");
        }
        sysUser.setVerifyTime(new Date());
        sysUser.setLastLoginTime(new Date());
        sysUser.setIsValid(true);
        baseMapper.updateById(sysUser);
        return sysUser;
    }

    /**
     * 登录
     */
    @Override
    public SysUser login(SysUser user) {
        LambdaQueryWrapper<SysUser> wrapper = new QueryWrapper<SysUser>().lambda()
                .eq(SysUser::getUsername, user.getUserCount())
                        .or(w2 -> w2.eq(SysUser::getEmail, user.getUserCount()));
        SysUser sysUser = baseMapper.selectOne(wrapper);
        if (null == sysUser) {
            //用户不存在
            throw new BusinessException("用户名不存在");
        }
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        String key = RedisKey.LOGIN_ERROR_COUNT + sysUser.getId();
        if (!EncryptUtils.md5Encrypt(user.getPassword()).equals(sysUser.getPassword())) {
            valueOperations.increment(key, 1);
            //看看错误次数有没有超过5次,如果超过5次,锁定账号
            String errorCount = valueOperations.get(key);
            if (StringUtils.isNotBlank(errorCount) && Integer.valueOf(errorCount) == 5) {
                //锁定30分钟
                redisTemplate.expire(key, 30, TimeUnit.MINUTES);
            }
            throw new BusinessException("密码错误");
        }
        if (!sysUser.getIsValid()) {
            throw new BusinessException("账号未激活,请前往邮箱激活");
        }

        //被锁定
        String errorCount = valueOperations.get(key);
        if (StringUtils.isNotBlank(errorCount) && Integer.valueOf(errorCount) >= 5) {
            throw new BusinessException("账号被锁定！请等待30分钟后重试");
        }
        //devin 正常登录 IP暂时没弄,源项目也没弄
        sysUser.setLastLoginIp("localhost");
        sysUser.setLastLoginTime(new Date());
        baseMapper.updateById(sysUser);
        //清除锁定信息
        redisTemplate.delete(key);
        return sysUser.setPassword(null);
    }

    /**
     * 重命名-用户名
     */
    @Override
    public SysUser rename(SysUser user) {
        String newUsername = user.getUsername();
        Wrapper<SysUser> wrapper = new QueryWrapper<SysUser>().lambda().eq(SysUser::getUsername, newUsername);
        if (baseMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("用户名被占用了!");
        }
        user = baseMapper.selectById(user.getId()).setUsername(newUsername);
        if (baseMapper.updateById(user) <= 0) {
            throw new BusinessException("修改个人信息失败");
        }
        return user.setPassword(null);
    }

    /**
     * 修改密码
     */
    @Override
    public void updatePassword(SysUser user, String passwordNew) {
        String oldPassword = EncryptUtils.md5Encrypt(user.getPassword());
        user = baseMapper.selectById(user.getId());
        if (!oldPassword.equals(user.getPassword())) {
            //密码不一致
            throw new BusinessException("旧密码不正确!");
        }
        user.setPassword(EncryptUtils.md5Encrypt(passwordNew));
        baseMapper.updateById(user);
    }

    @Override
    public void updatePasswordByToken(String email, String sid, String passwordNew) {
        LambdaQueryWrapper<SysUser> eq = new QueryWrapper<SysUser>().lambda().eq(SysUser::getEmail, email).and(w -> w.eq(SysUser::getVerifyToken, sid));
        SysUser sysUser = baseMapper.selectOne(eq);
        if (null == sysUser || !sysUser.getIsValid()) {
            throw new BusinessException("非法的修改操作!");
        }
        if (baseMapper.updateById(sysUser.setPassword(EncryptUtils.md5Encrypt(passwordNew))) < 1) {
            throw new BusinessException("修改密码失败");
        }
    }

    @Override
    public String test() {
        int a = 0;
        return String.valueOf(1 / a);
    }
}

