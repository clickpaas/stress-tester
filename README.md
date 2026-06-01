# java 快速压测工具
## 快速测试示例

```
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
```

## 测试结果(console)

```
Test Name:	中文测试用例1--测试名称

 Concurrency Level:	10--并发数
 Time taken for tests:	5608.879 ms--测试耗时
 Complete Requests:	10000--完成测试次数
 Failed Requests:	0--失败次数
 Requests per second:	1785.6179--QPS
 Time per request:	5.6003027 ms--平均耗时
 Time per request:	0.5600302 ms (across all concurrent requests)--平均耗时，忽略并发影响
 Shortest request:	5.008459 ms--最短耗时
 Percentage of the requests served within a certain time (ms)
  50%	5.633083--50% 的耗时在5.633083毫秒以下
  66%	5.635542
  75%	5.637667
  80%	5.639583
  90%	5.650667
  95%	5.658875
  98%	5.677042
  99%	5.696833
 100%	9.594459 (longest request)--最长的耗时
```

## 测试结果(html)
<img width="474" height="548" alt="image" src="https://github.com/user-attachments/assets/4008eb81-a3bd-4d04-87dd-baa7db199291" />

## 测试结果(csv)
<img width="1263" height="241" alt="image" src="https://github.com/user-attachments/assets/7c80e5a8-3408-423e-8a97-e60983cba732" />

# 阶梯压测

## 测试用例

```
public class StepTest {

    public static void main(String[] args) {
        StressTask task = new StressTask() {
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

        StepStressResult result = StressTestUtils.stepTestAndReport("阶梯压测示例", steps, task);
//        for (StepResult sr : result.getSteps()) {
//            String stepText = "\n--- Step " + sr.getStepIndex() + ": " + sr.getConfig() + " ---";
//            String detailText = StressTestUtils.format(sr.getStressResult());
//            System.out.println(stepText + "\n" + detailText);
//        }
    }
}
```

## 测试结果(console)

```
===== Step 1/3: Step{concurrency=1, perThread=10, total=10, thinkTimeMs=100, warmUp=0, maxErrorRate=disabled} =====
===== Step 2/3: Step{concurrency=2, perThread=10, total=20, thinkTimeMs=100, warmUp=0, maxErrorRate=disabled} =====
===== Step 3/3: Step{concurrency=4, perThread=10, total=40, thinkTimeMs=100, warmUp=0, maxErrorRate=disabled} =====
===== Step Stress Test Result =====
Test Name: 阶梯压测示例
Total Steps: 3
Total Requests: 70
Total Failed: 0
Max Concurrency: 4
  Step 1: concurrency=1, requests=10, failed=0, avg=11.18ms
  Step 2: concurrency=2, requests=20, failed=0, avg=10.86ms
  Step 3: concurrency=4, requests=40, failed=0, avg=10.88ms

2026.06.01 17:38:14.378 INFO  StressTestUtils : 
===== Step Stress Test Result =====
Test Name: 阶梯压测示例
Total Steps: 3
Total Requests: 70
Total Failed: 0
Max Concurrency: 4
  Step 1: concurrency=1, requests=10, failed=0, avg=11.18ms
  Step 2: concurrency=2, requests=20, failed=0, avg=10.86ms
  Step 3: concurrency=4, requests=40, failed=0, avg=10.88ms

===== Step 1: concurrency=1, total=10 =====
 Test Name:	阶梯压测示例--测试名称

 Concurrency Level:	1--并发数
 Time taken for tests:	111.787125 ms--测试耗时
 Complete Requests:	10--完成测试次数
 Failed Requests:	0--失败次数
 Requests per second:	89.45574--QPS
 Time per request:	11.178713 ms--平均耗时
 Time per request:	11.178713 ms (across all concurrent requests)--平均耗时，忽略并发影响
 Shortest request:	11.012583 ms--最短耗时
 Percentage of the requests served within a certain time (ms)
  50%	11.026334--50% 的耗时在11.026334毫秒以下
  66%	11.026709
  75%	11.031875
  80%	11.036792
  90%	12.563375
  95%	12.563375
  98%	12.563375
  99%	12.563375
 100%	12.563375 (longest request)--最长的耗时
2026.06.01 17:38:14.383 INFO  StressTestUtils : 
===== Step 1: concurrency=1, total=10 =====
 Test Name:	阶梯压测示例--测试名称

 Concurrency Level:	1--并发数
 Time taken for tests:	111.787125 ms--测试耗时
 Complete Requests:	10--完成测试次数
 Failed Requests:	0--失败次数
 Requests per second:	89.45574--QPS
 Time per request:	11.178713 ms--平均耗时
 Time per request:	11.178713 ms (across all concurrent requests)--平均耗时，忽略并发影响
 Shortest request:	11.012583 ms--最短耗时
 Percentage of the requests served within a certain time (ms)
  50%	11.026334--50% 的耗时在11.026334毫秒以下
  66%	11.026709
  75%	11.031875
  80%	11.036792
  90%	12.563375
  95%	12.563375
  98%	12.563375
  99%	12.563375
 100%	12.563375 (longest request)--最长的耗时
2026.06.01 17:38:14.420 INFO  StressTestUtils : HTML report generated: /Users/tingfeng/logs/stress-tester/阶梯压测示例_step1_20260601_173814_386.html
2026.06.01 17:38:14.421 INFO  StressTestUtils : CSV result appended: /Users/tingfeng/logs/stress-tester/result.csv
===== Step 2: concurrency=2, total=20 =====
 Test Name:	阶梯压测示例--测试名称

 Concurrency Level:	2--并发数
 Time taken for tests:	108.61971 ms--测试耗时
 Complete Requests:	20--完成测试次数
 Failed Requests:	0--失败次数
 Requests per second:	184.16042--QPS
 Time per request:	10.860098 ms--平均耗时
 Time per request:	5.430049 ms (across all concurrent requests)--平均耗时，忽略并发影响
 Shortest request:	10.070042 ms--最短耗时
 Percentage of the requests served within a certain time (ms)
  50%	11.018875--50% 的耗时在11.018875毫秒以下
  66%	11.022625
  75%	11.026375
  80%	11.027209
  90%	11.036
  95%	11.040875
  98%	11.040875
  99%	11.040875
 100%	11.040875 (longest request)--最长的耗时
2026.06.01 17:38:14.422 INFO  StressTestUtils : 
===== Step 2: concurrency=2, total=20 =====
 Test Name:	阶梯压测示例--测试名称

 Concurrency Level:	2--并发数
 Time taken for tests:	108.61971 ms--测试耗时
 Complete Requests:	20--完成测试次数
 Failed Requests:	0--失败次数
 Requests per second:	184.16042--QPS
 Time per request:	10.860098 ms--平均耗时
 Time per request:	5.430049 ms (across all concurrent requests)--平均耗时，忽略并发影响
 Shortest request:	10.070042 ms--最短耗时
 Percentage of the requests served within a certain time (ms)
  50%	11.018875--50% 的耗时在11.018875毫秒以下
  66%	11.022625
  75%	11.026375
  80%	11.027209
  90%	11.036
  95%	11.040875
  98%	11.040875
  99%	11.040875
 100%	11.040875 (longest request)--最长的耗时
2026.06.01 17:38:14.428 INFO  StressTestUtils : HTML report generated: /Users/tingfeng/logs/stress-tester/阶梯压测示例_step2_20260601_173814_422.html
2026.06.01 17:38:14.429 INFO  StressTestUtils : CSV result appended: /Users/tingfeng/logs/stress-tester/result.csv
===== Step 3: concurrency=4, total=40 =====
 Test Name:	阶梯压测示例--测试名称

 Concurrency Level:	4--并发数
 Time taken for tests:	109.67496 ms--测试耗时
 Complete Requests:	40--完成测试次数
 Failed Requests:	0--失败次数
 Requests per second:	367.72726--QPS
 Time per request:	10.877627 ms--平均耗时
 Time per request:	2.7194068 ms (across all concurrent requests)--平均耗时，忽略并发影响
 Shortest request:	10.126875 ms--最短耗时
 Percentage of the requests served within a certain time (ms)
  50%	11.009333--50% 的耗时在11.009333毫秒以下
  66%	11.017042
  75%	11.020875
  80%	11.023583
  90%	11.032834
  95%	11.071834
  98%	11.082125
  99%	11.082125
 100%	11.082125 (longest request)--最长的耗时
2026.06.01 17:38:14.429 INFO  StressTestUtils : 
===== Step 3: concurrency=4, total=40 =====
 Test Name:	阶梯压测示例--测试名称

 Concurrency Level:	4--并发数
 Time taken for tests:	109.67496 ms--测试耗时
 Complete Requests:	40--完成测试次数
 Failed Requests:	0--失败次数
 Requests per second:	367.72726--QPS
 Time per request:	10.877627 ms--平均耗时
 Time per request:	2.7194068 ms (across all concurrent requests)--平均耗时，忽略并发影响
 Shortest request:	10.126875 ms--最短耗时
 Percentage of the requests served within a certain time (ms)
  50%	11.009333--50% 的耗时在11.009333毫秒以下
  66%	11.017042
  75%	11.020875
  80%	11.023583
  90%	11.032834
  95%	11.071834
  98%	11.082125
  99%	11.082125
 100%	11.082125 (longest request)--最长的耗时
```

## 测试结果(html)

<img width="1358" height="519" alt="image" src="https://github.com/user-attachments/assets/fcdc7e66-2d57-4ce3-936d-cc4835406f6e" />

