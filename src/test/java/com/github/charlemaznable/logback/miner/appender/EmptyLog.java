package com.github.charlemaznable.logback.miner.appender;

import com.github.charlemaznable.logback.miner.annotation.LogbackBean;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@LogbackBean
public class EmptyLog {

    private String logId;
    private String logContent;
}
