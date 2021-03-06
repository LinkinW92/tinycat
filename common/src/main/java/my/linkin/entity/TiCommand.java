package my.linkin.entity;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import my.linkin.Config;

import java.nio.ByteBuffer;

/**
 * @author chunhui.wu
 */
@Data
@Slf4j
public class TiCommand {


    private Header header;
    private byte[] body;

    public TiCommand of(Entity entity) {
        this.setBody(entity.encode());
        return this;
    }


    /**
     * create a heartbeat request
     */
    public static TiCommand heartbeat() {
        TiCommand req = new TiCommand();
        req.setHeader(Header.heartbeat());
        return req;
    }

    /**
     * create a request
     */
    public static TiCommand request() {
        TiCommand req = new TiCommand();
        req.setHeader(Header.request());
        return req;
    }

    /**
     * create a vote
     */
    public static TiCommand vote() {
        TiCommand req = new TiCommand();
        req.setHeader(Header.vote());
        return req;
    }

    /**
     * create a response
     */
    public static TiCommand response() {
        TiCommand req = new TiCommand();
        req.setHeader(Header.response());
        return req;
    }

    /**
     * create a response
     */
    public static TiCommand handshake() {
        TiCommand req = new TiCommand();
        req.setHeader(Header.handshake());
        return req;
    }

    public static TiCommand decode(ByteBuffer buffer) {
        ByteBuffer headerData = ByteBuffer.allocate(Config.HEADER_LENGTH);
        headerData.put(buffer.get());
        headerData.put(buffer.get());
        headerData.put(buffer.get());
        headerData.put(buffer.get());
        headerData.putInt(buffer.getInt());
        headerData.flip();
        Header h = Header.decode(headerData);
        TiCommand cmd = new TiCommand();
        cmd.header = h;
        if (h.getLength() > 0) {
            byte[] body = new byte[h.getLength()];
            buffer.get(body);
            cmd.body = body;
        }
        return cmd;
    }

    public ByteBuffer encode() {
        int bodyLength = this.body == null ? 0 : this.body.length;
        this.header.setLength(bodyLength);
        // 8 is the fixed length of header
        ByteBuffer buffer = ByteBuffer.allocate(Config.HEADER_LENGTH + bodyLength);
        buffer.put(this.header.encode());
        if (this.body != null) {
            buffer.put(body);
        }
        buffer.flip();
        return buffer;
    }
}
