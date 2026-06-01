package com.taobao.stresstester.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 阶梯压测单步结果
 */
public class StepResult {

	/** 步骤序号（从1开始） */
	private int stepIndex;
	/** 该步骤的配置 */
	private StepConfig config;
	/** 该步骤的压测结果 */
	private StressResult stressResult;

	public StepResult(int stepIndex, StepConfig config, StressResult stressResult) {
		this.stepIndex = stepIndex;
		this.config = config;
		this.stressResult = stressResult;
	}

	public int getStepIndex() {
		return stepIndex;
	}

	public StepConfig getConfig() {
		return config;
	}

	public StressResult getStressResult() {
		return stressResult;
	}
}
