package net.pms;

import java.io.*;
import java.net.BindException;
import net.pms.configuration.UmsConfiguration;
import net.pms.configuration.RendererConfigurations;
import net.pms.gui.GuiManager;
import net.pms.media.MediaInfo;
import net.pms.network.mediaserver.MediaServer;
import net.pms.network.webguiserver.WebGuiServer;
import net.pms.network.webguiserver.servlets.SseApiServlet;
import net.pms.network.webplayerserver.WebPlayerServer;
import net.pms.renderers.ConnectedRenderers;
import net.pms.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Servers {
    //private static final Logger LOGGER = LoggerFactory.getLogger(MediaInfo.class);
    /**
     * UPnP mediaServer that serves the XML files, media files and broadcast messages needed by UPnP Service.
     */
    private MediaServer mediaServer;

    /**
     * HTTP server that serves a gui.
     */
    private WebGuiServer webGuiServer;

    /**
     * HTTP server that serves a browser/player of media files.
     * Should replace the WebInterfaceServer at end.
     */
    private WebPlayerServer webPlayerServer;

    /**
     * User friendly name for the server.
     */
    private String serverName;

    public MediaServer getMediaServer() {
        return mediaServer;
    }

    public WebGuiServer getGuiServer() {
        return webGuiServer;
    }

    public WebPlayerServer getWebPlayerServer() {
        return webPlayerServer;
    }

    /**
     * Restarts the server. The trigger is either a button on the main UMS
     * window or via an action item.
     */
    // XXX: don't try to optimize this by reusing the same HttpMediaServer instance.
    // see the comment above HttpMediaServer.stop()
    public void resetMediaServer(Logger LOGGER) {
        TaskRunner.getInstance().submitNamed("restart", true, () -> {
            SseApiServlet.notify("server-restart", "Server is restarting", "Server status", "red", true);
            MediaServer.stop();
            resetRenderers(true);

            LOGGER.trace("Waiting 1 second...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                LOGGER.trace("Caught exception", e);
            }

            // re-create the server because may happened the
            // change of the used interface
            MediaServer.start();
            GuiManager.setReloadable(false);
        });
    }

    /**
     * Reset renderers.
     * The trigger is configuration change.
     * @param delete True if removal of known renderers is needed
     */
    public void resetRenderers(boolean delete) {
        RendererConfigurations.loadRendererConfigurations();
        if (delete) {
            ConnectedRenderers.deleteAllConnectedRenderers();
        }
    }

    /**
     * Reset the web graphical user interface server.
     * The trigger is init.
     */
    public void resetWebGuiServer(Logger LOGGER, UmsConfiguration umsConfiguration) {
        if (webGuiServer != null) {
            GuiManager.removeGui(webGuiServer);
            webGuiServer.stop();
        }
        try {
            webGuiServer = WebGuiServer.createServer(umsConfiguration.getWebGuiServerPort());
        } catch (BindException b) {
            try {
                LOGGER.info("Unable to bind web interface on port: " + umsConfiguration.getWebGuiServerPort() + ", because: " + b.getMessage());
                LOGGER.info("Falling back to random port.");
                webGuiServer = WebGuiServer.createServer(0);
            } catch (IOException ex) {
                LOGGER.error("FATAL ERROR: Unable to set the gui server, because: " + ex.getMessage());
                LOGGER.info("Maybe another process is running or the hostname is wrong.");
            }
        } catch (IOException ex) {
            LOGGER.error("FATAL ERROR: Unable to set the gui server, because: " + ex.getMessage());
        }
        if (webGuiServer != null && webGuiServer.getServer() != null) {
            GuiManager.addGui(webGuiServer);
            LOGGER.info("GUI is available at: " + webGuiServer.getUrl());
        }
    }

    /**
     * Reset the web player server.
     * The trigger is init and configuration change.
     */
    public void resetWebPlayerServer(Logger LOGGER, UmsConfiguration umsConfiguration) {
        if (webPlayerServer != null) {
            webPlayerServer.stop();
        }
        if (umsConfiguration.useWebPlayerServer()) {
            try {
                webPlayerServer = WebPlayerServer.createServer(umsConfiguration.getWebPlayerServerPort());
                GuiManager.updateServerStatus();
            } catch (BindException b) {
                LOGGER.error("FATAL ERROR: Unable to bind web player on port: " + umsConfiguration.getWebPlayerServerPort() + ", because: " + b.getMessage());
                LOGGER.info("Maybe another process is running or the hostname is wrong.");
            } catch (IOException ex) {
                LOGGER.error("FATAL ERROR: Unable to read server port value from configuration");
            }
        }
    }

    /**
     * Returns the user friendly name of the UMS server.
     * @return {@link String} with the user friendly name.
     */
    public String getServerName(String version) {
        if (serverName == null) {
            StringBuilder sb = new StringBuilder();
            sb.append(System.getProperty("os.name").replace(" ", "_"));
            sb.append('-');
            sb.append(System.getProperty("os.arch").replace(" ", "_"));
            sb.append('-');
            sb.append(System.getProperty("os.version").replace(" ", "_"));
            sb.append(", UPnP/1.0 DLNADOC/1.50, UMS/").append(version);
            serverName = sb.toString();
        }

        return serverName;
    }

}
