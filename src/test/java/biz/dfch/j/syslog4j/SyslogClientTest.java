package biz.dfch.j.syslog4j;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.graylog2.syslog4j.SyslogConfigIF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by root on 2/22/15.
 */
public class SyslogClientTest 
{
    private static final Logger LOG = LoggerFactory.getLogger(SyslogClientTest.class);

    @BeforeClass
    public static void BeforeClass()
    {
        System.out.println("BeforeClass");
    }

    @Before
    public void Before()
    {
        System.out.println("Before");
    }

    @Test
    public void doLogDebugSucceeds()
    {
        System.out.println("doLogDebugSucceeds");
        
        SyslogClient client = new SyslogClient("udp", "192.168.1.111", 514);
        client.logDebug("logDebug");
        client.logInfo("logInfo");
        client.logError("logError");
        client.logEmergency("logEmergency");
    }
    @Test
    public void doLogRFC5424()
    {
        System.out.println("doLogRFC5424");

        SyslogClient client = new SyslogClient("udp", "192.168.174.1", 514);
        SyslogConfigIF config = client.getConfig();
        client.logDebug("logDebug1");
        config.setLocalName("tralala");
        client.setFacility(0);
        client.logDebug("logDebug fac0");
        client.setFacility(16);
        client.logDebug("logDebug fac16");
        client.setFacility(17);
        client.logDebug("logDebug fac17");
        client.setFacility(18);
        client.logDebug("logDebug fac18");
        client.setFacility(23);
        client.logDebug("logDebug fac23");
        client.setFacility(16);
        client.logDebug("logDebug2");
        client.log(0, "myMessage");
        client.getConfig().setFacility("LOCAL0");
        client.logDebug("logDebug3");
        client.getConfig().setFacility("LOCAL1");
        client.logDebug("logDebug3");
        client.getConfig().setFacility("LOCAL2");
        client.logDebug("logDebug3");

    }
}
