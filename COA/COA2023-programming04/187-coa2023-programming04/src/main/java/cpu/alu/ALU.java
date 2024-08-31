package cpu.alu;

import util.DataType;

public class ALU {
    public DataType add(DataType src, DataType dest) {
        String srcStr = src.toString();
        String destStr = dest.toString();
        StringBuilder result = new StringBuilder();
        int c = 0;
        for (int i = 31; i >= 0; i--) {
            int X = Integer.parseInt(srcStr.substring(i, i + 1));
            int Y = Integer.parseInt(destStr.substring(i, i + 1));
            result.insert(0, X ^ Y ^ c);
            c = (X & Y) | (X & c) | (Y & c);
        }
        // TODO
        return new DataType(result.toString());
    }

    public DataType sub(DataType src, DataType dest) {
        String srcStr = src.toString();
        String destStr = dest.toString();
        StringBuilder result = new StringBuilder();
        int c = 1;
        for (int i = 31; i >= 0; i--) {
            int X = Integer.parseInt(srcStr.substring(i, i + 1)) ^ 1;
            int Y = Integer.parseInt(destStr.substring(i, i + 1));
            result.insert(0, X ^ Y ^ c);
            c = (X & Y) | (X & c) | (Y & c);
        }
        // TODO
        return new DataType(result.toString());
    }

    /**
     * 返回两个二进制整数的乘积(结果低位截取后32位)
     * dest * src
     *
     * @param src  32-bits
     * @param dest 32-bits
     * @return 32-bits
     */
    public DataType mul(DataType src, DataType dest) {
        String srcStr = src.toString();
        StringBuilder sb = new StringBuilder();
        sb.append("00000000000000000000000000000000");
        sb.append(src.toString());
        sb.append("0");
        for (int i = 0; i < srcStr.length(); i++) {
            if (sb.charAt(64) - sb.charAt(63) == 1){
                sb.insert(0,add(new DataType(sb.substring(0,32)), dest).toString());
                sb.delete(32,64);
            } else if (sb.charAt(64) - sb.charAt(63) == -1) {
                sb.insert(0,sub(dest, new DataType(sb.substring(0,32))).toString());
                sb.delete(32,64);
            }
            sb.delete(64, 65);
            sb.insert(0, sb.charAt(0));
        }
        //TODO
        return new DataType(sb.substring(32, 64));
    }

    DataType remainderReg = new DataType("00000000000000000000000000000000");

    /**
     * 返回两个二进制整数的除法结果
     * dest ÷ src
     *
     * @param src1  32-bits
     * @param dest1 32-bits
     * @return 32-bits
     */
    public DataType div(DataType src1, DataType dest1) {
        DataType src;
        DataType dest;
        if (src1.toString().charAt(0) == '1')
            src = sub(src1, new DataType("00000000000000000000000000000000"));
        else
            src = src1;
        if (dest1.toString().charAt(0) == '1')
            dest = sub(dest1, new DataType("00000000000000000000000000000000"));
        else
            dest = dest1;
        if (src.toString().equals("00000000000000000000000000000000"))
            throw new ArithmeticException();
        StringBuilder sb = new StringBuilder();
        sb.append("00000000000000000000000000000000");
        char pastSign = '0';
        char currentSign = '0';
        sb.append(dest.toString());
        for (int i = 0; i < 32; i++) {
            sb.delete(0, 1);
            pastSign = sb.charAt(0);
            sb.insert(0, sub(src, new DataType(sb.substring(0, 32))).toString());
            sb.delete(32, 64);
            currentSign = sb.charAt(0);
            if (currentSign != pastSign &&
                    (!sb.substring(0, 32).equals("00000000000000000000000000000000"))){
                sb.append(0);
                sb.insert(0, add(src, new DataType(sb.substring(0, 32))).toString());
                sb.delete(32, 64);
            }
            else {
                sb.append("1");
            }

        }
        if (dest1.toString().charAt(0) == '0')
            remainderReg = new DataType(sb.substring(0, 32));
        else
            remainderReg = new DataType(sub(new DataType(sb.substring(0, 32)), new DataType("00000000000000000000000000000000")).toString());

        if (dest1.toString().charAt(0) == src1.toString().charAt(0))
            return new DataType(sb.substring(32, 64));
        else
            return sub(new DataType(sb.substring(32, 64)), new DataType("00000000000000000000000000000000"));
        //TODO
    }


}

