package memory.cache.cacheReplacementStrategy;

import memory.cache.Cache;

/**
 * TODO 最近最少用算法
 */
public class LRUReplacement implements ReplacementStrategy {

    @Override
    public void hit(int rowNO) {
        int setSize = Cache.getCache().getSetSize();
        int setNO = rowNO / setSize;
        for (int i = setNO * setSize; i < setNO * setSize + setSize; i++) {
            if (i != rowNO)
                Cache.getCache().setTimeStamp(i);
            else
                Cache.getCache().setHitTimeStamp(rowNO);

        } // 命中，改变数据，重置被访问行的时间戳，其他行的时间戳+1
        //TODO
    }

    @Override
    public int replace(int start, int end, char[] addrTag, byte[] input) {
        Long LongestUse = 0L;
        int LongestUseIndex = 0;
        for (int i = start; i <= end; i++) {
            if (Cache.getCache().getTimeStamp(i) >= LongestUse) {
                LongestUse = Cache.getCache().getTimeStamp(i);
                LongestUseIndex = i;
            }
        } // 找到时间戳最大的CacheLine
        Cache.getCache().update(LongestUseIndex, addrTag, input); // 将数据写入该CacheLine
        //TODO
        return LongestUseIndex;
    }

}





























