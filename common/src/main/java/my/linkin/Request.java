package my.linkin;

import lombok.Data;

/**
 * @author chunhui.wu
 */
@Data
public class Request<T> {

    private T entity;
}
