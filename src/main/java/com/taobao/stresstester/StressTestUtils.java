package com.taobao.stresstester;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.taobao.stresstester.core.HtmlResultFormater;
import com.taobao.stresstester.core.SimpleResultFormater;
import com.taobao.stresstester.core.StepConfig;
import com.taobao.stresstester.core.StepResult;
import com.taobao.stresstester.core.StepStressResult;
import com.taobao.stresstester.core.StatisticsUtils;
import com.taobao.stresstester.core.StressResult;
import com.taobao.stresstester.core.StressResultFormater;
import com.taobao.stresstester.core.StressTask;
import com.taobao.stresstester.core.StressTester;

public class StressTestUtils {

	private static final Logger logger = LoggerFactory.getLogger(StressTestUtils.class);

	private static StressTester stressTester = new StressTester();
	private static SimpleResultFormater simpleResultFormater = new SimpleResultFormater();
	private static HtmlResultFormater htmlResultFormater = new HtmlResultFormater();

	private static final String REPORT_DIR = System.getProperty("user.home") + "/logs/stress-tester";
	private static final String CSV_PATH = System.getProperty("user.home") + "/logs/stress-tester/result.csv";

	public static StressResult test(int concurrencyLevel, int totalRequests, StressTask stressTask) {
		return stressTester.test(concurrencyLevel, totalRequests, stressTask);
	}

	/**
	 * 执行压力测试（带思考时间）
	 * @param concurrencyLevel 并发线程数
	 * @param totalRequests 总请求数
	 * @param stressTask 测试任务
	 * @param thinkTimeMs 思考时间（每次请求间隔），单位毫秒。0表示不等待
	 */
	public static StressResult test(int concurrencyLevel, int totalRequests, StressTask stressTask, long thinkTimeMs) {
		return stressTester.test(concurrencyLevel, totalRequests, stressTask, 0, thinkTimeMs);
	}

	public static StressResult test(int concurrencyLevel, int totalRequests, StressTask stressTask, int warmUpTime) {
		return stressTester.test(concurrencyLevel, totalRequests, stressTask, warmUpTime);
	}

	/**
	 * 执行压力测试（带预热时间和思考时间）
	 */
	public static StressResult test(int concurrencyLevel, int totalRequests, StressTask stressTask, int warmUpTime, long thinkTimeMs) {
		return stressTester.test(concurrencyLevel, totalRequests, stressTask, warmUpTime, thinkTimeMs);
	}

	/**
	 * 执行压力测试（带错误率终止选项）
	 * @param maxErrorRate 错误率阈值(0-100)，达到此值则终止压测。0表示不启用
	 */
	public static StressResult test(int concurrencyLevel, int totalRequests, StressTask stressTask, double maxErrorRate) {
		return stressTester.test(concurrencyLevel, totalRequests, stressTask, 0, 0, maxErrorRate);
	}

	/**
	 * 执行压力测试（带思考时间和错误率终止选项）
	 */
	public static StressResult test(int concurrencyLevel, int totalRequests, StressTask stressTask, long thinkTimeMs, double maxErrorRate) {
		return stressTester.test(concurrencyLevel, totalRequests, stressTask, 0, thinkTimeMs, maxErrorRate);
	}

	/**
	 * 执行压力测试（完整参数）
	 */
	public static StressResult test(int concurrencyLevel, int totalRequests, StressTask stressTask, int warmUpTime, long thinkTimeMs, double maxErrorRate) {
		return stressTester.test(concurrencyLevel, totalRequests, stressTask, warmUpTime, thinkTimeMs, maxErrorRate);
	}

	public static void testAndPrint(int concurrencyLevel, int totalRequests, StressTask stressTask) {
		testAndPrint(concurrencyLevel, totalRequests, stressTask, null);
	}

	/**
	 * 执行压力测试并打印结果（带思考时间）
	 * @param thinkTimeMs 思考时间（每次请求间隔），单位毫秒
	 */
	public static void testAndPrint(int concurrencyLevel, int totalRequests, StressTask stressTask, long thinkTimeMs) {
		testAndPrint(concurrencyLevel, totalRequests, stressTask, null, thinkTimeMs);
	}

	/** 执行压力测试并打印结果（带错误率终止选项） */
	public static void testAndPrint(int concurrencyLevel, int totalRequests, StressTask stressTask, double maxErrorRate) {
		testAndPrint(concurrencyLevel, totalRequests, stressTask, null, 0, maxErrorRate);
	}

	/** 执行压力测试并打印结果（带思考时间和错误率终止选项） */
	public static void testAndPrint(int concurrencyLevel, int totalRequests, StressTask stressTask, long thinkTimeMs, double maxErrorRate) {
		testAndPrint(concurrencyLevel, totalRequests, stressTask, null, thinkTimeMs, maxErrorRate);
	}

	public static void testAndPrint(int concurrencyLevel, int totalRequests, StressTask stressTask, String testName) {
		StressResult stressResult = test(concurrencyLevel, totalRequests, stressTask);
		printWithAbortInfo(stressResult);
	}

	public static void testAndPrint(int concurrencyLevel, int totalRequests, StressTask stressTask, String testName, long thinkTimeMs) {
		StressResult stressResult = test(concurrencyLevel, totalRequests, stressTask, thinkTimeMs);
		printWithAbortInfo(stressResult);
	}

	public static void testAndPrint(int concurrencyLevel, int totalRequests, StressTask stressTask, String testName, long thinkTimeMs, double maxErrorRate) {
		StressResult stressResult = test(concurrencyLevel, totalRequests, stressTask, thinkTimeMs, maxErrorRate);
		printWithAbortInfo(stressResult);
	}

	public static String format(StressResult stressResult) {
		return format(stressResult, simpleResultFormater);
	}

	private static void printWithAbortInfo(StressResult stressResult) {
		String str = format(stressResult);
		System.out.println(str);
		if (stressResult.isAborted()) {
			System.out.println("[ABORTED] Test was terminated early due to error rate threshold exceeded.");
		}
	}

	public static String format(StressResult stressResult, StressResultFormater stressResultFormater) {
		StringWriter sw = new StringWriter();
		stressResultFormater.format(stressResult, sw);
		return sw.toString();
	}

	/**
	 * 执行压力测试，输出性能日志到控制台+logback(app.log)，同时生成 HTML 报告和 CSV 到 ~/logs/stress-tester/
	 */
	public static StressResult testAndReport(int concurrencyLevel, int totalRequests, StressTask stressTask) {
        return testAndReport(null, concurrencyLevel, totalRequests, stressTask);
	}

	/**
	 * 执行压力测试并生成报告（带思考时间）
	 * @param thinkTimeMs 思考时间（每次请求间隔），单位毫秒
	 */
	public static StressResult testAndReport(int concurrencyLevel, int totalRequests, StressTask stressTask, long thinkTimeMs) {
        return testAndReport(null, concurrencyLevel, totalRequests, stressTask, thinkTimeMs);
	}

	/** 执行压力测试并生成报告（带错误率终止选项） */
	public static StressResult testAndReport(int concurrencyLevel, int totalRequests, StressTask stressTask, double maxErrorRate) {
        return testAndReport(null, concurrencyLevel, totalRequests, stressTask, 0, maxErrorRate);
	}

	/** 执行压力测试并生成报告（带思考时间和错误率终止选项） */
	public static StressResult testAndReport(int concurrencyLevel, int totalRequests, StressTask stressTask, long thinkTimeMs, double maxErrorRate) {
        return testAndReport(null, concurrencyLevel, totalRequests, stressTask, thinkTimeMs, maxErrorRate);
	}

	public static StressResult testAndReport(String testName, int concurrencyLevel, int totalRequests, StressTask stressTask) {
		return doTestAndReport(testName, concurrencyLevel, totalRequests, stressTask, 0, 0);
	}

	public static StressResult testAndReport(String testName, int concurrencyLevel, int totalRequests, StressTask stressTask, long thinkTimeMs) {
		return doTestAndReport(testName, concurrencyLevel, totalRequests, stressTask, thinkTimeMs, 0);
	}

	public static StressResult testAndReport(String testName, int concurrencyLevel, int totalRequests, StressTask stressTask, long thinkTimeMs, double maxErrorRate) {
		return doTestAndReport(testName, concurrencyLevel, totalRequests, stressTask, thinkTimeMs, maxErrorRate);
	}

	private static StressResult doTestAndReport(String testName, int concurrencyLevel, int totalRequests, StressTask stressTask, long thinkTimeMs, double maxErrorRate) {
		// 1. 执行压力测试
		StressResult stressResult = test(concurrencyLevel, totalRequests, stressTask, thinkTimeMs, maxErrorRate);

		// 2. 格式化日志文本（包含 testName）
		String logText = formatWithTestName(stressResult, testName);

		// 3. 输出到控制台
//		System.out.println(logText);

		// 4. 通过 logback 输出到控制台和 app.log（同时输出两处）
		logger.info("\n{}", logText);

		// 5. 生成 HTML 报告
		generateHtmlReport(stressResult, testName);

		// 6. 追加 CSV 结果（含 testName）
		appendCsvResult(stressResult, testName);

		if (stressResult.isAborted()) {
			System.out.println("[ABORTED] Test was terminated early due to error rate threshold exceeded.");
			logger.info("[ABORTED] Test was terminated early due to error rate threshold exceeded.");
		}

		return stressResult;
	}

	// ==================== 阶梯压测 API ====================

	/**
	 * 阶梯压测：按顺序执行多个步骤，每个步骤有不同的并发数和请求数
	 *
	 * <pre>
	 * 使用示例:
	 * List&lt;StepConfig&gt; steps = Arrays.asList(
	 *     StepConfig.builder().concurrencyLevel(5).requestsPerThread(10).build(),
	 *     StepConfig.builder().concurrencyLevel(10).requestsPerThread(10).build(),
	 *     StepConfig.builder().concurrencyLevel(20).requestsPerThread(10).build()
	 * );
	 * StepStressResult result = StressTestUtils.stepTest(steps, task);
	 * </pre>
	 *
	 * @param steps 阶梯配置列表（按执行顺序）
	 * @param stressTask 测试任务
	 * @return 阶梯压测总结果
	 */
	public static StepStressResult stepTest(List<StepConfig> steps, StressTask stressTask) {
		return stressTester.stepTest(steps, stressTask);
	}

	/**
	 * 阶梯压测并打印结果到控制台
	 */
	public static void stepTestAndPrint(List<StepConfig> steps, StressTask stressTask) {
		stepTestAndPrint(null, steps, stressTask);
	}

	/**
	 * 阶梯压测并打印结果到控制台
	 * @param testName 测试名称
	 */
	public static void stepTestAndPrint(String testName, List<StepConfig> steps, StressTask stressTask) {
		StepStressResult result = stepTest(steps, stressTask);
		result.setTestName(testName);

		System.out.println("\n" + result.toString());

		// 输出每个步骤的详细结果
		for (StepResult sr : result.getSteps()) {
			String stepText = "\n--- Step " + sr.getStepIndex() + ": " + sr.getConfig() + " ---";
			if (testName != null && !testName.isEmpty()) {
				stepText += "\n Test Name:\t" + testName;
			}
			String detailText = format(sr.getStressResult());
			System.out.println(stepText + "\n" + detailText);
		}
	}

	/**
	 * 阶梯压测：输出日志到控制台+logback，同时生成 HTML 报告和 CSV
	 */
	public static StepStressResult stepTestAndReport(List<StepConfig> steps, StressTask stressTask) {
		return stepTestAndReport(null, steps, stressTask);
	}

	/**
	 * 阶梯压测：输出日志到控制台+logback，同时生成 HTML 报告和 CSV
	 * @param testName 测试名称
	 */
	public static StepStressResult stepTestAndReport(String testName, List<StepConfig> steps, StressTask stressTask) {
		// 1. 执行阶梯压测
		StepStressResult result = stepTest(steps, stressTask);
		result.setTestName(testName);

		// 2. 格式化汇总文本
		String summaryText = result.toString();

		// 3. 输出到控制台
		System.out.println(summaryText);

		// 4. 通过 logback 输出
		logger.info("\n{}", summaryText);

		// 5. 输出每个步骤的详细结果
		for (StepResult sr : result.getSteps()) {
			StressResult srResult = sr.getStressResult();
			String stepDetail = formatWithStepName(sr, testName);
			System.out.println(stepDetail);
			logger.info("\n{}", stepDetail);

			// 6. 为每个步骤生成 HTML 报告
			generateHtmlReport(srResult, testName != null ? testName + "_step" + sr.getStepIndex() : "step" + sr.getStepIndex());

			// 7. 追加 CSV 结果
			appendCsvResult(srResult, testName != null ? testName + "_step" + sr.getStepIndex() : "step" + sr.getStepIndex());
		}

		// 8. 生成汇总 HTML 报告
		generateStepHtmlReport(result);

		// 9. 追加汇总 CSV
		appendStepCsvResult(result);

		return result;
	}

	/**
	 * 使用 SimpleResultFormater 格式化单步结果，并在头部加上步骤信息
	 */
	private static String formatWithStepName(StepResult stepResult, String testName) {
		StringBuilder sb = new StringBuilder();
		sb.append("===== Step ").append(stepResult.getStepIndex())
		  .append(": concurrency=").append(stepResult.getConfig().getConcurrencyLevel())
		  .append(", total=").append(stepResult.getStressResult().getTotalRequests())
		  .append(" =====\r\n");
		if (testName != null && !testName.isEmpty()) {
			sb.append(" Test Name:\t").append(testName).append("--测试名称\r\n");
		}
		sb.append(format(stepResult.getStressResult()));
		return sb.toString();
	}

	/**
	 * 使用 SimpleResultFormater 格式化，并在头部加上 testName 信息
	 */
	private static String formatWithTestName(StressResult stressResult, String testName) {
		StringBuilder sb = new StringBuilder();

		if (testName != null && !testName.isEmpty()) {
			sb.append(" Test Name:\t").append(testName).append("--测试名称\r\n");
		}
		sb.append(format(stressResult));
		return sb.toString();
	}

	private static void generateHtmlReport(StressResult stressResult, String testName) {
		File reportDir = new File(REPORT_DIR);
		if (!reportDir.exists() && !reportDir.mkdirs()) {
			logger.error("Failed to create report directory: {}", REPORT_DIR);
			return;
		}

		String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS").format(new Date());
		String fileName = (testName != null && !testName.isEmpty() ? testName + "_" : "") + timestamp + ".html";
		File reportFile = new File(reportDir, fileName);

		try (FileWriter fw = new FileWriter(reportFile)) {
			htmlResultFormater.format(stressResult, fw, testName);
			logger.info("HTML report generated: {}", reportFile.getAbsolutePath());
		} catch (IOException e) {
			logger.error("Failed to write HTML report", e);
		}
	}

	private static final String[] CSV_HEADERS = {
		"timestamp", "testName", "concurrency", "totalRequests", "failedRequests",
		"timeTakenMs", "qps", "avgTimeMs", "avgTimePerThreadMs",
		"shortestMs", "longestMs", "p50Ms", "p66Ms", "p75Ms", "p80Ms",
		"p90Ms", "p95Ms", "p98Ms", "p99Ms"
	};

	private static synchronized void appendCsvResult(StressResult stressResult, String testName) {
		File csvDir = new File(REPORT_DIR);
		if (!csvDir.exists() && !csvDir.mkdirs()) {
			logger.error("Failed to create report directory: {}", REPORT_DIR);
			return;
		}

		boolean needHeader = !(new File(CSV_PATH).exists());

		int totalRequests = stressResult.getTotalRequests();
		int concurrencyLevel = stressResult.getConcurrencyLevel();
		List<Long> allTimes = stressResult.getAllTimes();
		int actualCompleted = allTimes.size(); // 实际完成请求数（可能因提前终止而小于计划值）
		long totaleTimes = StatisticsUtils.getTotal(allTimes);

		float takesMs = StatisticsUtils.toMs(stressResult.getTestsTakenTime());
		float qps = 1000 * 1000000 * (concurrencyLevel * (actualCompleted / (float) totaleTimes));
		float avgTime = StatisticsUtils.toMs(StatisticsUtils.getAverage(totaleTimes, actualCompleted));
		float avgTimePerThread = avgTime / concurrencyLevel;

		try (PrintWriter pw = new PrintWriter(
				new BufferedWriter(new OutputStreamWriter(new FileOutputStream(CSV_PATH, true), "UTF-8")))) {
			if (needHeader) {
				pw.write("\uFEFF"); // UTF-8 BOM，使Excel等工具正确识别编码
				for (int i = 0; i < CSV_HEADERS.length; i++) {
					pw.print(i > 0 ? "," : "");
					pw.print(CSV_HEADERS[i]);
				}
				pw.println();
			}

			String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
			pw.print(timestamp); pw.print(",");
			pw.print(testName != null ? testName : ""); pw.print(",");
			pw.print(concurrencyLevel); pw.print(",");
			pw.print(totalRequests); pw.print(",");
			pw.print(stressResult.getFailedRequests()); pw.print(",");
			pw.printf("%.6f", takesMs); pw.print(",");
			pw.printf("%.2f", qps); pw.print(",");
			pw.printf("%.10f", avgTime); pw.print(",");
			pw.printf("%.10f", avgTimePerThread); pw.print(",");
			pw.printf("%.6f", StatisticsUtils.toMs(allTimes.get(0))); pw.print(",");
			pw.printf("%.6f", StatisticsUtils.toMs(allTimes.get(allTimes.size() - 1))); pw.print(",");
			pw.printf("%.6f", StatisticsUtils.toMs(allTimes.get(actualCompleted / 2))); pw.print(",");
			pw.printf("%.6f", StatisticsUtils.toMs(allTimes.get(Math.min(actualCompleted * 66 / 100, actualCompleted - 1)))); pw.print(",");
			pw.printf("%.6f", StatisticsUtils.toMs(allTimes.get(Math.min(actualCompleted * 75 / 100, actualCompleted - 1)))); pw.print(",");
			pw.printf("%.6f", StatisticsUtils.toMs(allTimes.get(Math.min(actualCompleted * 80 / 100, actualCompleted - 1)))); pw.print(",");
			pw.printf("%.6f", StatisticsUtils.toMs(allTimes.get(Math.min(actualCompleted * 90 / 100, actualCompleted - 1)))); pw.print(",");
			pw.printf("%.6f", StatisticsUtils.toMs(allTimes.get(Math.min(actualCompleted * 95 / 100, actualCompleted - 1)))); pw.print(",");
			pw.printf("%.6f", StatisticsUtils.toMs(allTimes.get(Math.min(actualCompleted * 98 / 100, actualCompleted - 1)))); pw.print(",");
			pw.printf("%.6f", StatisticsUtils.toMs(allTimes.get(Math.min(actualCompleted * 99 / 100, actualCompleted - 1))));
			pw.println();
			logger.info("CSV result appended: {}", CSV_PATH);
		} catch (IOException e) {
			logger.error("Failed to append result.csv", e);
		}
	}

	private static final String[] STEP_CSV_HEADERS = {
		"timestamp", "testName", "stepIndex", "concurrency", "totalRequests", "failedRequests",
		"timeTakenMs", "qps", "avgTimeMs"
	};

	/** 生成阶梯压测汇总 HTML 报告 */
	private static void generateStepHtmlReport(StepStressResult stepResult) {
		File reportDir = new File(REPORT_DIR);
		if (!reportDir.exists() && !reportDir.mkdirs()) {
			logger.error("Failed to create report directory: {}", REPORT_DIR);
			return;
		}

		String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS").format(new Date());
		String namePrefix = stepResult.getTestName() != null ? stepResult.getTestName() : "stepTest";
		File reportFile = new File(reportDir, namePrefix + "_summary_" + timestamp + ".html");

		StringBuilder html = new StringBuilder();
		html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'>");
		html.append("<title>Step Stress Test Report</title>");
		html.append("<style>body{font-family:Arial;margin:20px;} table{border-collapse:collapse;width:100%;} ");
		html.append("th,td{border:1px solid #ddd;padding:8px;text-align:left;} th{background:#4CAF50;color:white;}");
		html.append("tr:nth-child(even){background:#f2f2f2;} .summary{background:#e8f5e9;padding:15px;border-radius:8px;margin-bottom:20px;}</style>");
		html.append("</head><body>");

		html.append("<h1>Step Stress Test Summary</h1>");
		html.append("<div class='summary'>");
		html.append("<p><strong>Test Name:</strong> ").append(stepResult.getTestName() != null ? stepResult.getTestName() : "-").append("</p>");
		html.append("<p><strong>Total Steps:</strong> ").append(stepResult.getStepCount()).append("</p>");
		html.append("<p><strong>Total Requests:</strong> ").append(stepResult.getTotalRequests()).append("</p>");
		html.append("<p><strong>Total Failed:</strong> ").append(stepResult.getTotalFailedRequests()).append("</p>");
		html.append("<p><strong>Max Concurrency:</strong> ").append(stepResult.getMaxConcurrencyLevel()).append("</p>");
		html.append("</div>");

		html.append("<h2>Step Details</h2>");
		html.append("<table><tr><th>Step</th><th>Concurrency</th><th>Total Requests</th><th>Failed</th><th>Avg Time(ms)</th></tr>");

		for (StepResult sr : stepResult.getSteps()) {
			StressResult r = sr.getStressResult();
			List<Long> times = r.getAllTimes();
			float avgMs = StatisticsUtils.toMs(StatisticsUtils.getTotal(times)) / (float) r.getTotalRequests();
			long totalTimeNs = r.getTestsTakenTime();
			float takesMs = StatisticsUtils.toMs(totalTimeNs);

			html.append("<tr>")
				.append("<td>").append(sr.getStepIndex()).append("</td>")
				.append("<td>").append(r.getConcurrencyLevel()).append("</td>")
				.append("<td>").append(r.getTotalRequests()).append("</td>")
				.append("<td>").append(r.getFailedRequests()).append("</td>")
				.append("<td>").append(String.format("%.3f", avgMs)).append("</td>")
				.append("</tr>");
		}

		html.append("</table></body></html>");

		try (FileWriter fw = new FileWriter(reportFile)) {
			fw.write(html.toString());
			logger.info("Step HTML report generated: {}", reportFile.getAbsolutePath());
		} catch (IOException e) {
			logger.error("Failed to write step HTML report", e);
		}
	}

	/** 追加阶梯压测汇总 CSV */
	private static synchronized void appendStepCsvResult(StepStressResult stepResult) {
		File csvDir = new File(REPORT_DIR);
		if (!csvDir.exists() && !csvDir.mkdirs()) {
			logger.error("Failed to create report directory: {}", REPORT_DIR);
			return;
		}

		String stepCsvPath = CSV_PATH.replace(".csv", "_step.csv");
		boolean needHeader = !(new File(stepCsvPath).exists());

		try (PrintWriter pw = new PrintWriter(
				new BufferedWriter(new FileWriter(stepCsvPath, true)))) {

			if (needHeader) {
				for (int i = 0; i < STEP_CSV_HEADERS.length; i++) {
					pw.print(i > 0 ? "," : "");
					pw.print(STEP_CSV_HEADERS[i]);
				}
				pw.println();
			}

			for (StepResult sr : stepResult.getSteps()) {
				StressResult r = sr.getStressResult();
				List<Long> times = r.getAllTimes();
				long totaleTimes = StatisticsUtils.getTotal(times);
				float takesMs = StatisticsUtils.toMs(r.getTestsTakenTime());
				int totalReq = r.getTotalRequests();
				int concurrency = r.getConcurrencyLevel();
				float qps = 1000 * 1000000 * (concurrency * (totalReq / (float) totaleTimes));
				float avgTime = StatisticsUtils.toMs(StatisticsUtils.getAverage(totaleTimes, totalReq));

				String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
				pw.print(timestamp); pw.print(",");
				pw.print(stepResult.getTestName() != null ? stepResult.getTestName() : ""); pw.print(",");
				pw.print(sr.getStepIndex()); pw.print(",");
				pw.print(concurrency); pw.print(",");
				pw.print(totalReq); pw.print(",");
				pw.print(r.getFailedRequests()); pw.print(",");
				pw.printf("%.6f", takesMs); pw.print(",");
				pw.printf("%.2f", qps); pw.print(",");
				pw.printf("%.6f", avgTime);
				pw.println();
			}
			logger.info("Step CSV result appended: {}", stepCsvPath);
		} catch (IOException e) {
			logger.error("Failed to append step result.csv", e);
		}
	}

}
