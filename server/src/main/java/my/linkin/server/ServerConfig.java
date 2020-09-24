package my.linkin.server;

import lombok.Data;
import my.linkin.Config;

/**
 * @author chunhui.wu
 */
@Data
public class ServerConfig extends Config {

    /**
     * the thread nums for publicExecutor
     */
    private int publicSize = 4;
}
