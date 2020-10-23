package my.linkin.algorithm;

/**
 * @author chunhui.wu
 * <p>
 * 重拍链表 leetcode 143
 */
public class ReorderLinkedList {

    public void reorderList(ListNode head) {
        if (head == null || head.next == null || head.next.next == null) {
            return;
        }
        ListNode next = head.next, pivot = head, swap = pivot, cur = head;
        while (next != null) {
            while (pivot != null) {
                if (pivot.next != null && pivot.next.next == null) {
                    swap = pivot.next;
                    pivot.next = null;
                    break;
                }
                pivot = pivot.next;
            }
            pivot = cur.next;
            cur.next = swap;
            swap.next = pivot;
            next = next.next;
            cur = swap.next;
        }
        next = head;
        while (next != null) {
            System.out.print(next.val + ",");
            next = next.next;
        }
    }

    public static void main(String[] args) {
        ListNode n1 = new ListNode(1), n2 = new ListNode(2), n3 = new ListNode(3), n4 = new ListNode(4), n5 = new ListNode(5), n6 = new ListNode(6);
        n1.next = n2;
        n2.next = n3;
        n3.next = n4;
//        n4.next = n5;
//        n5.next = n6;
        ReorderLinkedList order = new ReorderLinkedList();
        order.reorderList(n1);
    }

    public static class ListNode {
        int val;
        ListNode next;

        ListNode() {
        }

        ListNode(int val) {
            this.val = val;
        }

        ListNode(int val, ListNode next) {
            this.val = val;
            this.next = next;
        }
    }

}
