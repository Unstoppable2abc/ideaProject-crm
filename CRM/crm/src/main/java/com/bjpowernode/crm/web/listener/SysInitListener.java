package com.bjpowernode.crm.web.listener;

import com.bjpowernode.crm.settings.domain.DicValue;
import com.bjpowernode.crm.settings.service.DicService;
import com.bjpowernode.crm.settings.service.impl.DicServiceImpl;
import com.bjpowernode.crm.utils.ServiceFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.*;

public class SysInitListener implements ServletContextListener {

    //该方法是用来监听上下文域对象的方法，当服务器启动，上下文域对象创建完毕后，马上执行该方法
    //event: 该参数能取得监听器对象，监听的什么对象，就可以通过参数取得什么对象。现在是取得上下文域对象
    @Override
    public void contextInitialized(ServletContextEvent event) {
        System.out.println("服务器缓存数据字典");
        //获取监听器对象
        ServletContext application = event.getServletContext();

        //取数据字典,调业务层
        DicService ds = (DicService) ServiceFactory.getService(new DicServiceImpl());

        /*
            管业务层要7个list
            打包成map
            map.put("appellationList",dvList1)
            map.put("clueStateList",dvList2)
            map.put("stageList",dvList3)
            ...
         */
        Map<String, List<DicValue>> map = ds.getAll();

        //将map解析为上下文域对象保存的键值对
        Set<String> set = map.keySet();

        for(String key : set){
            //将数据字典放到application域
            application.setAttribute(key,map.get(key));
        }

        //--------------------------------------------
        //数据字典处理完后，处理Stage2Possibility.properties文件
        /*
            处理Stage2Possibility.properties文件步骤：
                解析该文件，将该属性文件中的键值对关系处理成Java中键值对关系map
                Map<String(阶段stage),String(可能性possibility)> pMap = ...
                pMap.put("01资质审查",10);
                pMap.put("02需求分析",25);
                pMap.put("07...",...);

                pMap保存值之后，放在服务器缓存中
                application.setAttribute("pMap",pMap);
         */
        //解析properties文件
        Map<String,String> pMap = new HashMap<>();

        ResourceBundle rb = ResourceBundle.getBundle("Stage2Possibility"); //这里配置文件涉及到中文转义，用ResourceBundle，平常的用Properties

        Enumeration<String> e  = rb.getKeys();

        while (e.hasMoreElements()){
            //阶段
            String key = e.nextElement();
            //可能性
            String value = rb.getString(key);

            pMap.put(key,value);
        }

        //将pMap放到服务器缓存中
        application.setAttribute("pMap",pMap);

    }
}
