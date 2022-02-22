package com.github.charlemaznable.logback.miner.appender;

import com.github.charlemaznable.logback.miner.annotation.DqlLogBean;
import com.github.charlemaznable.logback.miner.annotation.DqlLogRollingSql;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@DqlLogBean
@DqlLogRollingSql(tableNamePattern = "S_ROLLING_LOG_%d{yyyyMMddHHmmss}", sqlId = "prepare")
public class RollSimpleLog {

    private String logId;
    private String logContent;
}
