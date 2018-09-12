package com.amzics.web.controller;


import com.amzics.common.consts.CookieKey;
import com.amzics.common.consts.SessionKey;
import com.amzics.common.utils.CaptchaUtils;
import com.amzics.common.utils.EncryptUtils;
import com.amzics.common.utils.JsonUtils;
import com.amzics.model.annotation.Authen;
import com.amzics.model.domain.SysUser;
import com.amzics.model.exception.BusinessException;
import com.amzics.model.pojo.RestResult;
import com.amzics.model.validate.SysUserGroup;
import com.amzics.service.SysUserService;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.util.ResourceUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.Cookie;
import javax.validation.constraints.NotBlank;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@Validated
@RestController
@RequestMapping("/user")
public class SysUserController extends BaseController {
    //region service
    @Autowired
    private SysUserService userService;
    //endregion

    @Autowired
    private JavaMailSender mailSender;

    @Value("${amzics.open-captcha}")
    private boolean openCaptcha;


    /**
     * 兼容前端,获取验证码地址,其实没有什么用,多转了一步
     */
    @RequestMapping(value = "/captcha")
    public RestResult captcha() {
        UriComponents url = ServletUriComponentsBuilder.fromContextPath(getRequest()).path("/user/generateCaptcha").queryParam("time", System.currentTimeMillis()).build();
        return sucess((Object) url.encode().toUriString());
    }

    /**
     * 生成验证码
     */
    @RequestMapping("/generateCaptcha")
    public ResponseEntity<byte[]> generateCaptcha() {
        String captcha = CaptchaUtils.generateCode(4);
        getSession().setAttribute(SessionKey.CAPTCHA, captcha);
        byte[] buffer = CaptchaUtils.generateImage(captcha);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Cache-Control", "no-store");
        headers.set("Pragma", "no-cache");
        headers.setContentType(MediaType.IMAGE_JPEG);
        return new ResponseEntity<byte[]>(buffer, headers, HttpStatus.OK);
    }

    /**
     * 注册
     */
    @PostMapping("/register")
    public RestResult register(@Validated(SysUserGroup.Register.class) SysUser user) {
        validateCaptcha(user.getCaptcha());
        userService.register(user);
        //发送邮件
        HashMap<String, String> map = new HashMap<>();
        map.put("userName", user.getUsername());
        map.put("action", "完成邮箱验证");
        map.put("url", ServletUriComponentsBuilder
                .fromContextPath(getRequest()).path("/activate.html")
                .queryParam("sid", user.getVerifyToken())
                .queryParam("email", user.getEmail()).build().toUriString());
        map.put("time", new SimpleDateFormat("yyyy.MM.dd").format(new Date()));
        sendEmail(user.getEmail(), map.get("action"), "classpath:resources/validate-email-templates.ftl", map);
        return sucess("验证邮件已发送，在邮件中点击链接完成激活");
    }

    /**
     * 激活账号
     */
    @RequestMapping("/activate")
    public RestResult activate(@NotBlank(message = "链接无效") String email, @NotBlank(message = "链接无效") String sid) {
        SysUser user = userService.active(email, sid);
        user.setPassword(null);
        //激活之后直接登录
        getSession().setAttribute(SessionKey.USER, user);
        return sucess("激活成功");
    }

    /**
     * 登陆
     */
    @PostMapping("/login")
    public RestResult login(SysUser user) {
        SysUser cookieUser = getUserByCookie();
        if (null == cookieUser) {
            // 表单登录，校验验证码
            validateCaptcha(user.getCaptcha());
        } else {
            //rememberMe登录
            user = cookieUser;
        }
        SysUser loginedUser = null;
        //登录
        try {
            loginedUser = userService.login(user);
        } catch (BusinessException e) {
            //登录失败、锁定、未激活、统统不允许remeberMe,必须移除cookie
            invalidRememberMe();
            throw e;
        }
        getSession().setAttribute(SessionKey.USER, loginedUser);

        //rememberMe
        Boolean rememberMe = user.getIsKeep();
        if (null != rememberMe && rememberMe) {
            getResponse().addCookie(rememberMeCookie(user));
        }
        return sucess("登录成功", loginedUser);
    }

    /**
     * 注销
     *
     * @return
     */
    @RequestMapping("/logout")
    @Authen
    public RestResult logout() {
        invalidRememberMe();
        //失效session
        getRequest().getSession().invalidate();
        return sucess("注销成功!");
    }

    /**
     * 获取用户信息 ps:我也不懂原项目搞那么多接口干嘛,这里兼容前端
     *
     * @return
     */
    @Authen
    @RequestMapping(value = {"/information", "/getUserInfo"})
    public RestResult getUserInfo() {
        return sucess(getUser());
    }

    /**
     * 修改用户名
     *
     * @param user
     * @return
     */
    @Authen
    @RequestMapping("/updateUserInfo")
    public RestResult updateUsername(SysUser user) {
        user = getUser().setUsername(user.getUsername());
        user = userService.rename(user);
        getSession().setAttribute(SessionKey.USER, user);
        return sucess("更新个人信息成功", user);
    }

    /**
     * 页面直接修改密码
     */
    @Authen
    @RequestMapping("/updatePassword")
    public RestResult updatePassword(@NotBlank(message = "旧密码不能为空") String passwordOld, @NotBlank(message = "新密码不能为空") String passwordNew) {
        userService.updatePassword(getUser().setPassword(passwordOld), passwordNew);
        //修改密码后,RememberMe必须无效
        invalidRememberMe();
        return sucess("修改密码成功!");
    }

    /**
     * 发送忘记密码邮件
     */
    //devin 这里到时候加上限制
    @RequestMapping("/forgetPasswordEmail")
    public RestResult forgetPassword(@Validated(SysUserGroup.RestPassword.class) SysUser user) {
        validateCaptcha(user.getCaptcha());
        Wrapper<SysUser> wrapper = new QueryWrapper<SysUser>().lambda().eq(SysUser::getEmail, user.getEmail()).eq(SysUser::getIsValid, true);
        user = userService.getOne(wrapper);
        if (null == user) {
            return error("邮箱不存在!");
        }
        String token = UUID.randomUUID().toString();
        userService.updateById(user.setVerifyToken(token));
        //发送邮件
        HashMap<String, String> map = new HashMap<>();
        map.put("userName", user.getUsername());
        map.put("action", "重置密码");
        map.put("url", ServletUriComponentsBuilder
                .fromContextPath(getRequest()).path("/reset-pass.html")
                .queryParam("sid", token)
                .queryParam("email", user.getEmail()).build().toUriString());
        map.put("time", new SimpleDateFormat("yyyy.MM.dd").format(new Date()));
        sendEmail(user.getEmail(), map.get("action"), "classpath:resources/validate-email-templates.ftl", map);
        return sucess("重置密码邮件已发送到您的邮箱，请查收！");
    }

    /**
     * 邮箱修改密码
     */
    @RequestMapping("/resetPassword")
    public RestResult resetPassword(@NotBlank(message = "邮箱不可为空") String email,
                                    @NotBlank(message = "sid不可为空") String sid,
                                    @NotBlank(message = "密码不可为空") String passwordNew) {
        userService.updatePasswordByToken(email, sid, passwordNew);
        //修改密码后,RememberMe必须无效
        invalidRememberMe();
        return sucess("修改成功");
    }

    /**
     * 吐槽
     */
    @RequestMapping(value = "/complain")
    public RestResult complain() {
        //devin 暂时没有时间加上,加上的时候记得限制访问
        return error("该功能暂时暂时要维护一段时间哦!");
    }

    @GetMapping("test")
    public RestResult testLog() {
        return sucess(userService.test());
    }
    //region private

    /**
     * 校验验证码
     *
     * @param clientCaptcha
     */
    private void validateCaptcha(String clientCaptcha) {
        String serverCaptcha = (String) getSession().getAttribute(SessionKey.CAPTCHA);
        if (!openCaptcha) {
            //不校验验证码
            return;
        }
        if (StringUtils.isBlank(clientCaptcha)) {
            throw new BusinessException("验证码不可为空！");
        }
        if (StringUtils.isBlank(serverCaptcha)) {
            throw new BusinessException("请先获取验证码！");
        }
        if (!clientCaptcha.equalsIgnoreCase(serverCaptcha)) {
            throw new BusinessException("验证码错误");
        }
        //防止暴力破解,验证码验证成功过后仅能使用一次
        getSession().removeAttribute(SessionKey.CAPTCHA);
    }

    /**
     * 发送邮件
     */
    private void sendEmail(String email, String subject, String templateLocation, Map<String, String> model) {
        try {
            File templateFile = ResourceUtils.getFile(templateLocation);
            String template = FileUtils.readFileToString(templateFile, "utf8");
            for (String key : model.keySet()) {
                template = template.replace("${" + key + "}", model.get(key));
            }
            MimeMessage message = mailSender.createMimeMessage();

            try {
                MimeMessageHelper helper = new MimeMessageHelper(message, true);
                helper.setFrom("750502229@qq.com", "【安知系统邮件】");
                helper.setTo(email);
                helper.setSubject(subject);
                helper.setText(template, true);
                mailSender.send(message);
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            throw new BusinessException("邮件发送失败,请检查邮箱配置 或 邮箱地址是否正确");
        }
    }

    /**
     * 记住我
     *
     * @return
     */
    private Cookie rememberMeCookie(SysUser user) {
        Map<String, String> infoMap = new HashMap<>(2);
        infoMap.put("userCount", user.getUserCount());
        infoMap.put("password", user.getPassword());
        String infoJson = JsonUtils.toJson(infoMap);
        Cookie cookie = new Cookie(CookieKey.REMEBER_ME, EncryptUtils.aesEncrypt(infoJson));
        //保存7天
        cookie.setMaxAge(60 * 60 * 24 * 7);
        return cookie;

    }

    /**
     * 从cookie里获取用户信息
     *
     * @return
     */
    private SysUser getUserByCookie() {
        Optional<Cookie> cookie = Arrays.stream(getRequest().getCookies()).filter(c -> CookieKey.REMEBER_ME.equals(c.getName())).findAny();
        if (cookie.isPresent()) {
            return JsonUtils.as(EncryptUtils.aesDecrypt(cookie.get().getValue()), SysUser.class);
        }
        return null;
    }

    /**
     * 移除记住我cookie
     */
    private void invalidRememberMe() {
        Cookie cookie = new Cookie(CookieKey.REMEBER_ME, "");
        cookie.setMaxAge(0);
        getResponse().addCookie(cookie);
    }
    //endregion
}