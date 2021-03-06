package my.linkin.entity;

import lombok.extern.slf4j.Slf4j;
import my.linkin.ex.TiException;

/**
 * @author chunhui.wu
 * the operation type occupt 1 byte in request header
 */
@Slf4j
public enum OpType {
    /**
     * heart beat request, make sure the server and client are keep alive
     */
    HEARTBEAT(0b01),

    /**
     * the handshake among servers
     */
    HANDSHAKE(0b10),
    /**
     * request type, just send a msg
     */
    REQUEST(0b11),
    /**
     * response type, just response to a request
     */
    RESPONSE(0b100),
    /**
     * vote for a candidate
     */
    VOTE(0b101);

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
