package memory.cache.cacheReplacementStrategy;


import memory.cache.Cache;
import memory.Memory;
import util.Transformer;

/**
 * TODO 先进先出算法
 */
public class FIFOReplacement implements ReplacementStrategy {

    @Override
    public void hit(int rowNO) {
        //TODO
    }

    @Override
    public int replace(int start, int end, char[] addrTag, byte[] input) {
        for (int i = start; i <= end ; i++) {
            if (Cache.getCache().getTimeStamp(i) == 0) {
                Cache.getCache().update(i, addrTag, input);
                return i;
            }
        }
        //TODO
        return -1;

    }

}
