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
@AutoService(DqlExecuteExtender.class)
public class DqlMtcpExtender implements DqlExecuteExtender {

    @Override
    public void preExecute(ILoggingEvent eventObject) {
        MtcpContext.setTenantId(MDC.get(TENANT_ID));
        MtcpContext.setTenantCode(MDC.get(TENANT_CODE));
    }

    @Override
    public void afterExecute(ILoggingEvent eventObject) {
        MtcpContext.clear();
    }
}
