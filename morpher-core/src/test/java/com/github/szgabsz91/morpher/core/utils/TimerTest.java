package com.github.szgabsz91.morpher.core.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;
import static org.junit.Assert.fail;

public class TimerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimerTest.class);

    private Timer timer;

    @BeforeEach
    public void setUp() {
        this.timer = new Timer();
    }

    @Test
    public void testConstructorWithoutAutostart() throws InterruptedException {
        Thread.sleep(100);
        assertThat(timer.getMeasuredDuration()).isEqualTo(Duration.ZERO);
        assertThat(timer.getSeconds()).isEqualTo(0.0);
    }

    @Test
    public void testConstructorWithAutostart() throws InterruptedException {
        Timer timer = new Timer(true);
        assertThat(timer.getMeasuredDuration()).isEqualTo(Duration.ZERO);
        assertThat(timer.getSeconds()).isEqualTo(0.0);
        Thread.sleep(100);
        timer.stop();
        assertThat(timer.getMeasuredDuration().getNano()).isGreaterThan(0);
        assertThat(timer.getSeconds()).isGreaterThan(0.0);
    }

    @Test
    public void testReset() throws InterruptedException {
        timer.start();
        Thread.sleep(100);
        timer.stop();
        assertThat(timer.getMeasuredDuration().getNano()).isGreaterThan(0);
        assertThat(timer.getSeconds()).isGreaterThan(0.0);

        timer.reset();
        assertThat(timer.getMeasuredDuration()).isEqualTo(Duration.ZERO);
        assertThat(timer.getSeconds()).isEqualTo(0.0);
    }

    @Test
    public void testMeasureWithSupplier() {
        int expected = 100;
        Supplier<Integer> supplier = () -> expected;
        Integer result = timer.measure(supplier);
        assertThat(result).isEqualTo(expected);
        assertThat(timer.getMeasuredDuration().getNano()).isGreaterThan(0);
        assertThat(timer.getSeconds()).isGreaterThan(0.0);
    }

    @Test
    public void testMeasureWithRunnable() {
        Runnable runnable = () -> {
            try {
                Thread.sleep(100);
            }
            catch (InterruptedException e) {
                LOGGER.error("Thread.sleep interrupted", e);
                fail("Thread.sleep interrupted");
            }
        };
        timer.measure(runnable);
        assertThat(timer.getMeasuredDuration().getNano()).isGreaterThan(0);
        assertThat(timer.getSeconds()).isGreaterThan(0.0);
    }

    @Test
    public void testStartAndStop() throws InterruptedException {
        timer.start();
        Thread.sleep(100);
        timer.stop();

        assertThat((double) timer.getMeasuredDuration().toMillis()).isCloseTo(100.0, offset(20.0));
        assertThat(timer.getSeconds()).isCloseTo(0.1, offset(0.05));
    }

    @Test
    public void testMultipleMeasurePhases() {
        int measurePhases = 3;
        Runnable runnable = () -> {
            try {
                Thread.sleep(100);
            }
            catch (InterruptedException e) {
                LOGGER.error("Thread.sleep interrupted", e);
                fail("Thread.sleep interrupted");
            }
        };
        for (int i = 0; i < measurePhases; i++) {
            timer.measure(runnable);
        }
        assertThat((double) timer.getMeasuredDuration().toMillis()).isCloseTo(300.0, offset(30.0));
    }

    @Test
    public void testRevert() {
        timer.measure(() -> {
            try {
                Thread.sleep(100L);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        timer.revert();
        timer.revert();
        assertThat(timer.getMeasuredDuration().toMillis()).isEqualTo(0L);
    }

}
