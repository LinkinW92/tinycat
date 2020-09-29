package my.linkin.algorithm;

import lombok.Data;

/**
 * @author chunhui.wu
 */
@Data
public class TreeNode {
    public int val;
    public TreeNode left;
    public TreeNode right;

    public TreeNode(int v) {
        this.val = v;
    }
}
