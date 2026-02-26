package main;

import java.util.concurrent.TimeUnit;

import com.util.time.Stopwatch;

/**
 * Simple demo of the {@link Stopwatch} utility.
 * 
 * @author Ahmed Ghannam
 * @version 2.0
 */
public class StopwatchDemo {

	public static void main(String[] args) throws InterruptedException {

		var sw = Stopwatch.startNew();

		// Simulate work
		Thread.sleep(750);

		sw.stop(); // optional call

		System.out.println("Formatted: " + sw);
		System.out.println("Millis (double): " + sw.elapsed(TimeUnit.MILLISECONDS));
		System.out.println("Millis (floor): " + sw.elapsed(TimeUnit.MILLISECONDS, Stopwatch.Rounding.FLOOR));
		System.out.println("Nanos: " + sw.elapsedNanos());
		
		System.out.println("Restarting...");
		sw.restart();
		Thread.sleep(10000); // 10 seconds 
		
		sw.stop();
		
		System.out.println("Seconds (double): " + Stopwatch.Formatter.format(sw));
	}
}
