package my.linkin.algorithm;

/**
 * @author chunhui.wu
 */
public class IslandCounter {

    public int numIslands(char[][] grid) {
        if (grid == null || grid.length == 0) {
            return 0;
        }
        int cnt = 0;
        int m = grid.length, n = grid[0].length;
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (grid[i][j] == '1') {
                    cnt++;
                    dispatch(grid, i, j);
                }
            }
        }
        return cnt;
    }

    private void dispatch(char[][] grid, int i, int j) {
        grid[i][j] = '2';
        if (i - 1 >= 0 && grid[i - 1][j] == '1') {
            dispatch(grid, i - 1, j);
        }
        if (i + 1 < grid.length && grid[i + 1][j] == '1') {
            dispatch(grid, i + 1, j);
        }
        if (j - 1 >= 0 && grid[i][j - 1] == '1') {
            dispatch(grid, i, j - 1);
        }
        if (j + 1 < grid[0].length && grid[i][j + 1] == '1') {
            dispatch(grid, i, j + 1);
        }
    }

    public static void main(String[] args) {
        IslandCounter ic = new IslandCounter();
//        char[][] a = new char[][]{{'1', '1', '0', '0', '0'}, {'1', '1', '0', '0', '0'}, {'0', '0', '1', '0', '0'}, {'0', '0', '0', '1', '1'}};
//        char[][] a = new char[][]{{'1','1','1','1','0'},  {'1','1','0','1','0'},  {'1','1','0','0','0'},  {'0','0','0','0','0'}};
//        char[][] a = new char[][]{{'1', '1', '1'}, {'0', '1', '0'}, {'1', '1', '1'}};
        char[][] a = new char[][]{{'0', '1', '0'}, {'1', '0', '1'}, {'0', '1', '0'}};
//        char[][] a = new char[][]{{'1', '0', '1', '1', '1'}, {'1', '0', '1', '0', '1'}, {'1', '1', '1', '0', '1'}};
        //{{'1','0','1','1','1'},{'1','0','1','0','1'},{'1','1','1','0','1'}}
        System.out.println(ic.numIslands(a));
    }
}
