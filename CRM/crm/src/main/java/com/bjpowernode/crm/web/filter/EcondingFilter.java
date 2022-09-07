package com.bjpowernode.crm.web.filter;

import javax.servlet.*;
import java.io.IOException;

public class EcondingFilter implements Filter {
    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        //过滤post请求中文乱码
        req.setCharacterEncoding("UTF-8");
        //过滤响应流中文乱码
        resp.setContentType("text/html; charset=UTF-8");
        //将请求放行
        chain.doFilter(req,resp);
    }
}
