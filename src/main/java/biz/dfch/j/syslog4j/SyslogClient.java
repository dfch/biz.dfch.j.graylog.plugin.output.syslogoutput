package biz.dfch.j.syslog4j;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.istack.internal.NotNull;
import org.msgpack.annotation.NotNullable;
import org.productivity.java.syslog4j.*;
import org.productivity.java.syslog4j.impl.message.modifier.checksum.ChecksumSyslogMessageModifier;
import org.productivity.java.syslog4j.impl.message.modifier.hash.HashSyslogMessageModifier;
import org.productivity.java.syslog4j.impl.message.modifier.sequential.SequentialSyslogMessageModifier;
import org.productivity.java.syslog4j.impl.message.structured.StructuredSyslogMessage;
import org.productivity.java.syslog4j.impl.net.tcp.TCPNetSyslogConfig;
import org.productivity.java.syslog4j.impl.net.tcp.ssl.SSLTCPNetSyslogConfig;
import org.productivity.java.syslog4j.impl.net.udp.UDPNetSyslog;
import org.productivity.java.syslog4j.impl.net.udp.UDPNetSyslogConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by root on 2/22/15.
 */
public class SyslogClient 
{
    private SyslogIF syslog = null;
    private SyslogConfigIF syslogConfig = null;
    private String syslogStructuredDataTag = "SDATA";

    private static final int SYSLOG_PORT_UDP_DEFAULT = 514;
    private static final int SYSLOG_PORT_TCP_DEFAULT = 5514;
    private static final int SYSLOG_PORT_TCPTLS_DEFAULT = 10514;

    private static final int SYSLOG_SEVERITY_EMERGENCY = 0;
    private static final int SYSLOG_SEVERITY_ALERT = 1;
    private static final int SYSLOG_SEVERITY_CRITICAL = 2;
    private static final int SYSLOG_SEVERITY_ERROR = 3;
    private static final int SYSLOG_SEVERITY_WARNING = 4;
    private static final int SYSLOG_SEVERITY_NOTICE = 5;
    private static final int SYSLOG_SEVERITY_INFORMATIONAL = 6;
    private static final int SYSLOG_SEVERITY_DEBUG = 7;

    private static final int SYSLOG_FACILITY_0 = 0;
    private static final int SYSLOG_FACILITY_LOCAL0 = 16;
    private static final int SYSLOG_FACILITY_MAX = 23;
    
    public SyslogClient(@NotNullable String transport,@NotNullable String serverName, int serverPort)
    {
        switch(transport.toUpperCase())
        {
            case "TCP":
                syslog = Syslog.getInstance("tcp");
                syslogConfig = syslog.getConfig();
                break;
            case "TCPTLS":
                syslogConfig = new SSLTCPNetSyslogConfig();
                syslog = Syslog.createInstance("sslTcp", syslogConfig);
                break;
            case "UDP":
            default:
                syslog = Syslog.getInstance("udp");
                syslogConfig = syslog.getConfig();
                break;
        }
        if(null == serverName || serverName.isEmpty())
        {
            serverName = "localhost";
        }
        if(0 == serverPort)
        {
            serverPort = SYSLOG_PORT_UDP_DEFAULT;
        }
        syslogConfig.setHost(serverName);
        syslogConfig.setPort(serverPort);
        
    }

    public SyslogConfigIF getConfig()
    {
        return this.syslogConfig;
    }

    public void setFacility(int val)
    {
        if(SYSLOG_FACILITY_0 > val && SYSLOG_FACILITY_MAX < val)
        {
            val = SYSLOG_FACILITY_LOCAL0;
        }
        syslogConfig.setFacility(val);
    }
    public void setLocalName(@NotNull String val)
    {
      syslogConfig.setLocalName(val);
    }

    public void setStructuredData(boolean flag, String tag)
    {
        if(null == tag || tag.isEmpty())
        {
            tag = syslogStructuredDataTag;
        }
        syslogStructuredDataTag = tag;
        syslogConfig.setUseStructuredData(flag);
    }

    public void log(int severity, @NotNullable String message)
    {
        if(SYSLOG_SEVERITY_EMERGENCY > severity && SYSLOG_SEVERITY_DEBUG < severity)
        {
            severity = SYSLOG_SEVERITY_INFORMATIONAL;
        }
        syslog.log(severity, message);
    }

    public void log(int severity, int facility, @NotNullable String message)
    {
        if(SYSLOG_SEVERITY_EMERGENCY > severity && SYSLOG_SEVERITY_DEBUG < severity)
        {
            severity = SYSLOG_SEVERITY_INFORMATIONAL;
        }
        if(SYSLOG_FACILITY_0 > facility && SYSLOG_FACILITY_MAX < facility)
        {
            facility = SYSLOG_FACILITY_LOCAL0;
        }
        syslogConfig.setFacility(facility);
        syslog.log(severity, message);
    }

    public void log(int severity, @NotNullable String messageId, @NotNullable Map<String, Object> structuredData, String message)
    {
        if(SYSLOG_SEVERITY_EMERGENCY > severity && SYSLOG_SEVERITY_DEBUG < severity)
        {
            severity = SYSLOG_SEVERITY_INFORMATIONAL;
        }

        Map structuredDataContainer = new HashMap();
        structuredDataContainer.put(syslogStructuredDataTag, structuredData);

        StructuredSyslogMessage structuredMessage = new StructuredSyslogMessage(
                messageId
                ,
                structuredDataContainer
                ,
                message
        );
        syslog.info(structuredMessage);
        this.log(severity, message);
    }

    public void logDebug(@NotNullable String message)
    {
        syslog.debug(message);
    }

    public void logInfo(@NotNullable String message)
    {
        syslog.info(message);
    }

    public void logNotice(@NotNullable String message)
    {
        syslog.notice(message);
    }

    public void logWarn(@NotNullable String message)
    {
        syslog.warn(message);
    }

    public void logError(@NotNullable String message)
    {
        syslog.error(message);
    }

    public void logCritical(@NotNullable String message)
    {
        syslog.critical(message);
    }

    public void logAlert(@NotNullable String message)
    {
        syslog.alert(message);
    }

    public void logEmergency(@NotNullable String message)
    {
        syslog.emergency(message);
    }
}
