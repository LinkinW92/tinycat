package my.linkin.algorithm;

import org.apache.commons.lang3.time.StopWatch;

/**
 * @author chunhui.wu
 * leetcode 62
 */
public class UniquePath {
    public int uniquePaths(int m, int n) {
        int max = m > n ? m : n, min = m > n ? n : m;
        return subPath(min, max);
    }

    public int subPath(int min, int max) {
        if (min == 1 || max == 1) {
            return 1;
        }
        int i = 0, counter = 0;
        while (i++ < max) {
            counter += (subPath(min - 1, max - i));
        }
        return counter;
    }

    public static void main(String[] args) {
        UniquePath up = new UniquePath();
        StopWatch watch = new StopWatch();
        watch.start();
        System.out.println(up.uniquePaths(7, 3));
        watch.stop();
        System.out.println(watch.getNanoTime() / 1000 / 1000 / 1000);
    }


}
