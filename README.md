# Stopwatch

A high-resolution elapsed-time utility for Java, backed by `System.nanoTime()`.

## Requirements

Java 21 or later.

## Overview

`Stopwatch` measures code execution time across one or more intervals. An *interval* is a
call to `start()`, the code under measurement, and a call to `stop()`. Elapsed time
accumulates across intervals and is reset only when `reset()` or `restart()` is called,
making it straightforward to time disjoint sections of code against a single total.

## Operations

| Method | Description |
|---|---|
| `start()` | Starts or resumes the timer. No-op if already running. |
| `stop()` | Freezes the timer and retains the elapsed value. No-op if already stopped. |
| `reset()` | Stops the timer and clears elapsed time to zero. |
| `restart()` | Clears elapsed time to zero and immediately starts timing again. |

## Querying Elapsed Time

The stopwatch can be queried at any time, whether running or stopped.

- **While running** — each call returns a live, increasing value.
- **While stopped** — each call returns the value frozen at the last `stop()`.

Elapsed time is available in several forms:

```java
sw.elapsedNanos();                                    // long — raw nanoseconds
sw.elapsedDuration();                                 // java.time.Duration
sw.elapsed(TimeUnit.MILLISECONDS);                    // double — fractional units
sw.elapsed(TimeUnit.SECONDS, Stopwatch.Rounding.FLOOR);   // long — with explicit rounding
```

`Rounding` has three modes: `FLOOR` (truncate), `CEILING` (round up), and `NEAREST` (half-up).

## Usage

### Basic

```java
Stopwatch sw = Stopwatch.startNew();
doWork();
sw.stop();
System.out.println(sw);  // e.g. "1.234 ms"
```

### Accumulating across intervals

```java
Stopwatch sw = new Stopwatch();

sw.start();
firstOperation();
sw.stop();

sw.start();
secondOperation();
sw.stop();

long total = sw.elapsedNanos(); // cumulative across both intervals
```

### Try-with-resources

`Stopwatch` implements `AutoCloseable`; `close()` is equivalent to `stop()`.

```java
try (Stopwatch sw = Stopwatch.startNew()) {
    doWork();
} // stop() called automatically
```

## Formatting

`toString()` delegates to the nested `Stopwatch.Formatter` class, which selects the
coarsest readable unit automatically and formats to four significant figures:

| Elapsed | Output |
|---|---|
| 500 ns | `500.0 ns` |
| 5,000 ns | `5.000 μs` |
| 5,000,000 ns | `5.000 ms` |
| 5,000,000,000 ns | `5.000 s` |
| 120,000,000,000 ns | `2.000 min` |
| 7,200,000,000,000 ns | `2.000 h` |
| 172,800,000,000,000 ns | `2.000 d` |

The decimal separator is always `.` regardless of the JVM's default locale.

## Thread Safety

All public methods are thread-safe. Elapsed-time reads are consistent with respect to
concurrent `start()` and `stop()` operations.

## License

See `LICENSE` for terms of use.