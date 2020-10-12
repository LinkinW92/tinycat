package my.linkin.algorithm;

import java.util.*;

/**
 * @author chunhui.wu
 * 三数之和
 */
public class ThreeSum {


    public List<List<Integer>> threeSum(int[] nums) {
        if (nums == null || nums.length < 3) {
            return new ArrayList<>();
        }
        Arrays.sort(nums);
        Map<String, List<Integer>> map = new HashMap<>(16);
        int mid = 1, low = mid - 1, high = mid + 1, s;
        while (mid < nums.length - 1) {
            while (low >= 0 && high < nums.length) {
                while (low >= 0 && nums[low] > 0) {
                    low--;
                }
                while (high < nums.length && nums[high] < 0) {
                    high++;
                }
                if (low < 0 || high > nums.length - 1) {
                    break;
                }
                s = nums[low] + nums[mid] + nums[high];
                if (s == 0) {
                    System.out.println(nums[low] + " " + nums[mid] + " " + nums[high]);
                    map.put(nums[low] + "-" + nums[mid] + "-" + nums[high], Arrays.asList(nums[low], nums[mid], nums[high]));
                    low--;
                    high++;
                    while (low - 1 >= 0 && nums[low] == nums[low - 1]) {
                        low--;
                    }
                    while (high + 1 < nums.length && nums[high] == nums[high + 1]) {
                        high++;
                    }
                    continue;
                }
                s = s < 0 ? high++ : low--;
            }
            mid++;
            low = mid - 1;
            high = mid + 1;
        }
        System.out.println("---------");
        for (List<Integer> li : map.values()) {
            for (Integer i : li) {
                System.out.print(i + " ");
            }
            System.out.println();
        }
        return new ArrayList<>(map.values());
    }

    public List<List<Integer>> ts(int[] nums) {
        if (nums == null || nums.length < 3) {
            return new ArrayList();
        }
        List<List<Integer>> res = new ArrayList<>();
        int length = nums.length;
        Arrays.sort(nums);
        if (nums[0] <= 0 && nums[length - 1] >= 0) {
            for (int i = 0; i < length - 2; ) {
                if (nums[i] > 0) {
                    break;
                }
                int first = i + 1;
                int last = length - 1;
                do {
                    if (first >= last || nums[i] * nums[last] > 0) {
                        break;
                    }
                    int result = nums[i] + nums[first] + nums[last];
                    if (result == 0) {
                        res.add(Arrays.asList(nums[i], nums[first], nums[last]));
                    }
                    if (result <= 0) {
                        while (first < last && nums[first] == nums[++first]) {
                        }
                    } else {
                        while (first < last && nums[last] == nums[--last]) {
                        }
                    }
                } while (first < last);
                while (i + 1 < length && nums[i] == nums[++i]) {
                }
            }
        }
        for (List<Integer> li : res) {
            for (Integer i : li) {
                System.out.print(i + " ");
            }
            System.out.println();
        }
        return res;
    }

    public static void main(String[] args) {
        ThreeSum ts = new ThreeSum();
        int[] a = new int[]{0, 0, 0};
//        int[] a = new int[]{-82, -70, -66, -49, -43, -29, -29, -14, -11, -6, -3, -3, 1, 2, 10, 12, 13, 15, 15, 17, 21, 26, 26, 28, 28, 29, 31, 33, 34, 36, 43, 46, 46, 47, 48, 49, 52, 55, 55, 56, 57, 61, 62, 65, 69, 71, 74, 76, 77, 79, 83, 84, 86, 93, 94};
//        int[] a = new int[]{-4, -2, 1, -5, -4, -4, 4, -2, 0, 4, 0, -2, 3, 1, -5, 0};
//        int[] a = new int[]{1, 1, 1};
        Arrays.sort(a);
        for (Integer i : a) {
            System.out.print(i + ",");
        }
        System.out.println();
        System.out.println("---------");
        ts.ts(a);
//        ts.threeSum(new int[]{-4, -2, -2, -2, 0, 1, 2, 2, 2, 3, 3, 4, 4, 6, 6});
    }
}
