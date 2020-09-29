package my.linkin.algorithm;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/**
 * @author chunhui.wu
 */
public class MaxCommonPathDepth {


    public int commonPath(TreeNode root, TreeNode n1, TreeNode n2) {
        List<TreeNode> path1 = new ArrayList<>(), path2 = new ArrayList<>();
        path1.add(root);
        path2.add(root);
        pathTo(root, n1, new ArrayDeque(), path1);
        pathTo(root, n2, new ArrayDeque(), path2);
        TreeNode commonNode = null;
        int idx1 = 0, idx2 = 0;
        while (idx1 < path1.size() && idx2 < path2.size()) {
            if (path1.get(idx1).v == path2.get(idx2).v) {
                commonNode = path1.get(idx1);
            }
            idx1++;
            idx2++;
        }
        if (idx1 < path1.size()) {
            List<TreeNode> li = new ArrayList<>();
            pathTo(n2, n1, new ArrayDeque<>(), li);
            for (TreeNode n : li) {
                if (n.v == n1.v) {
                    commonNode = n2;
                }
            }
        }
        if (idx2 < path2.size()) {
            List<TreeNode> li = new ArrayList<>();
            pathTo(n1, n2, new ArrayDeque<>(), li);
            for (TreeNode n : li) {
                if (n.v == n2.v) {
                    commonNode = n1;
                }
            }
        }
        return commonNode.v;
    }

    public void pathTo(TreeNode root, TreeNode n, ArrayDeque deque, List<TreeNode> path) {
        if (root == null) {
            return;
        }
        if (root.left != null) {
            deque.addLast(root.left);
            if (root.left.v == n.v) {
                path.addAll(deque);
                return;
            }
            pathTo(root.left, n, deque, path);
            deque.removeLast();
        }
        if (root.right != null) {
            deque.addLast(root.right);
            if (root.right.v == n.v) {
                path.addAll(deque);
                return;
            }
            pathTo(root.right, n, deque, path);
            deque.removeLast();
        }
    }


}
