package com.jchugh.hyperloglog;
import com.google.common.hash.Hashing;
import net.jcip.annotations.ThreadSafe;
import java.nio.charset.Charset;

/**
 * @author jchugh
 * Based on the HyperLogLog algorithm. Computes cardinality with error less than 1.04/sqrt(2 ^ logRegisterCount).
 * The register size is fixed at 8 bits for ease of implementation.
 */
@ThreadSafe
public class Cardinality {
    private final int SEED = 1;
    private final Object lock = new Object();
    private byte[] registers;
    private final int logRegisterCount;
    private final int registerCount;
    private final double correctionConstant;
    private final int shift;

    /**
     * This class and all it's methods except {@link #getCardinalityAsync()} are {@link net.jcip.annotations.ThreadSafe}
     * @param logRegisterCount the log2 of Number of registers you wish to use, eg 16 registers = 4 logRegisterCount (log2(16) = 4).
     *                         This value has to be between 4 and 16 (min and max inclusive).
     */
    public Cardinality(int logRegisterCount) {
        if (!verifynumRegistersExponent(logRegisterCount)) {
            throw new IllegalArgumentException("4 <= logRegisterCount <= 16");
        }
        this.registerCount = 1 << logRegisterCount;
        this.registers = new byte[this.registerCount];
        this.logRegisterCount = logRegisterCount;
        this.correctionConstant = getCorrectionConstant(this.registerCount) * registerCount * registerCount;
        this.shift = Integer.SIZE - logRegisterCount;
    }

    /**
     * Submit a {@code long} input
     * @param input the long input
     */
    public void offer(long input) {
        int x = Hashing.murmur3_32(SEED).hashLong(input).asInt();
        addToRegister(x);
    }

    /**
     * Submit a {@link String} input with {@link Charset}
     * @param input the String input
     * @param charset the Charset
     */
    public void offer(String input, Charset charset) {
        int x = Hashing.murmur3_32(SEED).hashString(input, charset).asInt();
        addToRegister(x);
    }

    /**
     * Submit a {@code byte[]} input. You can use {@link Cardinality#offer(String, Charset)} for Strings.
     * @param input the byte[] input
     */
    public void offer(byte[] input) {
        int x = Hashing.murmur3_32(SEED).hashBytes(input).asInt();
        addToRegister(x);
    }

    private void addToRegister(final int hash) {
        int j = hash >>> this.shift;
        // So that we do not get more Leading Zeros than the remaining number of bits after we have calculated j
        // and the rest of the hash value is 0. Thus max leading zeros == this.shift
        int w = (hash << logRegisterCount) | (1 << (logRegisterCount - 1));
        synchronized (lock) {
            registers[j] = ((byte) Math.max(registers[j], Integer.numberOfLeadingZeros(w) + 1));
        }
    }

    /**
     * Get the cardinality of the inputs received so far.
     * While the cardinality is calculated, no more values can be added.
     * that snapshot. Useful when cardinality is needed less often, but slows down the system if getCardinality()
     * is called a lot. Check {@link #getCardinalityAsync()}
     * @return cardinality
     */
    public long getCardinalitySync() {
        synchronized (lock) {
            return getCardinality();
        }
    }

    /**
     * <B>Use With Caution</B> this method is {@link net.jcip.annotations.NotThreadSafe}
     * Get the cardinality of the inputs received so far. Async.
     * While the cardinality is calculated, you can still add data to the registers, this may result in slightly skewed
     * cardinality for the instance it was called.
     * <B>Warning: Another thread can call {@link #reset()} while this executes.</B>
     * @return cardinality of the items seen so far
     */
    public long getCardinalityAsync() {
        return getCardinality();
    }

    private long getCardinality() {
        int zeroRegisterCount = 0;
        double indicator = 0.0;
        for (int i = 0; i < registerCount; ++i) {
            // 2 ^ -register[i]
            indicator += (1.0 / (1 << registers[i]));
            if (registers[i] == 0) zeroRegisterCount++;
        }
        indicator = 1 / indicator;
        double E = correctionConstant * indicator;
        return Cardinality.correct(E, registerCount, zeroRegisterCount);
    }

    /**
     * Reset all the registers to 0.
     * Creates a new list of registers, leaving previous one to be gc'd.
     */
    public void reset() {
        synchronized (lock) {
            this.registers = new byte[this.registerCount];
        }
    }

    private static boolean verifynumRegistersExponent(int numRegisterExponent) {
        if (numRegisterExponent < 4 || numRegisterExponent > 16) {
            return false;
        }
        return true;
    }

    private static double getCorrectionConstant(int numOfRegisters) {
        switch (numOfRegisters) {
            case 16: return 0.673;
            case 32: return 0.697;
            case 64: return 0.709;
            default: {
                return 0.7213 / (1 + 1.079/numOfRegisters);
            }
        }
    }

    /**
     * As Suggested by the addThis stream library, large range correction causes more error.
     * @param E
     * @param numOfRegisters
     * @param emptyRegisters
     * @return
     */
    private static long correct(final double E, final int numOfRegisters, final int emptyRegisters) {
        if (E <= (5.0 / 2.0) * numOfRegisters) {
            if (emptyRegisters != 0) {
                return Math.round(numOfRegisters * Math.log(numOfRegisters / Double.valueOf(emptyRegisters)));
            } else {
                return Math.round(E);
            }
        } else {
            return Math.round(E);
        }
    }
}
