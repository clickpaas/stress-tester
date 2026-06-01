package com.taobao.stresstester.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 测试工作线程
 * */
class StressThreadWorker implements Runnable {

	private StressTask service;
	private CyclicBarrier threadStartBarrier;
	private CountDownLatch threadEndLatch;
	private AtomicInteger failedCounter = null;
	private AtomicInteger completedCounter = null;
	private int count;
	private long thinkTimeMs; // 思考时间（毫秒）
	private double maxErrorRate; // 错误率阈值(0-100)，0表示不启用
	private AtomicBoolean aborted = null; // 终止标志
	// private static Log log = LogFactory.getLog(StressThreadWorker.class);
	private static final Logger log = LoggerFactory.getLogger(StressThreadWorker.class);

	private List<Long> everyTimes;

	public StressThreadWorker(StressContext stressContext, int count) {
		super();
		this.threadStartBarrier = stressContext.getThreadStartBarrier();
		this.threadEndLatch = stressContext.getThreadEndLatch();
		this.failedCounter = stressContext.getFailedCounter();
		this.completedCounter = stressContext.getCompletedCounter();
		this.count = count;
		this.thinkTimeMs = stressContext.getThinkTimeMs();
		this.maxErrorRate = stressContext.getMaxErrorRate();
		this.aborted = stressContext.getAborted();

		everyTimes = new ArrayList<Long>(count);

		this.service = stressContext.getTestService();
	}

	public List<Long> getEveryTimes() {
		return everyTimes;
	}

	@Override
	public void run() {
		try {
			threadStartBarrier.await();
			doRun();
		} catch (Exception e) {
			// log.error("Test exception", e);
			log.error("", e);
		}
	}

	protected void doRun() throws Exception {
		// 10000次测试工具耗时2ms
		for (int i = 0; i < count; i++) {
			// 检查是否已被其他线程终止（错误率超限）
			if (aborted != null && aborted.get()) {
				log.warn("Test aborted due to high error rate, stopping at request {} of {}", i + 1, count);
				break;
			}

			long start = System.nanoTime();
			try {
				service.doTask();
			} catch (Exception e) {
				failedCounter.incrementAndGet();
				log.error("", e);
			} finally {
				long stop = System.nanoTime();
				long limit = stop - start;
				everyTimes.add(limit);
			}

			// 更新已完成计数并检查错误率
			if (completedCounter != null) {
				int completed = completedCounter.incrementAndGet();
				if (maxErrorRate > 0 && aborted != null) {
					int failed = failedCounter.get();
					double errorRate = failed * 100.0 / completed;
					if (errorRate >= maxErrorRate) {
						log.error(String.format("Error rate %.2f%% reached threshold %.2f%%, aborting test", errorRate, maxErrorRate));
						aborted.compareAndSet(false, true);
						break;
					}
				}
			}

			// 思考时间：每次请求间隔等待（最后一次不需要等待）
			if (thinkTimeMs > 0 && i < count - 1) {
				Thread.sleep(thinkTimeMs);
			}
		}
		threadEndLatch.countDown();
	}

}
