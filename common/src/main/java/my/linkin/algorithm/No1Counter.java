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
     * @return digit one count
     */
    public int countDigitOne(int n) {
        int counter = 0;
        int max = String.valueOf(n).length();
        while (max > 0) {
            // 100, 010, 001
            // 110, 101, 011,
            // 111
            for (int i = 1; i <= max; i++) {
                counter += (factorial(max) / factorial(i) / factorial(max - i));
            }
            max--;
        }
        return counter;
    }

    private int factorial(int n) {
        int r = 1;
        for (int i = 2; i <= n; i++) {
            r *= i;
        }
        return r;
    }

    public static void main(String[] args) {
        System.out.println(new No1Counter().countDigitOne(12));
    }
}
