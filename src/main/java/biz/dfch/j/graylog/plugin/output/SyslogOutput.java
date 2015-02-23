package biz.dfch.j.graylog.plugin.output;

import biz.dfch.j.syslog4j.SyslogClient;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This is the plugin. Your class should implement one of the existing plugin
 * interfaces. (i.e. AlarmCallback, MessageInput, MessageOutput)
 */
public class SyslogOutput implements MessageOutput {
    private static final String CONFIG_SERVER_NAME = "CONFIG_SERVER_NAME";
    private static final String CONFIG_SERVER_PORT = "CONFIG_SERVER_PORT";
    private static final String CONFIG_USE_STRUCTURED_DATA = "CONFIG_USE_STRUCTURED_DATA";
    private static final String CONFIG_TRANSPORT_PROTOCOL = "CONFIG_TRANSPORT_PROTOCOL";
    private static final Map<String, String> CONFIG_TRANSPORT_PROTOCOL_OPTIONS = ImmutableMap.of(
            "UDP", "UDP"
            ,
            "TCP", "TCP"
            ,
            "TCPTLS", "TCPTLS"
    );
    private static final String CONFIG_LOGLEVEL_SEVERITY = "CONFIG_LOGLEVEL_SEVERITY";
    private static final String CONFIG_LOGLEVEL_FACILITY = "CONFIG_LOGLEVEL_FACILITY";

    private static final Logger LOG = LoggerFactory.getLogger(SyslogOutput.class);
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private Configuration configuration;
    private String streamTitle;

    private String syslogSeveritySource = null;
    private boolean isSyslogSeverityNumber = false;
    private int syslogSeverityNumber = 6;
    private String syslogFacilitySource = null;
    private boolean isSyslogFacilityNumber = false;
    private int syslogFacilityNumber = 16;

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

            String configTransportProtocol = configuration.getString("CONFIG_TRANSPORT_PROTOCOL");
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
            if (0 >= configServerPort) {
                switch (configTransportProtocol.toUpperCase()) 
                {
                    case "TCP":
                        configServerPort = 6514;
                        break;
                    case "TCPTLS":
                        configServerPort = 10514;
                        break;
                    case "UDP":
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

            LOG.info("Verifying configuration SUCCEEDED.");

            
            LOG.debug("Connecting to Syslog server ...");

            syslogClient = new SyslogClient(configTransportProtocol, configServerName, configServerPort);
            if(isSyslogFacilityNumber)
            {
                syslogClient.setFacility(syslogFacilityNumber);
            }

            LOG.info("Connecting to Syslog server SUCCEEDED.");

            isRunning.set(true);
        } catch (MessageOutputConfigurationException ex) {
            LOG.error("Connecting to Syslog server FAILED.", ex);
            throw ex;
        } catch (Exception ex) {
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
        if (!isRunning.get()) {
            return;
        }
        try {
            int syslogSeverity = getSyslogSeverityFromMessage(message);
            int syslogFacility = getSyslogFacilityFromMessage(message);
            if(!isSyslogFacilityNumber)
            {
                syslogClient.setFacility(syslogFacility);
            }
            switch (syslogSeverity) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                    syslogClient.log(syslogSeverity, message.getMessage());
                    break;
                default:
                    throw new Exception(String.format("syslogSeverityNumber: Parameter validation FAILED. Parameter contains invalid number ('%d⁾).", syslogSeverity));
            }
            //syslogClient.
        } catch (Exception ex) {
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
        if(!isSyslogSeverityNumber)
        {
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
        }
        return syslogSeverity;
    }

    private int getSyslogFacilityFromMessage(@NotNullable Message message)
    {
        // default syslog facility level
        int syslogFacility = syslogFacilityNumber;
        // try to get severity from field (can be string or number)
        if(!isSyslogFacilityNumber)
        {
            String syslogFacilityName = syslogFacilitySource;
            if(message.hasField(syslogFacilitySource))
            {
                syslogFacilityName = message.getField(syslogFacilitySource).toString();
            }
            try
            {
                // we presume the contents is a number
                syslogFacility = Integer.parseInt(syslogFacilityName);
            }
            catch(NumberFormatException ex)
            {
                syslogFacility = syslogFacilityNumber;
                LOG.warn(String.format("%s: Parameter validation FAILED. Parameter is not a valid syslog facility. Assigned default facility '%d'.", syslogFacilityName, syslogFacility));
            }
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
                            CONFIG_TRANSPORT_PROTOCOL, "Syslog Transport Protocol", CONFIG_TRANSPORT_PROTOCOL_OPTIONS.get("UDP").toString(), CONFIG_TRANSPORT_PROTOCOL_OPTIONS,
                            "Specifies the transport protocol to use",
                            ConfigurationField.Optional.NOT_OPTIONAL)
            );

            configurationRequest.addField(new TextField(
                            CONFIG_LOGLEVEL_SEVERITY, "Syslog severity log level", "<stream>",
                            "Specifies which severity log level to be used for messages. Log level can either be derived from a stream name ('<stream>'), a field names (use '<>' for built-in fields and plain field name for user-defined fields), or a fixed severity log level (0..7)",
                            ConfigurationField.Optional.NOT_OPTIONAL)
            );

            configurationRequest.addField(new TextField(
                            CONFIG_LOGLEVEL_FACILITY, "Syslog facility log level", "local0",
                            "Specifies which facility log level to be used for messages. Log level can either be derived from a stream name ('<stream>'), a field names (use '<>' for built-in fields and plain field name for user-defined fields) that resolves to a valid facility number, or a fixed facility log level (0..23)",
                            ConfigurationField.Optional.NOT_OPTIONAL)
            );

            configurationRequest.addField(new BooleanField(
                            CONFIG_USE_STRUCTURED_DATA, "Send message properties as structured message parameters", true,
                            "Set to true to send message properties as structured message properties.")
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
