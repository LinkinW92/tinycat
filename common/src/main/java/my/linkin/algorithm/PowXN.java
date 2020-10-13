package my.linkin.algorithm;

import java.util.HashMap;
import java.util.Map;

/**
 * @author chunhui.wu
 * 求pow(x,n)
 */
public class PowXN {
    public double myPow(double x, int n) {
        boolean flag = n > 0;
        double d = (n == Integer.MIN_VALUE) ? x : 1.0d;
        n = n < 0 ? (n == Integer.MIN_VALUE ? Integer.MAX_VALUE : -n) : n;
        int move = 1;
        Map<Integer, Double> map = new HashMap<>(32);
        map.put(1, x);
        while (move < 32 && n > 0) {
            int idx = n & (1 << (move - 1));
            n -= idx;
            if (!map.containsKey(1 << move)) {
                double pre = map.get(1 << (move - 1));
                map.put(1 << move, pre * pre);
            }
            if (idx != 0) {
                d *= map.get(1 << (move - 1));
            }
            move++;
        }
        return flag ? d : (1.0d / d);
    }

    public static void main(String[] args) {
        PowXN xn = new PowXN();
        System.out.println(xn.myPow(-2.0, -2147483648));
        System.out.println(Math.pow(-2.0d, -2147483648));
    }
}
