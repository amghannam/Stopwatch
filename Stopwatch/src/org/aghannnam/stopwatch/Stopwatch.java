/*
 * File: Stopwatch.java
 */
package org.aghannnam.stopwatch;

/**
 * This class implements a simple stopwatch utility intended for accurate
 * measurement of code execution time.
 * 
 * @author Ahmed Ghannam
 */
public class Stopwatch {
	/**
	 * The number of nanoseconds in one millisecond.
	 */
	private static final double NANOS_PER_MILLI = 1000000.0;

	/**
	 * Whether or not the current instance is running.
	 */
	private boolean running;

	/**
	 * The total elapsed time in nanoseconds.
	 */
	private long elapsed;

	/**
	 * The start timestamp, as determined by a call to
	 * <code>System.nanoTime()</code>.
	 */
	private long start;

	/**
	 * Constructs a new <code>Stopwatch</code> instance.
	 */
	public Stopwatch() {
		reset();
	}

	/**
	 * Stops time interval measurement and resets the elapsed time to zero.
	 */
	public void reset() {
		elapsed = 0;
		running = false;
		start = 0;
	}

	/**
	 * Stops time interval measurement, resets the elapsed time to zero, and
	 * starts measuring elapsed time again.
	 */
	public void restart() {
		reset();
		start();
	}

	/**
	 * Starts, or resumes, measuring elapsed time for an interval.
	 */
	public void start() {
		// Calling start() on a running stopwatch is a no-op
		if (!isRunning()) {
			running = true;
			start = System.nanoTime();
		}
	}

	/**
	 * Initializes a new <code>Stopwatch</code> instance, sets the elapsed time
	 * to zero, and starts measuring elapsed time.
	 * 
	 * @return a new <code>Stopwatch</code> instance
	 */
	public static Stopwatch startNew() {
		Stopwatch s = new Stopwatch();
		s.start();
		return s;
	}

	/**
	 * Stops measuring elapsed time for an interval.
	 */
	public void stop() {
		// Calling stop() on a stopped stopwatch is a no-op
		if (isRunning()) {
			long elapsedThisPeriod = System.nanoTime() - start;
			elapsed += elapsedThisPeriod;
			running = false;
		}
	}

	/**
	 * Returns the total elapsed time measured by the current instance, in
	 * nanoseconds.
	 * 
	 * @return the total elapsed time in nanoseconds
	 */
	public long elapsed() {
		return elapsed;
	}

	/**
	 * Returns the total elapsed time measured by the current instance, in
	 * milliseconds.
	 * 
	 * @return the total elapsed time in milliseconds
	 */
	public double elapsedMilliseconds() {
		return elapsed / NANOS_PER_MILLI;
	}

	/**
	 * Returns <code>true</code> if the current <code>Stopwatch</code> instance
	 * is running.
	 * 
	 * @return <code>true</code> if the stopwatch is running, or
	 *         <code>false</code> otherwise
	 */
	public boolean isRunning() {
		return running == true;
	}
}
