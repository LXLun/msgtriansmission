package send;

import java.util.List;

import simulation.SimulationTest;
import util.ReadAndWriteFile;
import entity.Message;

/**
 * 模拟服务重新启动时检查本地是否有待发送消息，并且发送等待消息
 * @author luoxilun
 *
 */
public class SendMessageTest
{
    public static void main(String[] args)
    {
        SimulationTest st = new SimulationTest();
        ReadAndWriteFile rawf = new ReadAndWriteFile();
        List<Message> list = rawf.readTxtFileIntoStringArrList();
        st.sendWaitMessage(list);
    }

}
