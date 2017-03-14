package com.amarsoft.rmi.requestdata;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;


/**
 * @author jwang
 *
 */

public interface CreatePocInspectListDate extends Remote{

    /**
     * 此方法调用分两种场景：1.v2,v3报告名单入一级监控表及初始化flow 2.v4报告转换成各种数据类型的名单入一级监控表及初始化flow
     * @para orgname:虚拟机构名称（poc+当前时间毫秒数）
     * @para entnameList：主体企业+关联企业
     * @para datatypeList：请求的数据类型（舆情，诉讼，被执行人，失信被执行人）
     * @return success/failure
     */
    public String InsertPocInspectList(String orgname,List <String> entnameList,List<String> datatypeList) throws RemoteException;

    /**
     * 此方法针对V4报告生成主体企业监控名单及初始化V4报告处理流程
     * @para orgname:虚拟机构名称（poc+当前时间毫秒数）
     * @para entname：主体企业
     * @return success/failure
     */
    public String InsertPocInspectListOfV4(String orgname,List <String> entnameList) throws RemoteException;
}
