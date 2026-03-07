package com.util.time;

import java.time.Duration;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * A high-resolution stopwatch utility backed by {@link System#nanoTime()}.
 *
 * <p>
 * A stopwatch can be started, stopped, and restarted any number of times.
 * Elapsed time accumulates across start/stop cycles until {@link #reset()} is
 * called. The stopwatch is <em>not</em> automatically reset on
 * {@link #start()}; use {@link #restart()} for that behavior.
 *
 * <h2>Basic usage</h2>
 * 
 * <pre>{@code
 * Stopwatch sw = Stopwatch.startNew();
 * doWork();
 * sw.stop(); // optional call
 * System.out.println(sw); // e.g. "1.234 ms"
 * System.out.println(sw.elapsed(TimeUnit.MILLISECONDS)); // 1.234
 * }</pre>
 *
 * <h2>Try-with-resources</h2>
 * 
 * <pre>{@code
 * try (Stopwatch sw = Stopwatch.startNew()) {
 * 	doWork();
 * } // stop() called automatically
 * }</pre>
 *
 * <h2>Thread safety</h2> All public methods are thread-safe. Elapsed-time reads
 * are consistent with respect to concurrent start/stop operations.
 *
 * @see Formatter
 * @see Rounding
 * @author Ahmed Ghannam
 * @version 2.0
 */
public final class Stopwatch implements AutoCloseable {

	/**
	 * The {@link System#nanoTime()} value recorded when the stopwatch was last
	 * started, adjusted so that {@code System.nanoTime() - startNanos} always
	 * equals the total accumulated elapsed time while the watch is running.
	 */
	private long startNanos;

	/**
	 * The total elapsed time recorded so far in nanoseconds, valid only while the
	 * stopwatch is <em>not</em> running. When running, the live value is computed
	 * as {@code System.nanoTime() - startNanos}.
	 */
	private long elapsedNanos;

	/**
	 * Whether the stopwatch is currently measuring time.
	 */
	private boolean running;

	/**
	 * Constructs a new, idle {@code Stopwatch} instance.
	 */
	public Stopwatch() {
		running = false;
		startNanos = 0L;
		elapsedNanos = 0L;
	}

	// -------------------------------------------------------------------------
	// Factory
	// -------------------------------------------------------------------------

	/**
	 * Creates a new {@code Stopwatch} and immediately starts it.
	 *
	 * <p>
	 * This is a convenience factory method equivalent to:
	 * 
	 * <pre>{@code
	 * Stopwatch sw = new Stopwatch();
	 * sw.start();
	 * }</pre>
	 *
	 * @return a new, running {@code Stopwatch} instance
	 */
	public static Stopwatch startNew() {
		final var sw = new Stopwatch();
		sw.start();
		return sw;
	}

	// -------------------------------------------------------------------------
	// Lifecycle
	// -------------------------------------------------------------------------

	/**
	 * Starts, or resumes, measuring elapsed time for the current instance.
	 *
	 * <p>
	 * Calling {@code start()} on a stopwatch that is already running is a no-op;
	 * elapsed time continues to accumulate uninterrupted.
	 */
	public synchronized void start() {
		if (!running) {
			startNanos = System.nanoTime() - elapsedNanos;
			running = true;
		}
	}

	/**
	 * Resets the elapsed time to zero and stops the stopwatch.
	 *
	 * <p>
	 * Invoking this method puts the stopwatch in the same state as a newly
	 * constructed instance.
	 */
	public synchronized void reset() {
		running = false;
		startNanos = 0L;
		elapsedNanos = 0L;
	}

	/**
	 * Resets the elapsed time to zero and immediately starts measuring again.
	 *
	 * <p>
	 * If the stopwatch is not running, this method produces the same state as
	 * invoking {@link #start()} on a newly constructed stopwatch, discarding any
	 * previously recorded elapsed time.
	 */
	public synchronized void restart() {
		elapsedNanos = 0L;
		startNanos = System.nanoTime();
		running = true;
	}

	/**
	 * Stops the stopwatch; called automatically at the end of a try-with-resources
	 * block.
	 *
	 * <p>
	 * Implements {@link AutoCloseable} so that {@code Stopwatch} can be used as a
	 * resource:
	 * 
	 * <pre>{@code
	 * try (Stopwatch sw = Stopwatch.startNew()) {
	 * 	doWork();
	 * }
	 * }</pre>
	 */
	@Override
	public void close() {
		stop();
	}

	/**
	 * Stops measuring elapsed time for the current instance.
	 *
	 * <p>
	 * The accumulated elapsed time is preserved and can be resumed by calling
	 * {@link #start()} again. Calling {@code stop()} on a stopwatch that is already
	 * stopped is a no-op.
	 * 
	 * @apiNote Calling this method is <em>optional</em> for retrieving elapsed
	 *          time; elapsed values can be queried while the stopwatch is running.
	 *          However, {@code stop()} freezes the current value and prevents
	 *          further accumulation until restarted.
	 */
	public synchronized void stop() {
		if (running) {
			elapsedNanos = System.nanoTime() - startNanos;
			running = false;
		}
	}

	// -------------------------------------------------------------------------
	// Elapsed-time queries
	// -------------------------------------------------------------------------

	/**
	 * Returns the elapsed time in the given {@link TimeUnit} as a {@code double}
	 * value, preserving fractional units.
	 *
	 * <p>
	 * For integer results with explicit rounding, use
	 * {@link #elapsed(TimeUnit, Rounding)}.
	 *
	 * @param unit the desired unit of time (non-null)
	 * @return the elapsed time in the specified unit
	 * @throws NullPointerException if {@code unit} is null
	 */
	public double elapsed(TimeUnit unit) {
		Objects.requireNonNull(unit, "unit");
		return (double) elapsedNanos() / unit.toNanos(1L);
	}

	/**
	 * Returns the elapsed time in the given {@link TimeUnit} as a {@code long}
	 * value, using the specified {@link Rounding} mode to handle fractional units.
	 *
	 * @param unit     the desired unit of time (non-null)
	 * @param rounding the rounding strategy to apply (non-null)
	 * @return the elapsed time, rounded according to {@code rounding}
	 * @throws NullPointerException if either argument is null
	 */
	public long elapsed(TimeUnit unit, Rounding rounding) {
		Objects.requireNonNull(unit, "unit");
		Objects.requireNonNull(rounding, "rounding");

		long nanos = elapsedNanos();
		long unitNanos = unit.toNanos(1L);

		return switch (rounding) {
		case Rounding.FLOOR -> nanos / unitNanos;
		case Rounding.CEILING -> (nanos + unitNanos - 1L) / unitNanos;
		// Addition-based rounding (nanos + unitNanos/2) can overflow for
		// extreme durations. The modulo form is equivalent and overflow-safe.
		case Rounding.NEAREST -> {
			long q = nanos / unitNanos;
			long r = nanos % unitNanos;
			yield (r >= unitNanos / 2L) ? q + 1L : q;
		}
		};
	}

	/**
	 * Returns the elapsed time as a {@link Duration}.
	 *
	 * @return the elapsed duration
	 */
	public Duration elapsedDuration() {
		return Duration.ofNanos(elapsedNanos());
	}

	/**
	 * Returns the total elapsed time measured so far, in nanoseconds.
	 *
	 * <p>
	 * If the stopwatch is running, the value is computed live; if it is stopped,
	 * the value recorded at the last {@link #stop()} call is returned.
	 *
	 * @return the elapsed time in nanoseconds (guaranteed non-negative under normal
	 *         operating conditions)
	 */
	public synchronized long elapsedNanos() {
		return running ? (System.nanoTime() - startNanos) : elapsedNanos;
	}

	// -------------------------------------------------------------------------
	// State query
	// -------------------------------------------------------------------------

	/**
	 * Returns {@code true} if the stopwatch is currently measuring time.
	 *
	 * @return {@code true} if running, {@code false} if stopped
	 */
	public synchronized boolean isRunning() {
		return running;
	}

	// -------------------------------------------------------------------------
	// Object
	// -------------------------------------------------------------------------

	/**
	 * Returns a human-readable representation of the elapsed time, delegating to
	 * {@link Formatter#format(Stopwatch)}.
	 *
	 * <p>
	 * The unit is chosen automatically to produce a compact, readable value (e.g.
	 * {@code "1.234 ms"}, {@code "3.500 s"}, {@code "72.00 min"}).
	 *
	 * @return a formatted elapsed-time string
	 */
	@Override
	public String toString() {
		return Formatter.format(this);
	}

	// =========================================================================
	// Nested types
	// =========================================================================

	/**
	 * Rounding modes used by {@link Stopwatch#elapsed(TimeUnit, Rounding)} when
	 * converting a nanosecond count to a coarser {@link TimeUnit}.
	 */
	public enum Rounding {
		/**
		 * Round towards zero (truncate).
		 */
		FLOOR,

		/**
		 * Round away from zero (always round up).
		 */
		CEILING,

		/**
		 * Round to the nearest unit (half-up).
		 */
		NEAREST
	}

	/**
	 * Stateless formatting utility for {@link Stopwatch} instances.
	 *
	 * <p>
	 * This class is responsible solely for the presentation of elapsed-time values;
	 * it has no knowledge of how time is measured or accumulated. The separation
	 * keeps {@link Stopwatch} focused on timing concerns while allowing the display
	 * logic to evolve independently.
	 *
	 * <p>
	 * The primary entry point is {@link #format(Stopwatch)}, which chooses an
	 * appropriate unit automatically and formats the result to four significant
	 * figures using a fixed {@link Locale#ROOT} locale so that the decimal
	 * separator is always {@code '.'} regardless of the JVM's default locale.
	 *
	 * <p>
	 * This class cannot be instantiated.
	 */
	public static final class Formatter {

		private Formatter() {
			throw new UnsupportedOperationException("Utility class");
		}

		/**
		 * Formats the elapsed time of {@code sw} using an automatically chosen unit
		 * from the ladder: ns → μs → ms → s → min → h → d.
		 *
		 * <p>
		 * The coarsest unit for which the elapsed value is ≥ 1 is selected (i.e. the
		 * ladder is walked from coarsest to finest and the first match is used),
		 * keeping the numeric portion compact and easy to read.
		 *
		 * @param sw the stopwatch to format (non-null)
		 * @return a formatted string such as {@code "1.234 ms"} or {@code "2.000 h"}
		 * @throws NullPointerException if {@code sw} is null
		 */
		public static String format(Stopwatch sw) {
			Objects.requireNonNull(sw, "sw");

			long nanos = sw.elapsedNanos();
			var unit = chooseUnit(nanos);
			double value = (double) nanos / unit.toNanos(1L);

			// Locale.ROOT ensures '.' as the decimal separator in all locales.
			return String.format(Locale.ROOT, "%.4g %s", value, abbreviate(unit));
		}

		/**
		 * Selects the coarsest {@link TimeUnit} for which the elapsed nanosecond count
		 * represents a value ≥ 1, walking from hours down to nanoseconds and returning
		 * the first match. Falls back to nanoseconds for very small durations where
		 * even microseconds round to zero.
		 *
		 * @param nanos raw elapsed nanoseconds
		 * @return the most readable {@link TimeUnit} for this duration
		 */
		private static TimeUnit chooseUnit(long nanos) {
			if (TimeUnit.NANOSECONDS.toDays(nanos) > 0)
				return TimeUnit.DAYS;
			if (TimeUnit.NANOSECONDS.toHours(nanos) > 0)
				return TimeUnit.HOURS;
			if (TimeUnit.NANOSECONDS.toMinutes(nanos) > 0)
				return TimeUnit.MINUTES;
			if (TimeUnit.NANOSECONDS.toSeconds(nanos) > 0)
				return TimeUnit.SECONDS;
			if (TimeUnit.NANOSECONDS.toMillis(nanos) > 0)
				return TimeUnit.MILLISECONDS;
			if (TimeUnit.NANOSECONDS.toMicros(nanos) > 0)
				return TimeUnit.MICROSECONDS;
			return TimeUnit.NANOSECONDS;
		}

		/**
		 * Returns the conventional abbreviation for the given {@link TimeUnit}.
		 *
		 * @param unit a time unit (non-null)
		 * @return abbreviated label, e.g. {@code "ms"}, {@code "μs"}, {@code "ns"}
		 * @throws AssertionError if an unexpected {@link TimeUnit} value is passed,
		 *                        which would indicate a programming error in
		 *                        {@link #chooseUnit(long)}
		 */
		private static String abbreviate(TimeUnit unit) {
			return switch (unit) {
			case TimeUnit.DAYS -> "d";
			case TimeUnit.HOURS -> "h";
			case TimeUnit.MINUTES -> "min";
			case TimeUnit.SECONDS -> "s";
			case TimeUnit.MILLISECONDS -> "ms";
			case TimeUnit.MICROSECONDS -> "μs";
			case TimeUnit.NANOSECONDS -> "ns";
			default -> throw new AssertionError("Unexpected unit: " + unit);
			};
		}
	}
}
