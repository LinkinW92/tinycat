package my.linkin.algorithm;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/**
 * @author chunhui.wu
 */
public class MaxCommonPathDepth {


    public TreeNode lowestCommonAncestor(TreeNode root, TreeNode p, TreeNode q) {
        if (null != ifSonNode(p, q)) {
            return p;
        }
        if (null != ifSonNode(q, p)) {
            return q;
        }
        List<TreeNode> path1 = new ArrayList<>(), path2 = new ArrayList<>();
        pathTo(root, p, new ArrayDeque(), path1);
        pathTo(root, q, new ArrayDeque(), path2);
        TreeNode commonNode = root;
        int idx1 = 0, idx2 = 0;
        while (idx1 < path1.size() && idx2 < path2.size()) {
            if (path1.get(idx1).val == path2.get(idx2).val) {
                commonNode = path1.get(idx1);
                idx1++;
                idx2++;
            } else {
                break;
            }
        }
        return commonNode;
    }

    private TreeNode ifSonNode(TreeNode rooter, TreeNode matcher) {
        List<TreeNode> li = new ArrayList<>();
        pathTo(rooter, matcher, new ArrayDeque<>(), li);
        for (TreeNode n : li) {
            if (n.val == matcher.val) {
                return rooter;
            }
        }
        return null;
    }

    public void pathTo(TreeNode root, TreeNode n, ArrayDeque deque, List<TreeNode> path) {
        if (root == null) {
            return;
        }
        if (root.left != null) {
            deque.addLast(root.left);
            if (root.left.val == n.val) {
                path.addAll(deque);
                return;
            }
            pathTo(root.left, n, deque, path);
            deque.removeLast();
        }
        if (root.right != null) {
            deque.addLast(root.right);
            if (root.right.val == n.val) {
                path.addAll(deque);
                return;
            }
            pathTo(root.right, n, deque, path);
            deque.removeLast();
        }
    }


}
