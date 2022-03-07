package com.github.charlemaznable.logback.dendrobe.appender;

import com.github.charlemaznable.logback.dendrobe.EqlLogBean;
import com.github.charlemaznable.logback.dendrobe.EqlLogColumn;
import com.github.charlemaznable.logback.dendrobe.EqlLogSkip;
import com.github.charlemaznable.logback.dendrobe.EqlLogTable;
import lombok.Getter;
import lombok.Setter;
import org.joda.time.DateTime;

import java.util.Date;

@Getter
@Setter
@EqlLogBean("db1")
@EqlLogTable("simple_log")
public class AnnotatedLog {

    @EqlLogColumn("log_id")
    private String aLogId;
    @EqlLogColumn("log_content")
    private String aLogContent;
    @EqlLogColumn("log_date")
    private Date aLogDate;
    @EqlLogColumn("log_date_time")
    private DateTime aLogDateTime;
    @EqlLogSkip
    private String aLogSkip;
}
