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
    public void testGetCardinalityBillionNumbers() throws Exception {
        Cardinality cardinality = new Cardinality(16);
        int size = 1000000000;
        Random random = new Random();
        IntStream.range(0, size)
                .forEach(value -> cardinality.offer(random.nextLong()));
        long estimate = cardinality.getCardinalitySync();
        double error = Math.abs(size - estimate)/Double.valueOf(size);
        assertTrue(error < 0.02, String.format("Error %f was not less than expected error %f", error, 0.02));
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