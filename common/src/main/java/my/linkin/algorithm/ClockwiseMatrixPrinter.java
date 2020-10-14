package my.linkin.algorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author chunhui.wu
 * <p>
 * 给定一个矩阵，顺时针打印输出
 */
public class ClockwiseMatrixPrinter {

    public List<Integer> spiralOrder(int[][] matrix) {
        List<Integer> list = new ArrayList<>();
        if (matrix == null || matrix.length == 0) {
            return new ArrayList<>();
        }
        final int r = matrix.length - 1, c = matrix[0].length - 1;
        if (matrix.length == 1) {
            for (int i = 0; i <= c; i++) {
                list.add(matrix[0][i]);
            }
            return list;
        }
        if (matrix[0].length == 1) {
            for (int i = 0; i <= r; i++) {
                list.add(matrix[i][0]);
            }
            return list;
        }

        int row = 0, col = 0, idx = 0, cnt, circle, total = 0;
        while (row < matrix.length / 2 && col < matrix[0].length / 2) {
            circle = (r + 1 - 2 * idx + c + 1 - 2 * idx) * 2 - 4;
            cnt = 0;
            for (int i = idx; i <= c - idx && cnt < circle; i++) {
                cnt++;
                list.add(matrix[idx][i]);
            }
            for (int i = idx + 1; i <= r - idx && cnt < circle; i++) {
                cnt++;
                list.add(matrix[i][c - idx]);
            }
            for (int i = c - idx - 1; i >= idx && cnt < circle; i--) {
                cnt++;
                list.add(matrix[r - idx][i]);
            }
            for (int i = r - idx - 1; i >= idx + 1 && cnt < circle; i--) {
                cnt++;
                list.add(matrix[i][idx]);
            }
            idx++;
            row++;
            col++;
            total += circle;
        }
        if (total < (r + 1) * (c + 1)) {
            if (matrix.length > matrix[0].length) {
                for (int i = idx; i <= r - idx; i++) {
                    list.add(matrix[i][matrix[0].length / 2]);
                }
            }
            if (matrix.length < matrix[0].length) {
                for (int i = idx; i <= c - idx; i++) {
                    list.add(matrix[matrix.length / 2][i]);
                }
            }
            if (matrix.length == matrix[0].length) {
                list.add(matrix[matrix.length / 2][matrix[0].length / 2]);
            }
        }
        return list;
    }

    public static void main(String[] args) {
        int[][] a = {{2, 3, 4}, {5, 6, 7}, {8, 9, 10}, {11, 12, 13}};
        int[][] m = generate(4, 2);
        List<Integer> list = new ClockwiseMatrixPrinter().spiralOrder(a);
        for (Integer i : list) {
            System.out.print(i + ",");
        }
    }

    public static int[][] generate(int m, int n) {
        Random r = new Random();
        int[][] a = new int[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                a[i][j] = r.nextInt(100);
            }
        }
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                System.out.print(a[i][j] + ",");
            }
            System.out.println();
        }
        System.out.println("-------------------");
        return a;
    }
}
