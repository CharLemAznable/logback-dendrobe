### logback-miner

[![Build Status](https://travis-ci.org/CharLemAznable/logback-miner.svg?branch=master)](https://travis-ci.org/CharLemAznable/logback-miner)
[![codecov](https://codecov.io/gh/CharLemAznable/logback-miner/branch/master/graph/badge.svg)](https://codecov.io/gh/CharLemAznable/logback-miner)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.charlemaznable/logback-miner/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.charlemaznable/logback-miner/)
[![MIT Licence](https://badges.frapsoft.com/os/mit/mit.svg?v=103)](https://opensource.org/licenses/mit-license.php)
![GitHub code size](https://img.shields.io/github/languages/code-size/CharLemAznable/logback-miner)

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=CharLemAznable_logback-miner&metric=alert_status)](https://sonarcloud.io/dashboard?id=CharLemAznable_logback-miner)

[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=CharLemAznable_logback-miner&metric=reliability_rating)](https://sonarcloud.io/dashboard?id=CharLemAznable_logback-miner)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=CharLemAznable_logback-miner&metric=bugs)](https://sonarcloud.io/dashboard?id=CharLemAznable_logback-miner)

[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=CharLemAznable_logback-miner&metric=security_rating)](https://sonarcloud.io/dashboard?id=CharLemAznable_logback-miner)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=CharLemAznable_logback-miner&metric=vulnerabilities)](https://sonarcloud.io/dashboard?id=CharLemAznable_logback-miner)

[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=CharLemAznable_logback-miner&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=CharLemAznable_logback-miner)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=CharLemAznable_logback-miner&metric=sqale_index)](https://sonarcloud.io/dashboard?id=CharLemAznable_logback-miner)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=CharLemAznable_logback-miner&metric=code_smells)](https://sonarcloud.io/dashboard?id=CharLemAznable_logback-miner)

[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=CharLemAznable_logback-miner&metric=ncloc)](https://sonarcloud.io/dashboard?id=CharLemAznable_logback-miner)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=CharLemAznable_logback-miner&metric=coverage)](https://sonarcloud.io/dashboard?id=CharLemAznable_logback-miner)
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=CharLemAznable_logback-miner&metric=duplicated_lines_density)](https://sonarcloud.io/dashboard?id=CharLemAznable_logback-miner)

自定义扩展Logback-Classic.

##### Maven Dependency

```xml
<dependency>
  <groupId>com.github.charlemaznable</groupId>
  <artifactId>logback-miner</artifactId>
  <version>0.0.1</version>
</dependency>
```

##### Maven Dependency SNAPSHOT

```xml
<dependency>
  <groupId>com.github.charlemaznable</groupId>
  <artifactId>logback-miner</artifactId>
  <version>0.0.2-SNAPSHOT</version>
</dependency>
```

#### 目标功能

1. 热更新日志配置
2. 简化日志配置, 将xml格式替换为properties格式
3. 日志默认配置, 默认使用异步日志队列输出格式化日志到标准输出
4. 特定类型的日志参数Bean映射插入数据库日志表

#### 快速开始

本地类路径添加配置文件```logback-miner.properties```, 配置日志配置diamond的group&dataId. 默认为```Logback:default```.

```
logback.miner.group=Logback
logback.miner.dataId=default
```

diamond配置日志:
```
context.packagingDataEnabled=false
context.maxCallerDataDepth=8
context.frameworkPackages=

context.property[property-name]=property-value

{root-or-class-name-or-package-name}[additivity]=true
{root-or-class-name-or-package-name}[level]=INFO

{root-or-class-name-or-package-name}[console.level]=info
{root-or-class-name-or-package-name}[console.charset]=utf-8
{root-or-class-name-or-package-name}[console.pattern]=%date [%-20thread] %5level %logger{50}\\(%line\\): %message%n
{root-or-class-name-or-package-name}[console.target]=System.out
{root-or-class-name-or-package-name}[console.immediateFlush]=true

{root-or-class-name-or-package-name}[dql.level]=info
{root-or-class-name-or-package-name}[dql.connection]=DEFAULT
```

可在本地配置文件```logback-miner.properties```内添加同名配置, 作为默认配置, 优先级低于diamond配置.

#### 功能说明

1. 日志级别

  * ```[level]```配置: 设置默认日志级别.
  * ```[console.level]```配置: 设置控制台输出日志级别, 覆盖当前日志的默认日志级别.
  * ```[dql.level]```配置: 设置数据库插入日志级别, 覆盖当前日志的默认日志级别.
  * 控制台输出日志/数据库插入日志级别未设置时, 优先使用当前日志的默认日志级别, 若未设置默认日志级别, 则使用父级日志的对应日志级别.

2. 日志参数Bean注解

  * ```@LogbackBean```: 只有添加此注解, 该类型的日志参数对象才会插入数据库日志表.
  * 可填写```@LogbackBean```注解的```value```, 覆盖日志配置的默认的dql连接配置.
  * 日志参数Bean默认插入的日志表名为类名的下划线格式, e.g. ```class TestLog```插入表```table TEST_LOG```, 可使用```@LogbackTable```注解另行指定.
  * 日志参数Bean默认插入的日志字段为类型声明的非静态字段, 列名为字段名的下划线格式, 可使用```@LogbackColumn```注解另行指定, 或使用```@LogbackSkip```注解指定排除.
  * 可使用```@LogbackSql```注解另行指定插入日志的```sqlFile```和```sqlId```, 默认为当前类型的```[log{类名}]```.

#### 待开发功能

1. 使用Vert.x Event Bus实现集群化日志收集
