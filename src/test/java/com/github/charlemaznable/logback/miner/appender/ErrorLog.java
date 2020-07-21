package com.github.charlemaznable.logback.miner.appender;

import com.github.charlemaznable.logback.miner.annotation.LogbackBean;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@LogbackBean("error")
public class ErrorLog {

    private String logId;
    private String logContent;
}
