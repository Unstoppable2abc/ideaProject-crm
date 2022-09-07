package com.bjpowernode.crm.workbench.dao;

import com.bjpowernode.crm.workbench.domain.ActivityRemark;

import java.util.List;

public interface ActivityRemarkDao {
    int getCountByAids(String[] ids);

    int deleteByAids(String[] ids);

    List<ActivityRemark> getRemarkListByAId(String id);

    int removeRemarkById(String id);

    int saveRemark(ActivityRemark ar);

    int updataRemark(ActivityRemark ar);
}
