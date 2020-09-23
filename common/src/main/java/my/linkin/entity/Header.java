package my.linkin.entity;

import lombok.Data;

/**
 * @author chunhui.wu
 * the header of a request
 */
@Data
public class Header {
    /**
     * the operation type, eg: a heartbeat request for handshake
     */
    private byte opType;
    /**
     * the length the the request body, we use 1 byte to save the length, so the
     * maximum length could not exceed 255 bytes. this is a tiny request
     */
    private byte length;

    public static Header heartbeat() {
        Header h = new Header();
        h.setOpType(OpType.HEART_BEAT.getIdentifier().byteValue());
        return h;
    }

    public static Header common() {
        Header h = new Header();
        h.setOpType(OpType.COMMON.getIdentifier().byteValue());
        return h;
    }
}
