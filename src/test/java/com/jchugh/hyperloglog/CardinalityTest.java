package com.jchugh.hyperloglog;

import org.testng.annotations.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;

import static org.testng.Assert.*;

/**
 * Created by jchugh on 7/28/15.
 */
public class CardinalityTest {

    @Test
    public void testGetCardinality() throws Exception {
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
        assertEquals(Cardinality.getCardinality(input, 12), 8);
    }

    @Test
    public void testGetCardinalityNumbers() throws Exception {
        List<byte[]> input = new LinkedList<>();
        int size = 100000;
        Random random = new Random();
        IntStream.range(0, size)
                .forEach(value -> input.add(String.valueOf(random.nextLong()).getBytes()));
        long estimate = Cardinality.getCardinality(input, 16);
        double error = Math.abs(size - estimate)/Double.valueOf(size);
        double expectedError = 1.04/Math.sqrt(1<<16);
        assertTrue(error < expectedError);
    }

}