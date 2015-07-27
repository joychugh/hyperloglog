package com.jchugh.hyperloglog;

import com.jchugh.hyperloglog.hashing.MurmurHash3;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;

/**
 * @author jchugh
 */
public class Cardinality {

    private static final BigDecimal A = BigDecimal.valueOf(0.7213);
    private static final BigDecimal ONE = BigDecimal.ONE;
    private static final BigDecimal B = BigDecimal.valueOf(1.079);

    public static int Cardinality(final List<byte[]> input, final int numRegistersExponent) {;
        final BigDecimal numRegisters = BigDecimal.valueOf(Math.pow(2, numRegistersExponent));
        // 0.7213/(1+1.079/2^numRegistersExponent for numRegisters > 128 ie. numRegistersExponent > 7)
        // for simplicity of implementation we will assume numRegistersExponent > 7
        final BigDecimal correctionConstant = A.divide(ONE.add(B.divide(numRegisters, MathContext.DECIMAL32)), MathContext.DECIMAL32);
        byte[] registers = new byte[numRegistersExponent];
        int zeroRegisterCount = 0;

        for (byte[] v: input) {
            int x = MurmurHash3.murmurhash3_x86_32(v, 0, v.length, 0);
            int shift = x < 0 ? (32 - numRegistersExponent) : (31 - numRegistersExponent);
            int j = 1 + x >>> shift;
            int w = x & (1 << shift) - 1;
            registers[j] = (byte) Math.max(registers[j], Integer.numberOfLeadingZeros(w));
        }

        BigDecimal sumStreams = BigDecimal.ZERO;
        for (int i=0; i < numRegistersExponent; ++i) {
            sumStreams = sumStreams.add(BigDecimal.valueOf(2).pow(-registers[i]));
            if (registers[i] == 0) zeroRegisterCount ++;
        }
        sumStreams = sumStreams.divide(BigDecimal.ONE, MathContext.DECIMAL32);
        BigDecimal E = correctionConstant.multiply(numRegisters.pow(2)).multiply(sumStreams);
        return E.intValue();
    }

    public static void main(String[] args) {
        int x = MurmurHash3.murmurhash3_x86_32("nope".getBytes(), 0, "nope".getBytes().length, 0);
        int y = x >>> 28;
    }
}
