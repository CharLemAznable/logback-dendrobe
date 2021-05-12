package com.github.charlemaznable.logback.miner.appender;

import com.github.charlemaznable.logback.miner.annotation.LogbackBean;
import com.github.charlemaznable.logback.miner.annotation.LogbackRolling;
import com.github.charlemaznable.logback.miner.annotation.LogbackSql;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@LogbackBean
@LogbackSql
@LogbackRolling(tableNamePattern = "B_ROLLING_LOG_%d{yyyyMMddHHmmss}")
public class RollSqlLog {

    private String logId;
    private String logContent;

    public RollSqlLog(String logContent) {
        this.logContent = logContent;
    }
}
