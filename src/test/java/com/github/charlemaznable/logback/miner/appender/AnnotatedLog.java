package com.github.charlemaznable.logback.miner.appender;

import com.github.charlemaznable.logback.miner.annotation.DqlLogBean;
import com.github.charlemaznable.logback.miner.annotation.DqlLogColumn;
import com.github.charlemaznable.logback.miner.annotation.DqlLogSkip;
import com.github.charlemaznable.logback.miner.annotation.DqlLogTable;
import lombok.Getter;
import lombok.Setter;
import org.joda.time.DateTime;

import java.util.Date;

@Getter
@Setter
@DqlLogBean("db1")
@DqlLogTable("simple_log")
public class AnnotatedLog {

    @DqlLogColumn("log_id")
    private String aLogId;
    @DqlLogColumn("log_content")
    private String aLogContent;
    @DqlLogColumn("log_date")
    private Date aLogDate;
    @DqlLogColumn("log_date_time")
    private DateTime aLogDateTime;
    @DqlLogSkip
    private String aLogSkip;
}
