package my.linkin.entity;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author chunhui.wu
 * a hearbeat body
 */
@Data
public class HeartBeat extends Entity {
    /**
     * the current millis that the HeartBeat being created, in a single physical machine, we use
     * this beatTime to calculate the time for handshake
     */
    private long beatTime;
    /**
     * the message send to the dual node, such as ping or pong
     */
    private String message;

    /**
     * some extent messages
     */
    private Map<String, Object> ext;

    public HeartBeat() {
    }

    public HeartBeat(String message) {
        this.beatTime = System.currentTimeMillis();
        this.message = message;
    }

    public void extent(String k, Object v) {
        if (this.ext == null) {
            this.ext = new HashMap<>(16);
        }
        ext.put(k, v);
    }
}
