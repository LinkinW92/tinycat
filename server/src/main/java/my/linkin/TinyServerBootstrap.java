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
        new TinyServer(1010, true);
    }
}
