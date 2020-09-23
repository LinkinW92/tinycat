package my.linkin.ex;

/**
 * @author chunhui.wu
 */
public class TiException extends RuntimeException{

    private String message;

    public TiException(String message) {
        super(message);
    }
}
