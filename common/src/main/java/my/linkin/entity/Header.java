package my.linkin.entity;

import lombok.Data;

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
     * the length the the request body, we use 1 byte to save the length, so the
     * maximum length could not exceed 255 bytes. This is a tiny command
     */
    private int length;

    /**
     * if command is a request, we need set the requestId for idempotent
     */
    private int requestId;

    /**
     * generate a requestId
     */
    private static AtomicInteger GENERATOR = new AtomicInteger(0);

    public static Header heartbeat() {
        Header h = new Header();
        h.setOpType(OpType.HEART_BEAT);
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

    public ByteBuffer encode() {
        ByteBuffer buffer = ByteBuffer.allocate(6);
        buffer.put((byte) (opType.getIdentifier() & 0xff));
        buffer.put((byte) (length & 0xff));
        buffer.putInt(requestId);
        buffer.flip();
        return buffer;
    }

    public static Header decode(ByteBuffer buffer) {
        Header h = new Header();
        h.setOpType(OpType.op(buffer.get()));
        h.setLength(buffer.get() & 0xff);
        h.setRequestId(buffer.getInt());
        return h;
    }
}
