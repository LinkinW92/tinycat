package my.linkin;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Hello world!
 *
 * @author chunhui.wu
 */
@SpringBootApplication
public class TinyClient implements CommandLineRunner {
    public static void main(String[] args) {
        SpringApplication.run(TinyClient.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Running tiny cat client...");
        new CatClient(1010).start();
    }
}
