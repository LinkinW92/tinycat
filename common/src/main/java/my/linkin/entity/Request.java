package my.linkin.entity;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import my.linkin.ex.TiException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @author chunhui.wu
 */
@Data
@Slf4j
public class Request {
    private Header header;
    private byte[] entity;


    /**
     * create a heartbeat request
     */
    public static Request heartbeat() {
        Request req = new Request();
        req.setHeader(Header.heartbeat());
        return req;
    }

    /**
     * create a common request
     */
    public static Request common() {
        Request req = new Request();
        req.setHeader(Header.common());
        return req;
    }

    /**
     * serialize an entity
     */
    public void serialize(Entity e) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(e);
            oos.flush();
            this.entity = bos.toByteArray();
            if (entity.length > 255) {
                throw new TiException("The entity length could not exceed 255 bytes");
            }
            this.getHeader().setLength((byte) (entity.length & 0XFF));
            oos.close();
            bos.close();
        } catch (Exception ex) {
            log.error("An error occurs when serialize an entity, ex:{}", ex);
            throw new TiException("An error occurs when serialize an entity");
        }
    }

    /**
     * deserialize the entity
     */
    public Entity deserialize() {
        Entity e;
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(this.entity);
            ObjectInputStream ois = new ObjectInputStream(bis);
            e = (Entity) ois.readObject();
            ois.close();
            bis.close();
        } catch (Exception ex) {
            log.error("An error occurs when deserialize, ex:{}", ex);
            throw new TiException("An error occurs when deserialize");
        }
        return e;
    }
}
