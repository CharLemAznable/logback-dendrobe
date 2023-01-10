### logback-dendrobe

[![Build](https://github.com/CharLemAznable/logback-dendrobe/actions/workflows/build.yml/badge.svg)](https://github.com/CharLemAznable/logback-dendrobe/actions/workflows/build.yml)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.charlemaznable/logback-dendrobe/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.charlemaznable/logback-dendrobe/)
[![MIT Licence](https://badges.frapsoft.com/os/mit/mit.svg?v=103)](https://opensource.org/licenses/mit-license.php)
![GitHub code size](https://img.shields.io/github/languages/code-size/CharLemAznable/logback-dendrobe)

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=CharLemAznable_logback-dendrobe&metric=alert_status)](https://sonarcloud.io/dashboard?id=CharLemAznable_logback-dendrobe)

[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=CharLemAznable_logback-dendrobe&metric=reliability_rating)](https://sonarcloud.io/dashboard?id=CharLemAznable_logback-dendrobe)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=CharLemAznable_logback-dendrobe&metric=bugs)](https://sonarcloud.io/dashboard?id=CharLemAznable_logback-dendrobe)

[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=CharLemAznable_logback-dendrobe&metric=security_rating)](https://sonarcloud.io/dashboard?id=CharLemAznable_logback-dendrobe)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=CharLemAznable_logback-dendrobe&metric=vulnerabilities)](https://sonarcloud.io/dashboard?id=CharLemAznable_logback-dendrobe)

[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=CharLemAznable_logback-dendrobe&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=CharLemAznable_logback-dendrobe)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=CharLemAznable_logback-dendrobe&metric=sqale_index)](https://sonarcloud.io/dashboard?id=CharLemAznable_logback-dendrobe)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=CharLemAznable_logback-dendrobe&metric=code_smells)](https://sonarcloud.io/dashboard?id=CharLemAznable_logback-dendrobe)

[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=CharLemAznable_logback-dendrobe&metric=ncloc)](https://sonarcloud.io/dashboard?id=CharLemAznable_logback-dendrobe)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=CharLemAznable_logback-dendrobe&metric=coverage)](https://sonarcloud.io/dashboard?id=CharLemAznable_logback-dendrobe)
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=CharLemAznable_logback-dendrobe&metric=duplicated_lines_density)](https://sonarcloud.io/dashboard?id=CharLemAznable_logback-dendrobe)

自定义扩展Logback-Classic.

##### Maven Dependency

```xml
<dependency>
  <groupId>com.github.charlemaznable</groupId>
  <artifactId>logback-dendrobe</artifactId>
  <version>2022.0.3</version>
</dependency>
```

##### Maven Dependency SNAPSHOT

```xml
<dependency>
  <groupId>com.github.charlemaznable</groupId>
  <artifactId>logback-dendrobe</artifactId>
  <version>2022.0.4-SNAPSHOT</version>
</dependency>
```

#### 目标功能

1. 简化日志配置, 将xml格式替换为properties格式, 并省略常用默认配置
2. 日志默认配置, 使用异步日志队列
3. 特定类型的日志参数Bean映射插入数据库日志表
4. 通过配置Vert.x集群, 将日志事件转化为EventBus消息, 便于将来统一日志服务
5. 通过配置ES客户端, 将日志数据直接提交ES存储

#### 快速开始

1. 本地类路径添加配置文件```logback-dendrobe.properties```:

```
root[level]=info    # 配置根级别logger日志级别为info, 默认为debug, "level"关键字和日志级别不区分大小写
```

2. 配置日志additive属性

```
{logger-name}[additivity]={true/yes/on/y为真值, 其他为假值}  # "additivity"关键字和配置值不区分大小写
```

3. 配置控制台输出

```
{logger-name}[console.level]=debug
{logger-name}[console.charset]=utf-8
{logger-name}[console.pattern]=%date [%20.20thread] %5level %50.50logger{50}\\(%4.4line\\): %message%n
{logger-name}[console.target]=System.out
{logger-name}[console.immediateFlush]={true/yes/on/y为真值, 其他为假值}
```

以上配置中:

  * "console.XXX"关键字不区分大小写
  * 日志级别不区分大小写, 覆盖当前级别日志的```[level]```
  * 控制台日志级别未设置时, 优先使用当前级别日志的```[level]```, 若未设置```[level]```, 则使用父级日志的控制台日志级别
  * 字符编码可选值参见```java.nio.charset.Charset```
  * 日志输出格式参见```ch.qos.logback.core.pattern.PatternLayoutEncoderBase```
  * 日志输出目标可选值为: ```System.out```或```System.err```

配置以上任一项, 即启动对应级别logger日志控制台输出

4. 配置数据库日志

```
{logger-name}[eql.level]=debug
{logger-name}[eql.connection]=DEFAULT
{logger-name}[eql.sql]=
{logger-name}[eql.tableNamePattern]=
{logger-name}[eql.prepareSql]=
```

以上配置中:

  * "eql.XXX"关键字不区分大小写
  * 日志级别不区分大小写, 覆盖当前级别日志的```[level]```
  * 数据库日志级别未设置时, 优先使用当前级别日志的```[level]```, 若未设置```[level]```, 则使用父级日志的数据库日志级别
  * eql.connection配置默认使用的Eql连接配置名, 即```new Eql("XXX")```中的```"XXX"```, 默认连接配置文件路径为```eql/eql-XXX.properties```
  * eql.sql配置默认使用的日志插入SQL语句, 可选参数参见```com.github.charlemaznable.logback.dendrobe.appender.LoggingEventElf```, 如: ```event.message```, ```mdc.XXX```, ```property.XXX```, 等
  * 配置eql.tableNamePattern可设置按日期时间滚动日志表, 配置eql.prepareSql可动态创建滚动日志表, 需在sql语句中使用```$activeTableName$```替代滚动日志表名

配置以上任一项, 即启动对应级别logger数据库日志

数据库插入参数规则:

  * 如参数中不包含```@EqlLogBean```注解的类型的对象, 则使用默认连接默认插入SQL, 可选参数为```event.message```, ```mdc.XXX```, ```property.XXX```, 等
  * 如参数中包含```@EqlLogBean```注解的类型的对象, 则遍历所有此类参数进行数据库插入, 除上述可选参数外, 另可使用```arg.字段名```作为参数
  * 可填写```@EqlLogBean```注解的```value```, 覆盖日志配置的默认的eql连接配置
  * 日志参数Bean默认插入的日志表名为类名的下划线格式, e.g. ```class TestLog```插入表```table TEST_LOG```, 可使用```@EqlLogTable```注解另行指定
  * 日志参数Bean默认插入的日志字段为类型声明的非静态字段, 列名为字段名的下划线格式, 可使用```@EqlLogColumn```注解另行指定, 或使用```@EqlLogSkip```注解指定排除
  * 可使用```@EqlLogSql```注解另行指定插入日志的```sqlFile```和```sqlId```, 默认为当前类型对应的```sqlFile```中的名为```[log{类名}]```的SQL语句
  * 可使用```@EqlLogRollingSql```注解指定滚动日志表名模式和滚动日志表准备sql语句, 需在sql语句中使用```$activeTableName$```替代滚动日志表名
  * 使用```@EqlLogRollingSql```时, 日志参数Bean默认插入的日志表名将改为```$activeTableName$```, 除非使用```@EqlLogTable```或```@EqlLogSql```进行自定义

5. 配置Vert.x日志

```
{logger-name}[vertx.level]=debug
{logger-name}[vertx.name]=DEFAULT
{logger-name}[vertx.address]={logger-name}
```

以上配置中:

  * "vertx.XXX"关键字不区分大小写
  * 日志级别不区分大小写, 覆盖当前级别日志的```[level]```
  * Vert.x日志级别未设置时, 优先使用当前级别日志的```[level]```, 若未设置```[level]```, 则使用父级日志的Vert.x日志级别
  * vertx.name配置Vert.x实例标识, 如果存在```vertx-${vertx.name}.properties```配置文件, 则自动加载并初始化Vert.x实例用于发送日志事件消息
  * 可使用```VertxManager#putExternalVertx```方法配置自定义的Vert.x实例, 需自行控制自定义Vert.x实例的生命周期, 使用自定义的名称作为vertx.name配置
  * vertx.address配置日志事件消息发送的地址topic, 默认为logger-name
  * 日志事件消息的接收端处理器类型为```io.vertx.core.Handler<io.vertx.core.eventbus.Message<io.vertx.core.json.JsonObject>>```, 其中JsonObject包含```event```, ```mdc```和```property```三个子JsonObject

配置以上任一项, 即启动对应级别logger Vert.x日志

6. 配置日志Context

```
context.packagingDataEnabled=false              # 配置logback LoggerContext
context.maxCallerDataDepth=8
context.frameworkPackages=

context.property[property-name]=property-value  # 配置上下文属性参数, 可用于日志输出参数
```

7. 配置文件输出

```
{logger-name}[file]={filepath/filename}
{logger-name}[file.level]=debug
{logger-name}[file.charset]=utf-8
{logger-name}[file.pattern]=%date [%20.20thread] %5level %50.50logger{50}\\(%4.4line\\): %message%n
{logger-name}[file.prudent]={true/yes/on/y为真值, 其他为假值}
{logger-name}[file.append]={true/yes/on/y为真值, 其他为假值}
{logger-name}[file.bufferSize]=8192
{logger-name}[file.immediateFlush]={true/yes/on/y为真值, 其他为假值}
```

以上配置中:

  * "file.XXX"关键字不区分大小写
  * 日志级别不区分大小写, 覆盖当前级别日志的```[level]```
  * 文件日志级别未设置时, 优先使用当前级别日志的```[level]```, 若未设置```[level]```, 则使用父级日志的文件日志级别
  * 字符编码可选值参见```java.nio.charset.Charset```
  * 日志输出格式参见```ch.qos.logback.core.pattern.PatternLayoutEncoderBase```

配置以上任一项, 即启动对应级别logger日志文件输出

若未配置```[file]```项或配置为空, 则文件输出不会开启

8. 配置滚动文件输出

```
{logger-name}[rollingfile]={filepath/filename}
{logger-name}[rollingfile.level]=debug
{logger-name}[rollingfile.charset]=utf-8
{logger-name}[rollingfile.pattern]=%date [%20.20thread] %5level %50.50logger{50}\\(%4.4line\\): %message%n
{logger-name}[rollingfile.prudent]={true/yes/on/y为真值, 其他为假值}
{logger-name}[rollingfile.append]={true/yes/on/y为真值, 其他为假值}
{logger-name}[rollingfile.bufferSize]=8192
{logger-name}[rollingfile.immediateFlush]={true/yes/on/y为真值, 其他为假值}
{logger-name}[rollingfile.fileNamePattern]={true/yes/on/y为真值, 其他为假值}
{logger-name}[rollingfile.maxFileSize]=10MB
{logger-name}[rollingfile.minIndex]=1
{logger-name}[rollingfile.maxIndex]=7
{logger-name}[rollingfile.maxHistory]=0
{logger-name}[rollingfile.cleanHistoryOnStart]={true/yes/on/y为真值, 其他为假值, 默认false}
```

以上配置中:

  * "rollingfile.XXX"关键字不区分大小写
  * 日志级别不区分大小写, 覆盖当前级别日志的```[level]```
  * 滚动文件日志级别未设置时, 优先使用当前级别日志的```[level]```, 若未设置```[level]```, 则使用父级日志的滚动文件日志级别
  * 字符编码可选值参见```java.nio.charset.Charset```
  * 日志输出格式参见```ch.qos.logback.core.pattern.PatternLayoutEncoderBase```

配置以上任一项, 即启动对应级别logger日志文件输出

若未配置```[rollingfile.fileNamePattern]```, 则滚动文件输出不会开启

滚动文件输出的rollingPolicy和triggeringPolicy规则为:

  * 同时含有时间和索引模式时, 使用```SizeAndTimeBasedRollingPolicy```做为rollingPolicy和triggeringPolicy, 按```[rollingfile.maxFileSize]```配置, 默认值为```10MB```
  * 仅含有时间模式时, 使用```TimeBasedRollingPolicy```做为rollingPolicy和triggeringPolicy
  * 仅含有索引模式时, 使用```FixedWindowRollingPolicy```做为rollingPolicy, ```SizeBasedTriggeringPolicy```做为triggeringPolicy, 按```[rollingfile.minIndex]```, ```[rollingfile.maxIndex]```和```[rollingfile.maxFileSize]```配置, 默认值为```1```, ```7```和```10MB```
  * 不含以上两种模式时, 滚动文件输出不会开启

9. 配置ElasticSearch输出

```
{logger-name}[es.level]=debug
{logger-name}[es.name]=DEFAULT
{logger-name}[es.index]={logger-name缩减字符串, 最长128个字符, 并将'.'替换为'_'}
```

以上配置中:

  * "es.XXX"关键字不区分大小写
  * 日志级别不区分大小写, 覆盖当前级别日志的```[level]```
  * ElasticSearch日志级别未设置时, 优先使用当前级别日志的```[level]```, 若未设置```[level]```, 则使用父级日志的ElasticSearch日志级别
  * es.name配置ElasticSearch客户端标识, 如果存在```es-${es.name}.properties```配置文件, 则自动加载并初始化ElasticSearch客户端用于发送日志事件消息
  * 可使用```EsClientManager#putExternalEsClient```方法配置自定义的ElasticSearch客户端, 需自行控制自定义ElasticSearch客户端的生命周期, 使用自定义的名称作为es.name配置
  * es.index配置日志事件消息存储的ElasticSearch索引, 默认为: logger-name的缩减字符串, 最长128个字符, 并将'.'替换为'_'
  * 日志事件存储的ElasticSearch文档结构参见```com.github.charlemaznable.logback.dendrobe.appender.LoggingEventElf```

配置以上任一项, 即启动对应级别logger ElasticSearch日志

10. 使用默认配置启动日志输出

```
{logger-name}[appenders]=[console][eql][vertx][file][rollingfile][es]
```

当仅需启动日志的某些输出端时, 可使用此配置:

  * 配置值中包含```[console]```字符串时, 启动默认控制台输出
  * 配置值中包含```[eql]```字符串时, 启动默认数据库输出
  * 配置值中包含```[vertx]```字符串时, 启动默认Vert.x输出
  * 配置值中包含```[file]```字符串时, 启动默认文件输出, 但需另外配置```{logger-name}[file]```指定日志文件名
  * 配置值中包含```[rollingfile]```字符串时, 启动默认滚动文件输出, 但需另外配置```{logger-name}[rollingfile.fileNamePattern]```指定文件名滚动规则
  * 配置值中包含```[es]```字符串时, 启动默认ElasticSearch输出
  * "appenders"关键字和配置值不区分大小写
