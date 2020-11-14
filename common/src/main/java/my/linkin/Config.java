package my.linkin;

/**
 * @author chunhui.wu
 */
public interface Config {
    /**
     * the maximum retry times for heartbeat command
     */
    int maxRetryTimes = 5;

    /**
     * the thread nums for publicExecutor
     */
    int publicExecutorCoreSize = 4;

    /**
     * the worker queue size for publicExecutor
     */
    int publicWorkerQueueSize = 255;

    /**
     * all nodes run on the same host, distinguish by different port
     */
    String HOST = "127.0.0.1";

    /**
     * the header length of tiCommand
     * */
    int HEADER_LENGTH = 8;
}
