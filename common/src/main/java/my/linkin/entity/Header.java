package my.linkin.entity;

import lombok.Data;
import my.linkin.Config;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author chunhui.wu
 * the header of a request with fixed length, only occupies 6 bytes.
 * we use one byte to represent opType, and one byte for length, and another 4 bytes for requestId
 */
@Data
public class Header {


    /**
     * the operation type, eg: a heartbeat command for handshake
     */
    private OpType opType;
    /**
     * if command is a request, we need set the requestId for idempotent, we use 24 bits to represent the requestId
     */
    private int requestId;
    /**
     * the length the the request body, we use 4 bytes to save the length
     */
    private int length;


    /**
     * generate a requestId
     */
    private static AtomicInteger GENERATOR = new AtomicInteger(0);

    public static Header heartbeat() {
        Header h = new Header();
        h.setOpType(OpType.HEARTBEAT);
        return h;
    }

    public static Header request() {
        Header h = new Header();
        h.setRequestId(GENERATOR.getAndIncrement());
        h.setOpType(OpType.REQUEST);
        return h;
    }

    public static Header response() {
        Header h = new Header();
        h.setOpType(OpType.RESPONSE);
        return h;
    }

    public static Header handshake() {
        Header h = new Header();
        h.setOpType(OpType.HANDSHAKE);
        return h;
    }

    public ByteBuffer encode() {
        ByteBuffer buffer = ByteBuffer.allocate(Config.HEADER_LENGTH);
        buffer.put((byte) (opType.getIdentifier() & 0xff));
        buffer.put((byte) ((requestId & 0xff0000) >> 16));
        buffer.put((byte) ((requestId & 0x00ff00) >> 8));
        buffer.put((byte) (requestId & 0xff));
        buffer.putInt(length);
        buffer.flip();
        return buffer;
    }

    public static Header decode(ByteBuffer buffer) {
        Header h = new Header();
        h.setOpType(OpType.op(buffer.get()));
        int requestId = ((buffer.get() << 16) | (buffer.get() << 8) | buffer.get()) & 0xffffff;
        h.setRequestId(requestId);
        h.setLength(buffer.getInt());
        return h;
    }
}
