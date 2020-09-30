package my.linkin.algorithm;

/**
 * @author chunhui.wu
 * <p>
 * 给定一个矩阵，顺时针打印输出
 */
public class ClockwiseMatrixPrinter {

    public int[] spiralOrder(int[][] a) {
        if (a.length == 0) {
            return new int[0];
        }
        final int R = a.length, C = a[0].length;
        int idx = 0, start = 0;
        int[] result = new int[R * C];
        while (idx <= R - idx && idx <= C - idx) {
            printCircle(a, idx, result, start);
            start = (R - idx * 2) * 2 + (C - idx * 2) * 2 - 4;
            idx++;
        }
        return result;
    }


    /**
     * @param a      原二位矩阵
     * @param idx    当前打印的是第几圈的，起始为0
     * @param result
     */
    private static void printCircle(int[][] a, int idx, int[] result, int start) {
        final int R = a.length - 1, C = a[0].length - 1;
        // 横向打印，从左至右
        for (int m = idx; m <= C - idx; m++) {
            print(a[idx][m]);
            result[start++] = a[idx][m];
        }
        if (idx + 1 > R) {
            return;
        }
        for (int m = idx + 1; m <= R - idx; m++) {
            print(a[m][C - idx]);
            result[start++] = a[m][C - idx];
        }

        if (C - idx - 1 < idx) {
            return;
        }
        for (int m = C - idx - 1; m >= idx; m--) {
            print(a[R - idx][m]);
            result[start++] = a[R - idx][m];
        }
        if (R - idx - 1 < idx + 1) {
            return;
        }
        for (int m = R - idx - 1; m >= idx + 1; m--) {
            print(a[m][idx]);
            result[start++] = a[m][idx];
        }
    }

    public static void print(int a) {
        System.out.print(a + " ");
    }

    public static void main(String[] args) {
        // {1}, {2}, {3}, {4}, {5}, {6}, {7}, {8}, {9}
        int[][] a = new int[][]{{1, 2, 3, 4, 5, 6, 7, 8}, {11, 22, 33, 44, 55, 66, 77, 88}};
        int[] r = new ClockwiseMatrixPrinter().spiralOrder(a);
//        for (int i : r) {
//            System.out.print(i + " ");
//        }
    }
}
