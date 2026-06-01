package com.taobao.stresstester.core;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * */
public class StressContext {

	// 每个线程的测试次数
	private int everyThreadCount;
	private CyclicBarrier threadStartBarrier;// 线程同步开始任务
	private CountDownLatch threadEndLatch;// 线程任务结束计数
	private AtomicInteger failedCounter; // 失败次数统计
	private AtomicInteger completedCounter; // 已完成请求数统计（用于计算错误率）
	private long thinkTimeMs; // 思考时间（每次请求间隔），单位毫秒
	/** 错误率阈值(0-100)，达到此值则终止压测。0表示不启用 */
	private double maxErrorRate;
	/** 终止标志，当错误率超限时由任意线程设为true */
	private AtomicBoolean aborted;

	private StressTask stressTask;

	public int getEveryThreadCount() {
		return everyThreadCount;
	}

	public void setEveryThreadCount(int everyThreadCount) {
		this.everyThreadCount = everyThreadCount;
	}

	public CyclicBarrier getThreadStartBarrier() {
		return threadStartBarrier;
	}

	public void setThreadStartBarrier(CyclicBarrier threadStartBarrier) {
		this.threadStartBarrier = threadStartBarrier;
	}

	public CountDownLatch getThreadEndLatch() {
		return threadEndLatch;
	}

	public void setThreadEndLatch(CountDownLatch threadEndLatch) {
		this.threadEndLatch = threadEndLatch;
	}

	public AtomicInteger getFailedCounter() {
		return failedCounter;
	}

	public void setFailedCounter(AtomicInteger failedCounter) {
		this.failedCounter = failedCounter;
	}

	public StressTask getTestService() {
		return stressTask;
	}

	public void setTestService(StressTask stressTask) {
		this.stressTask = stressTask;
	}

	public long getThinkTimeMs() {
		return thinkTimeMs;
	}

	public void setThinkTimeMs(long thinkTimeMs) {
		this.thinkTimeMs = thinkTimeMs;
	}

	public double getMaxErrorRate() {
		return maxErrorRate;
	}

	public void setMaxErrorRate(double maxErrorRate) {
		this.maxErrorRate = maxErrorRate;
	}

	public AtomicInteger getCompletedCounter() {
		return completedCounter;
	}

	public void setCompletedCounter(AtomicInteger completedCounter) {
		this.completedCounter = completedCounter;
	}

	public AtomicBoolean getAborted() {
		return aborted;
	}

	public void setAborted(AtomicBoolean aborted) {
		this.aborted = aborted;
	}

}
