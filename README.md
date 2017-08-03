## Overview
This repository contains a Java implementation of a ```Stopwatch``` utility class intended for precise measurement of code execution time. 
Using ```System.nanoTime()``` as its time source, this stopwatch supports the following basic operations:

- **Start**, which starts, or resumes, the timer. 
- **Stop**, which freezes the timer and records an elapsed time value. 
- **Reset**, which resets the timer, without starting again. 
- **Restart**, which resets and restarts the timer. 

The stopwatch can measure execution time for a single interval, or across multiple intervals. An *interval* is defined as a call to 
```start()```, followed by the code to benchmark, and ending with a call to ```stop()```. If you measure execution time across multiple 
intervals, the calculated elapsed time value is *cumulative* unless you ```restart()``` the timer for your next interval. 

You can query the stopwatch whether it is running or after it has halted. If the stopwatch is running, the elapsed time value will 
steadily increase each time the stopwatch is queried. This effectively means that calling ```stop()``` is optional; it will only freeze 
the timer and retain the last recorded elapsed time value. In other words, if the timer is idle, querying the stopwatch 
will return the elapsed time value as of the last call to ```stop()```, whenever it was. Elapsed time is calculated in either milliseconds or seconds, as desired. These values can also be converted to other units at your discretion. 

Finally, as the stopwatch uses Java's ```System.nanoTime()``` as its backbone, it is *not* thread-safe. 

### Help 
You are free to use this utility as you please. See license for details. For questions or inquiries, please contact me at 
amalghannam@crimson.ua.edu. 
