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

    public static <T> T decode(Class<T> clz, byte[] body) {
        if (body == null || body.length == 0) {
            return null;
        }
        return JSON.parseObject(body, clz);
    }
}
