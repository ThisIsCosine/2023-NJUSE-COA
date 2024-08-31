package cpu.alu;

import util.DataType;

/**
 * Arithmetic Logic Unit
 * ALU封装类
 */
public class ALU {

    /**
     * 返回两个二进制整数的和
     * dest + src
     *
     * @param src  32-bits
     * @param dest 32-bits
     * @return 32-bits
     */
    public DataType add(DataType src, DataType dest) {
        String srcStr = src.toString();
        String destStr = dest.toString();
        StringBuilder result = new StringBuilder();
        int c = 0;
        for (int i = 31; i >= 0; i--) {
            int X = Integer.parseInt(srcStr.substring(i, i + 1));
            int Y = Integer.parseInt(destStr.substring(i, i + 1));
            result.insert(0, X ^ Y ^ c); // PPT中的公式
            c = (X & Y) | (X & c) | (Y & c);
        }
        // TODO
        return new DataType(result.toString());
    }

    /**
     * 返回两个二进制整数的差
     * dest - src
     *
     * @param src  32-bits
     * @param dest 32-bits
     * @return 32-bits
     */
    public DataType sub(DataType src, DataType dest) {
        String srcStr = src.toString();
        String destStr = dest.toString();
        StringBuilder result = new StringBuilder();
        int c = 1; // 减法的核心要素
        for (int i = 31; i >= 0; i--) {
            int X = Integer.parseInt(srcStr.substring(i, i + 1)) ^ 1; // 因为初始 c = 1
            int Y = Integer.parseInt(destStr.substring(i, i + 1));
            result.insert(0, X ^ Y ^ c);
            c = (X & Y) | (X & c) | (Y & c);
        }
        // TODO
        return new DataType(result.toString());
    }

}
