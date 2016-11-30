package com.taobao.stresstester.core;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;

public class SimpleResultFormater implements StressResultFormater {
	// public static final Log log =
	// LogFactory.getLog(SimpleResultFormater.class);
	private static final Logger log = LoggerFactory.getLogger(SimpleResultFormater.class);

	public void format(StressResult stressResult, Writer writer) {
		long testsTakenTime = stressResult.getTestsTakenTime();
		int totalRequests = stressResult.getTotalRequests();
		int concurrencyLevel = stressResult.getConcurrencyLevel();

		float takes = StatisticsUtils.toMs(testsTakenTime);

		List<Long> allTimes = stressResult.getAllTimes();
		long totaleTimes = StatisticsUtils.getTotal(allTimes);

		// float tps = (totalRequests * 1000) / takes;
		float tps = 1000 * 1000000 * (concurrencyLevel * (totalRequests / (float) totaleTimes));

		float averageTime = StatisticsUtils.getAverage(totaleTimes, totalRequests);
		/** 理论单线程请求响应时间 */
		float onTheadAverageTime = averageTime / concurrencyLevel;

		int count_50 = totalRequests / 2;
		int count_66 = totalRequests * 66 / 100;
		int count_75 = totalRequests * 75 / 100;
		int count_80 = totalRequests * 80 / 100;
		int count_90 = totalRequests * 90 / 100;
		int count_95 = totalRequests * 95 / 100;
		int count_98 = totalRequests * 98 / 100;
		int count_99 = totalRequests * 99 / 100;

		long longestRequest = allTimes.get(allTimes.size() - 1);
		long shortestRequest = allTimes.get(0);

		StringBuilder view = new StringBuilder();

		// if (StringUtils.isNotBlank(serviceName)) {
		// view.append(" Service Name:\t").append(serviceName);
		// view.append("\r\n");
		// }
		view.append(" Concurrency Level:\t").append(concurrencyLevel).append("--并发数");
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
			// log.error("IOException:", e);
			log.error("", e);
		}

	}

}
