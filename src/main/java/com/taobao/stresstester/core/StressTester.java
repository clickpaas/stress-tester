package com.taobao.stresstester.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;

public class StressTester {
	// private static Log log = LogFactory.getLog(StressTester.class);

	private int defaultWarmUpTime = 0;

	private StressTask emptyTestService = new StressTask() {
		@Override
		public Object doTask() throws Exception {
			// ignore
			return null;
		}

	};

	static {
		warnSelf();
	}

	protected static void warnSelf() {
		for (int i = 0; i < 50; i++) {
			StressTester benchmark = new StressTester();
			benchmark.test(10, 100, null, 0);
		}
	}

	// warm up
	protected void warmUp(int warmUpTime, StressTask testervice) {
		for (int i = 0; i < warmUpTime; i++) {
			try {
				testervice.doTask();
				// benchmarkWorker.doRun();
			} catch (Exception e) {
				// log.error("Test exception", e);
			}
		}
	}

	public StressResult test(int concurrencyLevel, int totalRequests, StressTask stressTask) {
		return test(concurrencyLevel, totalRequests, stressTask, defaultWarmUpTime, 0, 0);
	}

	public StressResult test(int concurrencyLevel, int totalRequests, StressTask stressTask, int warmUpTime) {
		return test(concurrencyLevel, totalRequests, stressTask, warmUpTime, 0, 0);
	}

	public StressResult test(int concurrencyLevel, int totalRequests, StressTask stressTask, int warmUpTime, long thinkTimeMs) {
		return test(concurrencyLevel, totalRequests, stressTask, warmUpTime, thinkTimeMs, 0);
	}

	/**
	 * 核心压测方法
	 * @param maxErrorRate 错误率阈值(0-100)，达到此值则终止压测。0表示不启用
	 */
	public StressResult test(int concurrencyLevel, int totalRequests, StressTask stressTask, int warmUpTime, long thinkTimeMs, double maxErrorRate) {

		if (null == stressTask) {
			stressTask = emptyTestService;
		}
		warmUp(warmUpTime, stressTask);
		int everyThreadCount = totalRequests / concurrencyLevel;
		CyclicBarrier threadStartBarrier = new CyclicBarrier(concurrencyLevel);
		CountDownLatch threadEndLatch = new CountDownLatch(concurrencyLevel);
		AtomicInteger failedCounter = new AtomicInteger();
		AtomicInteger completedCounter = new AtomicInteger(0);
		AtomicBoolean abortedFlag = new AtomicBoolean(false);

		StressContext stressContext = new StressContext();
		stressContext.setTestService(stressTask);
		stressContext.setEveryThreadCount(everyThreadCount);
		stressContext.setThreadStartBarrier(threadStartBarrier);
		stressContext.setThreadEndLatch(threadEndLatch);
		stressContext.setFailedCounter(failedCounter);
		stressContext.setCompletedCounter(completedCounter);
		stressContext.setThinkTimeMs(thinkTimeMs);
		stressContext.setMaxErrorRate(maxErrorRate);
		stressContext.setAborted(abortedFlag);

		ExecutorService executorService = Executors.newFixedThreadPool(concurrencyLevel);

		List<StressThreadWorker> workers = new ArrayList<StressThreadWorker>(concurrencyLevel);
		for (int i = 0; i < concurrencyLevel; i++) {
			StressThreadWorker worker = new StressThreadWorker(stressContext, everyThreadCount);
			workers.add(worker);
		}

		for (int i = 0; i < concurrencyLevel; i++) {
			StressThreadWorker worker = workers.get(i);
			executorService.submit(worker);
		}

		try {
			threadEndLatch.await();
		} catch (InterruptedException e) {
			// log.error("InterruptedException", e);
		}

		executorService.shutdownNow();

		int realTotalRequests = everyThreadCount * concurrencyLevel;
		int failedRequests = failedCounter.get();
		StressResult stressResult = new StressResult();

		SortResult sortResult = getSortedTimes(workers);
		List<Long> allTimes = sortResult.allTimes;

		stressResult.setAllTimes(allTimes);
		List<Long> trheadTimes = sortResult.trheadTimes;
		long totalTime = trheadTimes.get(trheadTimes.size() - 1);

		stressResult.setTestsTakenTime(totalTime);
		stressResult.setFailedRequests(failedRequests);
		stressResult.setTotalRequests(realTotalRequests);
		stressResult.setConcurrencyLevel(concurrencyLevel);
		stressResult.setWorkers(workers);
		stressResult.setAborted(abortedFlag.get());

		if (abortedFlag.get()) {
			System.out.println("[WARN] Test aborted due to error rate exceeding " + maxErrorRate + "% threshold. "
					+ "Completed: " + completedCounter.get() + "/" + realTotalRequests
					+ ", Failed: " + failedRequests);
		}

		return stressResult;

	}

	/**
	 * 阶梯压测：按顺序执行多个步骤，每个步骤有不同的并发数和请求数
	 *
	 * @param steps 阶梯配置列表（按执行顺序）
	 * @param stressTask 测试任务
	 * @return 阶梯压测总结果
	 */
	public StepStressResult stepTest(List<StepConfig> steps, StressTask stressTask) {
		if (null == stressTask) {
			stressTask = emptyTestService;
		}

		StepStressResult result = new StepStressResult();

		for (int i = 0; i < steps.size(); i++) {
			StepConfig config = steps.get(i);
			System.out.println("===== Step " + (i + 1) + "/" + steps.size() + ": " + config + " =====");

			StressResult sr = test(
					config.getConcurrencyLevel(),
					config.getConcurrencyLevel() * config.getRequestsPerThread(),
					stressTask,
					config.getWarmUpTime(),
					config.getThinkTimeMs(),
					config.getMaxErrorRate()
			);

			result.addStep(new StepResult(i + 1, config, sr));

			// 如果当前步骤因错误率终止，后续步骤不再执行
			if (sr.isAborted()) {
				System.out.println("[WARN] Step " + (i + 1) + " aborted due to error rate, stopping remaining steps.");
				break;
			}
		}

		return result;
	}

	protected SortResult getSortedTimes(List<StressThreadWorker> workers) {
		List<Long> allTimes = new ArrayList<Long>();
		List<Long> trheadTimes = new ArrayList<Long>();
		for (StressThreadWorker worker : workers) {
			List<Long> everyWorkerTimes = worker.getEveryTimes();

			long workerTotalTime = StatisticsUtils.getTotal(everyWorkerTimes);
			trheadTimes.add(workerTotalTime);

			for (Long time : everyWorkerTimes) {
				allTimes.add(time);
			}
		}
		Collections.sort(allTimes);
		Collections.sort(trheadTimes);
		SortResult result = new SortResult();
		result.allTimes = allTimes;
		result.trheadTimes = trheadTimes;
		return result;
	}

	class SortResult {
		List<Long> allTimes;
		List<Long> trheadTimes;
	}

}
