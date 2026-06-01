package com.taobao.stresstester;

import com.taobao.stresstester.core.StressTask;

public class SimpleTest {

	public static void main(String[] args) {
        StressTask task = new StressTask() {
            @Override
            public Object doTask() throws Exception {
                Thread.sleep(5);
                return null;

            }
        };
        StressTestUtils.testAndReport("中文测试用例1", 10, 10000, task);
	}
}
