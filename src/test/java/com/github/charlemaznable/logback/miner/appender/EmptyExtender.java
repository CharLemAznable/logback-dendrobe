package com.github.charlemaznable.logback.miner.appender;

import com.github.charlemaznable.logback.miner.dql.DqlExecuteExtender;
import com.google.auto.service.AutoService;

@AutoService(DqlExecuteExtender.class)
public class EmptyExtender implements DqlExecuteExtender {
}
