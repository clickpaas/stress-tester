package com.taobao.stresstester;

import com.taobao.stresstester.core.StressTask;

public class SimpleTest {

	public static void main(String[] args) {
		StressTestUtils.testAndPrint(10, 10000, new StressTask() {
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
