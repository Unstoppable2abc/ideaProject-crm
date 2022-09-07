package com.bjpowernode.crm.workbench.web.controller;

import com.bjpowernode.crm.settings.domain.User;
import com.bjpowernode.crm.settings.service.UserService;
import com.bjpowernode.crm.settings.service.impl.UserServiceImpl;
import com.bjpowernode.crm.utils.DateTimeUtil;
import com.bjpowernode.crm.utils.PrintJson;
import com.bjpowernode.crm.utils.ServiceFactory;
import com.bjpowernode.crm.utils.UUIDUtil;
import com.bjpowernode.crm.vo.PaginationVO;
import com.bjpowernode.crm.workbench.dao.ClueDao;
import com.bjpowernode.crm.workbench.domain.*;
import com.bjpowernode.crm.workbench.service.ActivityService;
import com.bjpowernode.crm.workbench.service.ClueService;
import com.bjpowernode.crm.workbench.service.impl.ActivityServiceImpl;
import com.bjpowernode.crm.workbench.service.impl.ClueServiceImp;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClueController extends HttpServlet {
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("欢迎进入线索控制器");

        String servletPath = request.getServletPath();

        //这个路径开始的斜杠很容易丢失，注意！！！
        if ("/workbench/clue/getUserList.do".equals(servletPath)) {
            getUserList(request, response);
        }else if ("/workbench/clue/save.do".equals(servletPath)) {
            save(request, response);
        }else if ("/workbench/clue/detail.do".equals(servletPath)) {
            detail(request, response);
        }else if ("/workbench/clue/getActivityListByClueId.do".equals(servletPath)) {
            getActivityListByClueId(request, response);
        }else if ("/workbench/clue/unbund.do".equals(servletPath)) {
            unbund(request, response);
        }else if ("/workbench/clue/getActListByNameAndNotByClueId.do".equals(servletPath)) {
            getActListByNameAndNotByClueId(request, response);
        }else if ("/workbench/clue/bund.do".equals(servletPath)) {
            bund(request, response);
        }else if ("/workbench/clue/getActListByName.do".equals(servletPath)) {
            getActListByName(request, response);
        }else if ("/workbench/clue/convert.do".equals(servletPath)) {
            convert(request, response);
        }
    }

    private void convert(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("执行线索转换操作");

        String clueId = request.getParameter("clueId");
        //接收是否创建交易的标志
        String flag = request.getParameter("flag");

        String createBy = ((User)request.getSession().getAttribute("user")).getName();

        Tran t = null;

        if("a".equals(flag)){
            //创建了交易
            t = new Tran();
            //接受交易填写的信息
            String money = request.getParameter("money");
            String name = request.getParameter("name");
            String expectedDate = request.getParameter("expectedDate");
            String stage = request.getParameter("stage");
            String activityId = request.getParameter("activityId");
            String id = UUIDUtil.getUUID();
            String createTime = DateTimeUtil.getSysTime();

            t.setId(id);
            t.setMoney(money);
            t.setName(name);
            t.setExpectedDate(expectedDate);
            t.setActivityId(activityId);
            t.setStage(stage);
            t.setCreateTime(createTime);
        }

        ClueService cs = (ClueService) ServiceFactory.getService(new ClueServiceImp());
        /*
            需要为业务层传递的参数必须有clueId,有clueId才知道转换哪条线索
            还需要传递t，因为可能临时创建了交易
         */
        boolean flag1 = cs.convert(clueId,t,createBy);

        //传统请求，使用转发或重定向跳转页面
        if (flag1) {
            response.sendRedirect(request.getContextPath()+"/workbench/clue/index.jsp");
        }
    }

    private void getActListByName(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("执行线索转换市场活动搜索操作");

        String aname = request.getParameter("aname");

        ActivityService as = (ActivityService) ServiceFactory.getService(new ActivityServiceImpl());
        List<Activity> aList = as.getActListByName(aname);
        PrintJson.printJsonObj(response,aList);
    }

    private void bund(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("执行关联市场活动操作");

        String cid = request.getParameter("cid");
        String[] aids = request.getParameterValues("aid");

        ClueService cs = (ClueService) ServiceFactory.getService(new ClueServiceImp());
        boolean flag = cs.bund(cid,aids);

        PrintJson.printJsonFlag(response,flag);
    }

    private void getActListByNameAndNotByClueId(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("关联列表根据市场活动名称模糊查询+排除已关联的市场活动");

        String aname = request.getParameter("aname");
        String clueId = request.getParameter("clueId");

        Map<String,String> map = new HashMap<>();
        map.put("aname",aname);
        map.put("clueId",clueId);

        ActivityService as = (ActivityService) ServiceFactory.getService(new ActivityServiceImpl());
        List<Activity> aList = as.getActListByNameAndNotByClueId(map);
        PrintJson.printJsonObj(response,aList);
    }

    private void unbund(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("解除关联操作");

        String id = request.getParameter("id");

        ClueService cs = (ClueService) ServiceFactory.getService(new ClueServiceImp());
        boolean flag = cs.unbund(id);

        PrintJson.printJsonFlag(response,flag);
    }

    private void getActivityListByClueId(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("获取关联市场活动列表");

        String clueId = request.getParameter("clueId");

        ActivityService as = (ActivityService) ServiceFactory.getService(new ActivityServiceImpl());
        List<Activity> aList = as.getActivityListByClueId(clueId);

        PrintJson.printJsonObj(response,aList);
    }

    private void detail(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        System.out.println("线索详细页");

        String id = request.getParameter("id");

        ClueService cs = (ClueService) ServiceFactory.getService(new ClueServiceImp());

        Clue c = cs.detail(id);

        request.setAttribute("c",c);
        request.getRequestDispatcher("/workbench/clue/detail.jsp").forward(request,response);
    }

    private void save(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("执行线索添加操作");

        String id = UUIDUtil.getUUID();
        String fullname = request.getParameter("fullname");
        String appellation = request.getParameter("appellation");
        String owner = request.getParameter("owner");
        String company = request.getParameter("company");
        String job = request.getParameter("job");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String website = request.getParameter("website");
        String mphone = request.getParameter("mphone");
        String state = request.getParameter("state");
        String source = request.getParameter("source");
        String createTime = DateTimeUtil.getSysTime();
        String createBy = ((User)request.getSession().getAttribute("user")).getName();
        String description = request.getParameter("description");
        String contactSummary = request.getParameter("contactSummary");
        String nextContactTime = request.getParameter("nextContactTime");
        String address = request.getParameter("address");

        Clue c = new Clue();
        c.setId(id);
        c.setFullname(fullname);
        c.setAppellation(appellation);
        c.setOwner(owner);
        c.setCompany(company);
        c.setJob(job);
        c.setEmail(email);
        c.setPhone(phone);
        c.setWebsite(website);
        c.setMphone(mphone);
        c.setState(state);
        c.setSource(source);
        c.setCreateTime(createTime);
        c.setCreateBy(createBy);
        c.setDescription(description);
        c.setContactSummary(contactSummary);
        c.setNextContactTime(nextContactTime);
        c.setAddress(address);

        ClueService cs = (ClueService) ServiceFactory.getService(new ClueServiceImp());
        boolean flag = cs.save(c);
        PrintJson.printJsonFlag(response,flag);

    }

    private void getUserList(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("获取用户列表");

        UserService cs = (UserService) ServiceFactory.getService(new UserServiceImpl());

        List<User> uList = cs.getUserList();
        PrintJson.printJsonObj(response,uList);
    }
}
