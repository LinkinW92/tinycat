package my.linkin;

import my.linkin.server.CatServer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Hello world! i am tiny cat
 */
@SpringBootApplication
public class TinyCat implements CommandLineRunner {
    public static void main(String[] args) {
        SpringApplication.run(TinyCat.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Running cat...");
        new CatServer(1010).start();
    }
}
