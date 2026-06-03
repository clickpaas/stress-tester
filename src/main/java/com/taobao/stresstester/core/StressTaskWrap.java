package com.taobao.stresstester.core;

/**
 * 
 * 无返回值的测试任务，支持 lambda 表达式：() -> doSomething()
 * 
 */
@FunctionalInterface
public interface StressTaskWrap {

	public void execute() throws Exception;

	/**
	 * 转换为带返回值的 StressTask
	 */
	default StressTask toStressTask() {
		return () -> {
			execute();
			return null;
		};
	}

}
