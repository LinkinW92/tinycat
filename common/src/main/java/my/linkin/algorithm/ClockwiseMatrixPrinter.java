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
        int len = a.length * a[0].length;
        int[] r = new int[len];
        final int MAX_X = a.length, MAX_Y = a[0].length;
        int ymin = 0, ymax = MAX_Y - 1, xmin = 0, xmax = MAX_X - 1, idx = 0;
        while (xmin < xmax && ymin < ymax) {
            for (int i = ymin; i <= ymax; i++) {
                sout(a[xmin][i]);
                r[idx++] = (a[xmin][i]);
            }
            for (int j = xmin + 1; j <= xmax; j++) {
                sout(a[j][ymax]);
                r[idx++] = (a[j][ymax]);
            }
            for (int m = ymax - 1; m >= 0; m--) {
                sout(a[xmax][m]);
                r[idx++] = (a[xmax][m]);
            }
            for (int n = xmax - 1; n > xmin; n--) {
                sout(a[n][ymin]);
                r[idx++] = (a[n][ymin]);
            }
            xmin++;
            ymin++;
            xmax--;
            ymax--;
        }
        while (idx < len && xmin <= xmax) {
            r[idx++] = (a[xmin++][ymin]);
        }
        while (idx < len && ymin <= ymax) {
            r[idx++] = (a[xmin][ymin++]);
        }
        return r;
    }

    public static void sout(int a) {
        System.out.print(a + " ");
    }

    public static void main(String[] args) {
        int[][] a = new int[][]{{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};
        int[] r = new ClockwiseMatrixPrinter().spiralOrder(a);
//        for (int i : r) {
//            System.out.print(i + " ");
//        }
    }
}
