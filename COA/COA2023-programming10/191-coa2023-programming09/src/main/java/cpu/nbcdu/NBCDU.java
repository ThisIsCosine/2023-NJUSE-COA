package cpu.nbcdu;

import util.DataType;
import util.Transformer;

public class NBCDU {

    /**
     * @param src  A 32-bits NBCD String
     * @param dest A 32-bits NBCD String
     * @return dest + src
     */
    DataType add(DataType src, DataType dest) {
        if (src.toString().startsWith("1100") && dest.toString().startsWith("1101")){
            StringBuilder stringBuilder = new StringBuilder("1100" + dest.toString().substring(4, 32));
            return sub(new DataType(stringBuilder.toString()), src);
        }
        if (dest.toString().startsWith("1100") && src.toString().startsWith("1101")){
            StringBuilder stringBuilder = new StringBuilder("1100" + src.toString().substring(4, 32));
            return sub(new DataType(stringBuilder.toString()), dest);
        }
        StringBuilder result = new StringBuilder();
        int c = 0;
        for (int i = 7; i >= 1; i--) {
            for (int j = 3; j >= 0; j--) {
                int x = Integer.parseInt(src.toString().substring(i * 4 + j, i * 4 + j + 1));
                int y = Integer.parseInt(dest.toString().substring(i * 4 + j, i * 4 + j + 1));
                result.insert(0, x ^ y ^ c);
                c = (x & y) | (y & c) | (c & x);
            }
            if (c == 1 || Integer.parseInt(Transformer.binaryToInt(result.substring(0, 4))) > 9) {
                String tool = "0110";
                c = 1;
                int C = 0;
                StringBuilder tmp = new StringBuilder();
                for (int j = 3; j >= 0; j--) {
                    int x = Integer.parseInt(result.substring(j, j + 1));
                    int y = Integer.parseInt(tool.substring(j, j + 1));
                    tmp.insert(0, x ^ y ^ C);
                    C = (x & y) | (y & C) | (C & x);
                }
                result.delete(0, 4);
                result.insert(0, tmp);
            }
        }
        result.insert(0, src.toString().substring(0, 4));
        // TODO
        return new DataType(result.toString());
    }

    /***
     *
     * @param src A 32-bits NBCD String
     * @param dest A 32-bits NBCD String
     * @return dest - src
     */
    DataType sub(DataType src, DataType dest) {
        if (src.toString().equals("11000000000000000000000000000000") || src.toString().equals("11010000000000000000000000000000"))
            return dest;
        if (dest.toString().equals("11000000000000000000000000000000") || dest.toString().equals("11010000000000000000000000000000")){
            if (src.toString().startsWith("1100"))
                return new DataType("1101" + src.toString().substring(4, 32));
            if (src.toString().startsWith("1101"))
                return new DataType("1100" + src.toString().substring(4, 32));
        }
        if (src.toString().startsWith("1100") && dest.toString().startsWith("1101")){
            StringBuilder stringBuilder = new StringBuilder("1101" + src.toString().substring(4, 32));
            return add(new DataType(stringBuilder.toString()), dest);
        }
        if (src.toString().startsWith("1101") && dest.toString().startsWith("1100")){
            StringBuilder stringBuilder = new StringBuilder("1100" + src.toString().substring(4, 32));
            return add(new DataType(stringBuilder.toString()), dest);
        }
        if (src.toString().equals(dest.toString())) {
            if (src.toString().startsWith("1100"))
                return new DataType("11000000000000000000000000000000");
            if (src.toString().startsWith("1101"))
                return new DataType("11010000000000000000000000000000");
        }
        StringBuilder srcNew = new StringBuilder();
        for (int i = 7; i >= 1; i--) {
            String tool = "0110";
            int C = 0;
            StringBuilder tmp = new StringBuilder();
            for (int j = 3; j >= 0; j--) {
                int x = Integer.parseInt(src.toString().substring(i * 4 + j, i * 4 + j + 1));
                int y = Integer.parseInt(tool.substring(j, j + 1));
                tmp.insert(0, 1 - x ^ y ^ C);
                C = (x & y) | (y & C) | (C & x);
            }
            srcNew.insert(0, tmp);
        }
        srcNew = new StringBuilder(add(new DataType("1100" + srcNew.toString()), new DataType("11000000000000000000000000000001")).toString().substring(4, 32));
        StringBuilder result = new StringBuilder();
        int c = 0;
        for (int i = 7; i >= 1; i--) {
            for (int j = 3; j >= 0; j--) {
                int x = Integer.parseInt(srcNew.substring((i - 1) * 4 + j, (i - 1) * 4 + j + 1));
                int y = Integer.parseInt(dest.toString().substring(i * 4 + j, i * 4 + j + 1));
                result.insert(0, x ^ y ^ c);
                c = (x & y) | (y & c) | (c & x);
            }
            if (c == 1 || Integer.parseInt(Transformer.binaryToInt(result.substring(0, 4))) > 9) {
                String tool = "0110";
                c = 1;
                int C = 0;
                StringBuilder tmp = new StringBuilder();
                for (int j = 3; j >= 0; j--) {
                    int x = Integer.parseInt(result.substring(j, j + 1));
                    int y = Integer.parseInt(tool.substring(j, j + 1));
                    tmp.insert(0, x ^ y ^ C);
                    C = (x & y) | (y & C) | (C & x);
                }
                result.delete(0, 4);
                result.insert(0, tmp);
            }
        }
        StringBuilder resultNew = new StringBuilder();
        if (c == 0){
            for (int i = 6; i >= 0; i--) {
                String tool = "0110";
                int C = 0;
                StringBuilder tmp = new StringBuilder();
                for (int j = 3; j >= 0; j--) {
                    int x = Integer.parseInt(result.substring(i * 4 + j, i * 4 + j + 1));
                    int y = Integer.parseInt(tool.substring(j, j + 1));
                    tmp.insert(0, 1 - x ^ y ^ C);
                    C = (x & y) | (y & C) | (C & x);
                }
                resultNew.insert(0, tmp);
            }
            if (dest.toString().startsWith("1100")) {
                resultNew.insert(0, "1101");
                return add(new DataType(resultNew.toString()), new DataType("11010000000000000000000000000001"));
            }
            else {
                resultNew.insert(0, "1100");
                return add(new DataType(resultNew.toString()), new DataType("11000000000000000000000000000001"));
            }
        }
        result.insert(0, dest.toString().substring(0, 4));
        return new DataType(result.toString());

        // TODO

    }

}
