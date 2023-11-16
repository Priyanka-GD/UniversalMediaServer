/*
 * This file is part of Universal Media Server, based on PS3 Media Server.
 *
 * This program is free software; you can redistribute it and/or
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; version 2 of the License only.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package net.pms.network.webguiserver.servlets;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.pms.PMS;
import net.pms.iam.Account;
import net.pms.iam.AuthService;
import net.pms.iam.Permissions;
import net.pms.network.webguiserver.GuiHttpServlet;
import net.pms.network.webguiserver.WebGuiServletHelper;
import net.pms.platform.PlatformUtils;
import net.pms.util.PropertiesUtil;
import net.pms.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.SystemInfo;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;

@WebServlet(name = "AboutApiServlet", urlPatterns = {"/v1/api/about"}, displayName = "About Api Servlet")
public class AboutApiServlet extends GuiHttpServlet {
	private static final Logger LOGGER = LoggerFactory.getLogger(AboutApiServlet.class);
    private static final String GIT_COMMIT_ID = "git.commit.id";
    private static final String COMMIT_URL_TEMPLATE = "https://github.com/UniversalMediaServer/UniversalMediaServer/tree/";
    private static final String JSON_CONTENT_TYPE = "application/json";
    private static final int SUCCESS_RESPONSE_CODE = 200;


	private static SystemInfo systemInfo;
	private static HardwareAbstractionLayer hardware;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String path = req.getPathInfo();
            if ("/".equals(path)) {
                JsonObject jsonResponse = createAboutJsonResponse(req);
                WebGuiServletHelper.respond(req, resp, jsonResponse.toString(), SUCCESS_RESPONSE_CODE, JSON_CONTENT_TYPE);
            } else {
                LOGGER.trace("AboutApiServlet request not available: {}", path);
                WebGuiServletHelper.respondNotFound(req, resp);
            }
        } catch (RuntimeException e) {
            LOGGER.error("RuntimeException in AboutApiServlet: {}", e.getMessage());
            WebGuiServletHelper.respondInternalServerError(req, resp);
        }
    }

    private JsonObject createAboutJsonResponse(HttpServletRequest req) {
        JsonObject jsonResponse = new JsonObject();
        addBasicProjectInfo(jsonResponse);
        addProjectLinks(jsonResponse);
        addUserSpecificInfo(req, jsonResponse);
        return jsonResponse;
    }

    private void addBasicProjectInfo(JsonObject jsonResponse) {
        jsonResponse.addProperty("app", PropertiesUtil.getProjectProperties().get("project.name"));
        jsonResponse.addProperty("version", PMS.getVersion());
        addCommitInfo(jsonResponse);
        jsonResponse.addProperty("website", "https://www.universalmediaserver.com");
        jsonResponse.addProperty("licence", "GNU General Public License version 2");
        jsonResponse.addProperty("licenceUrl", "https://www.gnu.org/licenses/old-licenses/gpl-2.0.txt");
    }

    private void addCommitInfo(JsonObject jsonResponse) {
        String commitId = PropertiesUtil.getProjectProperties().get(GIT_COMMIT_ID);
        jsonResponse.addProperty("commit", commitId.substring(0, 9) + " (" + PropertiesUtil.getProjectProperties().get("git.commit.time") + ")");
        jsonResponse.addProperty("commitUrl", COMMIT_URL_TEMPLATE + commitId);
    }

    private void addProjectLinks(JsonObject jsonResponse) {
        JsonArray jsonLinks = new JsonArray();
        jsonLinks.add(toJsonObject("Crowdin", PMS.CROWDIN_LINK));
        jsonLinks.add(toJsonObject("FFmpeg", "https://ffmpeg.org/"));
        jsonLinks.add(toJsonObject("MPlayer", "http://www.mplayerhq.hu"));
        jsonLinks.add(toJsonObject("MPlayer, MEncoder and InterFrame builds", "https://www.spirton.com"));
        jsonLinks.add(toJsonObject("MediaInfo", "https://mediaarea.net/en/MediaInfo"));
        jsonLinks.add(toJsonObject("AviSynth MT", "https://forum.doom9.org/showthread.php?t=148782"));
        jsonLinks.add(toJsonObject("DryIcons", "https://dryicons.com/"));
        jsonLinks.add(toJsonObject("Jordan Michael Groll's Icons", "https://www.deviantart.com/jrdng"));
        jsonLinks.add(toJsonObject("SVP", "https://www.svp-team.com/"));
        jsonLinks.add(toJsonObject("OpenSubtitles.org", "https://www.opensubtitles.org/"));
        jsonLinks.add(toJsonObject("The Movie Database (TMDB)", "https://www.themoviedb.org"));
        jsonLinks.add(toJsonObject("UMS Support", "https://support.universalmediaserver.com/"));
        jsonLinks.add(toJsonObject("UMS Forums", "https://www.universalmediaserver.com/forum/"));

        jsonResponse.add("links", jsonLinks);
    }

    private void addUserSpecificInfo(HttpServletRequest req, JsonObject jsonResponse) {
        Account account = AuthService.getAccountLoggedIn(req);
        if (account != null && account.havePermission(Permissions.SETTINGS_VIEW | Permissions.SETTINGS_MODIFY)) {
            jsonResponse.addProperty("operatingSystem", getOperatingSystem());
            jsonResponse.addProperty("systemMemorySize", getSystemMemorySize());
            jsonResponse.addProperty("jvmMemoryMax", getJavaMemoryMax());
        }
    }


	private static JsonObject toJsonObject(String key, String value) {
		JsonObject result = new JsonObject();
		result.addProperty("key", key);
		result.addProperty("value", value);
		return result;
	}

	private static void initSystemInfo() {
		if (systemInfo == null) {
			systemInfo =  new SystemInfo();
		}
	}

	private static void initHardwareInfo() {
		initSystemInfo();
		if (hardware == null) {
			hardware =  systemInfo.getHardware();
		}
	}

	private static String getOperatingSystem() {
		initSystemInfo();
		OperatingSystem os = systemInfo.getOperatingSystem();
		StringBuilder sb = new StringBuilder();
		if (os != null && StringUtils.isNotBlank(os.toString())) {
			sb.append(os.toString()).append(" ").append(os.getBitness()).append("-bit");
		} else {
			sb.append(System.getProperty("os.name")).append(" ").append(PlatformUtils.getOSBitness()).append("-bit ");
			sb.append(System.getProperty("os.version"));
		}
		return sb.toString();
	}

	private static String getSystemMemorySize() {
		initHardwareInfo();
		GlobalMemory memory = hardware.getMemory();
		if (memory != null) {
			return StringUtil.formatBytes(memory.getTotal(), true);
		}
		return "-";
	}

	private static String getJavaMemoryMax() {
		long jvmMemory = Runtime.getRuntime().maxMemory();
		if (jvmMemory == Long.MAX_VALUE) {
			return ("-");
		} else {
			return StringUtil.formatBytes(jvmMemory, true);
		}
	}
}
