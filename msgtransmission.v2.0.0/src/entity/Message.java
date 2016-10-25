package entity;

import java.io.Serializable;

public class Message implements Serializable
{
    private static final long serialVersionUID = -723730347189949530L;
    private String id;
    private String message;
    //所有消息缓存在本地都是默认状态为false;
    //true:成功发送的消息;false:等待发送的消息;
    private int flag = 0;

    public Message()
    {

    }

    public Message(String id, String message, int msgtemp)
    {
        super();
        this.id = id;
        this.message = message;
        this.flag = msgtemp;
    }

    public String getId()
    {
        return this.id;
    }

    public static long getSerialversionuid()
    {
        return serialVersionUID;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getMessage()
    {
        return this.message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public int getFlag()
    {
        return this.flag;
    }

    public void setFlag(int flag)
    {
        this.flag = flag;
    }

    @Override
    public String toString()
    {
        return this.id + "," + this.message + "," + this.flag;
    }

}
