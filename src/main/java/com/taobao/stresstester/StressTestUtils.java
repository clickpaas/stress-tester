package com.taobao.stresstester;

import java.io.StringWriter;

import com.taobao.stresstester.core.SimpleResultFormater;
import com.taobao.stresstester.core.StressResult;
import com.taobao.stresstester.core.StressResultFormater;
import com.taobao.stresstester.core.StressTask;
import com.taobao.stresstester.core.StressTester;

/**
 */
public class StressTestUtils {

	private static StressTester stressTester = new StressTester();
	private static SimpleResultFormater simpleResultFormater = new SimpleResultFormater();

	public static StressResult test(int concurrencyLevel, int totalRequests, StressTask stressTask) {
		return stressTester.test(concurrencyLevel, totalRequests, stressTask);
	}

	public static StressResult test(int concurrencyLevel, int totalRequests, StressTask stressTask, int warmUpTime) {
		return stressTester.test(concurrencyLevel, totalRequests, stressTask, warmUpTime);
	}

	public static void testAndPrint(int concurrencyLevel, int totalRequests, StressTask stressTask) {
		testAndPrint(concurrencyLevel, totalRequests, stressTask, null);
	}

	public static void testAndPrint(int concurrencyLevel, int totalRequests, StressTask stressTask, String testName) {
		StressResult stressResult = test(concurrencyLevel, totalRequests, stressTask);
		String str = format(stressResult);
		System.out.println(str);
	}

	public static String format(StressResult stressResult) {
		return format(stressResult, simpleResultFormater);
	}

	public static String format(StressResult stressResult, StressResultFormater stressResultFormater) {
		StringWriter sw = new StringWriter();
		stressResultFormater.format(stressResult, sw);
		return sw.toString();
	}

}
