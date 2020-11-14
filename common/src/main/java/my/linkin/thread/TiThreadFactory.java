package my.linkin.thread;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author chunhui.wu
 */
@Slf4j
public class TiThreadFactory implements ThreadFactory {
    /**
     * make a distinguish between server and client
     */
    private String name;
    /**
     * the thread has been created by the role(sever or client)
     */
    private AtomicInteger idx;

    public TiThreadFactory(String name) {
        this.name = name;
        this.idx = new AtomicInteger(0);
    }

    @Override
    public Thread newThread(Runnable r) {
        String threadName = new StringBuilder(name).append("-").append(this.idx.getAndIncrement()).toString();
        log.info("Created a new thread named :{}", threadName);
        return new Thread(r, threadName);
    }
}
