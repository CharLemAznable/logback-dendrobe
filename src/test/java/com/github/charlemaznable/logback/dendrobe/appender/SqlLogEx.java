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
        sqlFile = "com/github/charlemaznable/logback/dendrobe/appender/SqlLog.eql",
        sqlId = "logSqlLog"
)
public class SqlLogEx {

    private String logId;
    private String logContent;
}
