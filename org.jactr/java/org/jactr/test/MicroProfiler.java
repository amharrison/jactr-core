package org.jactr.test;

import java.io.PrintStream;
import java.util.concurrent.Callable;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

public class MicroProfiler {
	

	final private SummaryStatistics _stats = new SummaryStatistics();
	final private String _name;
	private long _warmUp = -1;

	private MicroProfiler(String name) {
		_name = name;
	}
	
	public void withWarmup(long warmup) {
	  _warmUp = warmup;
	}

	public void time(Runnable runner) {
		long start = System.nanoTime();
		try {
			runner.run();
		} finally {
			long delta = System.nanoTime() - start;
			double seconds = delta * 1E-9;
			_stats.addValue(seconds);
		}
	}

	public <T> T time(Callable<T> callable) throws Exception {
		long start = System.nanoTime();
		try {
			return callable.call();
		} finally {
			long delta = System.nanoTime() - start;
			double seconds = delta * 1E-6;
			_stats.addValue(seconds);
			//handle the warmup
			if(_stats.getN()==_warmUp)
			  _stats.clear();
		}
	}
	
	public SummaryStatistics getStats() {
	  return _stats;
	}

	public static MicroProfiler profiling(Object object) {

		return new MicroProfiler(object.toString());
	}

	public void dump(PrintStream err) {
		err.println(String.format("\nProfiling for %s : %.4fms (%.4f), (%.4f - %.4f)ms", _name, _stats.getMean(),
				_stats.getStandardDeviation(), _stats.getMin(), _stats.getMax()));

	}

}
