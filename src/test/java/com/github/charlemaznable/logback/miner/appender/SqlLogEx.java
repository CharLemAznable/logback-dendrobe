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
        sqlFile = "com/github/charlemaznable/logback/miner/appender/SqlLog.eql",
        sqlId = "logSqlLog"
)
public class SqlLogEx {

    private String logId;
    private String logContent;
}
