package memory.disk;

import util.Transformer;

import java.io.*;
import java.util.Arrays;

/**
 * 磁盘抽象类，磁盘大小为64M
 */

public class Disk {

    public static int DISK_SIZE_B = 64 * 1024 * 1024;      // 磁盘大小 64 MB

    public static final int DISK_HEAD_NUM = 8;     // 磁头数
    public static final int TRACK_NUM = 256;        // 磁道数
    public static final int SECTOR_PER_TRACK = 64;  // 每磁道扇区数
    public static final int BYTE_PER_SECTOR = 512;  // 每扇区字节数

    private final DiskHead disk_head = new DiskHead();  // 磁头

    private static File disk_device;    // 虚拟磁盘文件

    // 单例模式
    private static final Disk diskInstance = new Disk();

    public static Disk getDisk() {
        return diskInstance;
    }

    private Disk() {
        disk_device = new File("DISK.vdev");
        if (disk_device.exists()) {
            disk_device.delete();
        }
        BufferedWriter writer = null;
        try {
            disk_device.createNewFile();
            // 初始化磁盘
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(disk_device)));
            char[] dataUnit = new char[1024];
            for (int i = 0; i < 64 * 16; i++) {
                char currentChar = 0x30;
                for (int j = 0; j < 64; j++) {
                    Arrays.fill(dataUnit, currentChar);
                    writer.write(dataUnit);
                    currentChar++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 从磁盘读取数据
     *
     * @param addr 数据在磁盘内的起始地址
     * @param len  待读取的字节数
     * @return 读取出的数据
     */
    public byte[] read(String addr, int len) {
        byte[] data = new byte[len];
        RandomAccessFile reader = null;
        try {
            reader = new RandomAccessFile(disk_device, "r");
            // 本作业只有64M磁盘，不会超出int表示范围
            // ps: 读磁盘会很慢，请尽可能减少read函数调用
            reader.skipBytes(Integer.parseInt(Transformer.binaryToInt("0" + addr)));
            disk_head.seek(Integer.parseInt(Transformer.binaryToInt("0" + addr)));
            for (int i = 0; i < len; i++) {
                data[i] = reader.readByte();
                disk_head.addPoint();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return data;
    }

    /**
     * 往磁盘内写数据
     *
     * @param addr 数据在磁盘内的起始地址
     * @param len  待写数据的字节数
     * @param data 待写数据
     */
    public void write(String addr, int len, byte[] data) {
        RandomAccessFile writer = null;
        try {
            writer = new RandomAccessFile(disk_device, "rw");
            writer.skipBytes(Integer.parseInt(Transformer.binaryToInt("0" + addr)));
            disk_head.seek(Integer.parseInt(Transformer.binaryToInt("0" + addr)));
            for (int i = 0; i < len; i++) {
                writer.write(data[i]);
                disk_head.addPoint();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 磁头，记录了自己当前所在位置
     */
    private static class DiskHead {
        int track = 0;  // 当前所在磁道号
        int sector = 0; // 当前所在扇区号
        int point = 0;  // 当前所在扇区内部的字节号

        /**
         * 将磁头移动到目标位置
         *
         * @param start 数据起始点
         */
        public void seek(int start) {
            int diskPoint = start % (TRACK_NUM * SECTOR_PER_TRACK * BYTE_PER_SECTOR);
            this.track = diskPoint / (SECTOR_PER_TRACK * BYTE_PER_SECTOR);
            this.sector = (diskPoint / BYTE_PER_SECTOR) % SECTOR_PER_TRACK;
            this.point = diskPoint % BYTE_PER_SECTOR;
            //TODO
        }

        /**
         * 磁头移动一个字节
         */
        public void addPoint() {
            if (this.point == BYTE_PER_SECTOR - 1) {
                if (this.sector == SECTOR_PER_TRACK - 1) {
                    if (this.track == TRACK_NUM - 1) {
                        this.track = 0;
                        this.sector = 0;
                        this.point = 0;
                    }
                    else {
                        this.sector = 0;
                        this.point = 0;
                        this.track++;
                    }
                }
                else {
                    this.point = 0;
                    this.sector++;
                }
            }
            else
                this.point++;
            //TODO
        }

    }

    /**
     * 以下方法会被用于测试，请勿修改
     */

    public int getCurrentTrack() {
        return disk_head.track;
    }

    public int getCurrentSector() {
        return disk_head.sector;
    }

    public int getCurrentPoint() {
        return disk_head.point;
    }

}
