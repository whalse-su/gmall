package com.atguigu.gmall.order.interceptor;


import com.atguigu.gmall.common.bean.UserInfo;
import com.atguigu.gmall.common.utils.CookieUtils;
import com.atguigu.gmall.common.utils.JwtUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;


@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired

    private static final ThreadLocal<UserInfo> THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 拦截器的前置方法。
     * 此处要获取userInfo，用于向handler中传递
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        UserInfo userInfo = new UserInfo();

        // 获取请求头信息
        String userId = request.getHeader("userId");
        userInfo.setUserId(Long.valueOf(userId));

        // 将userInfo放入THREAD_LOCAL中，以便能够传递给后续服务
        THREAD_LOCAL.set(userInfo);
        return true;
    }

    /**
     * 不能向后提供THREAD_LOCAL，否则会被修改；所以这里提供载荷的getter
     * @return
     */
    public static UserInfo getUserInfo() {
        return THREAD_LOCAL.get();
    }

    /**
     * 拦截器的完成方法。
     * 由于使用了Tomcat线程池，所以需要显式地删除ThreadLocal，否则会导致内存泄漏
     * afterCompletion是在DispatcherServlet向前端返回视图的时候起作用。
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        THREAD_LOCAL.remove();
    }
}
