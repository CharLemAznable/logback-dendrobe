package com.github.charlemaznable.logback.miner.appender;

import com.github.charlemaznable.logback.miner.annotation.DqlLogBean;
import lombok.Getter;
import lombok.Setter;
import org.joda.time.DateTime;

import java.util.Date;

@Getter
@Setter
@DqlLogBean
public class SimpleLog {

    private String logId;
    private String logContent;
    private Date logDate;
    private DateTime logDateTime;
}
