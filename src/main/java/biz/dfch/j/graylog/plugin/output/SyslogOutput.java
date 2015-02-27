package biz.dfch.j.graylog.plugin.output;

import biz.dfch.j.syslog4j.SyslogClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.inject.assistedinject.Assisted;
import org.graylog2.plugin.Message;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.configuration.ConfigurationRequest;
import org.graylog2.plugin.configuration.fields.*;
import org.graylog2.plugin.outputs.MessageOutput;
import org.graylog2.plugin.outputs.MessageOutputConfigurationException;
import org.graylog2.plugin.streams.Stream;
import org.msgpack.annotation.NotNullable;
import org.graylog2.syslog4j.SyslogConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This is the plugin. Your class should implement one of the existing plugin
 * interfaces. (i.e. AlarmCallback, MessageInput, MessageOutput)
 */
public class SyslogOutput implements MessageOutput {
    private static final String CONFIG_SERVER_NAME = "CONFIG_SERVER_NAME";
    private static final String CONFIG_SERVER_PORT = "CONFIG_SERVER_PORT";
    private static final String CONFIG_MESSAGE_ID = "CONFIG_MESSAGE_ID";
    private static final String CONFIG_STRUCTURED_DATA_TAG = "CONFIG_STRUCTURED_DATA_TAG";
    private static final String CONFIG_USE_STRUCTURED_DATA = "CONFIG_USE_STRUCTURED_DATA";
    private static final String CONFIG_TRANSPORT_PROTOCOL = "CONFIG_TRANSPORT_PROTOCOL";
    private static final Map<String, String> CONFIG_TRANSPORT_PROTOCOL_OPTIONS = ImmutableMap.of(
            "UDP-RFC3164", "UDP-RFC3164"
            ,
            "UDP-RFC5424", "UDP-RFC5424"
            ,
            "TCP-RFC5424", "TCP-RFC5424"
            ,
            "TCPTLS-RFC5424", "TCPTLS-RFC5424"
    );
    private static final String CONFIG_LOGLEVEL_SEVERITY = "CONFIG_LOGLEVEL_SEVERITY";
    private static final String CONFIG_LOGLEVEL_FACILITY = "CONFIG_LOGLEVEL_FACILITY";
    private static final String CONFIG_FIELDS = "CONFIG_FIELDS";
    private static final String CONFIG_INCLUDE_FIELD_NAMES = "CONFIG_INCLUDE_FIELD_NAMES";
    private static final String CONFIG_USE_MESSAGE_SOURCE = "CONFIG_USE_MESSAGE_SOURCE";
    
    private static final Logger LOG = LoggerFactory.getLogger(SyslogOutput.class);
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private Configuration configuration;
    private String streamTitle;
    List<String> fields;

    private String syslogStructuredDataTag = null;
    private String syslogSeveritySource = null;
    private boolean isSyslogSeverityNumber = false;
    private int syslogSeverityNumber = 6;
    private String syslogFacilitySource = null;
    private boolean isSyslogFacilityNumber = false;
    private int syslogFacilityNumber = 16;
    private String syslogMessageId = null;
    private String configTransportProtocol = null;

    SyslogClient syslogClient;

    @Inject
    public SyslogOutput
            (
                    @NotNullable @Assisted Stream stream,
                    @NotNullable @Assisted Configuration configuration
            )
            throws MessageOutputConfigurationException {

        try {
            LOG.debug("Verifying configuration ...");

            this.configuration = configuration;

            streamTitle = stream.getTitle();
            if (null == streamTitle || streamTitle.isEmpty()) 
            {
                throw new MessageOutputConfigurationException(String.format("streamTitle: Parameter validation FAILED. Value cannot be null or empty."));
            }

            configTransportProtocol = configuration.getString("CONFIG_TRANSPORT_PROTOCOL").toUpperCase();
            if (null == configTransportProtocol || configTransportProtocol.isEmpty() || !CONFIG_TRANSPORT_PROTOCOL_OPTIONS.containsKey(configTransportProtocol)) 
            {
                throw new MessageOutputConfigurationException(String.format("configTransportProtocol: Parameter validation FAILED. Value cannot be null or empty."));
            }

            String configServerName = configuration.getString("CONFIG_SERVER_NAME");
            if (null == configServerName || configServerName.isEmpty()) 
            {
                throw new MessageOutputConfigurationException(String.format("CONFIG_SERVER_NAME: Parameter validation FAILED. Value cannot be null or empty."));
            }

            int configServerPort = configuration.getInt("CONFIG_SERVER_PORT");
            if (0 >= configServerPort || 65535 < configServerPort) {
                switch (configTransportProtocol) 
                {
                    case "TCP-RFC5424":
                        configServerPort = 6514;
                        break;
                    case "TCPTLS-RFC5424":
                        configServerPort = 10514;
                        break;
                    default:
                        configServerPort = 514;
                        break;
                }
                LOG.info(String.format("CONFIG_SERVER_PORT: Port was set to '<=0'. Set to default '%d' (configTransportProtocol '%s')", configServerPort));
            }

            syslogSeveritySource = configuration.getString("CONFIG_LOGLEVEL_SEVERITY");
            if (null == syslogSeveritySource || syslogSeveritySource.isEmpty()) 
            {
                throw new MessageOutputConfigurationException(String.format("CONFIG_LOGLEVEL_SEVERITY: Parameter validation FAILED. Value cannot be null or empty."));
            }
            try 
            {
                syslogSeverityNumber = Integer.parseInt(syslogSeveritySource, 10);
                if (0 > syslogSeverityNumber || 7 < syslogSeverityNumber) 
                {
                    throw new MessageOutputConfigurationException(String.format("CONFIG_LOGLEVEL_SEVERITY: Parameter validation FAILED. Severity level must be between (inlcluding) 0 and 7."));
                }
                isSyslogSeverityNumber = true;
            } 
            catch (NumberFormatException ex) 
            {
                if ("<stream>".equalsIgnoreCase(syslogSeveritySource)) 
                {
                    syslogSeveritySource = streamTitle;
                }
            }

            syslogFacilitySource = configuration.getString("CONFIG_LOGLEVEL_FACILITY");
            if (null == syslogFacilitySource || syslogFacilitySource.isEmpty())
            {
                throw new MessageOutputConfigurationException(String.format("CONFIG_LOGLEVEL_FACILITY: Parameter validation FAILED. Value cannot be null or empty."));
            }
            try
            {
                syslogFacilityNumber = Integer.parseInt(syslogFacilitySource, 10);
                if (0 > syslogFacilityNumber || 23 < syslogFacilityNumber)
                {
                    throw new MessageOutputConfigurationException(String.format("CONFIG_LOGLEVEL_FACILITY: Parameter validation FAILED. Severity level must be between (inlcluding) 0 and 23."));
                }
                isSyslogFacilityNumber = true;
            }
            catch (NumberFormatException ex)
            {
                if ("<stream>".equalsIgnoreCase(syslogFacilitySource))
                {
                    syslogFacilitySource = streamTitle;
                }
            }

            syslogStructuredDataTag = configuration.getString("CONFIG_STRUCTURED_DATA_TAG");
            if (null == syslogStructuredDataTag || syslogStructuredDataTag.isEmpty())
            {
                syslogStructuredDataTag = "SDATA";
            }
            
            syslogMessageId = configuration.getString("CONFIG_MESSAGE_ID");
            if (null == syslogMessageId || syslogMessageId.isEmpty())
            {
                syslogMessageId = streamTitle;
            }

            fields = Arrays.asList(configuration.getString(CONFIG_FIELDS).split("\\s*,\\s*"));
            if(configuration.getString(CONFIG_FIELDS).isEmpty() || 0 >= fields.size() || fields.isEmpty())
            {
                LOG.info(String.format("No fields were specified. Using all fields."));
                fields = new ArrayList<String>();
//                fields.add("<timestamp>");
//                fields.add("<stream>");
//                fields.add("<source>");
//                fields.add("<message>");
                fields.add("*");
            }
            LOG.info("Verifying configuration SUCCEEDED.");

            
            LOG.debug(String.format("Connecting to Syslog server '%s://%s:%d' ...", configTransportProtocol, configServerName, configServerPort));

            switch (configTransportProtocol)
            {
                case "TCP-RFC5424":
                    syslogClient = new SyslogClient("TCP", configServerName, configServerPort);
                    break;
                case "TCPTLS-RFC5424":
                    syslogClient = new SyslogClient("TCPTLS", configServerName, configServerPort);
                    break;
                default:
                    syslogClient = new SyslogClient("UDP", configServerName, configServerPort);
                    break;
            }
            if(isSyslogFacilityNumber)
            {
                syslogClient.setFacility(syslogFacilityNumber);
            }

            LOG.info(String.format("Connecting to Syslog server '%s://%s:%d' SUCCEEDED.", configTransportProtocol, configServerName, configServerPort));

            isRunning.set(true);
        } 
        catch (MessageOutputConfigurationException ex) 
        {
            LOG.error("Connecting to Syslog server FAILED.", ex);
            throw ex;
        } catch (Exception ex) 
        {
            LOG.error("Connecting to Syslog server FAILED.", ex);
            throw new MessageOutputConfigurationException(ex.getMessage());
        }
    }

    @Override
    public boolean isRunning() {
        return isRunning.get();
    }

    @Override
    public void stop() {
        try {
            LOG.debug("Stopping ...");
            isRunning.set(false);
            LOG.info("Stopping SUCCEEDED.");
        } catch (Throwable ex) {
            LOG.error("Stopping FAILED.");
        }
    }

    @Override
    public void write(Message message) throws Exception {
        if (!isRunning.get()) 
        {
            return;
        }
        try 
        {
            if(!isSyslogFacilityNumber)
            {
                int syslogFacility = getSyslogFacilityFromMessage(message);
                syslogClient.setFacility(syslogFacility);
            }
            int syslogSeverity = syslogSeverityNumber;
            if(!isSyslogSeverityNumber)
            {
                syslogSeverity = getSyslogSeverityFromMessage(message);
            }

            if(configuration.getBoolean("CONFIG_USE_MESSAGE_SOURCE"))
            {
                syslogClient.setLocalName(message.getSource());
            }

            Map<String, Object> messageFields = message.getFields();
            boolean fIncludeFieldNames = configuration.getBoolean("CONFIG_INCLUDE_FIELD_NAMES");
            StringBuilder sb = new StringBuilder();
            Map<String, String> structuredData = new HashMap<>();
            for (String fieldName : fields) {
                switch (fieldName) {
                    case "<id>":
                        if (fIncludeFieldNames) {
                            sb.append("[id] ");
                        }
                        structuredData.put("id", message.getId());
                        sb.append(message.getId());
                        sb.append("|");
                        break;
                    case "<message>":
                        if (fIncludeFieldNames) {
                            sb.append("[message] ");
                        }
                        structuredData.put("message", message.getMessage());
                        sb.append(message.getMessage());
                        sb.append("|");
                        break;
                    case "<source>":
                        if (fIncludeFieldNames) {
                            sb.append("[source] ");
                        }
                        structuredData.put("source", message.getSource());
                        sb.append(message.getSource());
                        sb.append("|");
                        break;
                    case "<timestamp>":
                        if (fIncludeFieldNames) {
                            sb.append("[timestamp] ");
                        }
                        structuredData.put("timestamp", message.getTimestamp().toDateTimeISO().toString());
                        sb.append(message.getTimestamp());
                        sb.append("|");
                        break;
                    case "<stream>":
                        if (fIncludeFieldNames) {
                            sb.append("[stream] ");
                        }
                        structuredData.put("stream", streamTitle);
                        sb.append(streamTitle);
                        sb.append("|");
                        break;
                    case "*":
                        for(Map.Entry<String, Object> entry : messageFields.entrySet())
                        {
                            if (fIncludeFieldNames) {
                                sb.append("[");
                                sb.append(entry.getKey());
                                sb.append("] ");
                            }
                            String fieldValue = entry.getValue().toString();
                            structuredData.put(fieldName, fieldValue);
                            sb.append(fieldValue);
                            sb.append("|");
                        }
                        break;
                    default:
                        if (!messageFields.containsKey(fieldName)) {
                            LOG.warn(String.format("%s: field name does not exist. Skipping ...", fieldName));
                            continue;
                        }
                        if (fIncludeFieldNames) {
                            sb.append("[");
                            sb.append(fieldName);
                            sb.append("] ");
                        }
                        String fieldValue = messageFields.get(fieldName).toString();
                        structuredData.put(fieldName, fieldValue);
                        sb.append(fieldValue);
                        sb.append("|");
                        break;
                }
            }

            LOG.info(String.format("%s: [sev %d] [fac %d] [%b] %s", configTransportProtocol, syslogSeverity, syslogFacilityNumber, configuration.getBoolean("CONFIG_USE_STRUCTURED_DATA"), sb.toString()));
            if(configTransportProtocol.endsWith("RFC3164"))
            {
                switch(syslogSeverity)
                {
                    case 7:
                        syslogClient.logDebug(sb.toString());
                        break;
                    default:
                    case 6:
                        syslogClient.logInfo(sb.toString());
                        break;
                    case 5:
                        syslogClient.logNotice(sb.toString());
                        break;
                    case 4:
                        syslogClient.logWarn(sb.toString());
                        break;
                    case 3:
                        syslogClient.logError(sb.toString());
                        break;
                    case 2:
                        syslogClient.logCritical(sb.toString());
                        break;
                    case 1:
                        syslogClient.logAlert(sb.toString());
                        break;
                    case 0:
                        syslogClient.logEmergency(sb.toString());
                        break;
                }
            }
            else
            {
                if(syslogMessageId.startsWith("<") && syslogMessageId.endsWith(">"))
                {
                    if(message.hasField(syslogMessageId))
                    {
                        syslogMessageId = message.getField(syslogMessageId).toString();
                    }
                }
                if(!configuration.getBoolean("CONFIG_USE_STRUCTURED_DATA"))
                {
                    syslogClient.log(syslogSeverity, syslogMessageId, null, sb.toString());
                }
                else
                {
                    syslogClient.log(syslogSeverity, syslogMessageId, structuredData, sb.toString());
                }
            }
        } 
        catch (Exception ex) 
        {
            LOG.error("Exception occurred.", ex);
            ex.printStackTrace();
            throw ex;
        }
    }

    private int getSyslogSeverityFromMessage(@NotNullable Message message)
    {
        // default syslog severity level
        int syslogSeverity = syslogSeverityNumber;
        // try to get severity from field (can be string or number)
        String syslogSeverityName = syslogSeveritySource;
        if(message.hasField(syslogSeveritySource))
        {
            syslogSeverityName = message.getField(syslogSeveritySource).toString();
        }
        try
        {
            // we presume the contents is a number
            syslogSeverity = Integer.parseInt(syslogSeverityName);
        }
        catch(NumberFormatException ex)
        {
            // treat contents as string if number conversion failed
            switch(syslogSeverityName.toUpperCase())
            {
                case "EMERGENCY":
                    syslogSeverity = 0;
                    break;
                case "ALERT":
                case "ALRT":
                    syslogSeverity = 1;
                    break;
                case "CRITICAL":
                    syslogSeverity = 2;
                    break;
                case "ERROR":
                case "ERR":
                    syslogSeverity = 3;
                    break;
                case "WARNING":
                case "WARN":
                    syslogSeverity = 4;
                    break;
                case "NOTICE":
                    syslogSeverity = 5;
                    break;
                case "INFORMATIONAL":
                case "INFORMATION":
                case "INFO":
                    syslogSeverity = 6;
                    break;
                case "DBG":
                case "DEBUG":
                case "TRACE":
                    syslogSeverity = 7;
                    break;
                default:
                    syslogSeverity = syslogSeverityNumber;
                    LOG.warn(String.format("%s: Parameter validation FAILED. Parameter is not a valid syslog severity. Assigned default severity '%d'.", syslogSeverityName, syslogSeverity));
                    break;
            }
        }
        return syslogSeverity;
    }

    private int getSyslogFacilityFromMessage(@NotNullable Message message)
    {
        // default syslog facility level
        int syslogFacility = syslogFacilityNumber;
        // try to get severity from field (can be string or number)
        String syslogFacilityName = syslogFacilitySource;
        if(message.hasField(syslogFacilitySource))
        {
            syslogFacilityName = message.getField(syslogFacilitySource).toString();
        }
        try
        {
            // we presume the contents is a number
            syslogFacility = Integer.parseInt(syslogFacilityName, 10);
        }
        catch(NumberFormatException ex)
        {
            syslogFacility = syslogFacilityNumber;
            LOG.warn(String.format("%s: Parameter validation FAILED. Parameter is not a valid syslog facility. Assigned default facility '%d'.", syslogFacilityName, syslogFacility));
        }
        return syslogFacility;
    }

    @Override
    public void write(List<Message> messages) throws Exception
    {
        if (!isRunning.get())
        {
            return;
        }
        for (Message message : messages)
        {
            write(message);
        }
    }

    public static class Config extends MessageOutput.Config
    {
        @Override
        public ConfigurationRequest getRequestedConfiguration()
        {
            final ConfigurationRequest configurationRequest = new ConfigurationRequest();

            configurationRequest.addField(new TextField(
                            CONFIG_SERVER_NAME, "Name or IP address of Syslog server", "localhost",
                            "Specifies the name, FQDN or IP address of the Syslog server to send messages to.",
                            ConfigurationField.Optional.NOT_OPTIONAL)
            );

            configurationRequest.addField(new NumberField(
                            CONFIG_SERVER_PORT, "Port of Syslog server", 514,
                            "Specifies the TCP or UDP port of the Syslog server to send messages to. Common defaults are UDP:514, TCP:10514, TCP/TLS 6514",
                            ConfigurationField.Optional.NOT_OPTIONAL)
            );

            configurationRequest.addField(new DropdownField(
                            CONFIG_TRANSPORT_PROTOCOL, "Syslog Transport Protocol", CONFIG_TRANSPORT_PROTOCOL_OPTIONS.get("UDP-RFC3164"), CONFIG_TRANSPORT_PROTOCOL_OPTIONS,
                            "Specifies the transport protocol to use",
                            ConfigurationField.Optional.NOT_OPTIONAL)
            );

            configurationRequest.addField(new TextField(
                            CONFIG_LOGLEVEL_SEVERITY, "Syslog severity log level", "<stream>",
                            "Specifies which severity log level to be used for messages. Log level can either be derived from a stream name ('<stream>'), a field names (use '<>' for built-in fields and plain field name for user-defined fields), or a fixed severity log level (0..7)",
                            ConfigurationField.Optional.NOT_OPTIONAL)
            );

            configurationRequest.addField(new TextField(
                            CONFIG_LOGLEVEL_FACILITY, "Syslog facility log level", "16",
                            "Specifies which facility log level to be used for messages. Log level can either be derived from a stream name ('<stream>'), a field names (use '<>' for built-in fields and plain field name for user-defined fields) that resolves to a valid facility number, or a fixed facility log level (0..23)",
                            ConfigurationField.Optional.NOT_OPTIONAL)
            );

            configurationRequest.addField(new TextField(
                            CONFIG_FIELDS, "Message fields to send", "<stream>",
                            "Specifies which fields to inlcude in message. This can be either field names (use '<>' for built-in fields and plain field name for user-defined fields) or empty to include all fields.",
                            ConfigurationField.Optional.OPTIONAL)
            );

            configurationRequest.addField(new BooleanField(
                            CONFIG_INCLUDE_FIELD_NAMES, "Include field names in message", true,
                            "Set to true to include field names in message, or set to false to omit field names and only send field contents.")
            );

//            configurationRequest.addField(new BooleanField(
//                            CONFIG_USE_STRUCTURED_DATA, "Send message properties as structured message parameters", true,
//                            "[RFC5424] Set to true to send message properties as structured message properties.")
//            );

            configurationRequest.addField(new BooleanField(
                            CONFIG_USE_STRUCTURED_DATA, "Send message properties as structured message parameters", true,
                            "[RFC5424] Set to true to send message properties as structured message properties.")
            );

            configurationRequest.addField(new TextField(
                            CONFIG_STRUCTURED_DATA_TAG, "Syslog structured data tag", "SDATA",
                            "[RFC5424] Only used if CONFIG_USE_STRUCTURED_DATA is specified. This field is used for naming the structured data tag in a syslog message.",
                            ConfigurationField.Optional.OPTIONAL)
            );

            configurationRequest.addField(new TextField(
                            CONFIG_MESSAGE_ID, "Syslog message id", "<id>",
                            "[RFC5424] Specifies the MSGID field in a syslog message. Message id can either be derived from a stream name ('<stream>'), a field names (use '<>' for built-in fields and plain field name for user-defined fields), or a fixed string",
                            ConfigurationField.Optional.OPTIONAL)
            );

            configurationRequest.addField(new BooleanField(
                            CONFIG_USE_MESSAGE_SOURCE, "Use the source field from the message a Syslog localname", true,
                            "Set to true to use the source from the message field instead of the graylog node name.")
            );

            return configurationRequest;
        }
    }

    public interface Factory extends MessageOutput.Factory<SyslogOutput>
    {
        @Override
        SyslogOutput create(Stream stream, Configuration configuration);

        @Override
        Config getConfig();

        @Override
        Descriptor getDescriptor();
    }
    public static class Descriptor extends MessageOutput.Descriptor
    {
        public Descriptor()
        {
            super((new SyslogOutputMetaData()).getName(), false, "", (new SyslogOutputMetaData()).getDescription());
        }
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
