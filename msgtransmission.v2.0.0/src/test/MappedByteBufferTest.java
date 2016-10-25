package test;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import simulation.Send;
import entity.Message;

public class MappedByteBufferTest
{
    private final static String FILEPATH = "C:\\Users\\luoxilun\\Desktop\\msgtransmission.v.2.0.0.txt";
    private Send send = new Send();

    public void readAndWriteMappedByteBuffer()
    {
        String encode = "GBK";
        //换行符
        int LF = 10;
        //回车符  
        int CR = 13;
        byte[] lineByte = new byte[0];
        List<Message> list = new ArrayList<Message>();
        try
        {
            RandomAccessFile raf = new RandomAccessFile(FILEPATH, "rw");
            long totalLen = raf.length();
            byte[] temp = new byte[0];
            System.out.println("文件总长字节是: " + totalLen);
            //打开一个文件通道   
            FileChannel fc = raf.getChannel();
            //映射文件中的某一部分数据以读写模式到内存中   
            MappedByteBuffer mBuffer = fc.map(FileChannel.MapMode.READ_WRITE, 0, raf.length());
            while (fc.read(mBuffer) != -1)
            {
                //读取结束后的位置，相当于读取的长度
                int rSize = mBuffer.position();
                //用来存放读取的内容的数组  
                byte[] bs = new byte[rSize];
                //将position设回0,所以你可以重读Buffer中的所有数据,此处如果不设置,无法使用下面的get方法 
                mBuffer.rewind();
                //相当于rBuffer.get(bs,0,bs.length())：从position初始位置开始相对读,读bs.length个byte,并写入bs[0]到bs[bs.length-1]的区域  
                mBuffer.get(bs);
                mBuffer.clear();

                int startNum = 0;
                boolean hasLF = false;//是否有换行符ffff
                for (int i = 0; i < rSize; i++)
                {
                    if (bs[i] == LF)
                    {
                        hasLF = true;
                        int tempNum = temp.length;
                        int lineNum = i - startNum;
                        //数组大小已经去掉换行符
                        lineByte = new byte[tempNum + lineNum];
                        //填充了lineByte[0]~lineByte[tempNum-1] 
                        /*
                         * src - 源数组。
                        srcPos - 源数组中的起始位置。
                        dest - 目标数组。
                        destPos - 目标数据中的起始位置。
                        length - 要复制的数组元素的数量。
                         */
                        System.arraycopy(temp, 0, lineByte, 0, tempNum);
                        temp = new byte[0];
                        //填充lineByte[tempNum]~lineByte[tempNum+lineNum-1]
                        System.arraycopy(bs, startNum, lineByte, tempNum, lineNum);
                        //一行完整的字符串(过滤了换行和回车) 
                        String line = new String(lineByte, 0, lineByte.length, encode);
                        //将数据数据根据","分隔，放入数组中
                        String msgtemp[] = line.split(",");
                        //长度为3的数组才能被存入对象中
                        if (msgtemp.length == 3)
                        {
                            Message msg = new Message(msgtemp[0], msgtemp[1], Integer.parseInt(msgtemp[2]));
                            if (msg.getFlag() == 0)
                            {
                                this.send.sendWaitMessage();
                                msg.setFlag(1);
                                System.out.println(msg.toString());
                            }
                            list.add(msg);

                        }
                        //过滤回车符和换行符  
                        if (i + 1 < rSize && bs[i + 1] == CR)
                        {
                            startNum = i + 2;
                        }
                        else
                        {
                            startNum = i + 1;
                        }
                    }
                }
                if (hasLF)
                {
                    temp = new byte[bs.length - startNum];
                    System.arraycopy(bs, startNum, temp, 0, temp.length);
                }
                else
                {//兼容单次读取的内容不足一行的情况  
                    byte[] toTemp = new byte[temp.length + bs.length];
                    System.arraycopy(temp, 0, toTemp, 0, temp.length);
                    System.arraycopy(bs, 0, toTemp, temp.length, bs.length);
                    temp = toTemp;
                }
            }
            if (temp != null && temp.length > 0)
            {//兼容文件最后一行没有换行的情况  
                String line = new String(temp, 0, temp.length, encode);
                //                String msgtemp[] = line.split(",");
                //                //长度为3的数组才能被存入对象中
                //                if (msgtemp.length == 3)
                //                {
                //                    Message msg = new Message(msgtemp[0], msgtemp[1], msgtemp[2]);
                //                    list.add(msg);
                //                }
            }
            for (int i = 0; i < list.size(); i++)
            {
                String greeting = (list.get(i)).toString() + "\n";
                mBuffer.put(greeting.getBytes());
            }
            mBuffer.force();//强制输出,在buffer中的改动生效到文件   
            mBuffer.clear();
            fc.close();
            raf.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
