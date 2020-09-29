package my.linkin.algorithm;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * @author chunhui.wu
 * caculate the depth of a tree
 */
public class TreeDepth {

    public static int depthV1(TreeNode root) {
        if (root == null) {
            return 0;
        }
        return Math.max(depthV1(root.left), depthV1(root.right)) + 1;
    }

    public static int depth(TreeNode root) {
        if (root == null) {
            return 0;
        }
        Stack<TreeNode> s1 = new Stack<>(), s2 = new Stack<>(), temp;
        s1.push(root);
        int depth = 0;
        TreeNode pivot;
        while (!s1.isEmpty()) {
            pivot = s1.pop();
            if (pivot.left != null) {
                s2.add(pivot.left);
            }
            if (pivot.right != null) {
                s2.add(pivot.right);
            }
            if (s1.isEmpty()) {
                depth++;
                temp = s1;
                s1 = s2;
                s2 = temp;
            }
        }
        return depth;
    }

    public static void main(String[] args) {
        TreeNode n1 = new TreeNode(3), n2 = new TreeNode(9), n3 = new TreeNode(20), n4 = new TreeNode(15), n5 = new TreeNode(7);
        TreeNode n6 = new TreeNode(11);
        TreeNode n7 = new TreeNode(12);
        TreeNode n8 = new TreeNode(13);
        TreeNode n9 = new TreeNode(14);
        TreeNode n10 = new TreeNode(16);
        n1.left = n2;
        n1.right = n3;
        n3.left = n4;
        n3.right = n5;
        n5.left = n6;
        n5.right = n7;
        n7.left = n8;
        n7.right = n9;
        n9.left = n10;

//        System.out.println(depth(n1));
//        System.out.println(depthV1(n1));

        MaxCommonPathDepth commonPathDepth = new MaxCommonPathDepth();
        System.out.println(commonPathDepth.commonPath(n1, n2, n7));
    }
}
