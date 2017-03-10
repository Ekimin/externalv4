package com.amarsoft.app.job;

import com.amarsoft.amarmonitor.AmarMonitorAgent;
import com.amarsoft.app.dao.CommonMethod;
import com.amarsoft.app.model.MonitorModel;
import com.amarsoft.are.ARE;
import com.amarsoft.monitorPlugin.sink.ganglia.AbstractGangliaSink;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by ymhe on 2017/1/20.
 * externalv4
 */
public class ExternalV4Job implements ProcessJob {

    public void generateProcess(String azkabanExecId, String modelId, String bankId) {

        List<MonitorModel> monitorModelList = new LinkedList<MonitorModel>();
        ExternalV4Job externalV4Job = new ExternalV4Job();
        //获取企业名单
        CommonMethod commonMethod = new CommonMethod();
        monitorModelList = commonMethod.getMonitorEnts(bankId, modelId);
        boolean isChangedRunning = false;
        boolean isChangedSuccess = false;
        String jobClassName = ExternalV4Job.class.getName();
        int rmiSleepTime = Integer.valueOf(ARE.getProperty("rmiSleepTime", "60"));

        // 调用RMI修改状态位
        while (!isChangedRunning) {
            ARE.getLog().info("修改该job为running");
            isChangedRunning = commonMethod.updateFlowStatusByRMI(azkabanExecId, jobClassName, "running");
            // 如果RMI服务未启动或者出现其他异常时，发邮件通知并休眠
            if (!isChangedRunning) {
                try {
                    ARE.getLog().info("调用RMI服务出错，休眠" + (rmiSleepTime / 1000) + "秒");
                    AmarMonitorAgent agent = new AmarMonitorAgent();
                    agent.emitMetric("Inspect_RMI_Exception", "Inspect_RMI_Data_Process_Task_Update_Exception",
                            "uint32", "1", AbstractGangliaSink.GangliaOp.valueOf("GE"), "1", "2");
                    Thread.sleep(rmiSleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        ARE.getLog().info("job状态为修改结束...");


        //获取关联企业
        ARE.getLog().info("开挖关联企业");
        commonMethod.getRelaEnts(monitorModelList);
        ARE.getLog().info("挖完了......");

        //企业名单集体插入诉讼、舆情、失信、被执行人,初始化流程
        ARE.getLog().info("开始插入企业名单到对应实体表，并初始化flow");
        externalV4Job.insertEntList(monitorModelList);
        ARE.getLog().info("插入企业名单到对应实体表，并初始化flow完成");
        // 调用RMI修改状态位
        while (!isChangedSuccess) {
            ARE.getLog().info("修改该job为success");
            isChangedSuccess = commonMethod.updateFlowStatusByRMI(azkabanExecId, jobClassName, "success");
            // 如果RMI服务未启动或者出现其他异常时，发邮件通知并休眠
            if (!isChangedSuccess) {
                try {
                    ARE.getLog().info("调用RMI服务出错，休眠" + (rmiSleepTime / 1000) + "秒");
                    AmarMonitorAgent agent = new AmarMonitorAgent();
                    agent.emitMetric("Inspect_RMI_Exception", "Inspect_RMI_Data_Process_Task_Update_Exception",
                            "uint32", "1", AbstractGangliaSink.GangliaOp.valueOf("GE"), "1", "2");
                    Thread.sleep(rmiSleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        ARE.getLog().info("job状态为修改结束...");

    }

    /**
     * 将主体企业和关联企业通过doservice插入到对应主体表中
     *
     * @param monitorModelList 企业名单信息List
     */
    public void insertEntList(List<MonitorModel> monitorModelList) {
        CommonMethod commonMethod = new CommonMethod();
        for (MonitorModel monitorModel : monitorModelList) {
            String entName = monitorModel.getEnterpriseName(); //企业名
            String bankId = monitorModel.getBankId(); //机构号
            String orgName = monitorModel.getOrgName(); // 机构名

            String[] relaEnts = monitorModel.getRelaEnts().split(";");
            List<String> allEntList = new ArrayList<String>(Arrays.asList(relaEnts));
            allEntList.add(entName);

            for (String eName : allEntList) {
                if (eName != null && !eName.equals("")) {
                    commonMethod.syncMainEntToLiraOperation(eName, bankId, orgName);
                }
            }
            commonMethod.createPocInspectListDateV4(bankId, relaEnts);
        }
    }


}
