package com.bjpowernode.crm.settings.web.controller;

import com.bjpowernode.crm.settings.domain.User;
import com.bjpowernode.crm.settings.service.UserService;
import com.bjpowernode.crm.settings.service.impl.UserServiceImpl;
import com.bjpowernode.crm.utils.MD5Util;
import com.bjpowernode.crm.utils.PrintJson;
import com.bjpowernode.crm.utils.ServiceFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UserController extends HttpServlet {
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String servletPath = request.getServletPath();

        //这个路径开始的斜杠很容易丢失，注意！！！
        if("/settings/user/login.do".equals(servletPath)){
            login(request,response);
        }else if("".equals(servletPath)){

        }
    }

    private void login(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("进入到验证登录操作");

        //获取用户名和密码
        String loginAct = request.getParameter("loginAct");
        String loginPwd = request.getParameter("loginPwd");

        //将密码的明文形式转化为MD5的形式
        loginPwd = MD5Util.getMD5(loginPwd);
        //接受浏览器传送的ip地址
        String ip = request.getRemoteAddr();
        System.out.println("ip------:" + ip);

        //参数拿到后，到业务层
        //未来业务层开发，统一使用代理形态的接口对象
        UserService us = (UserService) ServiceFactory.getService(new UserServiceImpl());

        try{
            //将参数传过去，返回一个User对象
            User user = us.login(loginAct,loginPwd,ip);
            //放到request的session域中
            request.getSession().setAttribute("user", user);

            //如果执行到此处，说明业务层还没有抛出任何异常
            //返回一个json串，表示登录成功 {"success" : "true"}
            PrintJson.printJsonFlag(response,true);

        }catch (Exception e){
            //一旦程序执行了catch块，说明业务层验证失败，为controller抛出了异常
            //表示登录失败 {"success" : true , "msg" : ?}
            //获取异常信息
            String msg = e.getMessage();

            //使用map为ajax提供多项信息
            Map<String, Object> map = new HashMap<>();
            map.put("success",false);
            map.put("msg",msg);
            PrintJson.printJsonObj(response,map);
        }
    }
}
