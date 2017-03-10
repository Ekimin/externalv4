package com.amarsoft.rmi.requestdata.requestqueue;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by ryang on 2017/1/12.
 */
public interface IDataProcessTaskManage extends Remote {
    /**
     * @param batchid 机构编号
     * @param azkabanexecuteid 模型id
     * @return state
     * @throws RemoteException
     */
    //更新azkabanexecuteid
    public String updateAzKaBanId(String batchid, String azkabanexecuteid)throws RemoteException;
    /**
     * @param azkabanexecuteid 机构编号
     * @param jobclassname job实例类名
     * @param exesTatus 执行状态
     * @return state
     * @throws RemoteException
     */
    //更新任务状态：
    public String updateExeStatus(String azkabanexecuteid, String jobclassname, String exesTatus)throws RemoteException;
}
