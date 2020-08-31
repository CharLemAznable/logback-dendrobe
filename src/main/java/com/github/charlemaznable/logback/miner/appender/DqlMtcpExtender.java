package com.github.charlemaznable.logback.miner.appender;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.google.auto.service.AutoService;
import org.n3r.eql.mtcp.MtcpContext;
import org.slf4j.MDC;

import static org.n3r.eql.mtcp.MtcpContext.TENANT_CODE;
import static org.n3r.eql.mtcp.MtcpContext.TENANT_ID;

/**
 * Compatible with eql mtcp
 */
@AutoService(DqlAppendExtender.class)
public class DqlMtcpExtender implements DqlAppendExtender {

    @Override
    public void preAppend(ILoggingEvent eventObject) {
        MtcpContext.setTenantId(MDC.get(TENANT_ID));
        MtcpContext.setTenantCode(MDC.get(TENANT_CODE));
    }

    @Override
    public void afterAppend(ILoggingEvent eventObject) {
        MtcpContext.clear();
    }
}
