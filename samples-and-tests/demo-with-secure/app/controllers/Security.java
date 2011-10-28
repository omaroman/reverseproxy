/**
 * Author: OMAROMAN
 * Date: 10/13/11
 * Time: 11:38 AM
 */
package controllers;

import controllers.reverseproxy.secure.SecureController;
import play.Logger;
import play.Play;
import play.modules.reverseproxy.ReverseProxyUtility;

public class Security extends SecureController.Security {

    public static boolean check(String profile) {
        return profile.equals("admin") && session.get("username").equals("admin");
    }

    public static boolean authenticate(String username, String password) {
        return Play.configuration.getProperty("application.admin").equals(username) && Play.configuration.getProperty("application.adminpwd").equals(password);
    }

    public static void onAuthenticated() {
        try {
            Logger.debug("AUTHENTICATED");
            ReverseProxyUtility.redirectToReferredUrl();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public static void onDisconnected() {
//        ReverseProxyUtility.deleteReferredUrlCookie();
        flash.success("secure.signout");
        ReverseProxyUtility.redirectToDomain();
    }
}
