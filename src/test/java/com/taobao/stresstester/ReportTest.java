package com.taobao.stresstester;

import com.taobao.stresstester.core.StressTask;

public class ReportTest {

	public static void main(String[] args) {
		StressTestUtils.testAndReport("打印测试",10, 10000, new StressTask() {
			@Override
			public Object doTask() throws Exception {
				// System.out.println("Do my task.");
				if (true) {
					throw new IllegalArgumentException("xxxxxxxx");
				}
				return null;

			}
		});
	}
}
