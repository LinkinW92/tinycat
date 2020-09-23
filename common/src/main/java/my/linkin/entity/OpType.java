package my.linkin.entity;

import io.swagger.models.auth.In;

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
     * common type request, just send a msg
     */
    COMMON(0b00000010);

    private Integer identifier;

    OpType(int identifier) {
        this.identifier = identifier;
    }

    public Integer getIdentifier() {
        return identifier;
    }
}
