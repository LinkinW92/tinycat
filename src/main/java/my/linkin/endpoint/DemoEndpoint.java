package my.linkin.endpoint;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author chunhui, wu
 */
@RestController
@RequestMapping(value = "/demo")
public class DemoEndpoint {

    @GetMapping(value = "/hello",consumes = MediaType.ALL_VALUE)
    public void sayHello() {
        System.out.println("hello, tiny cat");
    }
}
