package util;

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
    /**
     * Integer to BinaryString
     *
     * @param numStr to be converted
     * @return result
     */
    public static String intToBinary(String numStr) {
        int num = Integer.parseInt(numStr);
        if (num >= 0)
            return absIntToBinary(num, 32);
        else {
            long absNum = (long)Math.pow(2, 32) + num;
            return absIntToBinary(absNum, 32);
        }
    }

    /**
     * add one to the operand
     *
     * @param operand the operand
     * @return result after adding, the first position means overflow (not equal to the carray to the next) and the remains means the result
     */
    private static String oneAdder(String operand) {
        int len = operand.length();
        StringBuffer temp = new StringBuffer(operand);
        temp = temp.reverse();
        int[] num = new int[len];
        for (int i = 0; i < len; i++) num[i] = temp.charAt(i) - '0';  //先转化为反转后对应的int数组
        int bit = 0x0;
        int carry = 0x1;
        char[] res = new char[len];
        for (int i = 0; i < len; i++) {
            bit = num[i] ^ carry;
            carry = num[i] & carry;
            res[i] = (char) ('0' + bit);  //显示转化为char
        }
        String result = new StringBuffer(new String(res)).reverse().toString();
        return "" + (result.charAt(0) == operand.charAt(0) ? '0' : '1') + result;  //注意有进位不等于溢出，溢出要另外判断
    }

    /**
     * convert the string's 0 and 1.
     * e.g 00000 to 11111
     *
     * @param operand string to convert (by default, it is 32 bits long)
     * @return string after converting
     */
    private static String negation(String operand) {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < operand.length(); i++) {
            result = operand.charAt(i) == '1' ? result.append("0") : result.append("1");
        }
        return result.toString();
    }

}
