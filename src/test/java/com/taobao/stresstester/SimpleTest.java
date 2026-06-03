package com.taobao.stresstester;

import java.util.Arrays;
import java.util.List;

import com.taobao.stresstester.core.StepConfig;
import com.taobao.stresstester.core.StepStressResult;
import com.taobao.stresstester.core.StressResult;
import com.taobao.stresstester.core.StressTask;

/**
 * StressTaskWrap（无返回值 lambda）用法示例
 */
public class SimpleTest {

	public static void main(String[] args) throws Exception {

		// ========== 1. 基础压测 + 自动报告 ==========
		System.out.println("\n>>>>>> 1. testAndReport（无返回值 lambda）");
		StressTestUtils.testAndReport("基础压测", 10, 10000, () -> pert());

		// ========== 2. 带思考时间的压测 ==========
		System.out.println("\n>>>>>> 2. testAndReport + 思考时间");
		StressTestUtils.testAndReport("思考时间压测", 5, 5000, () -> pert(), 50);

		// ========== 3. 带错误率终止阈值的压测 ==========
		System.out.println("\n>>>>>> 3. testAndReport + 错误率终止");
		StressResult result3 = StressTestUtils.testAndReport("错误率终止", 5, 200, () -> maybeFail(), 30.0);
		System.out.println("Aborted: " + result3.isAborted() + ", Failed: " + result3.getFailedRequests());

		// ========== 4. testAndPrint（仅打印控制台） ==========
		System.out.println("\n>>>>>> 4. testAndPrint");
		StressTestUtils.testAndPrint(5, 1000, () -> pert());

		// ========== 5. 阶梯压测 ==========
		System.out.println("\n>>>>>> 5. 阶梯压测");
		List<StepConfig> steps = Arrays.asList(
				StepConfig.builder().concurrencyLevel(2).requestsPerThread(10).thinkTimeMs(20).build(),
				StepConfig.builder().concurrencyLevel(5).requestsPerThread(10).thinkTimeMs(10).build(),
				StepConfig.builder().concurrencyLevel(10).requestsPerThread(10).thinkTimeMs(0).build()
		);
		StepStressResult stepResult = StressTestUtils.stepTestAndReport("阶梯压测", steps, () -> pert());
		System.out.println("总步数: " + stepResult.getStepCount() + ", 总请求: " + stepResult.getTotalRequests());

		// ========== 6. 有返回值写法（原来 StressTask 方式） ==========
		System.out.println("\n>>>>>> 6. StressTask 有返回值 lambda");
		StressTask task = () -> {
			Thread.sleep(3);
			return "done";
		};
		StressTestUtils.testAndReport("有返回值压测", 5, 2000, task);

		System.out.println("\n===== ALL TESTS DONE =====");
	}

	// ---- mock 业务方法 ----

	private static void pert() throws Exception {
		Thread.sleep(5);
	}

	private static void maybeFail() throws Exception {
		Thread.sleep(3);
		if (Math.random() < 0.4) {
			throw new RuntimeException("模拟业务异常");
		}
	}
}
