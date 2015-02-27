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
