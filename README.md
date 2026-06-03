# Stress Tester

轻量级 Java 压力测试工具，支持阶梯压测、思考时间、错误率终止阈值。

## 快速开始

```java
import com.taobao.stresstester.StressTestUtils;

// 无返回值 lambda —— 推荐
StressTestUtils.testAndReport("压测示例", 10, 10000, () -> {
    service.doSomething();   // 你的业务方法
});
```

### Maven

```xml
<dependency>
    <groupId>com.taobao</groupId>
    <artifactId>stress-tester</artifactId>
    <version>6.5.3-SNAPSHOT</version>
</dependency>
```

## 测试结果展示

### Console / Log

```
Test Name:	压测示例--测试名称

 Concurrency Level:	10--并发数
 Time taken for tests:	5608.879 ms--测试耗时
 Complete Requests:	10000--完成测试次数
 Failed Requests:	0--失败次数
 Requests per second:	1785.6179--QPS
 Time per request:	5.6003027 ms--平均耗时
 Time per request:	0.5600302 ms (across all concurrent requests)--平均耗时，忽略并发影响
 Shortest request:	5.008459 ms--最短耗时
 Percentage of the requests served within a certain time (ms)
  50%	5.633083
  66%	5.635542
  75%	5.637667
  80%	5.639583
  90%	5.650667
  95%	5.658875
  98%	5.677042
  99%	5.696833
 100%	9.594459 (longest request)
```

### HTML 报告

<img width="474" height="548" alt="HTML报告" src="https://github.com/user-attachments/assets/4008eb81-a3bd-4d04-87dd-baa7db199291" />

### CSV 数据

<img width="1263" height="241" alt="CSV数据" src="https://github.com/user-attachments/assets/7c80e5a8-3408-423e-8a97-e60983cba732" />

### 阶梯压测 Console

```
===== Step 1/3: Step{concurrency=1, perThread=10, total=10, thinkTimeMs=100} =====
===== Step 2/3: Step{concurrency=2, perThread=10, total=20, thinkTimeMs=100} =====
===== Step 3/3: Step{concurrency=4, perThread=10, total=40, thinkTimeMs=100} =====
===== Step Stress Test Result =====
Test Name: 阶梯压测示例
Total Steps: 3
Total Requests: 70
Total Failed: 0
Max Concurrency: 4
  Step 1: concurrency=1, requests=10, failed=0, avg=11.18ms
  Step 2: concurrency=2, requests=20, failed=0, avg=10.86ms
  Step 3: concurrency=4, requests=40, failed=0, avg=10.88ms
```

## API

### 1. 基础压测

| 方法 | 说明 | 输出 |
|------|------|------|
| `testAndReport(...)` | 执行 + 控制台日志 + HTML报告 + CSV | 最全 |
| `testAndPrint(...)` | 执行 + 控制台打印 | 快速 |
| `test(...)` | 仅执行，返回 `StressResult` | 纯数据 |

**参数：**
- `testName` — 测试名称（可选）
- `concurrencyLevel` — 并发线程数
- `totalRequests` — 总请求数
- `StressTaskWrap` / `StressTask` — 测试任务
- `thinkTimeMs` — 思考时间，每次请求间隔（可选，默认 0）
- `maxErrorRate` — 错误率阈值 0~100，达此值终止压测（可选，默认 0=不启用）

### 2. 无返回值 lambda（StressTaskWrap）

```java
StressTestUtils.testAndReport("测试1", 10, 10000, () -> {
    Thread.sleep(5);
    // 你的业务逻辑
});

// 带思考时间
StressTestUtils.testAndReport("测试2", 5, 5000, () -> callApi(), 50);

// 带错误率终止
StressTestUtils.testAndReport("测试3", 10, 1000, () -> callApi(), 30.0);
```

### 3. 有返回值 lambda（StressTask）

```java
StressResult result = StressTestUtils.testAndReport("测试", 10, 5000, () -> {
    // 业务逻辑
    return "success";  // 可以有返回值
});
```

### 4. testAndPrint（仅控制台）

```java
StressTestUtils.testAndPrint(5, 1000, () -> doWork());
StressTestUtils.testAndPrint(5, 1000, () -> doWork(), 100);  // 思考时间
StressTestUtils.testAndPrint(5, 1000, () -> doWork(), 50.0); // 错误率终止
```

### 5. 阶梯压测（Step Test）

逐步增加并发，观察性能拐点：

```java
List<StepConfig> steps = Arrays.asList(
    StepConfig.builder().concurrencyLevel(2).requestsPerThread(10).thinkTimeMs(20).build(),
    StepConfig.builder().concurrencyLevel(5).requestsPerThread(10).thinkTimeMs(10).build(),
    StepConfig.builder().concurrencyLevel(10).requestsPerThread(10).build()
);

StepStressResult result = StressTestUtils.stepTestAndReport("阶梯压测", steps, () -> callApi());
```

### 6. 仅执行，拿结果

```java
StressResult result = StressTestUtils.test(10, 10000, () -> doWork());

// 阶梯压测
StepStressResult stepResult = StressTestUtils.stepTest(steps, () -> doWork());
```

## StepConfig 构造

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `concurrencyLevel` | int | 是 | 当前步骤并发线程数 |
| `requestsPerThread` | int | 是 | 每线程执行的请求次数 |
| `thinkTimeMs` | long | 否 | 思考时间（ms），默认 0 |
| `warmUpTime` | int | 否 | 预热时间，默认 0 |
| `maxErrorRate` | double | 否 | 错误率终止阈值，默认 0 |

## 输出位置

- **控制台 + app.log** — 测试结果文本
- **HTML 报告** — `~/logs/stress-tester/{testName}_{timestamp}.html`
- **CSV 数据** — `~/logs/stress-tester/result.csv`
