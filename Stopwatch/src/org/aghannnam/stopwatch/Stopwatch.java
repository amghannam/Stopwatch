/*
 * File: Stopwatch.java
 */
package org.aghannnam.stopwatch;

/**
 * This class implements a simple stopwatch utility intended for precise
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
	 * The number of milliseconds in one second.
	 */
	private static final double MILLIS_PER_SECOND = 1000.0;

	/**
	 * Whether or not the current instance is running.
	 */
	private boolean running;

	/**
	 * The total elapsed time, in nanoseconds (raw value).
	 */
	private long elapsed;

	/**
	 * The start timestamp, as determined by a call to {@code System.nanoTime()}.
	 */
	private long start;

	/**
	 * Constructs a new {@code Stopwatch} instance.
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
	 * Stops time interval measurement, resets the elapsed time to zero, and starts
	 * measuring elapsed time again.
	 * 
	 * <p>This is a convenience method equivalent to calling {@code reset()} 
	 * followed by {@code start()}.</p>
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
			start = start <= 0 ? System.nanoTime() : System.nanoTime() - elapsed;
		}
	}

	/**
	 * Initializes a new {@code Stopwatch} instance, sets the elapsed time to zero,
	 * and starts measuring elapsed time.
	 * 
	 * @return a new {@code Stopwatch} instance 
	 */
	public static Stopwatch startNew() {
		final var s = new Stopwatch();
		s.start();
		return s;
	}

	/**
	 * Stops measuring elapsed time for an interval.
	 */
	public void stop() {
		// Calling stop() on a stopped stopwatch is a no-op
		if (isRunning()) {
			elapsed = System.nanoTime() - start;
			running = false;
		}
	}

	/**
	 * Returns the total elapsed time measured so far by the current instance, in
	 * seconds.
	 *
	 * @return the total elapsed time in seconds
	 */
	public double elapsedSeconds() {
		return elapsedMillis() / MILLIS_PER_SECOND;
	}

	/**
	 * Returns the total elapsed time measured so far by the current instance, in
	 * milliseconds.
	 *
	 * @return the total elapsed time in milliseconds
	 */
	public double elapsedMillis() {
		// If the stopwatch is running, compute and return the current timestamp.
		// Otherwise, just return the timestamp as of the last call to stop().
		return isRunning() ? (System.nanoTime() - start) / NANOS_PER_MILLI
				: elapsed / NANOS_PER_MILLI;
	}

	/**
	 * Returns {@code true} if the current {@code Stopwatch} instance is running.
	 *
	 * @return {@code true} if the stopwatch is running, or {@code false} otherwise
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * Returns a string representation of the current elapsed time, expressed in
	 * milliseconds using two decimal places.
	 *
	 * <p>Example: 1.2345 would return a string value equal to {@code "1.23 ms"}.
	 * </p>
	 *
	 * @return a string representation of the elapsed time in milliseconds
	 */
	@Override
	public String toString() {
		final double rounded = Math.round(elapsedMillis() * 100d) / 100d;
		return String.valueOf(rounded).concat(" ms");
	}
}