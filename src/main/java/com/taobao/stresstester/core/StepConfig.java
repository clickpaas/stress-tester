package com.taobao.stresstester.core;

/**
 * 阶梯压测单步配置（Builder模式）
 *
 * <pre>
 * 使用示例：
 * StepConfig step = StepConfig.builder()
 *     .concurrencyLevel(5)
 *     .requestsPerThread(20)
 *     .thinkTimeMs(100)
 *     .maxErrorRate(50.0)
 *     .build();
 * </pre>
 */
public class StepConfig {

	private int concurrencyLevel;
	private int requestsPerThread;
	private long thinkTimeMs;
	private int warmUpTime = 0;
	private double maxErrorRate = 0;

	private StepConfig() {}

	/** 获取该步骤的总请求数 */
	public int getTotalRequests() {
		return concurrencyLevel * requestsPerThread;
	}

	public int getConcurrencyLevel() { return concurrencyLevel; }
	public int getRequestsPerThread() { return requestsPerThread; }
	public long getThinkTimeMs() { return thinkTimeMs; }
	public int getWarmUpTime() { return warmUpTime; }
	public double getMaxErrorRate() { return maxErrorRate; }

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private final StepConfig config = new StepConfig();

		public Builder concurrencyLevel(int concurrencyLevel) {
			config.concurrencyLevel = concurrencyLevel;
			return this;
		}

		public Builder requestsPerThread(int requestsPerThread) {
			config.requestsPerThread = requestsPerThread;
			return this;
		}

		/**
		 * 思考时间（每次请求间隔），单位毫秒。0表示不等待
		 */
		public Builder thinkTimeMs(long thinkTimeMs) {
			config.thinkTimeMs = thinkTimeMs;
			return this;
		}

		/**
		 * 预热次数，默认0
		 */
		public Builder warmUpTime(int warmUpTime) {
			config.warmUpTime = warmUpTime;
			return this;
		}

		/**
		 * 错误率阈值(0-100)，达到此值则终止压测。0表示不启用
		 */
		public Builder maxErrorRate(double maxErrorRate) {
			config.maxErrorRate = maxErrorRate;
			return this;
		}

		public StepConfig build() {
			if (config.concurrencyLevel <= 0) {
				throw new IllegalArgumentException("concurrencyLevel must be > 0, got: " + config.concurrencyLevel);
			}
			if (config.requestsPerThread <= 0) {
				throw new IllegalArgumentException("requestsPerThread must be > 0, got: " + config.requestsPerThread);
			}
			if (config.maxErrorRate < 0 || config.maxErrorRate > 100) {
				throw new IllegalArgumentException("maxErrorRate must be in [0, 100], got: " + config.maxErrorRate);
			}
			if (config.thinkTimeMs < 0) {
				throw new IllegalArgumentException("thinkTimeMs must be >= 0, got: " + config.thinkTimeMs);
			}
			return config;
		}
	}

	@Override
	public String toString() {
		return "Step{concurrency=" + concurrencyLevel
				+ ", perThread=" + requestsPerThread
				+ ", total=" + getTotalRequests()
				+ ", thinkTimeMs=" + thinkTimeMs
				+ ", warmUp=" + warmUpTime
				+ ", maxErrorRate=" + (maxErrorRate > 0 ? maxErrorRate + "%" : "disabled") + "}";
	}
}
