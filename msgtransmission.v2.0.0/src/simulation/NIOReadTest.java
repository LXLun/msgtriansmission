package simulation;

import test.MappedByteBufferTest;

public class NIOReadTest
{

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        MappedByteBufferTest mbbt = new MappedByteBufferTest();
        mbbt.readAndWriteMappedByteBuffer();
    }

}
