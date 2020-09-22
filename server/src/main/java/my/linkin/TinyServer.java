package my.linkin;

import my.linkin.server.CatServer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Hello world!
 *
 * @author chunhui.wu
 */
@SpringBootApplication
public class TinyServer implements CommandLineRunner {
    public static void main(String[] args) {
        SpringApplication.run(TinyServer.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Running tiny cat server...");
        new CatServer(1010).start();
    }
}
