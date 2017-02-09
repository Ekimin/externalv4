package com.amarsoft.app.model;

/**
 * Created by ymhe on 2017/1/19.
 * externalv4
 * 流程模型
 */
public class MonitorModel {
    private String serialNo; //企业流水号
    private String orgName; //机构名
    private String enterpriseName; //企业名字
    private String idNo; //证监号
    private String monitorURL; //监控的URL
    private String stockBlock; //监控区间
    private String inspectLevel; //优先级：1,2,3 级别由高到低
    private String inspectState; //是否监控
    private String datasourceType; //数据源类型：舆情 诉讼 等
    private String modelId; //模型ID
    private String taskStage; //任务阶段
    private String bankId; //机构号
    private String inputTime;
    private int relaLevel; //关联企业深度：1,2,3
    private String relaEnts; //关联企业，";"分割

    public int getRelaLevel() {
        return relaLevel;
    }

    public void setRelaLevel(int relaLevel) {
        this.relaLevel = relaLevel;
    }

    public String getRelaEnts() {
        return relaEnts;
    }

    public void setRelaEnts(String relaEnts) {
        this.relaEnts = relaEnts;
    }

    public String getIdNo() {
        return idNo;
    }

    public void setIdNo(String idNo) {
        this.idNo = idNo;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getEnterpriseName() {
        return enterpriseName;
    }

    public void setEnterpriseName(String enterpriseName) {
        this.enterpriseName = enterpriseName;
    }

    public String getMonitorURL() {
        return monitorURL;
    }

    public void setMonitorURL(String monitorURL) {
        this.monitorURL = monitorURL;
    }

    public String getStockBlock() {
        return stockBlock;
    }

    public void setStockBlock(String stockBlock) {
        this.stockBlock = stockBlock;
    }

    public String getInspectLevel() {
        return inspectLevel;
    }

    public void setInspectLevel(String inspectLevel) {
        this.inspectLevel = inspectLevel;
    }

    public String getInspectState() {
        return inspectState;
    }

    public void setInspectState(String inspectState) {
        this.inspectState = inspectState;
    }

    public String getDatasourceType() {
        return datasourceType;
    }

    public void setDatasourceType(String datasourceType) {
        this.datasourceType = datasourceType;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public String getTaskStage() {
        return taskStage;
    }

    public void setTaskStage(String taskStage) {
        this.taskStage = taskStage;
    }

    public String getBankId() {
        return bankId;
    }

    public void setBankId(String bankId) {
        this.bankId = bankId;
    }

    public String getInputTime() {
        return inputTime;
    }

    public void setInputTime(String inputTime) {
        this.inputTime = inputTime;
    }
}
