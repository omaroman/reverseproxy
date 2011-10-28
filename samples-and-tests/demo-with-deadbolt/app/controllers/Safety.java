/**
 * Author: OMAROMAN
 * Date: 10/13/11
 * Time: 11:38 AM
 */
package controllers;

import controllers.reverseproxy.deadbolt.ProtectController;
import models.User;
import net.parnassoft.playutilities.exceptions.UnfoundRecordException;
import play.Logger;
import play.modules.reverseproxy.ReverseProxyUtility;

public class Safety extends ProtectController.Safety {

    public static boolean authenticate(String username, String password) {
        Logger.debug("SCOPE -> Safety.authenticate");
        try {
            User user = User.findByUsername(username);
            return user.authenticate(password);
        } catch (UnfoundRecordException e) {
            return false;
        }
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
        Logger.debug("SCOPE -> Safety.onDisconnected");
        ReverseProxyUtility.redirectToDomain();
    }
}
