package biz.dfch.j.syslog4j;

import com.fasterxml.jackson.databind.ObjectMapper;
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
            serverPort = 514;
        }
        syslogConfig.setHost(serverName);
        syslogConfig.setPort(serverPort);
        
//        syslog.getConfig().addMessageModifier(ChecksumSyslogMessageModifier.createCRC32());
//        syslog.getConfig().addMessageModifier(SequentialSyslogMessageModifier.createDefault());
//        syslog.getConfig().addMessageModifier(HashSyslogMessageModifier.createSHA256());
    }

    public void setFacility(int val)
    {
        if(0 > val && 23 < val)
        {
            val = 16;
        }
        syslogConfig.setFacility(val);
    }

    public void setStructuredData(boolean flag, String tag)
    {
        if(null == tag || tag.isEmpty())
        {
            tag = "SDATA";
        }
        syslogStructuredDataTag = tag;
        syslogConfig.setUseStructuredData(flag);
    }

    public void log(int severity, @NotNullable String message)
    {
        if(0 > severity && 7 < severity)
        {
            severity = 6;
        }
        syslog.log(severity, message);
    }

    public void log(int severity, @NotNullable Map<String, Object> structuredData, String message)
    {
        if(0 > severity && 7 < severity)
        {
            severity = 6;
        }

        Map structuredDataContainer = new HashMap();
        structuredDataContainer.put(syslogStructuredDataTag, structuredData);

        StructuredSyslogMessage structuredMessage = new StructuredSyslogMessage(
                "messageId"
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