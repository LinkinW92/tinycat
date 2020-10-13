package my.linkin.algorithm;

import java.util.ArrayList;
import java.util.List;

/**
 * @author chunhui.wu
 */
public class MergeSpace {

    public int[][] merge(int[][] intervals) {
        if (intervals.length == 0) {
            return new int[0][2];
        }
        int row = intervals.length;
        List<Integer[]> r = new ArrayList<>();
        // 以一维数组为单位，进行排序
        for (int i = 0; i <= row - 1; i++) {
            for (int j = 0; j < row - 1 - i; j++) {
                if (intervals[j][0] > intervals[j + 1][0]) {
                    int[] temp = intervals[j + 1];
                    intervals[j + 1] = intervals[j];
                    intervals[j] = temp;
                }
            }
        }
        for (int i = 0; i <= row - 1; ) {
            int from = intervals[i][0], to = intervals[i][1];
            while (i < row - 1 && to >= intervals[i + 1][0]) {
                to = to < intervals[i + 1][1] ? intervals[i + 1][1] : to;
                from = from > intervals[i + 1][0] ? intervals[i + 1][0] : from;
                i++;
            }
            r.add(new Integer[]{from, to});
            i++;
        }
        int[][] res = new int[r.size()][2];
        int i = 0;
        for (Integer[] a : r) {
            res[i][0] = a[0];
            res[i][1] = a[1];
            System.out.println(res[i][0] + "," + res[i][1]);
            i++;
        }
        return res;
    }

    public static void main(String[] args) {
//        int[][] a = {{1, 3}, {2, 6}, {8, 10}, {15, 18}};
//        int[][] a = {{1, 4}, {4, 5}};
        int[][] a = {{1, 4}, {0, 0}, {2, 5}, {4, 10}, {3, 8}, {-1, 0}};
        MergeSpace ms = new MergeSpace();
        ms.merge(a);
    }
}
