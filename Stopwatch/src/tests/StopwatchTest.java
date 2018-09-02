/*
 * File: StopwatchTest.java
 */
package tests;

import static org.junit.Assert.*;

import org.aghannnam.stopwatch.Stopwatch;

import org.junit.Before;
import org.junit.Test;

/**
 * This class is a collection of unit tests for the Stopwatch class. 
 * 
 * @author Ahmed Ghannam
 */
public class StopwatchTest {
	
	private Stopwatch s; 
	
	@Before
	public void setUp() throws Exception {
		this.s = new Stopwatch();
	}
	
	/**
	 * Test method for {@link org.aghannnam.stopwatch.Stopwatch#reset()}.
	 */
	@Test
	public void testReset() {
		// Reset is already called by the constructor 
		assertFalse(s.isRunning()); 
	}

	/**
	 * Test method for {@link org.aghannnam.stopwatch.Stopwatch#start()}.
	 */
	@Test
	public void testStart() {
		s.start(); 
		assertTrue(s.isRunning());
	}

	/**
	 * Test method for {@link org.aghannnam.stopwatch.Stopwatch#restart()}.
	 */
	@Test
	public void testRestart() {
		s.restart();
		assertTrue(s.isRunning());
	}

	/**
	 * Test method for {@link org.aghannnam.stopwatch.Stopwatch#startNew()}.
	 */
	@Test
	public void testStartNew() {
		s = Stopwatch.startNew();
		assertTrue(s.isRunning());
	}

	/**
	 * Test method for {@link org.aghannnam.stopwatch.Stopwatch#stop()}.
	 */
	@Test
	public void testStop() {
		s.stop();
		assertFalse(s.isRunning());
	}

	/**
	 * Test method for {@link org.aghannnam.stopwatch.Stopwatch#elapsedSeconds()}.
	 * @throws InterruptedException 
	 */
	@Test
	public void testElapsedSeconds() throws InterruptedException {
		s.restart();
		Thread.sleep(1000);
		assertTrue(s.elapsedSeconds() >= 1);
		Thread.sleep(1000);
		assertTrue(s.elapsedSeconds() >= 2);
		s.stop();
		assertTrue(s.elapsedSeconds() >= 2);
		
		s.restart();
		assertFalse(s.elapsedSeconds() >= 2); 
	}

	/**
	 * Test method for {@link org.aghannnam.stopwatch.Stopwatch#elapsedMillis()}.
	 */
	@Test
	public void testElapsedMillis() throws InterruptedException {
		s.restart();
		Thread.sleep(1000);
		assertTrue(s.elapsedMillis() >= 999);
		Thread.sleep(1000);
		assertTrue(s.elapsedMillis() >= 1999); 
		s.stop();
		assertTrue(s.elapsedMillis() >= 1999);
		
		s.restart();
		assertFalse(s.elapsedMillis() >= 1999); 
	}
}