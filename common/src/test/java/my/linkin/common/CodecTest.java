package my.linkin.common;

import com.alibaba.fastjson.JSON;
import my.linkin.entity.Entity;
import my.linkin.entity.Heartbeat;
import my.linkin.entity.TiCommand;
import org.junit.Test;

import java.nio.ByteBuffer;

/**
 * @author chunhui.wu
 */
public class CodecTest {
    @Test
    public void testCodec() {
        TiCommand cmd = TiCommand.heartbeat();
        Heartbeat beat = new Heartbeat("ping");
        beat.extent("hello", "world");
        cmd.setBody(beat.encode());
        ByteBuffer buffer = cmd.encode();
        TiCommand cmd2 = TiCommand.decode(buffer);
        System.out.println(JSON.toJSONString(Entity.decode(Heartbeat.class, cmd2.getBody())));
        System.out.println(JSON.toJSONString(cmd2.getHeader()));
        // test for requestId
        TiCommand cmd3 = TiCommand.request();
        System.out.println(JSON.toJSONString(cmd3.getHeader()));
        TiCommand cmd4 = TiCommand.request();
        System.out.println(JSON.toJSONString(cmd4.getHeader()));
    }
}
