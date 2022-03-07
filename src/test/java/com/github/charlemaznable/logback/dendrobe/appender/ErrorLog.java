package com.github.charlemaznable.logback.dendrobe.appender;

import com.github.charlemaznable.logback.dendrobe.EqlLogBean;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqlLogBean("error")
public class ErrorLog {

    private String logId;
    private String logContent;
}
