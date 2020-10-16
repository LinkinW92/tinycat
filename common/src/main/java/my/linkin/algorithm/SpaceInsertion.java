package my.linkin.algorithm;

/**
 * @author chunhui.wu
 * 区间插入 leetcode 57
 */
public class SpaceInsertion {

    public int[][] insert(int[][] intervals, int[] newInterval) {
        if (intervals == null || intervals.length == 0) {
            return new int[][]{{newInterval[0], newInterval[1]}};
        }
        int[] a = new int[2 * intervals.length + 2];
        int idx = 0, i = 0, j = 0, r, c, from = -1, to = -1;
        while (idx < a.length - 2 && j < 2) {
            r = idx >> 1;
            c = (idx & 1) == 0 ? 0 : 1;
            a[i++] = intervals[r][c] > newInterval[j] ? newInterval[j++] : intervals[r][c];
            idx++;
            if (j == 1 && from == -1) {
                from = i - 1;
                idx--;
            }
            if (j == 2 && to == -1) {
                to = i - 1;
                idx--;
            }
        }
        while (idx < a.length - 2) {
            r = idx >> 1;
            c = (idx & 1) == 0 ? 0 : 1;
            a[i++] = intervals[r][c];
            idx++;
        }
        while (j < 2) {
            a[i++] = newInterval[j++];
            if (j == 1 && from == -1) {
                from = i - 1;
            }
            if (j == 2 && to == -1) {
                to = i - 1;
            }
        }
        while (from > 0 && a[from] == a[from - 1]) {
            from--;
        }
        while (to < a.length - 1 && a[to] == a[to + 1]) {
            to++;
        }
        if ((from & 1) == 1) {
            from--;
        }
        if ((to & 1) == 0) {
            to++;
        }
        int[][] res = new int[(a.length - to + from + 1) / 2][2];
        int n = 0;
        for (int m = 0; m < a.length; m++) {
            if (m > from && m < to) {
                m = to;
            }
            res[n >> 1][(n & 1) == 0 ? 0 : 1] = a[m];
            n++;
        }
        return res;
    }

    public static void main(String[] args) {
        int[][] a = {};
        int[] n = {0, 7};
        SpaceInsertion si = new SpaceInsertion();
        si.insert(a, n);
    }
}
