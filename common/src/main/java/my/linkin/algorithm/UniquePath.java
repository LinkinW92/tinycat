package my.linkin.algorithm;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author chunhui.wu
 * leetcode 62
 */
public class UniquePath {
    public int uniquePaths(int m, int n) {
        AtomicInteger counter = new AtomicInteger(0);
        dispatch(1, 1, n, m, counter);
        return counter.get();
    }

    public void dispatch(int sx, int sy, final int r, final int c, AtomicInteger counter) {
        if (sx == r && sy == c) {
            counter.incrementAndGet();
            return;
        }
        if (sx < r) {
            dispatch(sx + 1, sy, r, c, counter);
        }
        if (sy < c) {
            dispatch(sx, sy + 1, r, c, counter);
        }
    }

    public static void main(String[] args) {
        UniquePath up = new UniquePath();
        System.out.println(up.uniquePaths(23, 12));
    }


}
