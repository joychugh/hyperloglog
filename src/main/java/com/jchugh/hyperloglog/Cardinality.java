package com.jchugh.hyperloglog;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.hash.HashingInputStream;
import com.jchugh.hyperloglog.hashing.MurmurHash3;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jchugh
 * This does not work right now,
 * When it will, There will be a test included.
 */
public class Cardinality {
    private final List<Byte> registers;
    private final int logRegisterCount;
    private final int registerCount;
    private final double correctionConstant;
    private int zeroValueRegisters;

    public Cardinality(int logRegisterCount) {
        if (!verifynumRegistersExponent(logRegisterCount)) {
            throw new IllegalArgumentException("4 <= logRegisterCount <= 16");
        }
        this.registerCount = 1 << logRegisterCount;
        this.registers = new ArrayList<>(this.registerCount);
        this.logRegisterCount = logRegisterCount;
        this.correctionConstant = getCorrectionConstant(this.registerCount);
        zeroValueRegisters = 0;
    }

    public void offer(long input) {
        int x = Hashing.murmur3_32().hashLong(input).asInt();
        int shift = Integer.SIZE - logRegisterCount;
        int j = x >>> shift;
        int w = (x << logRegisterCount) | (1 << (logRegisterCount - 1));
        registers.add((byte) Math.max(registers.get(j), Integer.numberOfLeadingZeros(w) + 1));

    }

    public void offer(String input) {

    }

    public void offer(byte[] input) {

    }


    public static long getCardinality(final List<byte[]> input, final int numRegistersExponent) {
        if (!verifynumRegistersExponent(numRegistersExponent)) {
            throw new IllegalArgumentException("4 <= numRegistersExponent <= 16");
        }
        final int numRegisters = 1 << numRegistersExponent;
        // for simplicity of implementation, bits per register = 1 byte = 8
        final double correctionConstant = getCorrectionConstant(numRegisters);
        byte[] registers = new byte[numRegisters];
        int zeroRegisterCount = 0;

        for (byte[] v: input) {
            int x = MurmurHash3.murmurhash3_x86_32(v, 0, v.length, 0);
            int shift = Integer.SIZE - numRegistersExponent;
            int j = x >>> shift;
            // To prevent  0 hash value giving 32 leading zeros
            int w = (x << numRegistersExponent) | (1 << (numRegistersExponent - 1));
            registers[j] = (byte) Math.max(registers[j], Integer.numberOfLeadingZeros(w) + 1);
        }

        double indicator = 0.0;
        for (int i=0; i < numRegisters; ++i) {
            // 2 ^ -register[i]
            indicator += (1.0 / (1 << registers[i]));
            if (registers[i] == 0) zeroRegisterCount++;
        }
        indicator = 1/indicator;
        double E = correctionConstant * numRegisters * numRegisters * indicator;

        return Cardinality.correct(E, numRegisters, zeroRegisterCount);
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

    private static long correct(double E, int numOfRegisters, int emptyRegisters) {
        if (E <= (5.0/2.0) * numOfRegisters) {
            if (emptyRegisters != 0) {
                return Math.round(numOfRegisters * Math.log(numOfRegisters/Double.valueOf(emptyRegisters)));
            } else {
                return Double.valueOf(E).longValue();
            }
        }
        if (E <= (1.0/30.0) * (1l << 32)) {
            return Double.valueOf(E).longValue();
        } else {
            return Math.round(-(1l << 32) * Math.log(1 - E/(1l << 32)));
        }
    }
}
