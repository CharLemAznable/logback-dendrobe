package com.github.charlemaznable.logback.dendrobe.appender;

import com.github.charlemaznable.logback.dendrobe.EqlLogBean;
import com.github.charlemaznable.logback.dendrobe.EqlLogSql;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqlLogBean
@EqlLogSql(
        sqlClass = SqlLog.class,
        sqlId = "logSqlLog"
)
public class SqlLogEx2 {

    private String logId;
    private String logContent;
}
