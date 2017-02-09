package com.amarsoft.app.dao;

import com.amarsoft.app.model.MonitorModel;
import com.amarsoft.are.ARE;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ymhe on 2017/1/19.
 * externalv4
 */
public class CommonMethod {


    /**
     * 读取一级监控表，获得企业名单和对应的url
     *
     * @param bankID
     * @return:监控表列表
     */
    public List<MonitorModel> getMonitorEnts(String bankID, String modelId) {
        List<MonitorModel> entMonitorUrl = new ArrayList<MonitorModel>();

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String selectSql = "select serinalno,enterprisename,idno,monitorurl,stockblock,inspectlevel,inspectstate,inputtime from spider_inspect_entity where bankID = ? and  inspectstate = 'Y' and modelId = ?";

        try {
            conn = ARE.getDBConnection("78_crsbjt");
            ps = conn.prepareStatement(selectSql);
            ps.setString(1, bankID);
            ps.setString(2, modelId);
            rs = ps.executeQuery();
            while (rs.next()) {
                MonitorModel MonitorModel = new MonitorModel();
                MonitorModel.setSerialNo(rs.getString("serinalno"));
                MonitorModel.setEnterpriseName(rs.getString("enterprisename"));
                MonitorModel.setIdNo(rs.getString("idno"));
                MonitorModel.setMonitorURL(rs.getString("monitorurl"));
                MonitorModel.setStockBlock(rs.getString("stockblock"));
                MonitorModel.setInspectLevel(rs.getString("inspectlevel"));
                MonitorModel.setInspectState(rs.getString("inspectstate"));
                MonitorModel.setInputTime(rs.getString("inputtime"));
                entMonitorUrl.add(MonitorModel);
            }
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

    /** 获取关联企业
     * @param monitorModels *
     * @return
     */
    public void getRelaEnts(List<MonitorModel> monitorModels) {
        String relaEnts = null; //关联企业
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        Map<String, String> relaMap = new HashMap<String, String>();

        String sql = "select LEVEL1RELAENTNAME, LEVEL2RELAENTNAME from relation_info_mlevel where ENTNAME=? AND LEVEL='2'";

        try {
            conn = ARE.getDBConnection("relaEnt");
            ps = conn.prepareStatement(sql);

            for (MonitorModel monitorModel : monitorModels) {
                String entName = monitorModel.getEnterpriseName();
                ps.setString(1, entName);

                rs = ps.executeQuery();

                while (rs.next()) {
                    String level1 = rs.getString(1);
                    String level2 = rs.getString(2);

                    relaMap.put(level1, entName);
                    relaMap.put(level2, entName);
                }
                //处理成;分割字符串
                int count = 0;
                for(Map.Entry<String, String> entry : relaMap.entrySet()){
                    if(count == 0){
                        relaEnts = entry.getKey();
                        count++;
                    }else{
                        relaEnts += ";" + entry.getKey();
                    }
                }
                monitorModel.setRelaEnts(relaEnts);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
