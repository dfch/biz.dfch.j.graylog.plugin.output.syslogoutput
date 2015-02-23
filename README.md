biz.dfch.j.graylog.plugin.output.SyslogOutput
=============================================

Plugin: biz.dfch.j.graylog.plugin.output.SyslogOutput

d-fens GmbH, General-Guisan-Strasse 6, CH-6300 Zug, Switzerland

Work in Progress
================

This Graylog Output Plugin lets you send messages to an upstream Syslog server (RFC3164 or RFC5424).

See [Syslog Output Plugin for Graylog v1.0.0](http://d-fens.ch/) and [Creating a Graylog Output Plugin](http://d-fens.ch/2015/01/07/howto-creating-a-graylog-output-plugin/) (v0.92.x) for further description on how to create plugins.

You can [download the binary](https://drone.io/github.com/dfch/biz.dfch.j.graylog.plugin.output.SyslogOutput/files) [![Build Status](https://drone.io/github.com/dfch/biz.dfch.j.graylog.plugin.output.SyslogOutput/status.png)](https://drone.io/github.com/dfch/biz.dfch.j.graylog.plugin.output.SyslogOutput/latest) at our [drone.io](https://drone.io/github.com/dfch) account.

Getting started for users
-------------------------

This project is using Maven and requires Java 7 or higher.

* Clone this repository.
* Run `mvn package` to build a JAR file.
* Optional: Run `mvn jdeb:jdeb` and `mvn rpm:rpm` to create a DEB and RPM package respectively.
* Copy generated jar file in target directory to your graylog server plugin directory.
* Restart the graylog server.
