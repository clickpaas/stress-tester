package com.taobao.stresstester;

public class SimpleTest {

	public static void main(String[] args) {
		// 使用 StressTaskWrap + lambda（无返回值）
		StressTestUtils.testAndReport("中文测试用例1", 10, 10000, () -> pert());
	}

	private static void pert() throws Exception {
		Thread.sleep(5);
	}
}
