package com.github.szgabsz91.morpher.transformationengines.lattice.impl.testutils;

import org.slf4j.Logger;

@SuppressWarnings("PMD.LoggerIsNotStaticFinal")
public class LazyLogger {

    private final Logger logger;
    private final int interval;
    private Long lastTimestamp;

    public LazyLogger(Logger logger, int interval) {
        this.interval = interval;
        this.logger = logger;
    }

    public void log(String firstMessage, String subsequentMessages) {
        this.log(firstMessage, subsequentMessages, new Object[0]);
    }

    public void log(String firstMessage, String subsequentMessages, Object... objects) {
        if (this.lastTimestamp == null) {
            if (objects.length > 0) {
                logger.debug(firstMessage, objects);
            }
            else {
                logger.debug(firstMessage);
            }

            this.lastTimestamp = System.nanoTime();
            return;
        }

        final long currentTimestamp = System.nanoTime();
        final int elapsedSeconds = (int) ((currentTimestamp - this.lastTimestamp) / 1_000_000_000.0);

        if (elapsedSeconds >= interval) {
            if (objects.length > 0) {
                logger.debug(subsequentMessages, objects);
            }
            else {
                logger.debug(subsequentMessages);
            }

            this.lastTimestamp = currentTimestamp;
        }
    }

}
