package com.github.charlemaznable.logback.miner.appender;

import com.github.charlemaznable.logback.miner.annotation.LogbackBean;
import com.github.charlemaznable.logback.miner.annotation.LogbackSql;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@LogbackBean
@LogbackSql
public class SqlLog {

    private String logId;
    private String logContent;
}
