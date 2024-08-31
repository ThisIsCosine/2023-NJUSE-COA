package cpu.controller;

import cpu.alu.ALU;
import memory.Memory;
import util.DataType;
import util.Transformer;

import java.util.Arrays;


public class Controller {
    // general purpose register
    char[][] GPR = new char[32][32];
    // program counter
    char[] PC = new char[32];
    // instruction register
    char[] IR = new char[32];
    // memory address register
    char[] MAR = new char[32];
    // memory buffer register
    char[] MBR =  new char[32];
    char[] ICC = new char[2];

    // 单例模式
    private static final Controller controller = new Controller();

    private Controller(){
        //规定第0个寄存器为zero寄存器
        GPR[0] = new char[]{'0','0','0','0','0','0','0','0',
                '0','0','0','0','0','0','0','0',
                '0','0','0','0','0','0','0','0',
                '0','0','0','0','0','0','0','0'};
        ICC = new char[]{'0','0'}; // ICC初始化为00
    }

    public static Controller getController(){
        return controller;
    }

    public void reset(){
        PC = new char[32];
        IR = new char[32];
        MAR = new char[32];
        GPR[0] = new char[]{'0','0','0','0','0','0','0','0',
                '0','0','0','0','0','0','0','0',
                '0','0','0','0','0','0','0','0',
                '0','0','0','0','0','0','0','0'};
        ICC = new char[]{'0','0'}; // ICC初始化为00
        interruptController.reset();
    }

    public InterruptController interruptController = new InterruptController();
    public ALU alu = new ALU();

    public void tick(){
        if (Arrays.equals(ICC, new char[]{'0', '0'})) {
            getInstruct();
            ICC[0] = '1';
            ICC[1] = '0';
        } else if (Arrays.equals(ICC, new char[]{'0', '1'})) {
            findOperand();
            ICC[0] = '1';
            ICC[1] = '0';
        } else if (Arrays.equals(ICC, new char[]{'1', '0'})) {
            operate();
            ICC[0] = '0';
            ICC[1] = '0';
        } else {
            interrupt();
            ICC[0] = '0';
            ICC[1] = '0';
        }
        // TODO
    }

    /** 执行取指操作 */
    private void getInstruct(){
        GPR[0] = "00000000000000000000000000000000".toCharArray();
        System.arraycopy(PC, 0, MAR, 0, 32); //Load MAR
        System.arraycopy(new ALU().add(new DataType(new String(PC)), new DataType(Transformer.intToBinary("4"))).toString().toCharArray(), 0, PC, 0, 32);
        Memory memory = Memory.getMemory();
        byte[] insByte = memory.read(new String(MAR), 4);
        for (int i = 0; i < 4; i++) {
            System.arraycopy(Transformer.intToBinary(Integer.toString((int) insByte[i])).substring(24, 32).toCharArray(), 0, MBR, i * 8, 8);
        }
        System.arraycopy(MBR, 0, IR, 0, 32);
        // TODO
    }

    /** 执行间址操作 */
    private void findOperand(){
        Memory memory = Memory.getMemory();
        System.arraycopy(GPR[Integer.parseInt(Transformer.binaryToInt(new String(IR).substring(20, 25)))], 0, MAR, 0, 32);
        byte[] insByte = memory.read(new String(MAR), 4);
        for (int i = 0; i < 4; i++) {
            System.arraycopy(Transformer.intToBinary(Integer.toString((int) insByte[i])).substring(24, 32).toCharArray(), 0, GPR[Integer.parseInt(Transformer.binaryToInt(new String(IR).substring(20, 25)))], i * 8, 8);
        }
        // TODO
    }

    /** 执行周期 */
    private void operate(){
        Memory memory = Memory.getMemory();
        switch (new String(IR).substring(0, 7)) {
            case "1100110" : // add
                String addScr1 = new String(GPR[Integer.parseInt(Transformer.binaryToInt(new String(IR).substring(15, 20)))]);
                String addScr2 = new String(GPR[Integer.parseInt(Transformer.binaryToInt(new String(IR).substring(20, 25)))]);
                String result = new ALU().add(new DataType(addScr1), new DataType(addScr2)).toString();
                int desIndex = Integer.parseInt(Transformer.binaryToInt(new String(IR).substring(7, 12)));
                System.arraycopy(result.toCharArray(), 0, GPR[desIndex], 0, 32);
                break;
            case "1101110" : // addc
                findOperand();
                String loadSrc2 = new String(GPR[Integer.parseInt(Transformer.binaryToInt(new String(IR).substring(20, 25)))]);
                String result2 = new ALU().add(new DataType(new String(GPR[Integer.parseInt(Transformer.binaryToInt(new String(IR).substring(25, 30)))])), new DataType(loadSrc2)).toString();
                int desIndex2 = Integer.parseInt(Transformer.binaryToInt(new String(IR).substring(7, 12)));
                System.arraycopy(result2.toCharArray(), 0, GPR[desIndex2], 0, 32);
                break;
            case "1100100": // addi
                int desIndex3 = Integer.parseInt(Transformer.binaryToInt(new String(IR).substring(7, 12)));
                String addScr3 = new String(GPR[Integer.parseInt(Transformer.binaryToInt(new String(IR).substring(15, 20)))]);
                String addScr4 = "00000000000000000000" + new String(IR).substring(20, 32);
                String result3 = new ALU().add(new DataType(addScr3), new DataType(addScr4)).toString();
                System.arraycopy(result3.toCharArray(), 0, GPR[desIndex3], 0, 32);
                break;

            case "1110110" : // lui
                int luiDesIndex = Integer.parseInt(Transformer.binaryToInt(new String(IR).substring(7, 12)));
                String loadSrc = new String(IR).substring(12, 32) + "000000000000";
                System.arraycopy(loadSrc.toCharArray(), 0, GPR[luiDesIndex], 0, 32);
                break;
            case "1100000": // lw
                int lwDesIndex = Integer.parseInt(Transformer.binaryToInt(new String(IR).substring(7, 12)));
                String lwSrcMA = new String(GPR[Integer.parseInt(Transformer.binaryToInt(new String(IR).substring(15, 20)))]);
                byte[] insByte = memory.read(lwSrcMA, 4);
                for (int i = 0; i < 4; i++) {
                    String perByte = Transformer.intToBinary(Integer.toString((int) insByte[i])).substring(24, 32);
                    for (int j = 0; j < 8; j++) {
                        GPR[lwDesIndex][i * 8 + j] = perByte.charAt(j);
                    }
                }
                break;
            case "1110011": // jarl
                System.arraycopy(PC, 0, GPR[Integer.parseInt(Transformer.binaryToInt(new String(IR).substring(7, 12)))], 0, 32);
                System.arraycopy(GPR[Integer.parseInt(Transformer.binaryToInt(new String(IR).substring(15, 20)))], 0, PC, 0, 32);
                break;
            case "1100111": // ecall
                System.arraycopy(PC, 0, GPR[1], 0, 32);
                interrupt();
                System.arraycopy(GPR[1], 0, PC, 0, 32);
                break;
            default:
                System.out.println("unDefinite Institution");
                break;
        }
        // TODO
    }

    /** 执行中断操作 */
    private void interrupt(){
        interruptController.handleInterrupt();
        ICC[0] = '0';
        ICC[1] = '0';
        // TODO
    }

    public class InterruptController{
        // 中断信号：是否发生中断
        public boolean signal;
        public StringBuffer console = new StringBuffer();
        /** 处理中断 */
        public void handleInterrupt(){
            console.append("ecall ");
        }
        public void reset(){
            signal = false;
            console = new StringBuffer();
        }
    }

    // 以下一系列的get方法用于检查寄存器中的内容进行测试，请勿修改

    // 假定代码程序存储在主存起始位置，忽略系统程序空间
    public void loadPC(){
        PC = GPR[0];
    }

    public char[] getRA() {
        //规定第1个寄存器为返回地址寄存器
        return GPR[1];
    }

    public char[] getGPR(int i) {
        return GPR[i];
    }
}
