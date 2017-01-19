package com.amarsoft.app.common;

import com.amarsoft.app.model.ProcessTaskModel;
import com.amarsoft.are.ARE;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
    public List<ProcessTaskModel> getEntMonitorUrl(String bankID, String modelId) {
        List<ProcessTaskModel> entMonitorUrl = new ArrayList<ProcessTaskModel>();

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
                ProcessTaskModel ProcessTaskModel = new ProcessTaskModel();
                ProcessTaskModel.setSerialNo(rs.getString("serinalno"));
                ProcessTaskModel.setEnterpriseName(rs.getString("enterprisename"));
                ProcessTaskModel.setIdNo(rs.getString("idno"));
                ProcessTaskModel.setMonitorURL(rs.getString("monitorurl"));
                ProcessTaskModel.setStockBlock(rs.getString("stockblock"));
                ProcessTaskModel.setInspectLevel(rs.getString("inspectlevel"));
                ProcessTaskModel.setInspectState(rs.getString("inspectstate"));
                ProcessTaskModel.setInputTime(rs.getString("inputtime"));
                entMonitorUrl.add(ProcessTaskModel);
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
}
