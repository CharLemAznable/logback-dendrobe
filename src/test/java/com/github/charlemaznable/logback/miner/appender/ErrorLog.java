package com.github.charlemaznable.logback.miner.appender;

import com.github.charlemaznable.logback.miner.annotation.DqlLogBean;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@DqlLogBean("error")
public class ErrorLog {

    private String logId;
    private String logContent;
}
