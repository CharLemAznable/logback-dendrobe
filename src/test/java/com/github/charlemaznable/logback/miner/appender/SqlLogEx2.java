package com.github.charlemaznable.logback.miner.appender;

import com.github.charlemaznable.logback.miner.annotation.DqlLogBean;
import com.github.charlemaznable.logback.miner.annotation.DqlLogSql;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@DqlLogBean
@DqlLogSql(
        sqlClass = SqlLog.class,
        sqlId = "logSqlLog"
)
public class SqlLogEx2 {

    private String logId;
    private String logContent;
}
