package memory.disk;


import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;


public class Scheduler {

    /**
     * 先来先服务算法
     *
     * @param start   磁头初始位置
     * @param request 请求访问的磁道号
     * @return 平均寻道长度
     */
    public double FCFS(int start, int[] request) {
        int lenSum = 0;
        for (int i : request) {
            lenSum += Math.abs(i - start);
            start = i;
        }
        //TODO
        return (double) lenSum / request.length;
    }

    /**
     * 最短寻道时间优先算法
     *
     * @param start   磁头初始位置
     * @param request 请求访问的磁道号
     * @return 平均寻道长度
     */
    public double SSTF(int start, int[] request) {
        int lenSum = 0;
        ArrayList<Integer> rq = new ArrayList<>();
        for (int i : request)
            rq.add(i);
        rq.add(start);
        rq.sort(Comparator.naturalOrder());
        int index = rq.indexOf(start);
        while (rq.size() > 1) {
            if (index == 0) {
                lenSum += rq.get(1) - rq.get(0);
                rq.remove(0);
            }
            else if (index == rq.size() - 1) {
                lenSum += rq.get(index) - rq.get(index - 1);
                rq.remove(index);
                index -= 1;
            }
            else if ((rq.get(index + 1) - rq.get(index)) > (rq.get(index) - rq.get(index - 1))) {
                lenSum += rq.get(index) - rq.get(index - 1);
                rq.remove(index);
                index = index - 1;
            }
            else if ((rq.get(index + 1) - rq.get(index)) < (rq.get(index) - rq.get(index - 1))) {
                lenSum += rq.get(index + 1) - rq.get(index);
                rq.remove(index);
            }
        }
        //TODO
        return (double) lenSum / request.length;
    }

    /**
     * 扫描算法
     *
     * @param start     磁头初始位置
     * @param request   请求访问的磁道号
     * @param direction 磁头初始移动方向，true表示磁道号增大的方向，false表示磁道号减小的方向
     * @return 平均寻道长度
     */
    public double SCAN(int start, int[] request, boolean direction) {
        int lenSum = 0;
        Arrays.sort(request);
        if (direction) {
            if (start <= request[0])
                lenSum = request[request.length - 1] - start;
            else
                lenSum = (Disk.TRACK_NUM - 1 - start) + (Disk.TRACK_NUM - 1 - request[0]);
        }
        else {
            if (start >= request[request.length - 1] )
                lenSum = start - request[0];
            else
                lenSum = start + request[request.length - 1];
        }
        /*int lenSum = 0;
        ArrayList<Integer> rq = new ArrayList<>();
        for (int i : request)
            rq.add(i);
        rq.add(start);
        rq.sort(Comparator.naturalOrder());
        int index = rq.indexOf(start);
        while (rq.size() > 1) {
            if (direction) {
                while (index < rq.size() - 1) {
                    lenSum += rq.get(index + 1) - rq.get(index);
                    rq.remove(index);
                }
                direction = !direction;
            }
            else {
                while (index > 0) {
                    lenSum += rq.get(index) - rq.get(index - 1);
                    rq.remove(index);
                    index -= 1;
                }
            }
        }*/
        //TODO
        return (double) lenSum / request.length;
    }

    /**
     * C-SCAN算法：默认磁头向磁道号增大方向移动
     *
     * @param start     磁头初始位置
     * @param request   请求访问的磁道号
     * @return 平均寻道长度
     */
    public double CSCAN(int start,int[] request){
        int lenSum = 0;
        ArrayList<Integer> rq = new ArrayList<>();
        for (int i : request)
            rq.add(i);
        rq.add(start);
        rq.sort(Comparator.naturalOrder());
        int index = rq.indexOf(start);
        if (index == 0) {
            lenSum = rq.get(rq.size() - 1) - start;
        }
        else {
            lenSum += (Disk.TRACK_NUM - 1 - start) + Disk.TRACK_NUM - 1 + rq.get(index - 1);
        }
        //TODO
        return (double) lenSum / request.length;
    }

    /**
     * LOOK算法
     *
     * @param start     磁头初始位置
     * @param request   请求访问的磁道号
     * @param direction 磁头初始移动方向，true表示磁道号增大的方向，false表示磁道号减小的方向
     * @return 平均寻道长度
     */
    public double LOOK(int start,int[] request,boolean direction){
        int lenSum = 0;
        ArrayList<Integer> rq = new ArrayList<>();
        for (int i : request)
            rq.add(i);
        rq.add(start);
        rq.sort(Comparator.naturalOrder());
        int index = rq.indexOf(start);
        if (direction && index == 0)
            lenSum = rq.get(rq.size() - 1) - start;
        else if (direction && index == rq.size() - 1)
            lenSum = start - rq.get(0);
        else
            lenSum = direction ? (rq.get(rq.size() - 1) - start) + (rq.get(rq.size() - 1) - rq.get(0)) : (start - rq.get(0)) + (rq.get(rq.size() - 1) - rq.get(0));
        //TODO
        return (double) lenSum / request.length;
    }

    /**
     * C-LOOK算法：默认磁头向磁道号增大方向移动
     *
     * @param start     磁头初始位置
     * @param request   请求访问的磁道号
     * @return 平均寻道长度
     */
    public double CLOOK(int start,int[] request){
        int lenSum = 0;
        ArrayList<Integer> rq = new ArrayList<>();
        for (int i : request)
            rq.add(i);
        rq.add(start);
        rq.sort(Comparator.naturalOrder());
        int index = rq.indexOf(start);
        if (index == 0)
            lenSum = rq.get(rq.size() - 1) - start;
        else
            lenSum = (rq.get(rq.size() - 1) - start) + (rq.get(rq.size() - 1) - rq.get(0)) + (rq.get(index - 1) - rq.get(0)) ;
        //TODO
        return (double) lenSum / request.length;
    }

}
