package memory.cache;

import memory.Memory;
import memory.cache.cacheReplacementStrategy.FIFOReplacement;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

public class WriteThroughTest {

    private final Memory memory = Memory.getMemory();
    private final Cache cache = Cache.getCache();


    @BeforeClass
    public static void init() {
        Cache.getCache().setSETS(128);
        Cache.getCache().setSetSize(4);
        Cache.getCache().setReplacementStrategy(new FIFOReplacement());
        Cache.isWriteBack = false;
    }

    @Before
    public void clearCache() {
        cache.clear();
    }

    /**
     * 写入数据，同时写入内存
     */
    @Test
    public void test01() {
        String pAddr = "00000000000000000000000000000000";
        String pAddr2 = "00000000000000000000000000000010";
        byte[] input1 = {0b01110100, 0b01010100};
        byte[] input2 = {0b00000000, 0b00000011};
        byte[] input3 = {0b00000001, 0b00000010, 0b000000011, 0b00000100};
        byte[] input4 = {0b01110111, 0b01111110, 0b00010110, 0b01111110, 0b00110101, 0b00010100, 0b01010111, 0b00011101, 0b01111100, 0b01000000};
        byte[] dataRead;

        memory.write(pAddr, input1.length, input1);
        dataRead = cache.read(pAddr, input1.length);
        assertArrayEquals(input1, dataRead);
        assertTrue(cache.checkStatus(new int[]{0}, new boolean[]{true}, new char[][]{"00000000000000000000000000".toCharArray()}));

        cache.write(pAddr, input2.length, input2);
        dataRead = cache.read(pAddr, input2.length);
        assertArrayEquals(input2, dataRead);
        assertArrayEquals(input2, memory.read(pAddr, input2.length));

        cache.write(pAddr2, input3.length, input3);
        dataRead = cache.read(pAddr2, input3.length);
        assertArrayEquals(input3, dataRead);
        assertTrue(cache.checkStatus(new int[]{0}, new boolean[]{true}, new char[][]{"00000000000000000000000000".toCharArray()}));
        assertArrayEquals(input3, memory.read(pAddr2, input3.length));

        cache.write(pAddr2, input4.length, input4);
        dataRead = cache.read(pAddr2, input4.length);
        assertArrayEquals(input4, dataRead);
        assertTrue(cache.checkStatus(new int[]{0}, new boolean[]{true}, new char[][]{"00000000000000000000000000".toCharArray()}));
        assertArrayEquals(input4, memory.read(pAddr2, input4.length));
    }

    /**
     * 从cache读入数据，然后修改cache同时修改内存，然后再读内存
     */
    @Test
    public void test02(){
        byte[] input1 = new byte[32 * 1024];
        byte[] input2 = new byte[64];
        byte[] input3 = new byte[64];
        Arrays.fill(input1, (byte) 'a');
        Arrays.fill(input2, (byte) 'b');
        Arrays.fill(input3, (byte) 'c');
        String pAddr1 = "00000000000000000000000000000000";
        String pAddr2 = "00000000000000000000000001000000";
        String pAddr3 = "00000000101000000000000001000000";
        byte[] dataRead;

        memory.write(pAddr1, input1.length, input1);
        dataRead = cache.read(pAddr1,input1.length);
        assertArrayEquals(input1, dataRead);

        cache.write(pAddr2, input2.length, input2);
        dataRead = cache.read(pAddr2, input2.length);
        assertArrayEquals(input2, dataRead);
        assertArrayEquals(input2, memory.read(pAddr2, input2.length));

        memory.write(pAddr3, input3.length, input3);
        dataRead = cache.read(pAddr3, input3.length);
        assertArrayEquals(input3, dataRead);
    }

    /**
     * 写入数据同时写入内存，再修改内存，发生替换，cache数据更新
     */
    @Test
    public void test03(){
        byte[] input1 = new byte[32 * 1024];
        byte[] input2 = new byte[64];
        Arrays.fill(input1, (byte) 'd');
        Arrays.fill(input2, (byte) 'e');
        String pAddr1 = "00000000000000000000000000000000";
        String pAddr2 = "00000000101000000000000001000000";
        byte[] dataRead;

        // 数据位于cache第1组的第0行
        cache.write(pAddr2, input2.length, input2);
        dataRead = cache.read(pAddr2, input2.length);
        assertArrayEquals(input2, dataRead);
        assertArrayEquals(input2, memory.read(pAddr2, input2.length));

        // memory 被修改，所以cache中的所有被Match的行的valid都变成了False，再次read的时候，有一行会被插入到第1组第0行的位置，此时发生替换
        memory.write(pAddr1, input1.length, input1);
        dataRead = cache.read(pAddr1, input1.length);
        assertArrayEquals(input1, dataRead);
        assertTrue(cache.checkStatus(new int[]{4}, new boolean[]{true}, new char[][]{"00000000000000000000000011".toCharArray()}));
    }

    /**
     * same to test03
     */
    @Test
    public void test04(){
        byte[] input1 = new byte[32 * 1024];
        byte[] input2 = new byte[65];
        Arrays.fill(input1, (byte) 'f');
        Arrays.fill(input2, (byte) 'g');
        String pAddr1 = "00000000000000000000000000000000";
        String pAddr2 = "00000000101000000000000001000000";
        byte[] dataRead;

        cache.write(pAddr2, input2.length, input2);
        dataRead = cache.read(pAddr2, input2.length);
        assertArrayEquals(input2, dataRead);
        assertArrayEquals(input2, memory.read(pAddr2, input2.length));

        memory.write(pAddr1, input1.length, input1);
        dataRead = cache.read(pAddr1, input1.length);
        assertArrayEquals(input1, dataRead);
        assertTrue(cache.checkStatus(new int[]{4, 8}, new boolean[]{true, true}, new char[][]{"00000000000000000000000011".toCharArray(), "00000000000000000000000011".toCharArray()}));
    }

}
