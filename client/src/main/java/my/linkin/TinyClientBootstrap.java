package my.linkin;

import my.linkin.client.TinyClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Hello world!
 *
 * @author chunhui.wu
 */
@SpringBootApplication
public class TinyClientBootstrap implements CommandLineRunner {
    public static void main(String[] args) {
        SpringApplication.run(TinyClientBootstrap.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Running tiny cat client...");
        new TinyClient(1010);
    }
}
