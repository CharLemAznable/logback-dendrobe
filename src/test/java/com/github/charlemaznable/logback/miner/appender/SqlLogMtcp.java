package com.github.charlemaznable.logback.miner.appender;

import com.github.charlemaznable.logback.miner.annotation.LogbackBean;
import com.github.charlemaznable.logback.miner.annotation.LogbackSql;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@LogbackBean("db.mtcp")
@LogbackSql(
        sqlClass = SqlLog.class,
        sqlId = "logSqlLog"
)
public class SqlLogMtcp {

    private String logId;
    private String logContent;
}
