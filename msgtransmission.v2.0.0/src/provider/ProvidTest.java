package provider;

import java.util.ArrayList;
import java.util.List;

import simulation.SimulationTest;
import util.ReadAndWriteFile;
import entity.Message;

/**
 * 模拟消息产生的类
 * @author luoxilun
 *
 */
public class ProvidTest
{
    //模拟产生消息的数目
    private static final int SEND_NUMBER = 100;
    private static final String MESSAGE = "MDZZ";

    public static void main(String[] args)
    {
        ReadAndWriteFile rawf = new ReadAndWriteFile();
        SimulationTest st = new SimulationTest();
        List<Message> list = new ArrayList<Message>();
        //模拟消息发送的过程，消息先以默认等待的状态缓存在本地
        for (int i = 0; i < SEND_NUMBER; i++)
        {
            Message msgs = new Message();
            msgs.setId(String.valueOf(i));
            msgs.setMessage(MESSAGE);
            list.add(msgs);
        }
        rawf.writeFile(list);
        st.genneratingWaitMsg(list);
        st.notifyUser(list);
    }
}