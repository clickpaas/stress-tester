package com.taobao.stresstester;

import com.taobao.stresstester.core.StepConfig;
import com.taobao.stresstester.core.StepResult;
import com.taobao.stresstester.core.StepStressResult;
import com.taobao.stresstester.core.StressTask;

import java.util.Arrays;
import java.util.List;

public class StepTest {

	public static void main(String[] args) {
        StressTask task = new StressTask(){
            @Override
            public Object doTask() throws Exception {
                Thread.sleep(10);
                return null;
            }
        };
        List<StepConfig> steps = Arrays.asList(
                StepConfig.builder().concurrencyLevel(1).requestsPerThread(10).thinkTimeMs(100).build(),
                StepConfig.builder().concurrencyLevel(2).requestsPerThread(10).thinkTimeMs(100).build(),
                StepConfig.builder().concurrencyLevel(4).requestsPerThread(10).thinkTimeMs(100).build()
        );

        StepStressResult result = StressTestUtils.stepTest(steps, task);
        result.setTestName("阶梯压测测试完成");
        for (StepResult sr : result.getSteps()) {
            String stepText = "\n--- Step " + sr.getStepIndex() + ": " + sr.getConfig() + " ---";
            String detailText = StressTestUtils.format(sr.getStressResult());
            System.out.println(stepText + "\n" + detailText);
        }
	}
}
