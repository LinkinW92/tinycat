package my.linkin.algorithm;

import java.util.*;

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
        Map<Integer, Integer> m1 = new HashMap<>(128), m2 = new HashMap<>(128), temp;
        for (int i = 1; i <= 6; i++) {
            m1.put(i, 1);
        }
        final int pn = n;
        while (--n > 0) {
            for (Map.Entry<Integer, Integer> entry : m1.entrySet()) {
                for (int i = 1; i <= 6; i++) {
                    int k = entry.getKey() + i;
                    if (m2.containsKey(k)) {
                        m2.put(k, m2.get(k) + 1);
                    } else {
                        m2.put(k, 1);
                    }
                }
            }
            temp = m1;
            m1 = m2;
            m2 = temp;
        }
        List<Integer> keys = new ArrayList<>(m1.keySet());
        Collections.sort(keys, Comparator.comparing(Integer::intValue));
        double[] r = new double[5 * pn + 1];
        int idx = 0;
        double total = Math.pow(6d, pn);
        for (Integer key : keys) {
            if (key < pn) {
                continue;
            }
            r[idx++] = (double) m1.get(key) / total;
        }
        for (double d : r) {
            System.out.println(d);
        }
        return r;
    }

    public static void main(String[] args) {
        DiceNumber dn = new DiceNumber();
        dn.twoSum(3);
        //[0.00463,0.01389,0.02778,0.0463,0.06944,0.09722,0.11574,0.125,0.125,0.11574,0.09722,0.06944,0.0463,0.02778,0.01389,0.00463]
    }
}
