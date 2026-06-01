package com.taobao.stresstester.core;

import java.util.List;

/**
 * 测试结果
 * 
 * */
public class StressResult {

	private int concurrencyLevel;// 并发线程数
	private int totalRequests;// 总请求次数

	private long testsTakenTime;// 总耗时
	private int failedRequests;// 失败请求次数

	private List<Long> allTimes;// 每次请求的耗时

	private List<StressThreadWorker> workers;

	/** 是否因错误率超限而被终止 */
	private boolean aborted;

	public long getTestsTakenTime() {
		return testsTakenTime;
	}

	public int getConcurrencyLevel() {
		return concurrencyLevel;
	}

	public void setConcurrencyLevel(int concurrencyLevel) {
		this.concurrencyLevel = concurrencyLevel;
	}

	public int getTotalRequests() {
		return totalRequests;
	}

	public void setTotalRequests(int totalRequests) {
		this.totalRequests = totalRequests;
	}

	public void setTestsTakenTime(long testsTakenTime) {
		this.testsTakenTime = testsTakenTime;
	}

	public int getFailedRequests() {
		return failedRequests;
	}

	public void setFailedRequests(int failedRequests) {
		this.failedRequests = failedRequests;
	}

	public List<Long> getAllTimes() {
		return allTimes;
	}

	public void setAllTimes(List<Long> allTimes) {
		this.allTimes = allTimes;
	}

	public List<StressThreadWorker> getWorkers() {
		return workers;
	}

	public void setWorkers(List<StressThreadWorker> workers) {
		this.workers = workers;
	}

	public boolean isAborted() {
		return aborted;
	}

	public void setAborted(boolean aborted) {
		this.aborted = aborted;
	}

}
