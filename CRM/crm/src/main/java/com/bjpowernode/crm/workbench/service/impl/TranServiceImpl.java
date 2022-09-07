package com.bjpowernode.crm.workbench.service.impl;

import com.bjpowernode.crm.utils.DateTimeUtil;
import com.bjpowernode.crm.utils.SqlSessionUtil;
import com.bjpowernode.crm.utils.UUIDUtil;
import com.bjpowernode.crm.workbench.dao.CustomerDao;
import com.bjpowernode.crm.workbench.dao.TranDao;
import com.bjpowernode.crm.workbench.dao.TranHistoryDao;
import com.bjpowernode.crm.workbench.domain.Customer;
import com.bjpowernode.crm.workbench.domain.Tran;
import com.bjpowernode.crm.workbench.domain.TranHistory;
import com.bjpowernode.crm.workbench.service.TranService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TranServiceImpl implements TranService {

    private CustomerDao customerDao = SqlSessionUtil.getSqlSession().getMapper(CustomerDao.class);
    private TranDao tranDao = SqlSessionUtil.getSqlSession().getMapper(TranDao.class);
    private TranHistoryDao tranHistoryDao = SqlSessionUtil.getSqlSession().getMapper(TranHistoryDao.class);

    /*
        添加交易业务：
            在做添加之前，t里面少了客户的主键：customerId
            先处理客户相关的需求：（1）判断customerName,根据客户名去客户表精确查询
                                  如果有这个客户，则取出这个客户的id，添加到t对象里
                                  如果没有这个客户，则在客户表新建一个客户，再将客户id取出封装到t对象里
                             （2）经过以上操作后，t对象的信息就全了，新建交易信息
                             （3）添加交易完成后，需要新建一条交易历史
     */
    @Override
    public boolean save(Tran t, String customerName) {

        boolean flag = true;

        Customer cus = customerDao.getCustomerByName(customerName);

        if (cus == null) {
            cus = new Customer();
            cus.setId(UUIDUtil.getUUID());
            cus.setName(customerName);
            cus.setDescription(t.getDescription());
            cus.setCreateTime(DateTimeUtil.getSysTime());
            cus.setCreateBy(t.getCreateBy());
            cus.setOwner(t.getOwner());
            cus.setNextContactTime(t.getNextContactTime());
            //添加客户
            int count1 = customerDao.save(cus);
            if (count1 != 1) {
                flag = false;
            }
        }

        //经过以上操作，不论客户是否存在，我们都已经拥有了客户，可以将客户id封装到t对象中
        t.setCustomerId(cus.getId());

        //添加交易
        int count2 = tranDao.save(t);
        if (count2 != 1) {
            flag = false;
        }

        //添加交易历史
        TranHistory th = new TranHistory();
        th.setId(UUIDUtil.getUUID());
        th.setTranId(t.getId());
        th.setStage(t.getStage());
        th.setExpectedDate(t.getExpectedDate());
        th.setMoney(t.getMoney());
        th.setCreateTime(t.getCreateTime());
        th.setCreateBy(t.getCreateBy());

        int count3 = tranHistoryDao.save(th);
        if (count3 != 1) {
            flag = false;
        }

        return flag;
    }

    @Override
    public Tran detail(String id) {
        Tran t = tranDao.detail(id);
        return t;
    }

    @Override
    public List<TranHistory> getHistoryByTranId(String tranId) {
        List<TranHistory> thList = tranHistoryDao.getHistoryByTranId(tranId);

        return thList;
    }

    @Override
    public boolean changeStage(Tran t) {
        boolean flag = true;

        //改变交易阶段
        int count1 = tranDao.changeStage(t);
        if (count1 != 1) {
            flag = false;
        }

        //交易阶段改变后，生成一条交易历史
        TranHistory th = new TranHistory();
        th.setId(UUIDUtil.getUUID());
        th.setStage(t.getStage());
        th.setMoney(t.getMoney());
        th.setCreateTime(t.getCreateTime());
        th.setCreateBy(t.getCreateBy());
        th.setExpectedDate(t.getExpectedDate());

        int count2 = tranHistoryDao.save(th);
        if (count2 != 1) {
            flag = false;
        }

        return flag;
    }

    @Override
    public Map<String, Object> getCharts() {
        //获得total
        int total = tranDao.getTotal();
        //获得dataList
        List<Map<String,Object>> dataList = tranDao.getCharts();
        //map打包
        Map<String,Object> map = new HashMap<>();
        map.put("total",total);
        map.put("dataList",dataList);
        //返回map
        return map;
    }
}
