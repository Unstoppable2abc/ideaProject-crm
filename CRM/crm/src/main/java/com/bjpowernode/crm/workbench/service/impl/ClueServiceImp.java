package com.bjpowernode.crm.workbench.service.impl;

import com.bjpowernode.crm.utils.DateTimeUtil;
import com.bjpowernode.crm.utils.SqlSessionUtil;
import com.bjpowernode.crm.utils.UUIDUtil;
import com.bjpowernode.crm.workbench.dao.*;
import com.bjpowernode.crm.workbench.domain.*;
import com.bjpowernode.crm.workbench.service.ClueService;

import java.util.List;

public class ClueServiceImp implements ClueService {
    //线索相关表
    private ClueDao clueDao = SqlSessionUtil.getSqlSession().getMapper(ClueDao.class);
    private ClueActivityRelationDao clueActivityRelationDao = SqlSessionUtil.getSqlSession().getMapper(ClueActivityRelationDao.class);
    private ClueRemarkDao clueRemarkDao = SqlSessionUtil.getSqlSession().getMapper(ClueRemarkDao.class);

    //客户相关表
    private CustomerDao customerDao = SqlSessionUtil.getSqlSession().getMapper(CustomerDao.class);
    private CustomerRemarkDao customerRemarkDao = SqlSessionUtil.getSqlSession().getMapper(CustomerRemarkDao.class);

    //联系人相关表
    private ContactsDao contactsDao = SqlSessionUtil.getSqlSession().getMapper(ContactsDao.class);
    private ContactsRemarkDao contactsRemarkDao = SqlSessionUtil.getSqlSession().getMapper(ContactsRemarkDao.class);
    private ContactsActivityRelationDao contactsActivityRelationDao = SqlSessionUtil.getSqlSession().getMapper(ContactsActivityRelationDao.class);

    //交易相关表
    private TranDao tranDao = SqlSessionUtil.getSqlSession().getMapper(TranDao.class);
    private TranHistoryDao tranHistoryDao = SqlSessionUtil.getSqlSession().getMapper(TranHistoryDao.class);

    @Override
    public boolean save(Clue c) {
        boolean flag = true;

        int count = clueDao.save(c);

        if (count != 1) {
            flag = false;
        }
        return flag;
    }

    @Override
    public Clue detail(String id) {
        Clue c = clueDao.detail(id);

        return c;
    }

    @Override
    public boolean unbund(String id) {
        boolean flag = true;

        int count = clueActivityRelationDao.unbund(id);
        if (count != 1) {
            flag = false;
        }
        return flag;
    }

    @Override
    public boolean bund(String cid, String[] aids) {
        boolean flag = true;

        for(String aid : aids){
            //取得每一个aid和cid做关联
            ClueActivityRelation car = new ClueActivityRelation();
            car.setId(UUIDUtil.getUUID());
            car.setClueId(cid);
            car.setActivityId(aid);

            int count = clueActivityRelationDao.bund(car);
            if (count != 1) {
                flag = false;
            }
        }

        return flag;
    }

    @Override
    public boolean convert(String clueId, Tran t, String createBy) {
        String createTime = DateTimeUtil.getSysTime();

        boolean flag = true;
        // (1) 获取到线索id，通过线索id获取线索对象（线索对象当中封装了线索的信息）
        Clue c = clueDao.getById(clueId);

        // (2) 通过线索对象提取客户信息，当该客户不存在的时候，新建客户（根据公司的名称精确匹配，判断该客户是否存在！）
        //先查询表，看看客户是否存在
        String company = c.getCompany();
        Customer customer = customerDao.getCustomerByName(company);
        //如果customer为null，说明没有这个客户，需要新建一个（new一个对象，封装）
        if(customer == null){
            customer = new Customer();

            customer.setId(UUIDUtil.getUUID());
            customer.setAddress(c.getAddress());
            customer.setWebsite(c.getWebsite());
            customer.setPhone(c.getPhone());
            customer.setOwner(c.getOwner());
            customer.setNextContactTime(c.getNextContactTime());
            customer.setName(c.getFullname());
            customer.setDescription(c.getDescription());
            customer.setContactSummary(c.getContactSummary());
            customer.setCreateTime(createTime);
            customer.setCreateBy(createBy);
            //添加客户
            int count1 = customerDao.save(customer);
            if (count1 != 1) {
                flag = false;
            }
        }

        /*
            经过第二步处理后，客户的信息我们已经有了，将来在处理其他表的时候，如果要使用到客户的id，直接使用customer.getId();
         */
        // (3) 通过线索对象提取联系人信息，保存联系人
        Contacts contacts = new Contacts();

        contacts.setId(UUIDUtil.getUUID());
        contacts.setSource(c.getSource());
        contacts.setOwner(c.getOwner());
        contacts.setNextContactTime(c.getNextContactTime());
        contacts.setMphone(c.getMphone());
        contacts.setJob(c.getJob());
        contacts.setFullname(c.getFullname());
        contacts.setEmail(c.getEmail());
        contacts.setDescription(c.getDescription());
        contacts.setCustomerId(customer.getId());   //客户的Id在上一步已经拥有了，直接用
        contacts.setCreateTime(createTime);
        contacts.setCreateBy(createBy);
        contacts.setAppellation(c.getAppellation());
        contacts.setAddress(c.getAddress());
        contacts.setContactSummary(c.getContactSummary());
        //添加联系人
        int count2 = contactsDao.save(contacts);
        if (count2 != 1) {
            flag = false;
        }

        // (4) 线索备注转换到客户备注以及联系人备注
        //查询与该线索关联的备注信息列表
        List<ClueRemark> clueRemarkList = clueRemarkDao.getListClueById(clueId);
        //取出每一条线索的备注
        for(ClueRemark clueRemark : clueRemarkList){
            //取出备注信息（转换到客户备注和联系人备注的主要的是这个备注信息）
            String noteContent = clueRemark.getNoteContent();

            //创建客户备注对象，添加客户备注
            CustomerRemark customerRemark = new CustomerRemark();
            customerRemark.setId(UUIDUtil.getUUID());
            customerRemark.setNoteContent(noteContent);
            customerRemark.setCreateTime(createTime);
            customerRemark.setCreateBy(createBy);
            customerRemark.setEditFlag("0");
            customerRemark.setCustomerId(customer.getId());
            int count3 = customerRemarkDao.save(customerRemark);
            if (count3 != 1) {
                flag = false;
            }

            //创建联系人备注对象，添加联系人备注
            ContactsRemark contactsRemark = new ContactsRemark();
            contactsRemark.setId(UUIDUtil.getUUID());
            contactsRemark.setContactsId(contacts.getId());
            contactsRemark.setNoteContent(noteContent);
            contactsRemark.setEditFlag("0");
            contactsRemark.setCreateTime(createTime);
            contactsRemark.setCreateBy(createBy);
            int count4 = contactsRemarkDao.save(contactsRemark);
            if(count4 != 1){
                flag = false;
            }
        }

        // (5) “线索和市场活动”的关系转换到“联系人和市场活动”的关系
        //先查询出“线索和市场活动”关系表,取关联市场活动id（activityId）
        List<ClueActivityRelation> clueActivityRelationList = clueActivityRelationDao.getListByClueId(clueId);

        for(ClueActivityRelation clueActivityRelation : clueActivityRelationList){
            String activityId = clueActivityRelation.getActivityId();

            ContactsActivityRelation contactsActivityRelation = new ContactsActivityRelation();
            contactsActivityRelation.setId(UUIDUtil.getUUID());
            contactsActivityRelation.setActivityId(activityId);
            contactsActivityRelation.setContactsId(contacts.getId());
            int count5 = contactsActivityRelationDao.save(contactsActivityRelation);
            if(count5 != 1){
                flag = false;
            }
        }

        // (6) 如果有创建交易需求，创建一条交易
        if (t != null) {
            //对象在controller已经封装好了： id,money,name,expectedDate,stage,activityId,createBy,createTime
            //接下来可以通过第一步生成的c对象，取出一些信息，继续完善对t对象的封装
            t.setSource(c.getSource());
            t.setOwner(c.getOwner());
            t.setNextContactTime(c.getNextContactTime());
            t.setDescription(c.getDescription());
            t.setCustomerId(customer.getId());
            t.setContactSummary(c.getContactSummary());
            t.setContactsId(contacts.getId());

            //添加交易
            int count6 = tranDao.save(t);
            if(count6 != 1){
                flag = false;
            }

            // (7) 如果创建了交易，则创建一条该交易下的交易历史
            TranHistory tranHistory = new TranHistory();
            tranHistory.setId(UUIDUtil.getUUID());
            tranHistory.setCreateBy(createBy);
            tranHistory.setCreateTime(createTime);
            tranHistory.setMoney(t.getMoney());
            tranHistory.setExpectedDate(t.getExpectedDate());
            tranHistory.setStage(t.getStage());
            tranHistory.setTranId(t.getId());
            int count7 = tranHistoryDao.save(tranHistory);
            if(count7 != 1){
                flag = false;
            }

        }


        // (8) 删除线索备注
        for(ClueRemark clueRemark : clueRemarkList){
            int count8 = clueRemarkDao.delete(clueRemark);
            if(count8 != 1){
                flag = false;
            }
        }

        // (9) 删除线索和市场活动的关系
        for(ClueActivityRelation clueActivityRelation : clueActivityRelationList){
            int count9 = clueActivityRelationDao.delete(clueActivityRelation);
            if(count9 != 1){
                flag = false;
            }
        }

        // (10) 删除线索
        int count10 = clueDao.delete(clueId);
        if(count10 != 1){
            flag = false;
        }

        return flag;
    }

}

