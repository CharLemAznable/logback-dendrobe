package com.github.charlemaznable.logback.dendrobe.eql;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.google.auto.service.AutoService;
import lombok.val;
import org.n3r.eql.mtcp.MtcpContext;

import static org.n3r.eql.mtcp.MtcpContext.TENANT_CODE;
import static org.n3r.eql.mtcp.MtcpContext.TENANT_ID;

/**
 * Compatible with eql mtcp
 */
@AutoService(EqlExecuteExtender.class)
public final class EqlMtcpExtender implements EqlExecuteExtender {

    @Override
    public void preExecute(ILoggingEvent eventObject) {
        val mdc = eventObject.getMDCPropertyMap();
        MtcpContext.setTenantId(mdc.get(TENANT_ID));
        MtcpContext.setTenantCode(mdc.get(TENANT_CODE));
    }

    @Override
    public void afterExecute(ILoggingEvent eventObject) {
        MtcpContext.clear();
    }
}
