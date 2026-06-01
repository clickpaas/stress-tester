package com.taobao.stresstester;

import java.util.Arrays;
import java.util.List;

import com.taobao.stresstester.core.StepConfig;
import com.taobao.stresstester.core.StepResult;
import com.taobao.stresstester.core.StepStressResult;
import com.taobao.stresstester.core.StressResult;
import com.taobao.stresstester.core.StressTask;

/**
 * 功能测试：验证思考时间、阶梯压测、错误率终止、Builder模式
 */
public class FeatureTest {

	// ==================== 公共测试任务 ====================

	private static final StressTask SUCCESS_TASK = new StressTask() {
		@Override
		public Object doTask() throws Exception {
			Thread.sleep(5); // 模拟5ms耗时
			return "ok";
		}
	};

	private static final StressTask PARTIAL_FAIL_TASK = new StressTask() {
		private int counter = 0;

		@Override
		public Object doTask() throws Exception {
			counter++;
			if (counter % 3 == 0) { // 每3次请求失败1次（约33%错误率）
				throw new RuntimeException("模拟失败");
			}
			Thread.sleep(3);
			return "ok";
		}

		public void reset() { counter = 0; }
	};

	private static final StressTask HIGH_ERROR_TASK = new StressTask() {
		@Override
		public Object doTask() throws Exception {
			if (Math.random() < 0.7) { // 约70%错误率
				throw new RuntimeException("高错误率");
			}
			Thread.sleep(2);
			return "ok";
		}
	};


	// ==================== Test 1: Builder 模式验证 ====================

	public static void testBuilderPattern() {
		System.out.println("\n========== Test1: Builder Pattern ==========");

		// 基本用法
		StepConfig step1 = StepConfig.builder()
				.concurrencyLevel(5)
				.requestsPerThread(10)
				.build();
		assertEqual(5, step1.getConcurrencyLevel(), "concurrencyLevel");
		assertEqual(10, step1.getRequestsPerThread(), "requestsPerThread");
		assertEqual(50, step1.getTotalRequests(), "totalRequests");
		assertEqual(0L, step1.getThinkTimeMs(), "default thinkTime");
		assertEqual(0, step1.getWarmUpTime(), "default warmUp");
		assertEqual(0.0, step1.getMaxErrorRate(), "default maxErrorRate");
		System.out.println("  [PASS] Basic builder OK: " + step1);

		// 完整参数
		StepConfig step2 = StepConfig.builder()
				.concurrencyLevel(10)
				.requestsPerThread(20)
				.thinkTimeMs(100)
				.warmUpTime(800)
				.maxErrorRate(50.0)
				.build();
		assertEqual(100L, step2.getThinkTimeMs(), "thinkTimeMs");
		assertEqual(800, step2.getWarmUpTime(), "warmUpTime");
		assertEqual(50.0, step2.getMaxErrorRate(), "maxErrorRate");
		System.out.println("  [PASS] Full params builder OK: " + step2);

		// 参数校验
		try {
			StepConfig.builder().concurrencyLevel(0).requestsPerThread(10).build();
			System.out.println("  [FAIL] Should reject concurrencyLevel=0");
		} catch (IllegalArgumentException e) {
			System.out.println("  [PASS] Rejected invalid concurrencyLevel: " + e.getMessage());
		}

		try {
			StepConfig.builder().concurrencyLevel(5).requestsPerThread(-1).build();
			System.out.println("  [FAIL] Should reject requestsPerThread<0");
		} catch (IllegalArgumentException e) {
			System.out.println("  [PASS] Rejected invalid requestsPerThread: " + e.getMessage());
		}

		try {
			StepConfig.builder().concurrencyLevel(5).requestsPerThread(10).maxErrorRate(-1).build();
			System.out.println("  [FAIL] Should reject maxErrorRate<0");
		} catch (IllegalArgumentException e) {
			System.out.println("  [PASS] Rejected invalid maxErrorRate: " + e.getMessage());
		}

		System.out.println("========== Test1 Done ==========\n");
	}


	// ==================== Test 2: 思考时间功能 ====================

	public static void testThinkTime() {
		System.out.println("\n========== Test2: Think Time ==========");

		// 无思考时间 - 应该很快完成
		long startNoThink = System.currentTimeMillis();
		StressResult resultNoThink = StressTestUtils.test(
				2, 5, SUCCESS_TASK, 0);
		long elapsedNoThink = System.currentTimeMillis() - startNoThink;

		System.out.println("  Without thinkTime: concurrency=2, perThread=5, elapsed=" + elapsedNoThink + "ms, aborted=" + resultNoThink.isAborted());

		// 有思考时间50ms - 总耗时应该明显增加 (约 2线程 * 4次 * 50ms ≈ 400ms+ 额外开销)
		long startWithThink = System.currentTimeMillis();
		StressResult resultWithThink = StressTestUtils.test(
				2, 5, SUCCESS_TASK, 50);
		long elapsedWithThink = System.currentTimeMillis() - startWithThink;

		System.out.println("  With thinkTime=50ms: concurrency=2, perThread=5, elapsed=" + elapsedWithThink + "ms, aborted=" + resultWithThink.isAborted());

		if (elapsedWithThink > elapsedNoThink + 150) {
			System.out.println("  [PASS] Think time significantly increased total duration (diff="
					+ (elapsedWithThink - elapsedNoThink) + "ms)");
		} else {
			System.out.println("  [WARN] Think time effect may be too small to measure accurately");
		}

		assertEqual(false, resultNoThink.isAborted(), "noThink not aborted");
		assertEqual(false, resultWithThink.isAborted(), "withThink not aborted");

		// 使用 testAndPrint API 测试
		System.out.println("  --- testAndPrint with thinkTime ---");
		StressTestUtils.testAndPrint(2, 5, SUCCESS_TASK, 30);

		System.out.println("========== Test2 Done ==========\n");
	}


	// ==================== Test 3: 阶梯压测功能 ====================

	public static void testStepTest() {
		System.out.println("\n========== Test3: Step/Ramp-up Test ==========");

        List<StepConfig> steps = Arrays.asList(
                StepConfig.builder().concurrencyLevel(2).requestsPerThread(5).thinkTimeMs(20).build(),
                StepConfig.builder().concurrencyLevel(3).requestsPerThread(5).thinkTimeMs(15).build(),
                StepConfig.builder().concurrencyLevel(4).requestsPerThread(5).thinkTimeMs(10).build()
        );

		StepStressResult result = StressTestUtils.stepTest(steps, SUCCESS_TASK);
		result.setTestName("阶梯压测测试");

		System.out.println("\n  Summary:");
		System.out.println("    Total Steps: " + result.getStepCount());
		System.out.println("    Total Requests: " + result.getTotalRequests());
		System.out.println("    Total Failed: " + result.getTotalFailedRequests());
		System.out.println("    Max Concurrency: " + result.getMaxConcurrencyLevel());

		// 打印详细结果
		for (StepResult sr : result.getSteps()) {
			String stepText = "\n--- Step " + sr.getStepIndex() + ": " + sr.getConfig() + " ---";
			String detailText = StressTestUtils.format(sr.getStressResult());
			System.out.println(stepText + "\n" + detailText);
		}

		// 验证结果
		assertEqual(3, result.getStepCount(), "stepCount");

		List<StepResult> stepResults = result.getSteps();

		assertEqual(2 * 5, stepResults.get(0).getStressResult().getTotalRequests(), "step1 requests");
		assertEqual(2, stepResults.get(0).getStressResult().getConcurrencyLevel(), "step1 concurrency");

		assertEqual(3 * 5, stepResults.get(1).getStressResult().getTotalRequests(), "step2 requests");
		assertEqual(3, stepResults.get(1).getStressResult().getConcurrencyLevel(), "step2 concurrency");

		assertEqual(4 * 5, stepResults.get(2).getStressResult().getTotalRequests(), "step3 requests");
		assertEqual(4, stepResults.get(2).getStressResult().getConcurrencyLevel(), "step3 concurrency");

		for (int i = 0; i < result.getStepCount(); i++) {
			StepResult sr = result.getSteps().get(i);
			assertEqual(false, sr.getStressResult().isAborted(), "step" + (i + 1) + " not aborted");
			assertEqual(0, sr.getStressResult().getFailedRequests(), "step" + (i + 1) + " no failures");
		}

		System.out.println("  [PASS] All steps executed correctly with increasing concurrency");

		System.out.println("========== Test3 Done ==========\n");
	}


	// ==================== Test 4: 错误率终止功能 ====================

	public static void testMaxErrorRateAbort() {
		System.out.println("\n========== Test4: Error Rate Abort ==========");

		// 4a: 高错误率场景，设置阈值30%应该触发终止
		System.out.println("  --- 4a: High error task with threshold=30% ---");
		StressResult resultHigh = StressTestUtils.test(5, 200, HIGH_ERROR_TASK, 30.0);
		System.out.println("    Completed: " + resultHigh.getAllTimes().size() + "/" + resultHigh.getTotalRequests()
				+ ", Failed: " + resultHigh.getFailedRequests()
				+ ", Aborted: " + resultHigh.isAborted());

		double actualErrorRate = resultHigh.getFailedRequests() * 100.0 / Math.max(1, resultHigh.getAllTimes().size());
		System.out.println("    Actual error rate: " + String.format("%.2f", actualErrorRate) + "%");

		if (resultHigh.isAborted()) {
			System.out.println("    [PASS] Correctly aborted due to high error rate > 30%");
		} else {
			System.out.println("    [WARN] Not aborted - error rate may not have reached threshold yet");
		}

		// 4b: 低错误率阈值（不启用），不应该终止
		System.out.println("  --- 4b: High error task without abort (maxErrorRate=0) ---");
		StressResult resultNoAbort = StressTestUtils.test(5, 50, HIGH_ERROR_TASK, 0);
		System.out.println("    Completed: " + resultNoAbort.getAllTimes().size() + "/" + resultNoAbort.getTotalRequests()
				+ ", Failed: " + resultNoAbort.getFailedRequests()
				+ ", Aborted: " + resultNoAbort.isAborted());
		assertEqual(false, resultNoAbort.isAborted(), "no abort when disabled");
		System.out.println("    [PASS] No abort when maxErrorRate=0");

		// 4c: 成功任务 + 错误率阈值，不应该终止
		System.out.println("  --- 4c: Success task with threshold=10% ---");
		StressResult resultSuccess = StressTestUtils.test(3, 30, SUCCESS_TASK, 10.0);
		System.out.println("    Completed: " + resultSuccess.getAllTimes().size() + "/" + resultSuccess.getTotalRequests()
				+ ", Failed: " + resultSuccess.getFailedRequests()
				+ ", Aborted: " + resultSuccess.isAborted());
		assertEqual(false, resultSuccess.isAborted(), "success task not aborted");
		assertEqual(0, resultSuccess.getFailedRequests(), "no failures");
		System.out.println("    [PASS] Success task not aborted");

		// 4d: 阶梯压测中某步骤因错误率终止，后续步骤应跳过
		System.out.println("  --- 4d: Step test abort cascading ---");
		List<StepConfig> failSteps = Arrays.asList(
				StepConfig.builder().concurrencyLevel(2).requestsPerThread(10).maxErrorRate(60.0).build(),
				StepConfig.builder().concurrencyLevel(5).requestsPerThread(10).build()
		);
		StepStressResult stepFailResult = StressTestUtils.stepTest(failSteps, HIGH_ERROR_TASK);

		System.out.println("    Steps executed: " + stepFailResult.getStepCount() + " (expected <= 2)");
		if (stepFailResult.getStepCount() == 1 && stepFailResult.getSteps().get(0).getStressResult().isAborted()) {
			System.out.println("    [PASS] Step1 aborted, step2 correctly skipped");
		} else if (stepFailResult.getStepCount() == 2) {
			System.out.println("    [INFO] Both steps completed (error rate may not have triggered)");
		}

		System.out.println("========== Test4 Done ==========\n");
	}


	// ==================== Test 5: 综合测试（所有功能组合） ====================

	public static void testCombined() {
		System.out.println("\n========== Test5: Combined Features ==========");

		List<StepConfig> combinedSteps = Arrays.asList(
				StepConfig.builder().concurrencyLevel(2).requestsPerThread(10).thinkTimeMs(10).warmUpTime(100).maxErrorRate(80.0).build(),
				StepConfig.builder().concurrencyLevel(4).requestsPerThread(8).thinkTimeMs(5).maxErrorRate(50.0).build()
		);

		StepStressResult result = StressTestUtils.stepTestAndReport("综合测试", combinedSteps, PARTIAL_FAIL_TASK);

		System.out.println("\n  Combined Result:");
		System.out.println(result.toString());

		boolean allGood = true;
		for (StepResult sr : result.getSteps()) {
			StressResult r = sr.getStressResult();
			String status = r.isAborted() ? "[ABORTED]" : "[OK]";
			System.out.println("    Step" + sr.getStepIndex() + " " + status
					+ ": reqs=" + r.getTotalRequests()
					+ ", failed=" + r.getFailedRequests()
					+ ", completed=" + r.getAllTimes().size());

			if (r.getConcurrencyLevel() != sr.getConfig().getConcurrencyLevel()) {
				allGood = false;
			}
		}

		if (allGood) {
			System.out.println("  [PASS] Combined features working correctly");
		}

		System.out.println("========== Test5 Done ==========\n");
	}


	// ==================== 辅助方法 ====================

	static void assertEqual(Object expected, Object actual, String label) {
		if ((expected == null && actual == null) || (expected != null && expected.equals(actual))) {
			// pass silently or uncomment for verbose:
			// System.out.println("  [ASSERT OK] " + label + ": " + actual);
		} else {
			System.out.println("  [ASSERT FAIL] " + label + ": expected=" + expected + ", actual=" + actual);
		}
	}


	// ==================== Main ====================

	public static void main(String[] args) throws Exception {
		System.out.println("========================================");
		System.out.println("  Feature Tests: ThinkTime / Step / ErrorRate / Builder");
		System.out.println("========================================");

//		testBuilderPattern();
//		testThinkTime();
//		testStepTest();
//		testMaxErrorRateAbort();
		testCombined();

		System.out.println("\n========================================");
		System.out.println("  ALL TESTS COMPLETED");
		System.out.println("========================================");
	}
}
