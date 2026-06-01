package com.taobao.stresstester.core;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleResultFormater implements StressResultFormater {
	private static final Logger log = LoggerFactory.getLogger(SimpleResultFormater.class);

	@Override
	public void format(StressResult stressResult, Writer writer) {
		format(stressResult, writer, null);
	}

	public void format(StressResult stressResult, Writer writer, String testName) {
		long testsTakenTime = stressResult.getTestsTakenTime();
		int totalRequests = stressResult.getTotalRequests();
		int concurrencyLevel = stressResult.getConcurrencyLevel();

		float takes = StatisticsUtils.toMs(testsTakenTime);

		List<Long> allTimes = stressResult.getAllTimes();
		int actualCompleted = allTimes.size(); // 实际完成请求数（可能因提前终止而小于计划值）
		long totaleTimes = StatisticsUtils.getTotal(allTimes);

		float tps = 1000 * 1000000 * (concurrencyLevel * (actualCompleted / (float) totaleTimes));

		float averageTime = StatisticsUtils.getAverage(totaleTimes, actualCompleted);
		float onTheadAverageTime = averageTime / concurrencyLevel;

		int count_50 = Math.min(actualCompleted / 2, actualCompleted - 1);
		int count_66 = Math.min(actualCompleted * 66 / 100, actualCompleted - 1);
		int count_75 = Math.min(actualCompleted * 75 / 100, actualCompleted - 1);
		int count_80 = Math.min(actualCompleted * 80 / 100, actualCompleted - 1);
		int count_90 = Math.min(actualCompleted * 90 / 100, actualCompleted - 1);
		int count_95 = Math.min(actualCompleted * 95 / 100, actualCompleted - 1);
		int count_98 = Math.min(actualCompleted * 98 / 100, actualCompleted - 1);
		int count_99 = Math.min(actualCompleted * 99 / 100, actualCompleted - 1);

		long longestRequest = allTimes.get(allTimes.size() - 1);
		long shortestRequest = allTimes.get(0);

		StringBuilder view = new StringBuilder();

		if (testName != null && !testName.isEmpty()) {
			view.append(" Test Name:\t").append(testName).append("--测试名称");
		}
		view.append("\r\n Concurrency Level:\t").append(concurrencyLevel).append("--并发数");
		view.append("\r\n Time taken for tests:\t").append(takes).append(" ms").append("--测试耗时");
		view.append("\r\n Complete Requests:\t").append(totalRequests).append("--完成测试次数");
		view.append("\r\n Failed Requests:\t").append(stressResult.getFailedRequests()).append("--失败次数");
		view.append("\r\n Requests per second:\t").append(tps).append("--QPS");
		view.append("\r\n Time per request:\t").append(StatisticsUtils.toMs(averageTime)).append(" ms")
				.append("--平均耗时");
		view.append("\r\n Time per request:\t").append(StatisticsUtils.toMs(onTheadAverageTime))
				.append(" ms (across all concurrent requests)").append("--平均耗时，忽略并发影响");
		view.append("\r\n Shortest request:\t").append(StatisticsUtils.toMs(shortestRequest)).append(" ms")
				.append("--最短耗时");

		StringBuilder certainTimeView = view;
		certainTimeView.append("\r\n Percentage of the requests served within a certain time (ms)");
		certainTimeView.append("\r\n  50%\t").append(StatisticsUtils.toMs(allTimes.get(count_50)))
				.append("--50% 的耗时在" + StatisticsUtils.toMs(allTimes.get(count_50)) + "毫秒以下");
		certainTimeView.append("\r\n  66%\t").append(StatisticsUtils.toMs(allTimes.get(count_66)));
		certainTimeView.append("\r\n  75%\t").append(StatisticsUtils.toMs(allTimes.get(count_75)));
		certainTimeView.append("\r\n  80%\t").append(StatisticsUtils.toMs(allTimes.get(count_80)));
		certainTimeView.append("\r\n  90%\t").append(StatisticsUtils.toMs(allTimes.get(count_90)));
		certainTimeView.append("\r\n  95%\t").append(StatisticsUtils.toMs(allTimes.get(count_95)));
		certainTimeView.append("\r\n  98%\t").append(StatisticsUtils.toMs(allTimes.get(count_98)));
		certainTimeView.append("\r\n  99%\t").append(StatisticsUtils.toMs(allTimes.get(count_99)));
		certainTimeView.append("\r\n 100%\t").append(StatisticsUtils.toMs(longestRequest)).append(" (longest request)")
				.append("--最长的耗时");

		try {
			writer.write(view.toString());
		} catch (IOException e) {
			log.error("", e);
		}
	}

}
