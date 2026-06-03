package com.taobao.stresstester.core;

/**
 * 
 * 需要执行的测试任务
 * 
 */
@FunctionalInterface
public interface StressTask {


	public Object doTask() throws Exception;

}
