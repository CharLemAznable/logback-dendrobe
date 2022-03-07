package com.github.charlemaznable.logback.dendrobe.appender;

import com.github.charlemaznable.logback.dendrobe.EqlLogBean;
import com.github.charlemaznable.logback.dendrobe.EqlLogRollingSql;
import com.github.charlemaznable.logback.dendrobe.EqlLogSql;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqlLogBean
@EqlLogSql
@EqlLogRollingSql(tableNamePattern = "B_ROLLING_LOG_%d{yyyyMMddHHmmss}")
public class RollSqlLog {

    private String logId;
    private String logContent;

    public RollSqlLog(String logContent) {
        this.logContent = logContent;
    }
}
