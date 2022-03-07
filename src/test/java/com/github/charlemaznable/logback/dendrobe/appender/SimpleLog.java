package com.github.charlemaznable.logback.dendrobe.appender;

import com.github.charlemaznable.logback.dendrobe.EqlLogBean;
import lombok.Getter;
import lombok.Setter;
import org.joda.time.DateTime;

import java.util.Date;

@Getter
@Setter
@EqlLogBean
public class SimpleLog {

    private String logId;
    private String logContent;
    private Date logDate;
    private DateTime logDateTime;
}
