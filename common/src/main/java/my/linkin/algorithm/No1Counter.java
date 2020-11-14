package my.linkin.algorithm;


/**
 * 给定一个数n， 求1 ~ n范围内1出现的次数
 *
 * @author linkin
 * leetcode offer 43
 */
public class No1Counter {

    /**
     * counter logic
     *
     * @param n the given number
     * @return count of digit one
     */
    public int countDigitOne(int n) {
        int counter = 0;
        String sn = String.valueOf(n);
        int max = sn.length();
        final int m = max;
        while (max > 0) {
            // 100, 010, 001
            // 110, 101, 011,
            // 111
            // 800000000 ~ 87654321  -> 0 ~ 7654321
            if (max < m) {
                for (int i = 1; i <= max; i++) {
                    counter = counter + (int) Math.pow(10d, max - i) - (int) Math.pow(10d, max - i - 1);
                }
            } else {
                if (sn.charAt(0) == '1') {
                    int c = 1;
                    for (int i = 1; i < m; i++) {
                        c *= (sn.charAt(i) + 1);
                    }
                    counter += c;
                }
                String idx = String.valueOf(n - (int) Math.pow(10, m));
                for (int i = 1; i <= max; i++) {
                    max--;
                }
            }
            max--;
        }
        return counter;
    }

    public static void main(String[] args) {
        System.out.println(new No1Counter().countDigitOne(12));
    }
}
