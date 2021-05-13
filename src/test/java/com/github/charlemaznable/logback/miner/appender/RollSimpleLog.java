package com.github.charlemaznable.logback.miner.appender;

import com.github.charlemaznable.logback.miner.annotation.LogbackBean;
import com.github.charlemaznable.logback.miner.annotation.LogbackRollingSql;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@LogbackBean
@LogbackRollingSql(tableNamePattern = "S_ROLLING_LOG_%d{yyyyMMddHHmmss}", sqlId = "prepare")
public class RollSimpleLog {

    private String logId;
    private String logContent;
}
