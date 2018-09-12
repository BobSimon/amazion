package com.amzics.web.config;

import com.amzics.web.interceptor.AuthenticationInterceptor;
import com.amzics.web.interceptor.TimespanInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.HttpPutFormContentFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@Configuration
public class MvcConfig extends WebMvcConfigurationSupport {
    //region 拦截器
    @Bean
    public AuthenticationInterceptor authenInterceptor() {
        return new AuthenticationInterceptor();
    }
    @Bean
    public TimespanInterceptor timespanInterceptor() {
        return new TimespanInterceptor();
    }
    //endregion

    //region 过滤器
    @Bean
    public HttpPutFormContentFilter httpPutFormContentFilter() {
        return new HttpPutFormContentFilter();
    }
    //endregion

    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        //认证
        registry.addInterceptor(authenInterceptor()).addPathPatterns("/**");
        //统计耗时
        registry.addInterceptor(timespanInterceptor()).addPathPatterns("/**");

        super.addInterceptors(registry);
    }
}
