package com.github.charlemaznable.logback.miner.appender;

import com.github.charlemaznable.logback.miner.annotation.LogbackBean;
import com.github.charlemaznable.logback.miner.annotation.LogbackColumn;
import com.github.charlemaznable.logback.miner.annotation.LogbackSkip;
import com.github.charlemaznable.logback.miner.annotation.LogbackTable;
import lombok.Getter;
import lombok.Setter;
import org.joda.time.DateTime;

import java.util.Date;

@Getter
@Setter
@LogbackBean("db1")
@LogbackTable("SIMPLE_LOG")
public class AnnotatedLog {

    @LogbackColumn("log_id")
    private String aLogId;
    @LogbackColumn("log_content")
    private String aLogContent;
    @LogbackColumn("log_date")
    private Date aLogDate;
    @LogbackColumn("log_date_time")
    private DateTime aLogDateTime;
    @LogbackSkip
    private String aLogSkip;
}
