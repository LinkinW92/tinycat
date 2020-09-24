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
}
