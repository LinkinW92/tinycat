package my.linkin.algorithm;

/**
 * @author chunhui.wu
 * leetcode 55
 */
public class CanJump {

    public boolean canJump(int[] nums) {
        if (nums == null || nums.length == 0) {
            return false;
        }
        if (nums.length > 1 && nums[0] == 0) {
            return false;
        }
        if (nums.length == 1 && nums[0] >= 0) {
            return true;
        }
        return search(nums, 0);
    }

    public boolean search(int[] nums, int index) {
        if (index >= nums.length - 1) {
            return true;
        }
        if (nums[index] == 0 && nums.length - index > 0) {
            return false;
        }
        if (nums.length - index - 1 <= nums[index]) {
            return true;
        }
        for (int i = 1; i <= nums[index]; i++) {
            boolean flag = search(nums, index + i);
            if (flag) {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        CanJump cj = new CanJump();
        int[] a = {5, 9, 3, 2, 1, 0, 2, 3, 3, 1, 0, 0};
        System.out.println(cj.canJump(a));
    }
}
