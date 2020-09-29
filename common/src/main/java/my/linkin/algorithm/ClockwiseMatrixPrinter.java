package my.linkin.algorithm;

/**
 * @author chunhui.wu
 * <p>
 * 给定一个矩阵，顺时针打印输出
 */
public class ClockwiseMatrixPrinter {

    public static void clockwise(int[][] a) {
        final int MAX_X = a.length, MAX_Y = a[0].length;
        int ymin = 0, ymax = MAX_Y - 1, xmin = 0, xmax = MAX_X - 1;
        while (xmin < xmax && ymin < ymax) {
            for (int i = ymin; i <= ymax; i++) {
                sout(a[xmin][i]);
            }
            for (int j = xmin + 1; j <= xmax; j++) {
                sout(a[j][ymax]);
            }
            for (int m = ymax - 1; m >= 0; m--) {
                sout(a[xmax][m]);
            }
            for (int n = xmax - 1; n > xmin; n--) {
                sout(a[n][ymin]);
            }
            xmin++;
            ymin++;
            xmax--;
            ymax--;
        }
        while (xmin < xmax) {
            sout(a[xmin++][ymin]);
        }
        while (ymin <= ymax) {
            sout(a[xmin][ymin++]);
        }
    }

    public static void sout(int a) {
        System.out.print(a + " ");
    }

    public static void main(String[] args) {
        int[][] a = new int[][]{{1, 2, 3, 4, 100}, {5, 6, 7, 8, 200}, {9, 10, 11, 12, 300}};
        clockwise(a);
    }
}
