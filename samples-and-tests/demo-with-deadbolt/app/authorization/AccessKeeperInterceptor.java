package authorization;

import controllers.deadbolt.*;
import controllers.reverseproxy.deadbolt.ProhibitedController;
import controllers.reverseproxy.deadbolt.ProtectController;
import models.User;
import models.deadbolt.ExternalizedRestrictions;
import models.deadbolt.RoleHolder;
import net.parnassoft.playutilities.*;
import net.parnassoft.playutilities.annotations.Interceptor;
import net.parnassoft.playutilities.exceptions.UnfoundRecordException;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Util;


@Interceptor
public class AccessKeeperInterceptor extends Controller implements DeadboltHandler {

    @Util
    public static boolean isConnected() {
        return SessionUtility.contains("username") || SessionUtility.contains("user_email");
    }

    @Util
    private static String connected() {
        if (SessionUtility.contains("username")) {
            return (String) SessionUtility.get("username");
        } else if (SessionUtility.contains("user_email")) {
            return (String) SessionUtility.get("user_email");
        }
        return null;
    }

    @Util
    private static boolean isRestrictedArea() {
        if (ControllerUtility.hasActionAnnotation(Restrict.class) ||
            ControllerUtility.hasActionAnnotation(Restrictions.class) ||
            ControllerUtility.hasActionAnnotation(ExternalRestrictions.class) ||
            ControllerUtility.hasActionAnnotation(RestrictedResource.class)) {

            Logger.debug("RESTRICTED AREA...");
            return true;
        }

        Logger.debug("UNRESTRICTED AREA...");
        return false;
    }

	public void beforeRoleCheck() {

        if (!isRestrictedArea()) {
            return; // DO NOTHING...
        }

        if (!isConnected()) {
            // <<< DO invoke this way
            //UrlUtility.redirectByReverseRouting("reverseproxy.deadbolt.ProtectController.signin");
            try {
                // or
                ProtectController.signin();
            } catch (Throwable throwable) {
                //throwable.printStackTrace();
                // Do Nothing...
            }
        }

    }

    public RoleHolder getRoleHolder() {
        String username = connected();
        User user = null;
        try {
            user = User.findByUsername(username);
        } catch (UnfoundRecordException e) {
            // DO NOTHING...
        }
        return user;
    }

    public void onAccessFailure(String controllerClassName) {
        // <<< DO invoke this way:
        //  UrlUtility.redirectByReverseRouting("reverseproxy.deadbolt.ProhibitedController.prohibited");
        // or
        ProhibitedController.prohibited();
    }

    public ExternalizedRestrictionsAccessor getExternalizedRestrictionsAccessor() {
        return new ExternalizedRestrictionsAccessor() {
            public ExternalizedRestrictions getExternalizedRestrictions(String name) {
                return null;
            }
        };
    }

	public RestrictedResourcesHandler getRestrictedResourcesHandler() {
		return new DynamicAccess();
	}
}