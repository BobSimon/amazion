package com.amzics.model.domain;

import com.amzics.model.validate.SysUserGroup;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_sys_user")
public class SysUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 索引Id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空", groups = SysUserGroup.Register.class)
    private String username;

    /**
     * 用户密码，MD5加密
     */
    @NotBlank(message = "密码不能为空", groups = {SysUserGroup.Register.class})
    private String password;

    /**
     * 邮箱
     */
    @NotBlank(message = "邮箱不能为空", groups = {SysUserGroup.Register.class, SysUserGroup.RestPassword.class})
    @Email(message = "邮箱格式不正确", groups = {SysUserGroup.Register.class, SysUserGroup.RestPassword.class})
    private String email;

    /**
     * 电话
     */
    private String phone;

    /**
     * 角色Id(1-卖家 2-普通用户)
     */
    private Boolean roleid;

    /**
     * 用户等级/类型(0-普通用户的权限 1-VIP权限)
     */
    private Boolean userGrade;

    /**
     * 角色描述
     */
    private String description;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 最后一次登录时间
     */
    private Date lastLoginTime;

    /**
     * 为了安全，考虑生产时加入
     */
    private Date actatorOssCode;

    /**
     * 验证码
     */
    private String captcha;

    /**
     * 验证时间
     */
    private Date verifyTime;

    /**
     * 1表示有效，0表示无效
     */
    private Boolean isValid;

    /**
     * 最后一次登录IP
     */
    private String lastLoginIp;

    /**
     * 更新用户信息时间
     */
    private Date updateTime;

    /**
     * 邮箱校验的token
     */
    private String verifyToken;

    //region ext
    /**
     * 为了兼容前端
     */
    @TableField(exist = false)
    private String userCount;

    @TableField(exist = false)
    private Boolean isKeep;
    //endregion
}
