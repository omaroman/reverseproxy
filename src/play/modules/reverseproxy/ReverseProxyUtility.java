/**
 * Author: OMAROMAN
 * Date: 10/20/11
 * Time: 12:48 PM
 */

package play.modules.reverseproxy;

import net.parnassoft.playutilities.CookieUtility;
import net.parnassoft.playutilities.RequestUtility;
import net.parnassoft.playutilities.UrlUtility;
import play.Logger;
import play.Play;
import play.libs.Crypto;
import play.mvc.Http;

public class ReverseProxyUtility {

    private final static String COOKIE_NAME = "REFERRED_URL";

    public static String getBase() {
        Http.Request request = Http.Request.current();
        if (Config.isReverseProxyEnabled()) {
            int port = request.secure ? Config.getReverseProxyHttpsPort() : Config.getReverseProxyHttpPort();
            return String.format("%s%s", request.domain, (port == 80 || port == 443) ? "" : ":" + port);
        } else {
            return RequestUtility.getBase();
        }
    }

    public static void redirectToReferredUrl() throws Throwable {
        String referredUrl = readCookie();
        String fullReferredUrl = String.format("%s://%s%s", RequestUtility.getScheme(), getBase(), referredUrl);
        Logger.debug("%s: %s", COOKIE_NAME, fullReferredUrl);
        removeCookie();
        UrlUtility.redirectToUrl(fullReferredUrl);
    }

    public static void redirectToDomain() {
        String url = String.format("%s://%s", RequestUtility.getScheme(), getBase());
        UrlUtility.redirectToUrl(url);
    }

    public static void writeCookie() {
        Http.Request request = Http.Request.current();
        String url = "GET".equals(request.method) ? request.url : "/";
        String urlEncrypted = Crypto.encryptAES(url);
        CookieUtility.writeCookie(COOKIE_NAME, urlEncrypted);
    }

    public static String readCookie() {
        String urlEncrypted = CookieUtility.readCookie(COOKIE_NAME);
        if (urlEncrypted != null) {
            return Crypto.decryptAES(urlEncrypted);
        }
        return "/"; // Index
    }

    public static void removeCookie() {
        CookieUtility.removeCookie(COOKIE_NAME);
    }

    // -----
    public static class Config {

        public static String getReverseProxyHttpAddress() {
            return Play.configuration.getProperty("reverse_proxy.http.address");
        }

        public static boolean isReverseProxyEnabled() {
            return Boolean.parseBoolean(Play.configuration.getProperty("reverse_proxy.enable"));
        }

        public static int getReverseProxyHttpPort() {
            return Integer.parseInt(Play.configuration.getProperty("reverse_proxy.http.port"));
        }

        public static int getReverseProxyHttpsPort() {
            return Integer.parseInt(Play.configuration.getProperty("reverse_proxy.https.port"));
        }
    }
}
