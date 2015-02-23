package biz.dfch.j.syslog4j;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
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
}
