package my.linkin.algorithm;

import java.util.ArrayList;
import java.util.List;

/**
 * @author chunhui.wu
 */
public class MergeSpace {

    public int[][] merge(int[][] intervals) {
        int row = intervals.length;
        List<Integer[]> r = new ArrayList<>();
        Node root = null, cur, pre = null;
        for (int i = 0; i <= row - 1; ) {
            int from = intervals[i][0], to = intervals[i][1];
            while (i < row - 1 && to >= intervals[i + 1][0]) {
                to = to < intervals[i + 1][1] ? intervals[i + 1][1] : to;
                from = from > intervals[i + 1][0] ? intervals[i + 1][0] : from;
                i++;
            }
            if (root == null) {
                root = new Node(new int[]{from, to});
                pre = root;
            } else {
                cur = new Node(new int[]{from, to});
                pre.next = cur;
                pre = cur;
            }
        }
        while (true) {
            cur = root;
            while (cur !=null) {

            }
        }
        int[][] res = new int[r.size()][2];
        int i = 0;
        for (Integer[] a : r) {
            res[i][0] = a[0];
            res[i][1] = a[1];
            System.out.println(res[i][0] + "," + res[i][1]);
            i++;
        }
        return res;
    }

    class Node {
        private int[] a;
        private Node next;

        public Node(int[] a) {
            this.a = a;
        }

        public int[] getA() {
            return a;
        }

        public void setA(int[] a) {
            this.a = a;
        }

        public Node getNext() {
            return next;
        }

        public void setNext(Node next) {
            this.next = next;
        }
    }


    public static void main(String[] args) {
//        int[][] a = {{1, 3}, {2, 6}, {8, 10}, {15, 18}};
        int[][] a = {{1, 4}, {0, 0}};
        MergeSpace ms = new MergeSpace();
        ms.merge(a);
    }
}
