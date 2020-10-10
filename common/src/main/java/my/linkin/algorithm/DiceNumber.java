package my.linkin.algorithm;

import java.util.HashMap;
import java.util.Map;

/**
 * @author chunhui.wu
 * n 个骰子的点数
 */
public class DiceNumber {

    /**
     * @param n n个骰子
     * @return 各个骰子的点数之和对应的概率，点数之和由小到大排列
     */
    public double[] twoSum(int n) {
        int pn = n, k, d = 6, total = (int) Math.pow(6d, pn);
        Map<Integer, Integer> m1 = new HashMap<>(32), m2;
        for (int i = 1; i <= d; i++) {
            m1.put(i, 1);
        }
        while (--n > 0) {
            m2 = new HashMap<>(32);
            for (Map.Entry<Integer, Integer> entry : m1.entrySet()) {
                for (int i = 1; i <= d; i++) {
                    k = entry.getKey() + i;
                    m2.put(k, m2.getOrDefault(k, 0) + entry.getValue());
                }
            }
            m2.remove(pn - n);
            m1 = m2;
        }
        double[] r = new double[5 * pn + 1];
        for (Integer key : m1.keySet()) {
            r[key - pn] = (double) m1.get(key) / total;
        }
        return r;
    }

    public double[] twoSumAdvanced(int n) {
        int dp[] = new int[70];
        for (int i = 1; i <= 6; i++) {
            dp[i] = 1;
        }
        for (int i = 2; i <= n; i++) {
            for (int j = 6 * i; j >= i; j--) {
                dp[j] = 0;
                for (int cur = 1; cur <= 6; cur++) {
                    if (j - cur < i - 1) {
                        break;
                    }
                    dp[j] += dp[j - cur];
                }

            }
        }
        double all = Math.pow(6, n);
        double[] res = new double[5 * n + 1];
        for (int i = n; i <= 6 * n; i++) {
            res[i - n] = (dp[i] * 1.0 / all);
        }
        return res;
    }

    public static void main(String[] args) {
        DiceNumber dn = new DiceNumber();
        int n = 11;
        double[] r1 = dn.twoSum(n), r2 = dn.twoSumAdvanced(n);
        for (int i = 0; i <= 5 * n; i++) {
            System.out.println(r1[i] + " || " + r2[i]);
        }
        //[0.00463,0.01389,0.02778,0.0463,0.06944,0.09722,0.11574,0.125,0.125,0.11574,0.09722,0.06944,0.0463,0.02778,0.01389,0.00463]
    }
}
