package com.amarsoft.app.job;

import com.amarsoft.app.dao.CommonMethod;
import com.amarsoft.app.model.MonitorModel;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by ymhe on 2017/1/20.
 * externalv4
 */
public class ExternalV4Job implements ProcessJob {

    public void generateProcess(String batchId, String modelId, String bankId) {

        List<MonitorModel> monitorModelList = new LinkedList<MonitorModel>();
        //获取企业名单
        CommonMethod commonMethod = new CommonMethod();
        monitorModelList = commonMethod.getMonitorEnts(bankId, modelId);

        //TODO:测试功能，给list加入一个指定企业
        MonitorModel monitorModelTest = new MonitorModel();
        monitorModelTest.setEnterpriseName("昆明中金盛捷民间融资登记服务有限公司");
        monitorModelList.add(monitorModelTest);
        //获取关联企业
        commonMethod.getRelaEnts(monitorModelList);

        //企业名单集体插入诉讼、舆情、失信、被执行人
        //doservervice

        //生成spider_inspect_entity，和流程
        //API

    }
}
