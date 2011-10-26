/**
 * Author: OMAROMAN
 * Date: 9/19/11
 * Time: 12:59 PM
 */

package play.modules.reverseproxy;

import net.parnassoft.playutilities.ControllerUtility;
import net.parnassoft.playutilities.UrlUtility;
import play.Logger;
import play.Play;
import play.PlayPlugin;
import play.classloading.ApplicationClasses;
import play.exceptions.ConfigurationException;
import play.modules.reverseproxy.annotations.GlobalSwitchScheme;
import play.modules.reverseproxy.annotations.SchemeType;
import play.modules.reverseproxy.annotations.SwitchScheme;
import play.mvc.Http;

import java.lang.reflect.Method;

public class ReverseProxyPlugin extends PlayPlugin {

    @Override
    public void onLoad() {
//        Logger.info("Yeeha, ReverseProxyController module loaded");

        boolean reverseProxyEnabled = ReverseProxyUtility.Config.isReverseProxyEnabled();
        String httpPort = Play.configuration.getProperty("http.port");
        String httpsPort = Play.configuration.getProperty("https.port");

        if (httpPort == null && httpsPort != null) {
            String error = "===== https.port property is configured, but http.port property is not configured. Check your application.conf =====";
            throw new ConfigurationException(error);
        } else if (!reverseProxyEnabled) {
            String error = "===== WARNING: ReverseProxy is declared, but is not enabled. =====";
            Logger.warn(error);
            if (httpsPort == null) {
                error = "===== ReverseProxy is not enabled and https.port property is not configured, therefore, only built-in sever HTTP port will be used. Check your application.conf =====";
                Logger.warn(error);
            }
        } else if (reverseProxyEnabled && httpsPort == null) {
            String error = "===== ReverseProxy is enabled, but https.port property is not configured. Check your application.conf =====";
            throw new ConfigurationException(error);
        } else if (ReverseProxyUtility.Config.getReverseProxyHttpAddress() == null) {
            String error = "===== ReverseProxy is declared and enabled, but doesn't have an address configured. Check your application.conf =====";
            Logger.fatal(error);
            throw new ConfigurationException(error);
        } else if (ReverseProxyUtility.Config.getReverseProxyHttpPort() == 0 || ReverseProxyUtility.Config.getReverseProxyHttpsPort() == 0) {
            String error = "===== ReverseProxy is declared and enabled, but doesn't have ports configured. Check your application.conf =====";
            Logger.fatal(error);
            throw new ConfigurationException(error);
        }

        String reverseProxyAddress = Play.configuration.getProperty("reverse_proxy.http.address");
        Play.configuration.setProperty("XForwardedSupport", reverseProxyAddress);
    }

    @Override
    public void enhance(ApplicationClasses.ApplicationClass appClass) throws Exception {
//        new ReverseProxyEnhancer().enhanceThisClass(appClass);
    }

    @Override
    public void beforeActionInvocation(Method actionMethod) {
        ReverseProxy.initSwitchScheme();
    }


}
