package com.github.charlemaznable.logback.miner.appender;

import com.github.charlemaznable.logback.miner.annotation.DqlLogBean;
import com.github.charlemaznable.logback.miner.annotation.DqlLogRollingSql;
import com.github.charlemaznable.logback.miner.annotation.DqlLogSql;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@DqlLogBean
@DqlLogSql
@DqlLogRollingSql(tableNamePattern = "B_ROLLING_LOG_%d{yyyyMMddHHmmss}")
public class RollSqlLog {

    private String logId;
    private String logContent;

    public RollSqlLog(String logContent) {
        this.logContent = logContent;
    }
}
