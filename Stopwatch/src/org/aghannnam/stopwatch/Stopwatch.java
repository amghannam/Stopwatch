/*
 * File: Stopwatch.java
 */
package org.aghannnam.stopwatch;

import java.util.concurrent.TimeUnit;

/**
 * This class implements a simple stopwatch utility intended for precise
 * measurement of code execution time.
 * 
 * <p>
 * Using {@code System.nanoTime} as its time source, it can measure execution
 * time for a single interval or across multiple intervals. An interval is
 * defined as a call to {@code start}, followed by the code segment to
 * benchmark, and ending with a call to {@code stop}. In the case of multiple
 * intervals, the calculated elapsed time value is cumulative unless
 * {@code restart} is called for the next interval.
 * 
 * <p>
 * The stopwatch can be queried whether it is running or after it has halted.
 * While running, the elapsed time value will steadily increase each time the
 * stopwatch is queried. This effectively means that calling {@code stop} is
 * optional, as it will only freeze the timer and retain the last recorded
 * duration. In other words, if the stopwatch is idle, querying it will return
 * the elapsed time value as of the last call to {@code stop}, whenever it was.
 *
 * <p>
 * Finally, elapsed time can be calculated in milliseconds, seconds, minutes, or
 * hours, as desired. These values are also convertible to other units at the
 * programmer's discretion.
 *
 * <p>
 * <b>Important:</b> Since this utility is built on top of
 * {@code System.nanoTime}, it is <i>not</i> thread-safe. Please use with
 * caution.
 * 
 * @author Ahmed Ghannam
 */
public class Stopwatch {
	/**
	 * The start timestamp, as determined by a call to {@code System.nanoTime}.
	 */
	private long start;

	/**
	 * The total elapsed time recorded so far in nanoseconds (raw value).
	 */
	private long elapsedNanos;

	/**
	 * Whether or not the stopwatch instance is running.
	 */
	private boolean running;

	/**
	 * Constructs a new, idle {@code Stopwatch} instance.
	 */
	public Stopwatch() {
		reset();
	}

	/**
	 * Stops time interval measurement, resets the elapsed time to zero, and starts
	 * measuring time again.
	 * 
	 * <p>
	 * This is a convenience method equivalent to calling {@code reset} followed by
	 * {@code start}.
	 */
	public void restart() {
		reset();
		start();
	}

	/**
	 * Stops time interval measurement and resets the elapsed time value to zero.
	 */
	public void reset() {
		running = false;
		start = 0;
		elapsedNanos = 0;
	}

	/**
	 * Initializes a new {@code Stopwatch} instance, sets the elapsed time value to
	 * zero, and starts measuring elapsed time.
	 * 
	 * @return a running {@code Stopwatch} instance
	 */
	public static Stopwatch startNew() {
		final var s = new Stopwatch();
		s.start();
		return s;
	}

	/**
	 * Starts, or resumes, measuring elapsed time for an interval.
	 */
	public void start() {
		// Calling start() on a running stopwatch is a no-op.
		if (!isRunning()) {
			running = true;
			start = start <= 0 ? System.nanoTime() 
					           : System.nanoTime() - elapsedNanos;
		}
	}

	/**
	 * Stops measuring elapsed time for an interval. This is an optional 
	 * operation. 
	 */
	public void stop() {
		// Calling stop() on a stopped stopwatch is a no-op.
		if (isRunning()) {
			elapsedNanos = System.nanoTime() - start;
			running = false;
		}
	}

	/**
	 * Returns the elapsed time measured so far by this stopwatch in the specified
	 * {@code TimeUnit}.
	 * 
	 * @param timeUnit the desired time unit
	 * @return the elapsed time value in the specified time unit
	 */
	public double elapsed(TimeUnit timeUnit) {
		// If the stopwatch is running, compute and return the current timestamp.
		// Otherwise, just return the timestamp as of the last call to stop().
		final long duration = isRunning() ? System.nanoTime() - start : elapsedNanos;
		return UnitConverter.convert(duration, timeUnit);
	}

	/**
	 * Returns {@code true} if the stopwatch is currently running.
	 * 
	 * @return {@code true} if this stopwatch is running, or {@code false} otherwise
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * Returns a string representation of the elapsed time measured so far by this
	 * stopwatch, expressed in either milliseconds or seconds using two decimal
	 * places.
	 * 
	 * <p>
	 * <b>Example:</b> An elapsed time of 1.2345 milliseconds would return a string
	 * exactly equal to {@code "1.23ms"}.
	 * 
	 * <p>
	 * This is meant as a convenience method that simplifies querying the stopwatch
	 * for reporting purposes.
	 * 
	 * @return a string representation of the current elapsed time, in milliseconds
	 *         or seconds
	 */
	@Override
	public String toString() {
		final double duration = elapsed(TimeUnit.MILLISECONDS);
		final boolean isInMillis = duration < 1000.0 ? true : false;
		final double result = isInMillis ? duration : duration / 1000.0;
		final double rounded = Math.round(result * 100d) / 100d;
		final String unit = isInMillis ? "ms" : "s";
		return String.valueOf(rounded).concat(unit);
	}

	/**
	 * An internal utility class to supplement the stopwatch's unit conversion
	 * operations.
	 * 
	 * @author Ahmed Ghannam
	 *
	 */
	private static class UnitConverter {
		/**
		 * The number of nanoseconds in a millisecond.
		 */
		static final double NANOS_PER_MILLI = 1e6;

		/**
		 * The number of nanoseconds in a second.
		 */
		static final double NANOS_PER_SECOND = 1e9;

		/**
		 * The number of nanoseconds in a minute.
		 */
		static final double NANOS_PER_MINUTE = 6e10;

		/**
		 * The number of nanoseconds in an hour.
		 */
		static final double NANOS_PER_HOUR = 3.6e12;

		/**
		 * Converts the specified nanosecond value to the specified time unit and
		 * returns the result.
		 * 
		 * @param nanoTime the value to convert (in nanoseconds)
		 * @param toUnit   the desired time unit
		 * @return the converted value
		 * @throws IllegalArgumentException if no valid time unit is given
		 */
		static double convert(long nanoTime, TimeUnit toUnit) {
			switch (toUnit) {
			case MILLISECONDS:
				return toMillis(nanoTime);
			case SECONDS:
				return toSeconds(nanoTime);
			case MINUTES:
				return toMinutes(nanoTime);
			case HOURS:
				return toHours(nanoTime);
			default:
				throw new IllegalArgumentException("no valid time unit specified");
			}
		}

		/*
		 *******************************************************
		 * 				Utility Conversion Methods 
		 *******************************************************
		 */

		static double toMillis(long nanos) {
			return nanos / NANOS_PER_MILLI;
		}

		static double toSeconds(long nanos) {
			return nanos / NANOS_PER_SECOND;
		}

		static double toMinutes(long nanos) {
			return nanos / NANOS_PER_MINUTE;
		}

		static double toHours(long nanos) {
			return nanos / NANOS_PER_HOUR;
		}
	}
}