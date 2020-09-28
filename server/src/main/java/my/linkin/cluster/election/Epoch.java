package my.linkin.cluster.election;

import lombok.Data;

/**
 * @author chunhui.wu
 */
@Data
public class Epoch {
    /**
     * the master of current epoch. The master may be null while master election proceeding
     */
    public String master;
    /**
     * Epoch represented by 64bits. The first 41bits is epoch id for current master, the epoch id increase at each time master changes,
     * the last 23bits is the transaction sequence number under the same master node.
     * <p>
     * We use System.currentMillis() to represent epoch id, which occupies 41bits.
     * the last 23bits is 0 ~ 2^24 - 1, so we use a random value between 0 ~ 2 ^ 10 to initial the sequence number.
     * Why is a random value, not zero? If there are two votes in the same millisecond, we cannot decide which vote to cast with the same initial value zero.
     * So, in here, we initial a random value for sequence number.
     * <p>
     * Why is the random value less than 2 ^ 10. We cannot set the maximum value too big. The bigger the initial value is, the less of the rest can we use.
     * <p>
     * We use 41bits current time in millisecond and 23 bits of a random value to avoid using some central service like zookeeper or redis to create a epoch.
     * Although, this method still has some problems, such that two votes  with the same epoch id and the same sequence number with minimal probability.
     * <p>
     * In current version, we use to method mentioned above to create a epoch. More advance will be applied in the future search.
     */
    public long epoch;

    public static Epoch createEpoch() {
        Epoch e = new Epoch();
        long t = System.currentTimeMillis();
        int v = ((Double) (Math.random() * 1024)).intValue();
        e.setEpoch((t << 23) | (v & 0x7ffff));
        return e;
    }
}
