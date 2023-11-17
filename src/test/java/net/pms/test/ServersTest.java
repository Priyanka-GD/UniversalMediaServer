package net.pms.test;

import com.google.gson.JsonArray;
import net.pms.PMS;
import net.pms.Servers;
import net.pms.configuration.UmsConfiguration;
import net.pms.dlna.DLNAResource;
import net.pms.network.webguiserver.servlets.PlayerApiServlet;
import net.pms.renderers.Renderer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.junit.jupiter.api.Assertions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

public class ServersTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(PMS.class.getName());

    @Mock
    UmsConfiguration umsConfiguration;

    @BeforeEach
    public void SetUp() {
        // Any setup done here
        umsConfiguration = Mockito.mock(UmsConfiguration.class);
    }
    @Test
    public void testServerName(){
        Servers servers = new Servers();
        servers.getServerName("test");
        try {
            servers.getServerName("test");
        } catch (Exception ex) {
            LOGGER.error("Exception occured : " + ex);
        }
    }

    @Test
    public void testMediaReset(){
        Servers servers = new Servers();
        servers.getServerName("test");
        try {
            servers.resetMediaServer(LOGGER);
        } catch (Exception ex) {
            LOGGER.error("Exception occured : " + ex);
        }
    }

    @Test
    public void testGuiReset(){
        Servers servers = new Servers();
        servers.getServerName("test");
        try {
            servers.resetWebGuiServer(LOGGER, umsConfiguration);
        } catch (Exception ex) {
            LOGGER.error("Exception occured : " + ex);
        }
    }

    @Test
    public void testWebplayerReset(){
        Servers servers = new Servers();
        servers.getServerName("test");
        try {
            servers.resetWebPlayerServer(LOGGER, umsConfiguration);
        } catch (Exception ex) {
            LOGGER.error("Exception occured : " + ex);
        }
    }
}
