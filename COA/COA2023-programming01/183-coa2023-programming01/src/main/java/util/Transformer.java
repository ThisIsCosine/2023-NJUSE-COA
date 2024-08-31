package util;
import java.util.Iterator;
import java.io.BufferedReader;

public class Transformer {
    private static String absIntToBinary(long absNum, int digit){
        StringBuilder output = new StringBuilder();
        long LabsNum = (long) absNum;
        if (LabsNum >= 0) {
            for (long i = (long)Math.pow(2, digit - 1); i >= 1; i /= 2) {
                output.append(LabsNum / i);
                LabsNum = LabsNum % i;
            }
        }
        return output.toString();
    }

    public static String binaryToAbsInt(String binStr, int digit){
        long output = 0;
        for (int i = 0; i < binStr.length(); i++) {
            output += (long) Math.pow(2, digit - 1 - i) * Integer.parseInt(binStr.substring(i, i + 1));
        }
        return String.valueOf(output);
    }
    public static String intToBinary(String numStr) {
        int num = Integer.parseInt(numStr);
        if (num >= 0)
            return absIntToBinary(num, 32);
        else {
            long absNum = (long)Math.pow(2, 32) + num; //运用补码的知识，注意
            return absIntToBinary(absNum, 32);
        }
        // TODO:
    }

    public static String binaryToInt(String binStr) {
        if (binStr.charAt(0) == '0')
            return binaryToAbsInt(binStr, 32);
        else
             return String.valueOf((Long.parseLong(binaryToAbsInt(binStr, 32)) - (long) Math.pow(2, 32)));
        // TODO:
    }

    public static String decimalToNBCD(String decimalStr) {
        StringBuilder NBCDCode = new StringBuilder();
        int decimalNum = Integer.parseInt(decimalStr);
        if (decimalNum >= 0)
            NBCDCode.append("1100");
        else
            NBCDCode.append("1101");
        for (int i = 1000000; i >= 1 ; i = i / 10) {
            NBCDCode.append(absIntToBinary(Math.abs(decimalNum) / i, 4));
            decimalNum %= i;
        }
        return NBCDCode.toString();
        // TODO:
    }

    public static String NBCDToDecimal(String NBCDStr) {
        StringBuilder decimalNumStr = new StringBuilder();
        if (NBCDStr.startsWith("1101"))
            decimalNumStr.append("-");
        boolean effective = false;
        for (int i = 4; i < 32; i += 4) {
            String n = binaryToAbsInt(NBCDStr.substring(i, i + 4), 4);
            if (!n.equals("0"))
                effective = true;
            if (effective)
                decimalNumStr.append(n);
        }
        if (decimalNumStr.length() == 0 || decimalNumStr.toString().equals("-"))
            return "0";
        // TODO:
        return decimalNumStr.toString();
    }

    public static String floatToBinary(String floatStr) {
        if (floatStr.equals("0.0") || floatStr.equals("0"))
            return "00000000000000000000000000000000";
        if (floatStr.equals("+Inf"))
            return "01111111100000000000000000000000";
        if (floatStr.equals("-Inf"))
            return "11111111111111111111111111111111";
        float fl0 = Float.parseFloat(floatStr);
        if (fl0 > -Float.MIN_VALUE && fl0 < Float.MIN_VALUE)
            return "10000000000000000000000000000000";//对于0的苛刻要求，xswl
        if (fl0 > Float.MAX_VALUE)
            return "+Inf";
        if (fl0 < -Float.MAX_VALUE)
            return "-Inf";
        StringBuilder binaryFloat = new StringBuilder();
        if (fl0 >= 0)
            binaryFloat.append("0");
        else
            binaryFloat.append("1");

        float fl = (float) Math.abs(fl0);
        int maxIndex = 0; // 求出阶码
        boolean normal = true; // 是否是规格化数
        for (int i = 254; i > 0; i--) {
            if (fl / (float) Math.pow(2, i - 127) >= 1) {
                maxIndex = i; // 规格化数，阶码不为0
                break;
            }
            if (i == 1)
                normal = false; // 非规格化数阶码为0
        }
        if (normal){
            binaryFloat.append(absIntToBinary(maxIndex, 8));
            fl -= (float) Math.pow(2, maxIndex - 127);
            for (int i = 1; i <=  23; i++) {
                binaryFloat.append(Integer.toString((int)(fl / Math.pow(2, maxIndex - 127 - i))));
                int j = (int)(fl / Math.pow(2, maxIndex - 127 - i));
                fl -= j * (float)Math.pow(2, maxIndex - 127 - i);
            } // 规格化数尾数
            return binaryFloat.toString();
        }
        else {
            binaryFloat.append("00000000");
            for (int i = 1; i <=  23; i++) {
                binaryFloat.append(String.valueOf((int)(fl / Math.pow(2, - 126 - i))));
                fl -=(float) Math.pow(2, - 126 - i) *  ((int)(fl / Math.pow(2, - 126 - i)));
            } // 非规格化数尾数
            return binaryFloat.toString();
        }
        // TODO:
    }
    // 先判断特殊情况，后处理一般情况


    public static String binaryToFloat(String binStr) {

        if (binStr.equals("00000000000000000000000000000000") || binStr.equals("10000000000000000000000000000000"))
            return "0.0";
        if (binStr.equals("01111111100000000000000000000000"))
            return "+Inf";
        if (binStr.equals("11111111100000000000000000000000"))
            return "-Inf";
        if (binStr.substring(1,9).equals("11111111") && !binStr.substring(9, 32).equals("00000000000000000000000"))
            return "NaN";

        StringBuilder flStr = new StringBuilder();
        if (binStr.startsWith("1"))
            flStr.append("-");
        if (binStr.substring(1,9).equals("11111111")){
            flStr.append("Inf");
            return flStr.toString();
        }
        float fl = 0;
        if (binStr.substring(1, 9).equals("00000000")){
            for (int i = 1; i <= 23; i++) {
                fl += (float) (Math.pow(2, -126 - i) * (Integer.parseInt(binStr.substring(8 + i, 9 + i))));
            }
            flStr.append(String.valueOf(fl));
            return flStr.toString();
        }
        else {
            int maxIndex = Integer.parseInt(binaryToAbsInt(binStr.substring(1, 9), 8));
            fl += (float) Math.pow(2, maxIndex - 127);
            for (int i = 1; i <= 23; i++) {
                fl += (float) Math.pow(2, maxIndex - 127 - i) *  (Integer.parseInt(binStr.substring(8 + i, 9 + i)));

            }
            flStr.append(String.valueOf(fl));
            return flStr.toString();
        }
        // TODO:
    }

    public static void main(String[] args) {
        System.out.println(floatToBinary("1.0"));
        System.out.println(floatToBinary(Float.toString(1.5e30F)));
        System.out.println(1.5e30F + 1.0);
        System.out.println(floatToBinary(Float.toString((1.5e30F + 1.0F))));
        float f = 1.23456789e10F;
        System.out.println(123456789 == (int)(float)1234567890 );
    }
}


