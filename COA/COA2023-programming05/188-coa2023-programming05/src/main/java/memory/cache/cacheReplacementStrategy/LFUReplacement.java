package memory.cache.cacheReplacementStrategy;


import memory.cache.Cache;
import memory.Memory;

/**
 * TODO 最近不经常使用算法
 */
public class LFUReplacement implements ReplacementStrategy {

    @Override
    public void hit(int rowNO) {
        Cache.getCache().addVisited(rowNO); // rowNO行访问次数 +1
        //TODO
    }

    @Override
    public int replace(int start, int end, char[] addrTag, byte[] input) {
        int minVisited = Cache.getCache().getVisited(start);
        int minVisitedIndex = start;
        for (int i = start; i <= end; i++) {
            if (Cache.getCache().getVisited(i) <= minVisited) {
                minVisited = Cache.getCache().getVisited(i);
                minVisitedIndex = i;
            }
        } // 寻找访问次数最少的CacheLine
        Cache.getCache().update(minVisitedIndex, addrTag, input);
        //TODO
        return minVisitedIndex;
    }

}
