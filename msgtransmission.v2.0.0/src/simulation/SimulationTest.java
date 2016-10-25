package simulation;

import java.util.List;

import util.ReadAndWriteFile;
import entity.Message;

public class SimulationTest
{
    /**
     * 得到msg的flag判断是否有等待发送的消息，并且发送消息
     * @param list
     */
    public void sendWaitMessage(List<Message> list)
    {
        ReadAndWriteFile rawf = new ReadAndWriteFile();
        rawf.readTxtFileIntoStringArrList();
        Message msg = new Message();
        Send send = new Send();
        for (int i = 0; i < list.size(); i++)
        {
            msg = list.get(i);
            //得到消息中的flag,调用模拟消息发送的方法，发送flag为false的方法，并且更改其状态缓存到本地
            if ((msg.getFlag()) == 0)
            {
                send.sendWaitMessage();
                msg.setFlag(1);
            }
        }
        rawf.writeFile(list);
    }

    /**
     * 模拟消息发送时产产生等待消息
     * @param list
     */
    public void genneratingWaitMsg(List<Message> list)
    {
        ReadAndWriteFile rawf = new ReadAndWriteFile();
        rawf.readTxtFileIntoStringArrList();
        Message msg = new Message();
        for (int i = 0; i < list.size(); i++)
        {
            //模拟每10条数据中会产生一条等待消息,其它消息设置为成功发送
            if (i % 10 != 0)
            {
                msg = list.get(i);
                msg.setFlag(1);
            }
        }
        //将更新后的状态重新保存到本地
        rawf.writeFile(list);
    }

    /**
     * 通知用户待发送消息的情况
     */
    public void notifyUser(List<Message> list)
    {
        ReadAndWriteFile rawf = new ReadAndWriteFile();
        rawf.readTxtFileIntoStringArrList();
        int a = 0;
        Message msg = new Message();
        //统计发送成功消息的数量
        for (int i = 0; i < list.size(); i++)
        {
            msg = list.get(i);
            if (msg.getFlag() == 1)
            {
                a++;
            }
        }
        System.out.println("本次发送消息总条数为：" + list.size() + "; 成功发送的消息为" + a + "条;");
    }
}
