package com.bjpowernode.crm.web.filter;

import com.bjpowernode.crm.settings.domain.User;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class LoginFilter implements Filter {
    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        System.out.println("进入到过滤登录操作");
        //ServletRequest是父，HttpServletRequest是子，父转子要强制(相当于省长变市长，降职位)
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        String path = request.getServletPath();
        //不应该被拦截的资源自动放行
        //登录页和验证登录的要放行
        if("/login.jsp".equals(path) || "/settings/user/login.do".equals(path)){

            chain.doFilter(req,resp);

            //其他资源必须验证有没有登陆过
        }else {
            HttpSession session = request.getSession();
            User user = (User) session.getAttribute("user");

            //如果user不为null，说明登陆过
            if (user != null) {
                chain.doFilter(req,resp);
            //没有登陆过
            }else {
                //重定向到登录页
                response.sendRedirect(request.getContextPath()+"/login.jsp");

            }
        }
    }
}
