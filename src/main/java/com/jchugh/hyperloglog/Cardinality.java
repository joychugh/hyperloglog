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

    public static int getCardinality(final List<byte[]> input, final int numRegistersExponent) {
        final int numRegisters = 1 << numRegistersExponent;
        // 0.7213/(1+1.079/2^numRegistersExponent for numRegisters > 128 ie. numRegistersExponent > 7)
        // for simplicity of implementation we will assume numRegistersExponent > 7 and bits per register = 1 byte = 8
        final BigDecimal correctionConstant = A.divide(ONE.add(B.divide(BigDecimal.valueOf(numRegisters), MathContext.DECIMAL32)), MathContext.DECIMAL32);
        byte[] registers = new byte[numRegisters];
        int zeroRegisterCount = 0;

        for (byte[] v: input) {
            int x = MurmurHash3.murmurhash3_x86_32(v, 0, v.length, 0);
            int shift = Integer.SIZE - numRegistersExponent;
            int j = x >>> shift;
            int w = x << numRegistersExponent;
            registers[j] = (byte) Math.max(registers[j], Integer.numberOfLeadingZeros(w) + 1);
        }

        BigDecimal sumStreams = BigDecimal.ZERO;
        for (int i=0; i < numRegisters; ++i) {
            sumStreams = sumStreams.add(BigDecimal.valueOf(2).pow(-registers[i], MathContext.DECIMAL32));
            if (registers[i] == 0) zeroRegisterCount ++;
        }
        sumStreams = BigDecimal.ONE.divide(sumStreams, MathContext.DECIMAL32);
        BigDecimal E = correctionConstant.multiply(BigDecimal.valueOf(numRegisters * numRegisters)).multiply(sumStreams);
        return E.intValue();
    }

    public static void main(String[] args) {
        List<byte[]> input = new LinkedList<byte[]>();
        input.add("a".getBytes());
        input.add("a".getBytes());
        input.add("a".getBytes());
        input.add("b".getBytes());
        input.add("c".getBytes());
        input.add("d".getBytes());
        input.add("b".getBytes());
        input.add("c".getBytes());
        input.add("d".getBytes());
        input.add("c".getBytes());
        input.add("b".getBytes());
        input.add("b".getBytes());
        input.add("b".getBytes());
        input.add("d".getBytes());
        input.add("d".getBytes());
        input.add("e".getBytes());
        input.add("f".getBytes());
        input.add("g".getBytes());
        input.add("h".getBytes());
        Cardinality.getCardinality(input, 12);



    }
}
