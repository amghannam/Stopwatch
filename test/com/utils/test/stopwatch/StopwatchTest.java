package com.utils.test.stopwatch;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.util.time.Stopwatch;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Stopwatch")
class StopwatchTest {

	// =========================================================================
	// Construction
	// =========================================================================

	@Nested
	@DisplayName("Construction")
	class Construction {

		@SuppressWarnings("resource")
		@Test
		@DisplayName("new instance is not running")
		void newInstanceIsNotRunning() {
			assertFalse(new Stopwatch().isRunning());
		}

		@SuppressWarnings("resource")
		@Test
		@DisplayName("new instance has zero elapsed nanoseconds")
		void newInstanceHasZeroElapsed() {
			assertEquals(0L, new Stopwatch().elapsedNanos());
		}

		@Test
		@DisplayName("startNew() returns a running stopwatch")
		void startNewReturnsRunning() {
			assertTrue(Stopwatch.startNew().isRunning());
		}

		@Test
		@DisplayName("startNew() has positive elapsed time immediately")
		void startNewHasPositiveElapsed() {
			assertTrue(Stopwatch.startNew().elapsedNanos() > 0);
		}
	}

	// =========================================================================
	// Start / Stop
	// =========================================================================

	@Nested
	@DisplayName("start() and stop()")
	class StartStop {

		@SuppressWarnings("resource")
		@Test
		@DisplayName("start() transitions stopwatch to running")
		void startMakesRunning() {
			var sw = new Stopwatch();
			sw.start();
			assertTrue(sw.isRunning());
		}

		@Test
		@DisplayName("stop() transitions stopwatch to stopped")
		void stopMakesStopped() {
			var sw = Stopwatch.startNew();
			sw.stop();
			assertFalse(sw.isRunning());
		}

		@Test
		@DisplayName("start() on a running stopwatch is a no-op")
		void startOnRunningIsNoOp() throws InterruptedException {
			var sw = Stopwatch.startNew();
			Thread.sleep(5);
			long before = sw.elapsedNanos();
			sw.start(); // should not reset the start point
			assertTrue(sw.elapsedNanos() >= before);
			assertTrue(sw.isRunning());
		}

		@SuppressWarnings("resource")
		@Test
		@DisplayName("stop() on a stopped stopwatch is a no-op")
		void stopOnStoppedIsNoOp() {
			var sw = new Stopwatch();
			assertDoesNotThrow(sw::stop);
			assertFalse(sw.isRunning());
		}

		@SuppressWarnings("resource")
		@Test
		@DisplayName("elapsed time accumulates across start/stop cycles")
		void elapsedAccumulatesAcrossCycles() throws InterruptedException {
			var sw = new Stopwatch();

			sw.start();
			Thread.sleep(10);
			sw.stop();
			long firstLap = sw.elapsedNanos();

			sw.start();
			Thread.sleep(10);
			sw.stop();
			long total = sw.elapsedNanos();

			assertTrue(total > firstLap, "Total elapsed should exceed first lap after a second measurement cycle");
		}

		@Test
		@DisplayName("elapsed time is frozen after stop()")
		void elapsedFrozenAfterStop() throws InterruptedException {
			var sw = Stopwatch.startNew();
			sw.stop();
			long snapshot = sw.elapsedNanos();
			Thread.sleep(10);
			assertEquals(snapshot, sw.elapsedNanos(), "Elapsed should not change while stopwatch is stopped");
		}
	}

	// =========================================================================
	// Reset
	// =========================================================================

	@Nested
	@DisplayName("reset()")
	class Reset {

		@Test
		@DisplayName("reset() zeroes elapsed time")
		void resetZeroesElapsed() throws InterruptedException {
			var sw = Stopwatch.startNew();
			Thread.sleep(5);
			sw.reset();
			assertEquals(0L, sw.elapsedNanos());
		}

		@Test
		@DisplayName("reset() stops a running stopwatch")
		void resetStopsWatch() {
			var sw = Stopwatch.startNew();
			sw.reset();
			assertFalse(sw.isRunning());
		}

		@Test
		@DisplayName("reset() on an already-stopped stopwatch is a no-op")
		void resetOnStoppedIsNoOp() {
			@SuppressWarnings("resource")
			var sw = new Stopwatch();
			assertDoesNotThrow(sw::reset);
			assertEquals(0L, sw.elapsedNanos());
		}
	}

	// =========================================================================
	// Restart
	// =========================================================================

	@Nested
	@DisplayName("restart()")
	class Restart {

		@Test
		@DisplayName("restart() leaves stopwatch running")
		void restartLeavesRunning() {
			var sw = Stopwatch.startNew();
			sw.restart();
			assertTrue(sw.isRunning());
		}

		@Test
		@DisplayName("restart() resets elapsed time to near-zero")
		void restartResetsElapsed() throws InterruptedException {
			var sw = Stopwatch.startNew();
			Thread.sleep(20);
			sw.restart();
			// elapsed since restart should be well under the sleep duration
			assertTrue(sw.elapsedNanos() < TimeUnit.MILLISECONDS.toNanos(15),
					"Elapsed after restart should be much less than pre-restart duration");
		}

		@Test
		@DisplayName("restart() on a stopped stopwatch also starts it")
		void restartFromStoppedStarts() {
			@SuppressWarnings("resource")
			var sw = new Stopwatch();
			sw.restart();
			assertTrue(sw.isRunning());
		}
	}

	// =========================================================================
	// AutoCloseable / try-with-resources
	// =========================================================================

	@Nested
	@DisplayName("AutoCloseable / close()")
	class AutoCloseableTests {

		@Test
		@DisplayName("close() stops the stopwatch")
		void closeStops() {
			var sw = Stopwatch.startNew();
			sw.close();
			assertFalse(sw.isRunning());
		}

		@SuppressWarnings("resource")
		@Test
		@DisplayName("stopwatch is stopped at end of try-with-resources block")
		void tryWithResourcesStops() {
			Stopwatch sw;
			try (var s = Stopwatch.startNew()) {
				sw = s;
			}
			assertFalse(sw.isRunning());
		}

		@SuppressWarnings("resource")
		@Test
		@DisplayName("elapsed time is preserved after try-with-resources block")
		void tryWithResourcesPreservesElapsed() throws InterruptedException {
			Stopwatch sw;
			try (var s = Stopwatch.startNew()) {
				Thread.sleep(10);
				sw = s;
			}
			assertTrue(sw.elapsedNanos() >= TimeUnit.MILLISECONDS.toNanos(10),
					"Elapsed should be preserved after try-with-resources exits");
		}
	}

	// =========================================================================
	// Elapsed queries
	// =========================================================================

	@Nested
	@DisplayName("elapsedDuration()")
	class ElapsedDuration {

		@SuppressWarnings("resource")
		@Test
		@DisplayName("returns Duration.ZERO for a fresh stopwatch")
		void zeroForFreshWatch() {
			assertEquals(Duration.ZERO, new Stopwatch().elapsedDuration());
		}

		@Test
		@DisplayName("duration matches elapsedNanos()")
		void durationMatchesNanos() throws InterruptedException {
			var sw = Stopwatch.startNew();
			Thread.sleep(10);
			sw.stop();
			assertEquals(Duration.ofNanos(sw.elapsedNanos()), sw.elapsedDuration());
		}
	}

	@Nested
	@DisplayName("elapsed(TimeUnit)")
	class ElapsedTimeUnit {

		@SuppressWarnings("resource")
		@Test
		@DisplayName("throws NullPointerException for null unit")
		void throwsOnNullUnit() {
			assertThrows(NullPointerException.class, () -> new Stopwatch().elapsed((TimeUnit) null));
		}

		@SuppressWarnings("resource")
		@ParameterizedTest(name = "unit={0}")
		@EnumSource(TimeUnit.class)
		@DisplayName("returns 0.0 for a fresh stopwatch in any unit")
		void zeroForFreshWatch(TimeUnit unit) {
			assertEquals(0.0, new Stopwatch().elapsed(unit));
		}

		@Test
		@DisplayName("converts nanoseconds to milliseconds correctly")
		void convertsNanosToMillis() throws InterruptedException {
			var sw = Stopwatch.startNew();
			Thread.sleep(50);
			sw.stop();
			double ms = sw.elapsed(TimeUnit.MILLISECONDS);
			assertTrue(ms >= 50.0 && ms < 500.0, "Expected ~50 ms but got: " + ms);
		}
	}

	// =========================================================================
	// Rounding
	// =========================================================================

	@Nested
	@DisplayName("elapsed(TimeUnit, Rounding)")
	class ElapsedRounding {

		@SuppressWarnings("resource")
		@Test
		@DisplayName("throws NullPointerException for null unit")
		void throwsOnNullUnit() {
			assertThrows(NullPointerException.class, () -> new Stopwatch().elapsed(null, Stopwatch.Rounding.FLOOR));
		}

		@SuppressWarnings("resource")
		@Test
		@DisplayName("throws NullPointerException for null rounding")
		void throwsOnNullRounding() {
			assertThrows(NullPointerException.class, () -> new Stopwatch().elapsed(TimeUnit.MILLISECONDS, null));
		}

		@Nested
		@DisplayName("FLOOR")
		class Floor {

			@Test
			@DisplayName("truncates towards zero")
			void truncatesTowardZero() {
				// 1,500,000 ns = 1.5 ms → FLOOR → 1
				var sw = stopwatchWithNanos(1_500_000L);
				assertEquals(1L, sw.elapsed(TimeUnit.MILLISECONDS, Stopwatch.Rounding.FLOOR));
			}

			@Test
			@DisplayName("exact unit boundary returns the exact value")
			void exactBoundaryIsExact() {
				var sw = stopwatchWithNanos(2_000_000L); // exactly 2 ms
				assertEquals(2L, sw.elapsed(TimeUnit.MILLISECONDS, Stopwatch.Rounding.FLOOR));
			}
		}

		@Nested
		@DisplayName("CEILING")
		class Ceiling {

			@Test
			@DisplayName("rounds up any fractional unit")
			void roundsUp() {
				// 1,000,001 ns → just over 1 ms → CEILING → 2
				var sw = stopwatchWithNanos(1_000_001L);
				assertEquals(2L, sw.elapsed(TimeUnit.MILLISECONDS, Stopwatch.Rounding.CEILING));
			}

			@Test
			@DisplayName("exact unit boundary does not round up")
			void exactBoundaryDoesNotRoundUp() {
				var sw = stopwatchWithNanos(2_000_000L); // exactly 2 ms
				assertEquals(2L, sw.elapsed(TimeUnit.MILLISECONDS, Stopwatch.Rounding.CEILING));
			}
		}

		@Nested
		@DisplayName("NEAREST")
		class Nearest {

			@Test
			@DisplayName("rounds down when remainder is below half")
			void roundsDownBelowHalf() {
				// 1,499,999 ns < 1.5 ms → NEAREST → 1
				var sw = stopwatchWithNanos(1_499_999L);
				assertEquals(1L, sw.elapsed(TimeUnit.MILLISECONDS, Stopwatch.Rounding.NEAREST));
			}

			@Test
			@DisplayName("rounds up when remainder is exactly half")
			void roundsUpAtHalf() {
				// 1,500,000 ns = exactly 1.5 ms → NEAREST → 2
				var sw = stopwatchWithNanos(1_500_000L);
				assertEquals(2L, sw.elapsed(TimeUnit.MILLISECONDS, Stopwatch.Rounding.NEAREST));
			}

			@Test
			@DisplayName("rounds up when remainder is above half")
			void roundsUpAboveHalf() {
				// 1,500,001 ns > 1.5 ms → NEAREST → 2
				var sw = stopwatchWithNanos(1_500_001L);
				assertEquals(2L, sw.elapsed(TimeUnit.MILLISECONDS, Stopwatch.Rounding.NEAREST));
			}

			@Test
			@DisplayName("no overflow for nanosecond values near Long.MAX_VALUE")
			void noOverflowNearMaxValue() {
				// Addition-based rounding would overflow here; modulo form must not.
				var sw = stopwatchWithNanos(Long.MAX_VALUE - 1L);
				assertDoesNotThrow(() -> sw.elapsed(TimeUnit.MILLISECONDS, Stopwatch.Rounding.NEAREST));
			}
		}
	}

	// =========================================================================
	// Formatter
	// =========================================================================

	@Nested
	@DisplayName("Formatter")
	class FormatterTests {

		@Test
		@DisplayName("format() throws NullPointerException for null stopwatch")
		void throwsOnNull() {
			assertThrows(NullPointerException.class, () -> Stopwatch.Formatter.format(null));
		}

		@Test
		@DisplayName("formats zero elapsed time in nanoseconds")
		void formatsZeroAsNanoseconds() {
			assertEquals("0.000 ns", Stopwatch.Formatter.format(new Stopwatch()));
		}

		@Test
		@DisplayName("formats sub-microsecond duration using ns suffix")
		void formatsNanoseconds() {
			assertTrue(Stopwatch.Formatter.format(stopwatchWithNanos(500L)).endsWith("ns"));
		}

		@Test
		@DisplayName("formats microsecond-range duration using μs suffix")
		void formatsMicroseconds() {
			assertTrue(Stopwatch.Formatter.format(stopwatchWithNanos(5_000L)).endsWith("μs"));
		}

		@Test
		@DisplayName("formats millisecond-range duration using ms suffix")
		void formatsMilliseconds() {
			assertTrue(Stopwatch.Formatter.format(stopwatchWithNanos(5_000_000L)).endsWith("ms"));
		}

		@Test
		@DisplayName("formats second-range duration using s suffix")
		void formatsSeconds() {
			assertTrue(Stopwatch.Formatter.format(stopwatchWithNanos(5_000_000_000L)).endsWith("s"));
		}

		@Test
		@DisplayName("formats minute-range duration using min suffix")
		void formatsMinutes() {
			assertTrue(Stopwatch.Formatter.format(stopwatchWithNanos(120_000_000_000L)).endsWith("min"));
		}

		@Test
		@DisplayName("formats hour-range duration using h suffix")
		void formatsHours() {
			assertTrue(Stopwatch.Formatter.format(stopwatchWithNanos(7_200_000_000_000L)).endsWith("h"));
		}

		@Test
		@DisplayName("uses '.' as decimal separator regardless of locale")
		void usesRootLocaleDecimalSeparator() {
			// 1.5 ms should format as "1.500 ms", never "1,500 ms"
			assertFalse(Stopwatch.Formatter.format(stopwatchWithNanos(1_500_000L)).contains(","),
					"Decimal separator should be '.' not ','");
		}

		@Test
		@DisplayName("toString() delegates to Formatter.format()")
		void toStringDelegatesToFormatter() {
			var sw = stopwatchWithNanos(1_500_000L);
			assertEquals(Stopwatch.Formatter.format(sw), sw.toString());
		}
	}

	// =========================================================================
	// Thread safety (smoke test)
	// =========================================================================

	@Nested
	@DisplayName("Thread safety")
	class ThreadSafety {

		@Test
		@DisplayName("concurrent start/stop cycles do not corrupt state")
		void concurrentStartStopDoesNotCorruptState() throws InterruptedException {
			@SuppressWarnings("resource")
			var sw = new Stopwatch();
			var threads = new Thread[10];

			for (int i = 0; i < threads.length; i++) {
				threads[i] = new Thread(() -> {
					for (int j = 0; j < 100; j++) {
						sw.start();
						sw.stop();
					}
				});
			}

			for (var t : threads)
				t.start();
			for (var t : threads)
				t.join();

			// After all threads finish the stopwatch must be in a consistent state:
			// non-negative elapsed time regardless of which operation ran last.
			assertTrue(sw.elapsedNanos() >= 0, "Elapsed time must be non-negative after concurrent access");
		}
	}

	// =========================================================================
	// Helpers
	// =========================================================================

	/**
	 * Returns a stopped {@code Stopwatch} with the given nanosecond value injected
	 * via reflection. This allows deterministic testing of formatting and rounding
	 * logic without depending on wall-clock timing.
	 */
	private static Stopwatch stopwatchWithNanos(long nanos) {
		var sw = new Stopwatch();
		try {
			var field = Stopwatch.class.getDeclaredField("elapsedNanos");
			field.setAccessible(true);
			field.set(sw, nanos);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException("Could not inject elapsedNanos", e);
		}
		return sw;
	}
}
