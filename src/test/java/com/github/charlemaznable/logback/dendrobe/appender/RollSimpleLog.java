package com.github.charlemaznable.logback.dendrobe.appender;

import com.github.charlemaznable.logback.dendrobe.EqlLogBean;
import com.github.charlemaznable.logback.dendrobe.EqlLogRollingSql;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@EqlLogBean
@EqlLogRollingSql(tableNamePattern = "S_ROLLING_LOG_%d{yyyyMMddHHmmss}", sqlId = "prepare")
public class RollSimpleLog {

    private String logId;
    private String logContent;
}
