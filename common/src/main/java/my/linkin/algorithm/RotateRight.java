package my.linkin.algorithm;

/**
 * @author chunhui.wu
 * leetcode 61
 */
public class RotateRight {

    public ListNode rotateRight(ListNode head, int k) {
        if (head == null || head.next == null || k == 0) {
            return head;
        }
        ListNode pivot = head, pre = null, tail = null;
        int length = 0;
        while (pivot != null) {
            length++;
            if (pivot.next == null) {
                tail = pivot;
            }
            pivot = pivot.next;
        }
        k = k % length;
        if (k == 0) {
            return head;
        }
        pivot = head;
        int i = 0;
        while (i < length - k) {
            pre = pivot;
            pivot = pivot.next;
            i++;
        }
        pre.next = null;
        tail.next = head;
        return pivot;
    }

    public static void main(String[] args) {
        ListNode n1 = new ListNode(1), n2 = new ListNode(2), n3 = new ListNode(3), n4 = new ListNode(4), n5 = new ListNode(5);
        n1.next = n2;
        n2.next = n3;
        n3.next = n4;
        n4.next = n5;
        RotateRight rr = new RotateRight();
        ListNode newNode = rr.rotateRight(n1, 2);
        while (newNode != null) {
            System.out.print(newNode.val + ",");
            newNode = newNode.next;
        }
    }

    public static class ListNode {
        int val;
        ListNode next;

        ListNode(int x) {
            val = x;
        }
    }
}
