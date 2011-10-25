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
        Logger.info("Yeeha, ReverseProxyController module loaded");

        if (!ReverseProxyUtility.Config.isReverseProxyEnabled()) {
            String error = "===== WARNING: ReverseProxy is declared, but isn't enable. =====";
            Logger.warn(error);
            if (Play.configuration.getProperty("https.port") == null) {
                error = "===== ReverseProxy is not enable and built-in https port is not configured. Check your application.conf =====";
                Logger.fatal(error);
                throw new ConfigurationException(error);
            }
        } else if (ReverseProxyUtility.Config.getReverseProxyHttpAddress() == null) {
            String error = "===== ReverseProxy is declared and enable, but don't have a address specified. Check your application.conf =====";
            Logger.fatal(error);
            throw new ConfigurationException(error);
        } else if (ReverseProxyUtility.Config.getReverseProxyHttpPort() == 0 || ReverseProxyUtility.Config.getReverseProxyHttpsPort() == 0) {
            String error = "===== ReverseProxy is declared and enable, but don't have ports specified. Check your application.conf =====";
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

    public void onInvocationSuccess() {
        // TODO: Delete cookie
    }

    @Override
    public void beforeActionInvocation(Method actionMethod) {
        ReverseProxy.initSwitchScheme();
    }


}
