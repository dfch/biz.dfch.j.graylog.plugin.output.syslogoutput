package biz.dfch.j.syslog4j;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.msgpack.annotation.NotNullable;
import org.graylog2.syslog4j.*;
import org.graylog2.syslog4j.impl.message.modifier.checksum.ChecksumSyslogMessageModifier;
import org.graylog2.syslog4j.impl.message.modifier.hash.HashSyslogMessageModifier;
import org.graylog2.syslog4j.impl.message.modifier.sequential.SequentialSyslogMessageModifier;
import org.graylog2.syslog4j.impl.message.structured.StructuredSyslogMessage;
import org.graylog2.syslog4j.impl.net.tcp.TCPNetSyslogConfig;
import org.graylog2.syslog4j.impl.net.tcp.ssl.SSLTCPNetSyslogConfig;
import org.graylog2.syslog4j.impl.net.udp.UDPNetSyslog;
import org.graylog2.syslog4j.impl.net.udp.UDPNetSyslogConfig;

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
        switch(val)
        {
            case 0:
                syslog.getConfig().setFacility(SyslogConstants.FACILITY_KERN);
                break;
            case 1:
                syslog.getConfig().setFacility(SyslogConstants.FACILITY_USER);
                break;
            case 2:
                syslog.getConfig().setFacility(SyslogConstants.FACILITY_MAIL);
                break;
            case 3:
                syslog.getConfig().setFacility(SyslogConstants.FACILITY_DAEMON);
                break;
            case 4:
                syslog.getConfig().setFacility(SyslogConstants.FACILITY_AUTH);
                break;
            case 5:
                syslog.getConfig().setFacility(SyslogConstants.FACILITY_SYSLOG);
                break;
            case 6:
                syslog.getConfig().setFacility(SyslogConstants.FACILITY_LPR);
                break;
            case 7:
                syslog.getConfig().setFacility(SyslogConstants.FACILITY_NEWS);
                break;
            case 8:
                syslog.getConfig().setFacility(SyslogConstants.FACILITY_UUCP);
                break;
            case 9:
                syslog.getConfig().setFacility(SyslogConstants.FACILITY_CRON);
                break;
            case 10:
                syslog.getConfig().setFacility(SyslogConstants.FACILITY_AUTHPRIV);
                break;
            case 11:
                syslog.getConfig().setFacility(SyslogConstants.FACILITY_FTP);
                break;
            case 12:
                syslog.getConfig().setFacility(SyslogConstants.FACILITY_FTP);
                break;
            case 13:
                syslog.getConfig().setFacility(SyslogConstants.FACILITY_AUTH);
                break;
            case 14:
                syslog.getConfig().setFacility(SyslogConstants.FACILITY_AUTH);
                break;
            case 15:
                syslog.getConfig().setFacility(SyslogConstants.FACILITY_CRON);
                break;
            case 16:
                syslog.getConfig().setFacility(SyslogConstants.FACILITY_LOCAL0);
                break;
            case 17:
                syslog.getConfig().setFacility(SyslogConstants.FACILITY_LOCAL1);
                break;
            case 18:
                syslog.getConfig().setFacility(SyslogConstants.FACILITY_LOCAL2);
                break;
            case 19:
                syslog.getConfig().setFacility(SyslogConstants.FACILITY_LOCAL3);
                break;
            case 20:
                syslog.getConfig().setFacility(SyslogConstants.FACILITY_LOCAL4);
                break;
            case 21:
                syslog.getConfig().setFacility(SyslogConstants.FACILITY_LOCAL5);
                break;
            case 22:
                syslog.getConfig().setFacility(SyslogConstants.FACILITY_LOCAL6);
                break;
            case 23:
                syslog.getConfig().setFacility(SyslogConstants.FACILITY_LOCAL7);
                break;
            default:
                syslog.getConfig().setFacility(SyslogConstants.SYSLOG_FACILITY_DEFAULT);
                break;
        }
    }
    public void setLocalName(@NotNullable String val)
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

    public void log(int severity, @NotNullable String messageId, @NotNullable Map<String, String> structuredData, String message)
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
                null
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

/*
    d-fens Graylog SYSLOG Output Plugin
    Copyright (C) 2015  Ronald Rink, d-fens GmbH

    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 2.1 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA

 */
