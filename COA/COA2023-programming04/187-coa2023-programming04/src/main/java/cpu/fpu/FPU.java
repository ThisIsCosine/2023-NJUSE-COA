package cpu.fpu;

import cpu.alu.ALU;
import util.DataType;
import util.IEEE754Float;
import util.Transformer;

/**
 * floating point unit
 * 执行浮点运算的抽象单元
 * 浮点数精度：使用3位保护位进行计算
 */
public class FPU {

    private final String[][] addCorner = new String[][]{
            {IEEE754Float.P_ZERO, IEEE754Float.P_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.N_ZERO, IEEE754Float.P_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.P_ZERO, IEEE754Float.N_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.N_ZERO, IEEE754Float.N_ZERO, IEEE754Float.N_ZERO},
            {IEEE754Float.P_INF, IEEE754Float.N_INF, IEEE754Float.NaN},
            {IEEE754Float.N_INF, IEEE754Float.P_INF, IEEE754Float.NaN}
    };

    private final String[][] subCorner = new String[][]{
            {IEEE754Float.P_ZERO, IEEE754Float.P_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.N_ZERO, IEEE754Float.P_ZERO, IEEE754Float.N_ZERO},
            {IEEE754Float.P_ZERO, IEEE754Float.N_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.N_ZERO, IEEE754Float.N_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.P_INF, IEEE754Float.P_INF, IEEE754Float.NaN},
            {IEEE754Float.N_INF, IEEE754Float.N_INF, IEEE754Float.NaN}
    };

    private final String[][] mulCorner = new String[][]{
            {IEEE754Float.P_ZERO, IEEE754Float.N_ZERO, IEEE754Float.N_ZERO},
            {IEEE754Float.N_ZERO, IEEE754Float.P_ZERO, IEEE754Float.N_ZERO},
            {IEEE754Float.P_ZERO, IEEE754Float.P_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.N_ZERO, IEEE754Float.N_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.P_ZERO, IEEE754Float.P_INF, IEEE754Float.NaN},
            {IEEE754Float.P_ZERO, IEEE754Float.N_INF, IEEE754Float.NaN},
            {IEEE754Float.N_ZERO, IEEE754Float.P_INF, IEEE754Float.NaN},
            {IEEE754Float.N_ZERO, IEEE754Float.N_INF, IEEE754Float.NaN},
            {IEEE754Float.P_INF, IEEE754Float.P_ZERO, IEEE754Float.NaN},
            {IEEE754Float.P_INF, IEEE754Float.N_ZERO, IEEE754Float.NaN},
            {IEEE754Float.N_INF, IEEE754Float.P_ZERO, IEEE754Float.NaN},
            {IEEE754Float.N_INF, IEEE754Float.N_ZERO, IEEE754Float.NaN}
    };

    private final String[][] divCorner = new String[][]{
            {IEEE754Float.P_ZERO, IEEE754Float.P_ZERO, IEEE754Float.NaN},
            {IEEE754Float.N_ZERO, IEEE754Float.N_ZERO, IEEE754Float.NaN},
            {IEEE754Float.P_ZERO, IEEE754Float.N_ZERO, IEEE754Float.NaN},
            {IEEE754Float.N_ZERO, IEEE754Float.P_ZERO, IEEE754Float.NaN},
            {IEEE754Float.P_INF, IEEE754Float.P_INF, IEEE754Float.NaN},
            {IEEE754Float.N_INF, IEEE754Float.N_INF, IEEE754Float.NaN},
            {IEEE754Float.P_INF, IEEE754Float.N_INF, IEEE754Float.NaN},
            {IEEE754Float.N_INF, IEEE754Float.P_INF, IEEE754Float.NaN},
    };

    /**
     * compute the float add of (dest + src)
     */

    /**
     *
     * @param a 输入浮点数字符串
     * @return 尾数（需要加上前置位和保护位，前置位需要考虑规格化数和非规格化数，共32位）
     */
    private String getFrac (String a) {
        StringBuilder fracA = new StringBuilder(a.substring(9, 32));
        fracA.append("000");
        if (!a.startsWith("00000000", 1))
            fracA.insert(0, "000001");
        else
            fracA.insert(0, "000000");
        return fracA.toString();
    }
    public DataType add(DataType src, DataType dest) {

        //检查边界情况
        String a = dest.toString();
        String b = src.toString();
        if (a.matches(IEEE754Float.NaN_Regular) || b.matches(IEEE754Float.NaN_Regular)) {
            return new DataType(IEEE754Float.NaN);
        }
        if (cornerCheck(addCorner, a, b) != null)
            return new DataType(cornerCheck(addCorner, a, b));
        if (src.toString().equals("00000000000000000000000000000000") || src.toString().equals("10000000000000000000000000000000"))
            return dest;
        if (dest.toString().equals("00000000000000000000000000000000") ||dest.toString().equals("10000000000000000000000000000000"))
            return src;
        if (dest.toString().substring(1, 32).equals(src.toString().substring(1, 32)) && dest.toString().charAt(0) !=src.toString().charAt(0))
            return new DataType("00000000000000000000000000000000");

        //提取符号，阶码和尾数
        char signA = a.charAt(0);
        char sighB = b.charAt(0);

        StringBuilder expASB = new StringBuilder(a.substring(1, 9)); // 阶码是1到9位
        StringBuilder expBSB = new StringBuilder(b.substring(1, 9));
        if (expASB.toString().equals("11111111"))
            return dest;
        if (expBSB.toString().equals("11111111"))
            return src; // 如果加数其中之一是Inf
        if (expASB.toString().equals("00000000")) {
            expASB.deleteCharAt(7);
            expASB.append("1");
        }
        if (expBSB.toString().equals("00000000")) {
            expBSB.deleteCharAt(7);
            expBSB.append("1");
        } // 如果加数是非规格化数，将加数尾数首位为0,（在getFrac中体现），将阶码设为1（为了便于减127）
        expASB.insert(0,"000000000000000000000000");
        expBSB.insert(0, "000000000000000000000000"); // 添加前置24位0补全为32位

        String fracA = getFrac(a);
        String fracB = getFrac(b); // 考虑规格化数和非规格化数提取尾数加上保护位（共32位）

        //対阶，小数向大数对阶
        String expDif = new ALU().sub(new DataType(expBSB.toString()), new DataType(expASB.toString())).toString();
        if (expDif.startsWith("0")) {
            fracB = rightShift(fracB, Integer.parseInt(Transformer.binaryToInt(expDif)));
            expBSB = expASB;
        }// A阶码更大，B对阶，尾数右移，最终规格化结果时需要用到
        else {
            String expDif1 = new ALU().sub(new DataType(expDif), new DataType("00000000000000000000000000000000")).toString();
            fracA = rightShift(fracA, Integer.parseInt(Transformer.binaryToInt(expDif1)));
            expASB = expBSB;
        }// B阶码更大，A对阶，尾数右移，最终规格化结果时需要用到
        //尾数运算,原码加法
        char fracResultSign;
        String fracResult;
        if (signA == sighB) {
            fracResult = new ALU().add(new DataType(fracA), new DataType(fracB)).toString();
            fracResultSign = signA;
        } // A，B同号，两者直接加
        else {
            String posFracResult = new ALU().sub(new DataType(fracB), new DataType(fracA)).toString();
            if (posFracResult.charAt(0) == '0') {
                fracResult = "00000" + posFracResult.substring(5, 32);
                fracResultSign = signA;
            } // 有进位，正确 (符号与被减数A相同)，注意这里的A是被减数，也就是des，不要弄反
            else {
                fracResult = new ALU().sub(new DataType(posFracResult), new DataType("00000000000000000000000000000000")).toString();
                fracResultSign = sighB;
            } // 没有进位，计算它的补码 (符号与被减数A相反)
        } // A，B异号。两者相减（加B的补数）

        //规格化
        String ruledExpResult;
        StringBuilder ruledFracResultSB = new StringBuilder(fracResult.substring(4, 32)); // 一开始提取尾数前面加了5位0，现在因为可能有进位（溢出），所以只舍去前面4位0
        if (ruledFracResultSB.charAt(0) == '1') /* 有进位 */ {
            ruledExpResult = new ALU().add(new DataType("00000000000000000000000000000001"), new DataType(expASB.toString())).toString().substring(24, 32); // result阶码需要加1
            if (expASB.substring(24, 32).equals("11111111"))
                return new DataType(fracResultSign == '0' ? IEEE754Float.P_INF : IEEE754Float.N_INF); // 加1如果阶码上溢，返回无穷
        }
        else {
            ruledFracResultSB.deleteCharAt(0);
            while (ruledFracResultSB.charAt(0) == '0') /* 尾数结果是第一位是0 */{
                if (expASB.substring(24, 32).equals("00000001")) {
                    expASB.replace(24, 32, "00000000");
                    break; // 非规格化数不进行左移，只阶码变为全0
                }
                ruledFracResultSB.deleteCharAt(0);
                ruledFracResultSB.append("0");
                expASB.replace(24, 32, new ALU().sub(new DataType(Transformer.intToBinary("1")), new DataType(new StringBuilder("000000000000000000000000" + expASB.substring(24, 32)).toString())).toString().substring(24, 32));
            }
            ruledExpResult = expASB.substring(24, 32);
            // 规格化数尾数需要不断左移，阶码减1，直至成为非规格化数或者首位为1
        }
        //舍入得出结果
            return new DataType(round(fracResultSign, ruledExpResult, ruledFracResultSB.substring(0, 27))); // 舍入函数





        //TODO
    }

    /**
     * compute the float add of (dest - src)
     */
    public DataType sub(DataType src1, DataType dest) /*改变减数符号做加法即可*/ {
        StringBuilder stringBuilder = new StringBuilder(src1.toString().substring(1, 32));
        if (src1.toString().charAt(0) == '0')
            stringBuilder.insert(0, '1');
        else
            stringBuilder.insert(0, '0');
        DataType src = new DataType(stringBuilder.toString());
        String a = dest.toString();
        String b = src.toString();
        if (a.matches(IEEE754Float.NaN_Regular) || b.matches(IEEE754Float.NaN_Regular)) {
            return new DataType(IEEE754Float.NaN);
        }
        if (cornerCheck(subCorner, a, b) != null)
            return new DataType(cornerCheck(subCorner, a, b));
        if (src.toString().equals("00000000000000000000000000000000") || src.toString().equals("10000000000000000000000000000000"))
            return dest;
        if (dest.toString().equals("00000000000000000000000000000000") ||dest.toString().equals("10000000000000000000000000000000"))
            return src;
        if (dest.toString().substring(1, 32).equals(src.toString().substring(1, 32)) && dest.toString().charAt(0) !=src.toString().charAt(0))
            return new DataType("00000000000000000000000000000000");

        //提取符号，阶码和尾数
        char signA = a.charAt(0);
        char sighB = b.charAt(0);

        StringBuilder expASB = new StringBuilder(a.substring(1, 9));
        StringBuilder expBSB = new StringBuilder(b.substring(1, 9));
        if (expASB.toString().equals("11111111"))
            return dest;
        if (expBSB.toString().equals("11111111"))
            return src;
        if (expASB.toString().equals("00000000")) {
            expASB.deleteCharAt(7);
            expASB.append("1");
        }
        if (expBSB.toString().equals("00000000")) {
            expBSB.deleteCharAt(7);
            expBSB.append("1");
        }
        expASB.insert(0,"000000000000000000000000");
        expBSB.insert(0, "000000000000000000000000");

        String fracA = getFrac(a);
        String fracB = getFrac(b);

        //対阶
        String expDif = new ALU().sub(new DataType(expBSB.toString()), new DataType(expASB.toString())).toString();
        if (expDif.startsWith("0"))
            fracB = rightShift(fracB, Integer.parseInt(Transformer.binaryToInt(expDif)));
        else {
            String expDif1 = new ALU().sub(new DataType(expDif), new DataType("00000000000000000000000000000000")).toString();
            fracA = rightShift(fracA, Integer.parseInt(Transformer.binaryToInt(expDif1)));
            expASB = expBSB;
        }
        //尾数运算
        char fracResultSign;
        String fracResult;
        if (signA == sighB) {
            fracResult = new ALU().add(new DataType(fracA), new DataType(fracB)).toString();
            fracResultSign = signA;
        }
        else {
            DataType minusFracB = new ALU().sub(new DataType(fracB), new DataType("00000000000000000000000000000000"));
            String posFracResult = new ALU().add(new DataType(fracA), minusFracB).toString();
            if (posFracResult.charAt(0) == '0') {
                fracResult = "00000" + posFracResult.substring(5, 32);
                fracResultSign = signA;
            }
            else {
                fracResult = new ALU().sub(new DataType(posFracResult), new DataType("00000000000000000000000000000000")).toString();
                fracResultSign = sighB;
            }
        }

        //规格化
        String ruledExpResult;
        StringBuilder ruledFracResultSB = new StringBuilder(fracResult.substring(4, 32));
        if (ruledFracResultSB.charAt(0) == '1') {
            if (expASB.substring(24, 32).equals("11111111"))
                return new DataType(fracResultSign == '0' ? IEEE754Float.P_INF : IEEE754Float.N_INF);
            ruledExpResult = new ALU().add(new DataType("00000000000000000000000000000001"), new DataType(expASB.toString())).toString().substring(24, 32);
            if (expASB.substring(24, 32).equals("11111111"))
                return new DataType(fracResultSign == '0' ? IEEE754Float.P_INF : IEEE754Float.N_INF);
        }
        else {
            ruledFracResultSB.deleteCharAt(0);
            while (ruledFracResultSB.charAt(0) == '0') {
                if (expASB.substring(24, 32).equals("00000001")) {
                    expASB.replace(24, 32, "00000000");
                    break;
                }
                ruledFracResultSB.deleteCharAt(0);
                ruledFracResultSB.append("0");
                expASB.replace(24, 32, new ALU().sub(new DataType(Transformer.intToBinary("1")), new DataType(new StringBuilder("000000000000000000000000" + expASB.substring(24, 32)).toString())).toString().substring(24, 32));
            }
            ruledExpResult = expASB.substring(24, 32);
        }
        //舍入得出结果
        return new DataType(round(fracResultSign, ruledExpResult, ruledFracResultSB.substring(0, 27)));
        //TODO
    }

    /**
     * compute the float mul of (dest * src)
     */
    public DataType mul(DataType src,DataType dest){
        //检查边界情况
        String a = dest.toString();
        String b = src.toString();
        if (a.matches(IEEE754Float.NaN_Regular) || b.matches(IEEE754Float.NaN_Regular)) {
            return new DataType(IEEE754Float.NaN);
        }
        if (cornerCheck(mulCorner, dest.toString(), src.toString()) != null)
            return new DataType(cornerCheck(mulCorner, a, b));
        if (src.toString().equals(IEEE754Float.P_INF) || dest.toString().equals(IEEE754Float.P_INF))
            return src.toString().charAt(0) == dest.toString().charAt(0) ? new DataType(IEEE754Float.P_INF) : new DataType(IEEE754Float.N_INF);
        if (src.toString().equals(IEEE754Float.N_INF) || dest.toString().equals(IEEE754Float.N_INF))
            return src.toString().charAt(0) == dest.toString().charAt(0) ? new DataType(IEEE754Float.P_INF) : new DataType(IEEE754Float.N_INF);
        if (src.toString().equals("00000000000000000000000000000000") || src.toString().equals("10000000000000000000000000000000"))
            return src.toString().charAt(0) == dest.toString().charAt(0) ? new DataType("00000000000000000000000000000000") : new DataType("10000000000000000000000000000000");
        if (dest.toString().equals("00000000000000000000000000000000") ||dest.toString().equals("10000000000000000000000000000000"))
            return src.toString().charAt(0) == dest.toString().charAt(0) ? new DataType("00000000000000000000000000000000") : new DataType("10000000000000000000000000000000");

        //提取符号，阶码和尾数
        char signA = a.charAt(0);
        char sighB = b.charAt(0);

        StringBuilder expASB = new StringBuilder(a.substring(1, 9));
        StringBuilder expBSB = new StringBuilder(b.substring(1, 9));
        if (expASB.toString().equals("11111111"))
            return dest;
        if (expBSB.toString().equals("11111111"))
            return src;
        if (expASB.toString().equals("00000000")) {
            expASB.deleteCharAt(7);
            expASB.append("1");
        }
        if (expBSB.toString().equals("00000000")) {
            expBSB.deleteCharAt(7);
            expBSB.append("1");
        }
        expASB.insert(0, "000000000000000000000000");
        expBSB.insert(0, "000000000000000000000000");

        String fracA = getFrac(a);
        String fracB = getFrac(b);

        //模拟操作
        //符号
        char signResult = src.toString().charAt(0) == dest.toString().charAt(0) ? '0' : '1';
        //阶码运算
        DataType expResultWithBias32 = new ALU().add(new DataType(expASB.toString()), new DataType(expBSB.toString()));
        String expResult32 = new ALU().sub(new DataType("00000000000000000000000001111111"), expResultWithBias32).toString();// 减去一个偏移量


        //尾数乘法，27位原码运算 23尾数 + 3保护位 + 1前置位，这是正数乘法，不需要布斯算法
        StringBuilder fracResultSB = new StringBuilder("0000000000000000000000000000" + fracB.substring(5, 32)); // 多加一位防止进位
        for (int i = 0; i < 27; i++) {
            if (fracResultSB.charAt(54) == '1') {
                String mulAdd = new ALU().add(new DataType("00000" + fracResultSB.substring(1, 28)), new DataType("00000" + fracA.substring(5, 32))).toString();
                fracResultSB.replace(0, 28, mulAdd.substring(4, 32));
            }
            fracResultSB.deleteCharAt(54);
            fracResultSB.insert(0, "0");
        }
        expResult32 = new ALU().add(new DataType("00000000000000000000000000000001"), new DataType(expResult32)).toString();
        // 由于两个操作数的隐藏位均为1位，所以乘积的隐藏位为2位。为了方便后续操作，需要通过阶码加1的方式来间接实现小数点的左移，修正这个误差，以保证尾数的隐藏位均为1位。
        fracResultSB.deleteCharAt(0); // 去掉为了进位而添加的0
        //规格化
        /* while (隐藏位 == 0 && 阶码 > 0) {
            尾数左移，阶码减1; // 左规
        } */

        while (fracResultSB.charAt(0) == '0' && expResult32.startsWith("0") && !expResult32.equals("00000000000000000000000000000000")) {
            fracResultSB.append("0");
            fracResultSB.deleteCharAt(0);
            expResult32 = new ALU().sub(new DataType("00000000000000000000000000000001"), new DataType(expResult32)).toString();
        }
        /* while (尾数前27位不全为0 && 阶码 < 0) {
            尾数右移，阶码加1; // 右规
        } */
        while (!fracResultSB.toString().startsWith("0000000000000000000000000000") && expResult32.startsWith("1")) {
            fracResultSB = new StringBuilder(rightShift(fracResultSB.toString(), 1));
            expResult32 = new ALU().add(new DataType("00000000000000000000000000000001"), new DataType(expResult32)).toString();
        }
        /* if (阶码上溢) {
            将结果置为无穷;
        } else if (阶码下溢) {
            将结果置为0;
        } else if(阶码 == 0) {
            尾数右移一次化为非规格化数;
        } else {
            此时阶码正常，无需任何操作;
        } */
        if (Integer.parseInt(Transformer.binaryToInt(expResult32)) > 255)
            return new DataType(signResult == '0' ? IEEE754Float.P_INF : IEEE754Float.N_INF);
          else if (expResult32.startsWith("1")) {
            return new DataType(signResult == '0' ? IEEE754Float.P_ZERO : IEEE754Float.N_ZERO);
        } else if (expResult32.startsWith("00000000", 24)) {
            fracResultSB.deleteCharAt(53);
            fracResultSB.insert(0, "0");
        }
          return new DataType(round(signResult, expResult32.substring(24, 32), fracResultSB.substring(0, 54)));

        //TODO
    }

    /**
     * compute the float mul of (dest / src)
     */
    public DataType div(DataType src,DataType dest){

        String a = dest.toString();
        String b = src.toString();
        if (cornerCheck(divCorner, dest.toString(), src.toString()) != null)
            return new DataType(cornerCheck(divCorner, a, b));
        if (b.equals(IEEE754Float.N_ZERO) || b.equals(IEEE754Float.P_ZERO))
            throw new ArithmeticException();
        if (a.equals(IEEE754Float.N_ZERO) || a.equals(IEEE754Float.P_ZERO))
            return new DataType(a.charAt(0) == b.charAt(0) ? IEEE754Float.P_ZERO : IEEE754Float.N_ZERO);


        //提取符号，阶码和尾数
        char signA = a.charAt(0);
        char sighB = b.charAt(0);

        StringBuilder expASB = new StringBuilder(a.substring(1, 9));
        StringBuilder expBSB = new StringBuilder(b.substring(1, 9));
        if (expASB.toString().equals("11111111"))
            return dest;
        if (expBSB.toString().equals("11111111"))
            return src;
        if (expASB.toString().equals("00000000")) {
            expASB.deleteCharAt(7);
            expASB.append("1");
        }
        if (expBSB.toString().equals("00000000")) {
            expBSB.deleteCharAt(7);
            expBSB.append("1");
        }
        expASB.insert(0, "000000000000000000000000");
        expBSB.insert(0, "000000000000000000000000");

        String fracA = getFrac(a);
        String fracB = getFrac(b);

        //模拟操作
        //符号
        char signResult = src.toString().charAt(0) == dest.toString().charAt(0) ? '0' : '1';

        //阶码运算
        DataType expResultWithBias32 = new ALU().sub(new DataType(expBSB.toString()), new DataType(expASB.toString()));
        String expResult32 = new ALU().add(new DataType("00000000000000000000000001111111"), expResultWithBias32).toString();

        //尾数运算
        StringBuilder fracResultSB = new StringBuilder();
        for (int i = 0; i < 27; i++) {
            if (Integer.parseInt(Transformer.binaryToInt(fracA)) >= Integer.parseInt(Transformer.binaryToInt(fracB))) {
                fracResultSB.append("1");
                fracA = new ALU().sub(new DataType(fracB), new DataType(fracA)).toString();
            }
            else
                fracResultSB.append("0");
            StringBuilder ab = new StringBuilder(fracA);
            ab.append("0");
            ab.deleteCharAt(0);
            fracA = ab.toString();
        }
        if (fracResultSB.charAt(0) == '0')
            expResult32 = new ALU().sub(new DataType("00000000000000000000000000000001"), new DataType(expResult32)).toString();


        return new DataType(round(signResult, expResult32.substring(24, 32), fracResultSB.toString()));
        //TODO

    }

    /**
     * check corner cases of mul and div
     *
     * @param cornerMatrix corner cases pre-stored
     * @param oprA first operand (String)
     * @param oprB second operand (String)
     * @return the result of the corner case (String)
     */
    private String cornerCheck(String[][] cornerMatrix, String oprA, String oprB) {
        for (String[] matrix : cornerMatrix) {
            if (oprA.equals(matrix[0]) && oprB.equals(matrix[1])) {
                return matrix[2];
            }
        }
        return null;
    }

    /**
     * right shift a num without considering its sign using its string format
     *
     * @param operand to be moved
     * @param n       moving nums of bits
     * @return after moving
     */
    private String rightShift(String operand, int n) {
        StringBuilder result = new StringBuilder(operand);  //保证位数不变
        boolean sticky = false;
        for (int i = 0; i < n; i++) {
            sticky = sticky || result.toString().endsWith("1");
            result.insert(0, "0");
            result.deleteCharAt(result.length() - 1);
        }
        if (sticky) {
            result.replace(operand.length() - 1, operand.length(), "1");
        }
        return result.substring(0, operand.length());
    }

    /**
     * 对GRS保护位进行舍入
     *
     * @param sign    符号位
     * @param exp     阶码
     * @param sig_grs 带隐藏位和保护位的尾数
     * @return 舍入后的结果
     */
    private String round(char sign, String exp, String sig_grs) {
        int grs = Integer.parseInt(sig_grs.substring(24, 27), 2);
        if ((sig_grs.substring(27).contains("1")) && (grs % 2 == 0)) {
            grs++;
        }
        String sig = sig_grs.substring(0, 24); // 隐藏位+23位
        if (grs > 4) {
            sig = oneAdder(sig);
        } else if (grs == 4 && sig.endsWith("1")) {
            sig = oneAdder(sig);
        }

        if (Integer.parseInt(sig.substring(0, sig.length() - 23), 2) > 1) {
            sig = rightShift(sig, 1);
            exp = oneAdder(exp).substring(1);
        }
        if (exp.equals("11111111")) {
            return sign == '0' ? IEEE754Float.P_INF : IEEE754Float.N_INF;
        }

        return sign + exp + sig.substring(sig.length() - 23);
    }

    /**
     * add one to the operand
     *
     * @param operand the operand
     * @return result after adding, the first position means overflow (not equal to the carry to the next)
     *         and the remains means the result
     */
    private String oneAdder(String operand) {
        int len = operand.length();
        StringBuilder temp = new StringBuilder(operand);
        temp.reverse();
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

}
