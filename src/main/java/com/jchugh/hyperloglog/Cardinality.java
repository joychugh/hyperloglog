package com.jchugh.hyperloglog;

import com.jchugh.hyperloglog.hashing.MurmurHash3;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.LinkedList;
import java.util.List;

/**
 * @author jchugh
 * This does not work right now,
 * When it will, There will be a test included.
 */
public class Cardinality {

    private static final BigDecimal A = BigDecimal.valueOf(0.7213);
    private static final BigDecimal ONE = BigDecimal.ONE;
    private static final BigDecimal B = BigDecimal.valueOf(1.079);

    public static long getCardinality(final List<byte[]> input, final int numRegistersExponent) {
        final int numRegisters = 1 << numRegistersExponent;
        // 0.7213/(1+1.079/2^numRegistersExponent for numRegisters > 128 ie. numRegistersExponent > 7)
        // for simplicity of implementation we will assume numRegistersExponent > 7 and bits per register = 1 byte = 8
        final BigDecimal correctionConstant = A.divide(ONE.add(B.divide(BigDecimal.valueOf(numRegisters), MathContext.DECIMAL32)), MathContext.DECIMAL32);
        byte[] registers = new byte[numRegisters];
        double zeroRegisterCount = 0.0;

        for (byte[] v: input) {
            int x = MurmurHash3.murmurhash3_x86_32(v, 0, v.length, 0);
            int shift = Integer.SIZE - numRegistersExponent;
            int j = x >>> shift;
            // To prevent  0 hash value giving 32 leading zeros
            int w = (x << numRegistersExponent) | (1 << (numRegistersExponent - 1));
            registers[j] = (byte) Math.max(registers[j], Integer.numberOfLeadingZeros(w) + 1);
        }

        BigDecimal indicator = BigDecimal.ZERO;
        for (int i=0; i < numRegisters; ++i) {
            indicator = indicator.add(BigDecimal.valueOf(2).pow(-registers[i], MathContext.DECIMAL32));
            if (registers[i] == 0) zeroRegisterCount++;
        }
        indicator = BigDecimal.ONE.divide(indicator, MathContext.DECIMAL32);
        BigDecimal E = correctionConstant.multiply(BigDecimal.valueOf(numRegisters * numRegisters)).multiply(indicator);

        return Cardinality.correct(E, numRegisters, zeroRegisterCount);
    }

    private static long correct(BigDecimal E, int numOfRegisters, double emptyRegisters) {
        if (E.longValue() <= (5.0/2.0) * numOfRegisters) {
            if (emptyRegisters != 0) {
                return Math.round(numOfRegisters * Math.log(numOfRegisters/emptyRegisters));
            } else {
                return E.longValue();
            }
        }
        if (E.longValue() <= (1.0/30.0) * (1l << 32)) {
            return E.longValue();
        } else {
            return Math.round(-(1l << 32) * Math.log(BigDecimal.ONE.subtract(E.divide(BigDecimal.valueOf(1l << 32), MathContext.DECIMAL32)).doubleValue()));
        }
    }
}
