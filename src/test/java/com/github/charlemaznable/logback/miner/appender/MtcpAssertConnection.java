package com.github.charlemaznable.logback.miner.appender;

import org.n3r.eql.mtcp.MtcpContext;
import org.n3r.eql.trans.EqlSimpleConnection;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MtcpAssertConnection extends EqlSimpleConnection {

    @Override
    public Connection getConnection(String dbName) {
        assertEquals("testTenantId", MtcpContext.getTenantId());
        assertEquals("testTenantCode", MtcpContext.getTenantCode());
        return super.getConnection(dbName);
    }
}
