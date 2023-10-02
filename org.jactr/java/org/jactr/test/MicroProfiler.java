package org.jactr.test;

import java.io.PrintStream;
import java.util.concurrent.Callable;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.slf4j.LoggerFactory;

public class MicroProfiler {
	static private final transient org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MicroProfiler.class);

	final private SummaryStatistics _stats = new SummaryStatistics();
	final private String _name;

	private MicroProfiler(String name) {
		_name = name;
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
		}
	}

	public static MicroProfiler profiling(Object object) {

		return new MicroProfiler(object.toString());
	}

	public void dump(PrintStream err) {
		err.println(String.format("\nProfiling for %s : %.4fms (%.4f), (%.4f - %.4f)ms", _name, _stats.getMean(),
				_stats.getStandardDeviation(), _stats.getMin(), _stats.getMax()));

	}

}
