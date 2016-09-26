package com.taobao.stresstester;

import org.taobao.stresstester.core.StressTask;

public class Example {

	public static void main(String[] args) {
		// 并发数，总任务数，要测试的代码
		StressTestUtils.testAndPrint(100, 1000, new StressTask() {
			@Override
			public Object doTask() throws Exception {
				//System.out.println("Do my task.");
				return null;
			}
		});

	}
}
