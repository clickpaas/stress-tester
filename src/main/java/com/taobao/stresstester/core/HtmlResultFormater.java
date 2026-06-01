package com.taobao.stresstester.core;

import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HtmlResultFormater implements StressResultFormater {

	private static final Logger log = LoggerFactory.getLogger(HtmlResultFormater.class);
	private static final String TEMPLATE_PATH = "report.vm";

	static {
		Velocity.setProperty(Velocity.RESOURCE_LOADER, "classpath");
		Velocity.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
		Velocity.init();
	}

	@Override
	public void format(StressResult stressResult, Writer writer) {
		format(stressResult, writer, null);
	}

	public void format(StressResult stressResult, Writer writer, String testName) {
		int totalRequests = stressResult.getTotalRequests();
		int concurrencyLevel = stressResult.getConcurrencyLevel();

		float takes = StatisticsUtils.toMs(stressResult.getTestsTakenTime());

		List<Long> allTimes = stressResult.getAllTimes();
		int actualCompleted = allTimes.size(); // 实际完成请求数（可能因提前终止而小于计划值）
		long totaleTimes = StatisticsUtils.getTotal(allTimes);

		float tps = 1000 * 1000000 * (concurrencyLevel * (actualCompleted / (float) totaleTimes));

		float averageTime = StatisticsUtils.toMs(StatisticsUtils.getAverage(totaleTimes, actualCompleted));
		float onThreadAverageTime = averageTime / concurrencyLevel;

		long longestRequest = allTimes.get(allTimes.size() - 1);
		long shortestRequest = allTimes.get(0);

		String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		float p50 = StatisticsUtils.toMs(allTimes.get(Math.min(actualCompleted / 2, actualCompleted - 1)));
		float p99 = StatisticsUtils.toMs(allTimes.get(Math.min(actualCompleted * 99 / 100, actualCompleted - 1)));
		float pMax = StatisticsUtils.toMs(longestRequest);

		List<Map<String, String>> percentiles = buildPercentiles(allTimes, actualCompleted);

		VelocityContext ctx = new VelocityContext();
		ctx.put("testName", testName != null ? testName : "");
		ctx.put("timestamp", timestamp);
		ctx.put("concurrencyLevel", concurrencyLevel);
		ctx.put("takes", String.format("%.6f", takes));
		ctx.put("totalRequests", totalRequests);
		ctx.put("failedRequests", stressResult.getFailedRequests());
		ctx.put("qps", String.format("%.2f", tps));
		ctx.put("averageTime", String.format("%.10f", averageTime));
		ctx.put("onThreadAverageTime", String.format("%.10f", onThreadAverageTime));
		ctx.put("shortestRequest", String.format("%.6f", StatisticsUtils.toMs(shortestRequest)));
		ctx.put("longestRequest", String.format("%.6f", pMax));
		ctx.put("percentiles", percentiles);
		ctx.put("p50Pos", getPercentPosition(p50, pMax));
		ctx.put("p99Pos", getPercentPosition(p99, pMax));
		ctx.put("maxTime", String.format("%.3f", pMax));

		try {
			Velocity.mergeTemplate(TEMPLATE_PATH, "UTF-8", ctx, writer);
			writer.flush();
		} catch (Exception e) {
			log.error("Failed to generate HTML report", e);
		}
	}

	private static List<Map<String, String>> buildPercentiles(List<Long> allTimes, int totalRequests) {
		List<Map<String, String>> list = new ArrayList<>();
		list.add(percentileRow("50%", allTimes.get(totalRequests / 2)));
		list.add(percentileRow("66%", allTimes.get(totalRequests * 66 / 100)));
		list.add(percentileRow("75%", allTimes.get(totalRequests * 75 / 100)));
		list.add(percentileRow("80%", allTimes.get(totalRequests * 80 / 100)));
		list.add(percentileRow("90%", allTimes.get(totalRequests * 90 / 100)));
		list.add(percentileRow("95%", allTimes.get(totalRequests * 95 / 100)));
		list.add(percentileRow("98%", allTimes.get(totalRequests * 98 / 100)));
		list.add(percentileRow("99%", allTimes.get(totalRequests * 99 / 100)));
		list.add(percentileRow("100% (longest)", allTimes.get(allTimes.size() - 1)));
		return list;
	}

	private static Map<String, String> percentileRow(String label, long timeNs) {
		Map<String, String> row = new LinkedHashMap<>(2);
		row.put("label", label);
		row.put("value", String.format("%.6f", StatisticsUtils.toMs(timeNs)));
		return row;
	}

	private static float getPercentPosition(float value, float maxVal) {
		if (maxVal == 0) return 0;
		return Math.min((value / maxVal) * 100, 100);
	}
}
