biz.dfch.j.graylog.plugin.output.SyslogOutput
=============================================

Plugin: biz.dfch.j.graylog.plugin.output.SyslogOutput

d-fens GmbH, General-Guisan-Strasse 6, CH-6300 Zug, Switzerland

This Graylog Output Plugin lets you send messages to an upstream Syslog server (RFC3164 or RFC5424).

See [Syslog Output Plugin for Graylog v1.0.0](http://d-fens.ch/) and [Creating a Graylog Output Plugin](http://d-fens.ch/2015/01/07/howto-creating-a-graylog-output-plugin/) (v0.92.x) for further description on how to create plugins.

You can [download the binary](https://drone.io/github.com/dfch/biz.dfch.j.graylog.plugin.output.SyslogOutput/files) [![Build Status](https://drone.io/github.com/dfch/biz.dfch.j.graylog.plugin.output.SyslogOutput/status.png)](https://drone.io/github.com/dfch/biz.dfch.j.graylog.plugin.output.SyslogOutput/latest) at our [drone.io](https://drone.io/github.com/dfch) account.

Usage
-----

Here is a quick overview of the options you can use to configure the plugin:

* CONFIG_SERVER_NAME
Name or IP address of Syslog server

* CONFIG_SERVER_PORT
Port of Syslog server, specifies the TCP or UDP port of the Syslog server to send messages to. Common defaults are UDP:514, TCP:10514, TCP/TLS 6514

* CONFIG_TRANSPORT_PROTOCOL
Syslog Transport Protocol, can be either UDP-RFC3164, UDP-RFC5424 (essentially the same as with RFC3164), TCP-RFC5424 and TCPTSL-RFC5424

* CONFIG_LOGLEVEL_SEVERITY
Specifies which severity log level to be used for messages. Log level can either be derived from a stream name ('<stream>'), a field names (use '<>' for built-in fields and plain field name for user-defined fields), or a fixed severity log level (0..7).

* CONFIG_LOGLEVEL_FACILITY
Specifies which facility level to be used for messages. Facility level can either be derived from a stream name ('<stream>'), a field names (use '<>' for built-in fields and plain field name for user-defined fields) that resolves to a valid facility number, or a fixed facility level (0..23).

* CONFIG_FIELDS
Specifies which fields to inlcude in message. This can be either field names (use '<>' for built-in fields and plain field name for user-defined fields) or empty (or '*') to include all fields.

* CONFIG_INCLUDE_FIELD_NAMES
Set to true to include field names in message, or set to false to omit field names and only send field contents.

* CONFIG_USE_STRUCTURED_DATA
[RFC5424] Set to true to send message properties as structured message properties.

* CONFIG_STRUCTURED_DATA_TAG
[RFC5424] Only used if CONFIG_USE_STRUCTURED_DATA is specified. This field is used for naming the structured data tag in a syslog message.

* CONFIG_MESSAGE_ID
[RFC5424] Specifies the MSGID field in a syslog message. Message id can either be derived from a stream name ('<stream>'), a field names (use '<>' for built-in fields and plain field name for user-defined fields), or a fixed string.

* CONFIG_USE_MESSAGE_SOURCE
Set to true to use the source from the message field instead of the graylog node name.

Getting started for users
-------------------------

This project is using Maven and requires Java 7 or higher.

* Clone this repository.
* Run `mvn package` to build a JAR file.
* Optional: Run `mvn jdeb:jdeb` and `mvn rpm:rpm` to create a DEB and RPM package respectively.
* Copy generated jar file in target directory to your graylog server plugin directory.
* Restart the graylog server.
