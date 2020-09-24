package my.linkin.entity;

import com.alibaba.fastjson.JSON;

import java.nio.charset.Charset;

/**
 * @author chunhui.wu
 * the body of the tiCommand
 */
public abstract class Entity {


    private static final Charset UTF8 = Charset.forName("UTF8");

    public byte[] encode() {
        String json = JSON.toJSONString(this, false);
        return json.getBytes(UTF8);
    }

    public static Entity decode(Class<? extends Entity> clz, byte[] body) {
        return JSON.parseObject(body, clz);
    }
}
