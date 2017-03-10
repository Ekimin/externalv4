import com.amarsoft.app.job.ExternalV4Job;
import com.amarsoft.app.model.MonitorModel;
import com.amarsoft.are.ARE;
import com.amarsoft.are.util.CommandLineArgument;

/**
 * Created by ymhe on 2017/1/20.
 * externalv4
 */
public class testProcess {
    public static void main(String[] args){
        if (!ARE.isInitOk()) {
            ARE.init("etc/are.xml");
        }
        CommandLineArgument arg = new CommandLineArgument(args);

        String bankId = arg.getArgument("bankId");//机构编号
        String modelId = arg.getArgument("modelId");//模型编号
        String azkabanExecId = arg.getArgument("azkabanExecId");//azkaban执行编号
        String batchId = arg.getArgument("batchId");//王军批次号
        //TODO:测试数据
        bankId = "poc-EDS测试账号";
        modelId = "V4报告";
        azkabanExecId = "aztest";
        batchId = "poc-EDS测试账号-1489053234690";
        //获取报告名单企业

        if(bankId == null){
            ARE.setProperty("BANKID", "noBankId");//日志文件按银行编号存储区分
        }else{
            ARE.setProperty("BANKID", bankId);//日志文件按银行编号存储区分
        }

        ExternalV4Job externalV4Job = new ExternalV4Job();
        externalV4Job.generateProcess(azkabanExecId, modelId, bankId);
    }
}
