package my.linkin;

import my.linkin.server.TinyServer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Hello world!
 *
 * @author chunhui.wu
 */
@SpringBootApplication
public class TinyServerBootstrap implements CommandLineRunner {
    public static void main(String[] args) {
        SpringApplication.run(TinyServerBootstrap.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Running tiny cat server...");
        int port = 1010;
        boolean clusterModeEnable = false;
        /**
         * in cluster mode, we use idea to start up 3 server distinguished from port.
         * So we edit the run configuration and specified a integer as server port by the program arguments.
         * We use ports 1000, 2000 and 3000 to identify 3 servers. At the bootstrap, they do communication
         * with each other.
         *
         *
         * */
        if (args != null && args.length == 1) {
            port = Integer.valueOf(args[0]);
            clusterModeEnable = true;
        }
        new TinyServer(port, clusterModeEnable);
    }
}
