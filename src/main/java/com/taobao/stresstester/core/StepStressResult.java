package com.taobao.stresstester.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 阶梯压测总结果
 */
public class StepStressResult {

	/** 所有步骤结果（按执行顺序） */
	private List<StepResult> steps = new ArrayList<StepResult>();

	/** 阶梯压测名称 */
	private String testName;

	public void addStep(StepResult step) {
		steps.add(step);
	}

	public List<StepResult> getSteps() {
		return Collections.unmodifiableList(steps);
	}

	public int getStepCount() {
		return steps.size();
	}

	/** 获取所有步骤的汇总总请求数 */
	public int getTotalRequests() {
		int total = 0;
		for (StepResult sr : steps) {
			total += sr.getStressResult().getTotalRequests();
		}
		return total;
	}

	/** 获取所有步骤的汇总失败请求数 */
	public int getTotalFailedRequests() {
		int total = 0;
		for (StepResult sr : steps) {
			total += sr.getStressResult().getFailedRequests();
		}
		return total;
	}

	/** 获取所有步骤的所有耗时数据（合并） */
	public List<Long> getAllTimes() {
		List<Long> all = new ArrayList<Long>();
		for (StepResult sr : steps) {
			all.addAll(sr.getStressResult().getAllTimes());
		}
		return all;
	}

	/** 获取最大并发数（最后一步的并发数） */
	public int getMaxConcurrencyLevel() {
		if (steps.isEmpty()) return 0;
		return steps.get(steps.size() - 1).getStressResult().getConcurrencyLevel();
	}

	public String getTestName() {
		return testName;
	}

	public void setTestName(String testName) {
		this.testName = testName;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("===== Step Stress Test Result =====\n");
		if (testName != null && !testName.isEmpty()) {
			sb.append("Test Name: ").append(testName).append("\n");
		}
		sb.append("Total Steps: ").append(getStepCount()).append("\n");
		sb.append("Total Requests: ").append(getTotalRequests()).append("\n");
		sb.append("Total Failed: ").append(getTotalFailedRequests()).append("\n");
		sb.append("Max Concurrency: ").append(getMaxConcurrencyLevel()).append("\n");
		for (StepResult sr : steps) {
			StressResult r = sr.getStressResult();
			sb.append(String.format("  Step %d: concurrency=%d, requests=%d, failed=%d, avg=%.2fms%n",
					sr.getStepIndex(),
					r.getConcurrencyLevel(),
					r.getTotalRequests(),
					r.getFailedRequests(),
					StatisticsUtils.toMs(StatisticsUtils.getTotal(r.getAllTimes())) / (float) r.getTotalRequests()
			));
		}
		return sb.toString();
	}
}
