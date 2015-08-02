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
    public void testGetCardinalityNumbers_() throws Exception {
        Cardinality cardinality = new Cardinality(16);
        int size = 100000;
        Random random = new Random();
        IntStream.range(0, size)
                .forEach(value -> cardinality.offer(String.valueOf(random.nextLong()).getBytes()));
        long estimate = cardinality.getCardinalitySync();
        double error = Math.abs(size - estimate)/Double.valueOf(size);
        double expectedError = 1.04/Math.sqrt(1<<16);
        assertTrue(error < expectedError);
    }

    @Test
    public void testGetCardinality_() throws Exception {
        Cardinality cardinality = new Cardinality(12);
        cardinality.offer("a".getBytes());
        cardinality.offer("a".getBytes());
        cardinality.offer("a".getBytes());
        cardinality.offer("b".getBytes());
        cardinality.offer("c".getBytes());
        cardinality.offer("d".getBytes());
        cardinality.offer("b".getBytes());
        cardinality.offer("c".getBytes());
        cardinality.offer("d".getBytes());
        cardinality.offer("c".getBytes());
        cardinality.offer("b".getBytes());
        cardinality.offer("b".getBytes());
        cardinality.offer("b".getBytes());
        cardinality.offer("d".getBytes());
        cardinality.offer("d".getBytes());
        cardinality.offer("e".getBytes());
        cardinality.offer("f".getBytes());
        cardinality.offer("g".getBytes());
        cardinality.offer("h".getBytes());
        assertEquals(cardinality.getCardinalitySync(), 8);
    }

}