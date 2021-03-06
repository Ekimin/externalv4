package com.amarsoft.app.dao;

import com.amarsoft.app.model.MonitorModel;
import com.amarsoft.are.ARE;
import com.amarsoft.rmi.requestdata.requestqueue.IDataProcessTaskManage;
import com.amarsoft.rmi.requestdata.CreatePocInspectListDate;
import net.sf.json.JSONObject;
import org.codehaus.xfire.client.Client;
import org.codehaus.xfire.transport.http.CommonsHttpMessageSender;


import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.Naming;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by ymhe on 2017/1/19.
 * externalv4
 */
public class CommonMethod {

    private String registryHost;
    private String registryPort;

    public CommonMethod() {
        registryHost = ARE.getProperty("registryHost", "192.168.61.81");
        registryPort = ARE.getProperty("registryPort", "1098");
    }

    /**
     * 读取一级监控表，获得企业名单和对应的url
     *
     * @param bankID
     * @return:监控表列表
     */
    public List<MonitorModel> getMonitorEnts(String bankID, String modelId, String batchId) {
        List<MonitorModel> entMonitorUrl = new ArrayList<MonitorModel>();

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String selectSql = "select serinalno,orgname,enterprisename,idno,monitorurl,stockblock,inspectlevel,inspectstate,inputtime, bankid from spider_inspect_entity where bankID = ? and  inspectstate = 'Y' and modelId = ? and batchId=?";

        ARE.getLog().info("开始获取企业名单，bankID=" + bankID + "   modelId=" + modelId);
        try {
            conn = ARE.getDBConnection("crsbjt");
            ps = conn.prepareStatement(selectSql);
            ps.setString(1, bankID);
            ps.setString(2, modelId);
            ps.setString(3, batchId);
            ARE.getLog().info("ps = " + ps.toString());
            rs = ps.executeQuery();
            int count = 0;
            while (rs.next()) {
                MonitorModel monitorModel = new MonitorModel();
                monitorModel.setSerialNo(rs.getString("serinalno"));
                monitorModel.setOrgName(rs.getString("orgname"));
                monitorModel.setEnterpriseName(rs.getString("enterprisename"));
                monitorModel.setIdNo(rs.getString("idno")); //证件号
                monitorModel.setMonitorURL(rs.getString("monitorurl"));
                monitorModel.setStockBlock(rs.getString("stockblock"));
                monitorModel.setInspectLevel(rs.getString("inspectlevel"));
                monitorModel.setInspectState(rs.getString("inspectstate"));
                monitorModel.setInputTime(rs.getString("inputtime"));
                monitorModel.setBankId(rs.getString("bankid"));
                count++;
                entMonitorUrl.add(monitorModel);
            }
            ARE.getLog().info("读取到企业量：" + count);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return entMonitorUrl;
    }

    /**
     * 获取关联企业和外部企业数据
     *
     * @param monitorModels *
     * @return
     */
    public void getRelaEnts(List<MonitorModel> monitorModels) {
        String relaEnts = null; //关联企业
        Connection conn = null;
        ResultSet rs1 = null;
        ResultSet rs2 = null;
        PreparedStatement ps1 = null;
        PreparedStatement ps2 = null;
        Map<String, String> relaMap = new HashMap<String, String>();

        String sql1 = "select LEVEL1RELAENTNAME, LEVEL2RELAENTNAME from cr_relation_info_mlevel where ENTNAME=?";
        String sql2 = "select distinct GuaranteeCompanyName from ICP_Guarantee_Company where entname =?";

        try {
            conn = ARE.getDBConnection("crsbjt");
            ps1 = conn.prepareStatement(sql1);
            ps2 = conn.prepareStatement(sql2);
            for (MonitorModel monitorModel : monitorModels) {
                String entName = monitorModel.getEnterpriseName();
                ps1.setString(1, entName);
                ps2.setString(1, entName);
                rs1 = ps1.executeQuery();
                rs2 = ps2.executeQuery();

                while (rs1.next()) {
                    String level1 = rs1.getString(1);
                    String level2 = rs1.getString(2);

                    relaMap.put(level1, entName);
                    relaMap.put(level2, entName);
                }
                while (rs2.next()){
                    String outEnt = rs2.getString(1);
                    relaMap.put(outEnt, entName);
                }
                //处理成;分割字符串
                int count = 0;
                for (Map.Entry<String, String> entry : relaMap.entrySet()) {
                    if (count == 0) {
                        relaEnts = entry.getKey();
                        count++;
                    } else {
                        relaEnts += ";" + entry.getKey();
                    }
                }
                monitorModel.setRelaEnts(relaEnts);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            try{
                if(rs2!=null){
                    rs2.close();
                }
                if(rs1!=null){
                    rs1.close();
                }
                if(ps2!=null){
                    ps2.close();
                }
                if(ps1!=null){
                    ps1.close();
                }
                if(conn!=null){
                    conn.close();
                }
            }catch(SQLException e){
                ARE.getLog().error("获取关联企业出错", e);
            }
        }
    }


    /**
     * 调DOServer, 推企业名单到舆情dt_object_info表
     *
     * @param entName
     * @param bankId
     * @param orgName
     * @return
     */
    public boolean syncMainEntToPiraOperation(String entName, String bankId, String orgName) {

        try {
            JSONObject jsonObject0 = new JSONObject();// 必要参数Platform、Data
            jsonObject0.put("Platform", "operation");// 来源平台,dataservice、crservice、operation
            JSONObject dataJsonObejct = new JSONObject(); // Data中必要参数EntName、SenderId、CustomName
            dataJsonObejct.put("EntName", entName);// 企业名称
            dataJsonObejct.put("BeginDate", null);// 请求数据开始日期
            dataJsonObejct.put("EndDate", null);// 请求数据结束日期
            // dataJsonObejct.put("OrgCode", "111222444");//企业组织机构代码
            // dataJsonObejct.put("EntType", "E");//企业类型，E-单一企业，G-集团企业
            dataJsonObejct.put("PiorityLevel", "030");// 运营优先级,010-低,020-中,030-高
            dataJsonObejct.put("OperType", "040");// 数据运营类型,030-相关性运营,040-结构化运营
            dataJsonObejct.put("SenderId", bankId);// 请求机构senderId
            dataJsonObejct.put("CustomName", orgName);// 请求机构中文名称

            jsonObject0.put("Data", dataJsonObejct.toString());

            String paramsJson = JSONObject.fromObject(jsonObject0).toString();

            ARE.getLog().info("请求信息:" + paramsJson);

            Client client = new Client(new URL(ARE.getProperty("DoserivesUrl") + "/service/PiraMonitorListService?wsdl"));
            client.setProperty(CommonsHttpMessageSender.HTTP_TIMEOUT, String.valueOf(20000));// 设置发送的超时限制,单位是毫秒;
            client.setProperty(CommonsHttpMessageSender.DISABLE_KEEP_ALIVE, "true");
            client.setProperty(CommonsHttpMessageSender.DISABLE_EXPECT_CONTINUE, "true");

            Object[] results = client.invoke("addToList", new Object[]{paramsJson});
            ARE.getLog().debug("客户端结果：" + results[0]);

        } catch (MalformedURLException e) {
            ARE.getLog().error("推送信息至舆情监控名单DT_OBJECT_INFO出错,企业名称：" + entName, e);
            return false;
        } catch (Exception e) {
            ARE.getLog().error("推送信息至舆情监控名单DT_OBJECT_INFO出错,企业名称：" + entName, e);
            return false;
        }
        return true;
    }


    /**
     * 调DOService,插入企业名单信息到诉讼主体表（law_ent_info）
     *
     * @param entName 企业名
     * @param bankId  机构编号
     * @param orgName 机构名字
     * @return
     */
    public boolean syncMainEntToLiraOperation(String entName, String bankId, String orgName) {

        boolean flag = false;
        try {
            JSONObject jsonObject0 = new JSONObject();// 必要参数Platform、Data
            jsonObject0.put("Platform", "operation");// 来源平台,dataservice、crservice、operation

            JSONObject dataJsonObejct = new JSONObject(); // Data中必要参数EntName、SenderId、CustomName
            dataJsonObejct.put("EntName", entName);// 企业名称
            dataJsonObejct.put("BeginDate", null);// 请求数据开始日期
            dataJsonObejct.put("EndDate", null);// 请求数据结束日期
            // dataJsonObejct.put("OrgCode", "111222444");//企业组织机构代码
            // dataJsonObejct.put("EntType", "E");//企业类型，E-单一企业，G-集团企业
            dataJsonObejct.put("PiorityLevel", "010");// 运营优先级,010-低,020-中,030-高
            dataJsonObejct.put("OperType", "040");// 数据运营类型,030-相关性运营,040-结构化运营
            dataJsonObejct.put("SenderId", bankId);// 请求机构bankId
            dataJsonObejct.put("CustomName", orgName);// 请求机构中文名称

            jsonObject0.put("Data", dataJsonObejct.toString());

            String paramsJson = JSONObject.fromObject(jsonObject0).toString();

            ARE.getLog().info("请求信息:" + paramsJson);

            Client client = new Client(new URL(ARE.getProperty("DoserivesUrl") + "/service/LiraMonitorListService?wsdl"));
            client.setProperty(CommonsHttpMessageSender.HTTP_TIMEOUT, String.valueOf(20000));// 设置发送的超时限制,单位是毫秒;
            client.setProperty(CommonsHttpMessageSender.DISABLE_KEEP_ALIVE, "true");
            client.setProperty(CommonsHttpMessageSender.DISABLE_EXPECT_CONTINUE, "true");

            Object[] results = client.invoke("addToList", new Object[]{paramsJson});
            ARE.getLog().debug("客户端结果：" + results[0]);
            System.out.println("实现类：" + results[0].getClass().getName());
            flag = true;
        } catch (MalformedURLException e) {
            flag = false;
            e.printStackTrace();
        } catch (Exception e) {
            flag = false;
            e.printStackTrace();
        } finally {
            return flag;
        }
    }

    /**
     * 通过接口，由企业名单生成spider_inspect_entity,并初始化data_process_task流程.
     * <li>默认4种数据源都要</li>
     *
     * @param orgname
     * @param entnameList
     * @return
     */
    public boolean createPocInspectListDate(String orgname, List<String> entnameList, List<String> datasourceList) {
        boolean flag = false;
        ARE.getLog().info("======================远程API方法调用开始===================");
        try {
            CreatePocInspectListDate createPocInspectListDate = (CreatePocInspectListDate)
                    Naming.lookup("rmi://" + registryHost + ":" + registryPort + "/pocInspectList");

            String returnResult = createPocInspectListDate.InsertPocInspectList(orgname, entnameList, datasourceList);
            ARE.getLog().info("returnResult=" + returnResult);
            flag = true;
        } catch (Exception e) {
            ARE.getLog().error("远程RMI出错", e);
            flag = false;
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * @param batchId
     * @param jobClassName
     * @param status
     * @return
     */
    public boolean updateFlowStatusByRMI(String batchId, String jobClassName, String status) {
        boolean flag = false;
        ARE.getLog().info("======================远程API方法调用开始===================");
        try {
            IDataProcessTaskManage flowManage = (IDataProcessTaskManage)
                    Naming.lookup("rmi://" + registryHost + ":" + registryPort + "/flowManage");
            flowManage.updateExeStatus(batchId, jobClassName, status);
            flag = true;
        } catch (Exception e) {
            ARE.getLog().error("远程RMI出错", e);
            flag = false;
            e.printStackTrace();
        }
        return flag;
    }
}
