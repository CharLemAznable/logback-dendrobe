package com.github.charlemaznable.logback.dendrobe.appender;

import com.github.charlemaznable.logback.dendrobe.eql.EqlExecuteExtender;
import com.google.auto.service.AutoService;

@AutoService(EqlExecuteExtender.class)
public class EmptyExtender implements EqlExecuteExtender {
}
