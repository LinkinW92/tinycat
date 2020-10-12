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
        if (n < 0) {
            n = -n;
        }
        double d = (n & 1) == 0 ? 1 : x;
        n &= 0xfffe;
        Map<Integer, Double> map = new HashMap<>();
        double r = d * pow(x, n / 2, map) * pow(x, n / 2, map);
        return flag ? r : 1.0d / r;
    }

    private double pow(double x, int n, Map<Integer, Double> map) {
        if (map.containsKey(n)) {
            return map.get(n);
        }
        if (n == 2) {
            return x * x;
        }
        double r = pow(x, n / 2, map) * pow(x, n / 2, map);
        map.put(n, r);
        return r;
    }

    public static void main(String[] args) {
        PowXN xn = new PowXN();
        System.out.println(xn.myPow(2.0, 10));
    }
}
