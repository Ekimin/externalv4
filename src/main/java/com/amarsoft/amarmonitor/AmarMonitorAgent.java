package com.amarsoft.amarmonitor;

import com.amarsoft.are.ARE;
import com.amarsoft.monitorPlugin.sink.ganglia.AbstractGangliaSink.GangliaOp;
import com.amarsoft.monitorPlugin.sink.ganglia.AbstractGangliaSink.GangliaSlope;
import com.amarsoft.monitorPlugin.sink.ganglia.GangliaConf;
import com.amarsoft.monitorPlugin.sink.ganglia.GangliaSink31;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zpxiao
 * @since 2017年2月10日 上午10:57:17
 * 监控平台指标上报
 */
public class AmarMonitorAgent {

	private static String confFilePath;//configure file for javaAgent

	private static String instanceType;//监控对象类型
	private static String instanceId;//监控对象实例名称

	private static int collectPeriod = 20;//collect period in second
	private static int metaDataSendPeriod = 60;//collect period in second

	private static Pattern pattern = Pattern.compile("[0-9]*");

	/**
	 * @param groupName 指标组名称
	 * @param name 指标名称
	 * @param type 指标数据类型,取值类型 uint8、uint32、uint16、float、double、string
	 * @param value 指标值
	 * @param op 和阈值比较操作符,取值类型  GangliaOp.valueOf("EQ")、GangliaOp.valueOf("LE")、GangliaOp.valueOf("EQ")LE、GangliaOp.valueOf("GT")、GangliaOp.valueOf("GE"),EQ即（=）、LE即（<=）、即LE（<）、GT即（>）和GE即（>=）
	 * @param warnTh 警告阈值
	 * @param critTh 严重阈值
	 * @throws IOException
	 */
	public void emitMetric(String groupName, String name, String type, String value,GangliaOp op,
						   String warnTh, String critTh) {
		init();
		Thread memThread = new Thread(new EmitMetricThread(groupName,name,type,value,op,warnTh,critTh));
		//memThread.setDaemon(true);
		memThread.setName("EmitMetricThread");
		memThread.start();
	}

	/**
	 * 初始化instanceType、instanceId、confFilePath参数
	 */
	private static void init(){
		ARE.getLog().debug("初始化");
		if(instanceType==null){
			instanceType = ARE.getProperty("com.amarsoft.amarmonitor.AmarMonitorAgent.instanceType", "java");
		}
		if(instanceId==null){
			instanceId = ARE.getProperty("com.amarsoft.amarmonitor.AmarMonitorAgent.instanceId");
			if(instanceId==null){
				instanceId = ""+getJavaInstanceNum();
			}
		}
		if(confFilePath==null){
			confFilePath = ARE.getProperty("com.amarsoft.amarmonitor.AmarMonitorAgent.confFilePath", ARE.getProperty("APP_HOME")+"/etc/gangliaPlugin.properties");
		}
	}

	protected class EmitMetricThread implements Runnable {
		private String groupName;//指标组名称
		private String name;//指标名称
		private String type;//指标数据类型
		private String value;//指标值
		private GangliaOp op;//和阈值比较操作符
		private String warnTh;//警告阈值
		private String critTh;//严重阈值

		public EmitMetricThread(String groupName, String name, String type, String value,GangliaOp op,String warnTh, String critTh){
			this.groupName = groupName;
			this.name = name;
			this.type = type;
			this.value = value;
			this.op = op;
			this.warnTh = warnTh;
			this.critTh = critTh;

		}

		public void run() {
			int rate = metaDataSendPeriod/collectPeriod;
			GangliaSink31 ganliaSink = new GangliaSink31();
			ARE.getLog().debug(new StringBuilder().append("confFilePath:").append(confFilePath)
					.append(",instanceType:").append(instanceType).append(",instanceId:").append(instanceId).toString());
			ganliaSink.init(confFilePath, instanceType, instanceId);

			try {
				if(ARE.getLog().isInfoEnabled()){
					ARE.getLog().info(new StringBuilder().append("监控平台指标上报").append("groupName:")
							.append(this.groupName).append(",name:").append(this.name).append(",type:").append(this.type)
							.append(",value:").append(this.value).toString());
				}
				ganliaSink.emitMetric(this.groupName, this.name, this.type, this.value, new GangliaConf(),
						GangliaSlope.valueOf("both"), op, warnTh, critTh, rate);
			} catch (Exception e) {
				if(ARE.getLog().isErrorEnabled()){
					ARE.getLog().error(new StringBuilder().append("监控平台指标上报出错,").append("groupName:")
							.append(this.groupName).append(",name:").append(this.name).toString(),e);
				}
				e.printStackTrace();
			}
		}
	}

	private static int getJavaInstanceNum() {
		int javaProcessNum = 0;
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		try {
			File procFileSystem = new File("/proc/");

			File[] procFiles = procFileSystem.listFiles();

			for (int i = 0; i < procFiles.length; i++) {
				if (!procFiles[i].isDirectory()) {
					continue;
				}

				if (!isNumeric(procFiles[i].getName())) {
					continue;
				}

				// System.out.println("opening the file:" +
				// procFiles[i].getName());
				File[] processFile = procFiles[i].listFiles();
				for (int j = 0; j < processFile.length; j++) {
					if (processFile[j].isFile() && processFile[j].getName().equals("cmdline")) {
						FileInputStream input = new FileInputStream(processFile[j]);
						byte[] buf = new byte[4096];
						input.read(buf);
						input.close();

						for (int k = 0; k < buf.length; k++) {
							if (buf[k] == 0)
								buf[k] = ' ';
						}
						String cmdLine = new String(buf);
						// System.out.println("opening the file:cmdLine=" +
						// cmdLine);
						cmdLine = cmdLine.toUpperCase();
						// System.out.println("opening the file:cmdLine=" +
						// cmdLine);
						if (cmdLine.contains("JAVA") && cmdLine.contains("CLASSPATH")
								&& cmdLine.contains(instanceType.toUpperCase())) {
							javaProcessNum++;
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		// System.out.println("javaProcessNum=" + javaProcessNum);
		return javaProcessNum;
	}

	private static boolean isNumeric(String str) {
		Matcher isNum = pattern.matcher(str);
		if (!isNum.matches()) {
			return false;
		}
		return true;
	}
}
