package my.linkin;

import lombok.Data;

/**
 * @author chunhui.wu
 */
@Data
public class Config {
    /**
     * the maximum retry times for heartbeat command
     */
    protected int maxRetryTimes = 5;

    /**
     * the thread nums for publicExecutor
     */
    protected int publicExecutorCoreSize = 4;

    /**
     * the worker queue size for publicExecutor
     */
    protected int publicWorkerQueueSize = 255;
}
