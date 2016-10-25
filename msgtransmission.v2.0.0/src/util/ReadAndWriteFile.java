package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import simulation.Send;
import entity.Message;

/**
 * 读写txt文件
 * @author luoxilun
 * @param filepath 文件位置
 */
public class ReadAndWriteFile
{
    //设置本地缓存文件爱你的默认路径
    private final static String FILEPATH = "C:\\Users\\luoxilun\\Desktop\\msgtransmission.v.2.0.0.txt";
    private Send send = new Send();

    /**
     * 将list中的数据按行保存到本地
     * @param list
     */
    public void writeFile(List<Message> list)
    {
        FileOutputStream fos = null;
        FileOutputStream tfos = null;
        FileChannel fc = null;
        FileChannel tfc = null;
        ChangeString cs = new ChangeString();
        try
        {
            File file = new File(FILEPATH);
            if (!file.exists())
            {
                file.createNewFile();
            }
            //从FileOutputStream中获取文件流
            fos = new FileOutputStream(FILEPATH);
            tfos = new FileOutputStream("C:\\Users\\luoxilun\\Desktop\\contents.v.2.0.0.txt");
            fc = fos.getChannel();
            tfc = tfos.getChannel();
            //创建一个buffer并把准备写的数据填充进去; 
            ByteBuffer bb = ByteBuffer.allocate(1024);
            for (int i = 0; i < list.size(); i++)
            {
                System.out.println((list.get(i)).toString());
                String greeting = (list.get(i)).toString() + "\n";
                byte[] bytes = greeting.getBytes();
                for()
                {
                    
                }
                System.out.println((cs.binary(bytes, 2)).getBytes());
                fc.write(ByteBuffer.wrap((cs.binary(bytes, 2)).getBytes()));
                bb.clear();
            }
            fc.close();
            fos.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                tfos.close();
                tfc.close();
                fc.close();
                fos.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * 读取文件,将每行的数据转换成对象，并且放入list中
     * @param filePath
     * @return
     */
    public List<Message> readTxtFileIntoStringArrList()
    {
        //一次读取的字节长度
        int bufSize = 1000000;
        String encode = "GBK";
        //换行符
        int LF = 10;
        //回车符  
        int CR = 13;
        byte[] lineByte = new byte[0];
        List<Message> list = new ArrayList<Message>();
        File fis = new File(FILEPATH);
        try
        {
            FileChannel fc = new RandomAccessFile(fis, "r").getChannel();
            ByteBuffer rBuffer = ByteBuffer.allocate(bufSize);
            //temp：由于是按固定字节读取，在一次读取中，第一行和最后一行经常是不完整的行，因此定义此变量来存储上次的最后一行和这次的第一行的内容，
            //并将之连接成完成的一行，否则会出现汉字被拆分成2个字节，并被提前转换成字符串而乱码的问题，数组大小应大于文件中最长一行的字节数
            byte[] temp = new byte[0];
            //fc.read(rBuffer)：从文件管道读取内容到缓冲区(rBuffer)
            while (fc.read(rBuffer) != -1)
            {
                //读取结束后的位置，相当于读取的长度
                int rSize = rBuffer.position();
                //读取结束后的位置，相当于读取的长度
                byte[] bs = new byte[rSize];
                //将position设回0,所以你可以重读Buffer中的所有数据,此处如果不设置,无法使用下面的get方法 
                rBuffer.rewind();
                //相当于rBuffer.get(bs,0,bs.length())：从position初始位置开始相对读,读bs.length个byte,并写入bs[0]到bs[bs.length-1]的区域  
                rBuffer.get(bs);
                rBuffer.clear();

                int startNum = 0;
                boolean hasLF = false;//是否有换行符
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
                String msgtemp[] = line.split(",");
                //长度为3的数组才能被存入对象中
                if (msgtemp.length == 3)
                {
                    Message msg = new Message(msgtemp[0], msgtemp[1], Integer.parseInt(msgtemp[2]));
                    list.add(msg);
                }
                //              System.out.println(line);  
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        System.out.println(list.toString());
        return list;
    }

    /**
     * 按照id修改替换本地txt中指定的字段（io修改）
     * @param id
     */
    public void modifyTxt(String id)
    {
        BufferedReader reader = null;
        FileOutputStream out = null;
        try
        {
            File file = new File(FILEPATH);
            //用reader接受通过io读取的缓存文件
            reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            //创建文件输出流，将内容输出到本地
            out = new FileOutputStream(file);
            String outString = "";
            while (line != null && !line.isEmpty())
            {
                //根据id匹配行数
                if (line.indexOf(id) >= 0)
                {
                    //将数据数据根据","分隔，放入数组
                    String[] strs = line.split(",");
                    //替换字符串中指定位置的内容
                    line = line.replace(strs[2], "false");
                }
                //outString按行保存修改后的数据
                outString += line + System.getProperty("line.separator");
                line = reader.readLine();
            }
            //修改后的结果重新写入到本地
            out.write(outString.getBytes());
        }
        catch (Exception ex)
        {
            Logger.getLogger(ReadAndWriteFile.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally
        {
            try
            {
                reader.close();
                out.flush();
                out.close();
            }
            catch (IOException ex)
            {
                Logger.getLogger(ReadAndWriteFile.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
