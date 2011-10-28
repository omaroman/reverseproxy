/**
 * Author: OMAROMAN
 * Date: 10/20/11
 * Time: 5:21 PM
 */
package play.modules.reverseproxy;

import net.parnassoft.playutilities.ControllerUtility;
import net.parnassoft.playutilities.UrlUtility;
import play.Logger;
import play.Play;
import play.modules.reverseproxy.annotations.GlobalSwitchScheme;
import play.modules.reverseproxy.annotations.SchemeType;
import play.modules.reverseproxy.annotations.SwitchScheme;
import play.mvc.Http;

public class ReverseProxy {

    private static boolean reverseProxyEnabled = ReverseProxyUtility.Config.isReverseProxyEnabled();

    /**
     * This method gets the annotation and its values of @SwitchScheme or @GlobalSwitchScheme and continues the logic flow
     */
    public static void initSwitchScheme() {
//        Logger.debug("SCOPE -> initSwitchScheme");

        // Check if action it's annotated with @SwitchScheme
        SchemeType schemeType;
        boolean keepUrl = false;

        SwitchScheme switchScheme = ControllerUtility.getActionAnnotation(SwitchScheme.class);
        if (switchScheme != null) {
            // Action it's annotated with @SwitchScheme
            schemeType = switchScheme.type();
            keepUrl = switchScheme.keepUrl();
        } else {
            // Controller it's annotated with @SwitchScheme
            GlobalSwitchScheme globalSwitchScheme = ControllerUtility.getControllerAnnotation(GlobalSwitchScheme.class);
            if (globalSwitchScheme != null) {
                schemeType = globalSwitchScheme.type();
            } else {
                // DEFAULT, NO Action annotated and NO Controller annotated
                schemeType = SchemeType.HTTP;
            }
        }

//        Logger.debug("SchemeType:::%s, KeepUrl:::%b", schemeType, keepUrl);
        switchOver(schemeType, keepUrl);
    }

    /**
     * Check how will be used the params sent from @SwitchScheme and @DynamicSwitchScheme
     *
     * @param schemeType - Can get HTTP, HTTPS or UNSPECIFIED types,
     * @param keepActionUrl - A boolean that allow store or don't the referred URL of Action
     */
    private static void switchOver(SchemeType schemeType, boolean keepActionUrl) {
//        Logger.debug("SCOPE -> switchOver");
        if (schemeType == SchemeType.UNSPECIFIED) {
            Logger.warn("The action have a UNSPECIFIED SchemeType, by default will pass with the last SchemeType used on the flow");
            return;
        }

        if (keepActionUrl) {
            // creates a Cookie containing the referred url of the specified action
            ReverseProxyUtility.createReferredUrlCookie();
        }

        doSwitchScheme(schemeType);
    }

    /**
     * Choose the way to follow by the info was given in the Annotation
     *
     * @param schemeType - Can be UNSPECIFIED, HTTP or HTTPS
     */
    private static void doSwitchScheme(SchemeType schemeType) {
//        Logger.debug("SCOPE -> doSwitchScheme");

        // If ReverseProxy module is NOT enabled and https.port property is not configured...
        // Stop switching process between HTTP and HTTPS
        String httpsPort = Play.configuration.getProperty("https.port");
        if (!reverseProxyEnabled && httpsPort == null) {
            Http.Request.current().secure = false;
            return;
        }

        // Continue switching process between HTTP and HTTPS
        switchScheme(schemeType);
    }

    private static void switchScheme(SchemeType schemeType) throws IllegalArgumentException {
//        Logger.debug("SCOPE -> switchScheme");
        Http.Request request = Http.Request.current();
        switch (schemeType) {
            case HTTP :
                if (request.secure) {   // Secure request AND does not require HTTPS
                    hackRequestForSwitchingInsecureScheme();
                    UrlUtility.redirectToUriPattern(request.path);
                } else {    // Unsecure request AND does NOT require HTTPS
                    // DO NOTHING, continue using http
                    hackRequestForSwitchingInsecureScheme();
                    return;
                }
            case HTTPS:
                if (!request.secure) {  // Unsecure request AND requires HTTPS
                    hackRequestForSwitchingSecureScheme();
                    UrlUtility.redirectToUriPattern(request.path);
                } else {    // Secure request AND requires HTTPS
                    hackRequestForSwitchingSecureScheme();
                    return;
                }
            default:
                break;
        }
    }

    /**
     * Make the change to Secure request Scheme (HTTPS)
     */
    private static void hackRequestForSwitchingSecureScheme() {
//        Logger.debug("SCOPE -> hackRequestForSwitchingSecureScheme");
        Http.Request.current().secure = true;
        hackRequestPort();
    }

    /**
     * Make the change to Insecure request Scheme (HTTP)
     */
    private static void hackRequestForSwitchingInsecureScheme() {
//        Logger.debug("SCOPE -> hackRequestForSwitchingInsecureScheme");
        Http.Request.current().secure = false;
        hackRequestPort();
    }

    private static void hackRequestPort() {
//        Logger.debug("SCOPE -> hackRequestPort");
        String httpPort = Play.configuration.getProperty("http.port");
        String httpsPort = Play.configuration.getProperty("https.port");
        Http.Request request = Http.Request.current();

        if (reverseProxyEnabled && request.secure) {
            request.port = ReverseProxyUtility.Config.getReverseProxyHttpsPort();
        } else if (reverseProxyEnabled && !request.secure) {
            request.port = ReverseProxyUtility.Config.getReverseProxyHttpPort();
        } else if (!reverseProxyEnabled && request.secure) {
           request.port = Integer.parseInt(httpsPort);
        } else if (!reverseProxyEnabled && !request.secure) {
           request.port = Integer.parseInt(httpPort);
        }
    }
}
