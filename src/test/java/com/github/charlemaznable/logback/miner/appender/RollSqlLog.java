package com.github.charlemaznable.logback.miner.appender;

import com.github.charlemaznable.logback.miner.annotation.LogbackBean;
import com.github.charlemaznable.logback.miner.annotation.LogbackRollingSql;
import com.github.charlemaznable.logback.miner.annotation.LogbackSql;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@LogbackBean
@LogbackSql
@LogbackRollingSql(tableNamePattern = "B_ROLLING_LOG_%d{yyyyMMddHHmmss}")
public class RollSqlLog {

    private String logId;
    private String logContent;

    public RollSqlLog(String logContent) {
        this.logContent = logContent;
    }
}
