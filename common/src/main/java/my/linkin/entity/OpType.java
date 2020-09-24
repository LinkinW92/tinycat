package my.linkin.entity;

import my.linkin.ex.TiException;

/**
 * @author chunhui.wu
 * the operation type occupt 1 byte in request header
 */
public enum OpType {
    /**
     * heart beat request, make sure the server and client are keep alive
     */
    HEART_BEAT(0b00000001),
    /**
     * request type, just send a msg
     */
    REQUEST(0b00000010),
    /**
     * response type, just response to a request
     */
    RESPONSE(0b00000011);

    private Integer identifier;

    OpType(int identifier) {
        this.identifier = identifier;
    }

    public Integer getIdentifier() {
        return identifier;
    }

    public static OpType op(byte b) {
        for (OpType o : OpType.values()) {
            if ((o.identifier ^ b) == 0) {
                return o;
            }
        }
        throw new TiException("Invalid identifier for opType");
    }
}
