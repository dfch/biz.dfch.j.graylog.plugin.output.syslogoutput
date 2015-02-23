package biz.dfch.j.graylog.plugin.output;

import org.graylog2.plugin.PluginMetaData;
import org.graylog2.plugin.ServerStatus;
import org.graylog2.plugin.Version;

import java.net.URI;
import java.util.Collections;
import java.util.Set;

/**
 * Implement the PluginMetaData interface here.
 */
public class SyslogOutputMetaData implements PluginMetaData 
{
    @Override
    public String getUniqueId() 
    {
        return "biz.dfch.j.graylog.plugin.output.SyslogOutputPlugin";
    }

    @Override
    public String getName() 
    {
        return "d-fens Syslog Output";
    }

    @Override
    public String getAuthor() 
    {
        return "Ronald Rink, d-fens GmbH";
    }

    @Override
    public URI getURL() 
    {
        return URI.create("http://d-fens.ch/");
    }

    @Override
    public Version getVersion() 
    {
        return new Version(1, 0, 0);
    }

    @Override
    public String getDescription() 
    {
        return "d-fens plugin for sending Graylog messages to an upstream Syslog RFC3164 or RFC5424 server.";
    }

    @Override
    public Version getRequiredVersion() 
    {
        return new Version(1, 0, 0);
    }

    @Override
    public Set<ServerStatus.Capability> getRequiredCapabilities() 
    {
        return Collections.emptySet();
    }
}
