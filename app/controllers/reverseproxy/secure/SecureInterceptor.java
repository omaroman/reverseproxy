/**
 * Author: OMAROMAN
 * Date: 10/18/11
 * Time: 9:59 AM
 */

package controllers.reverseproxy.secure;

import net.parnassoft.playutilities.ControllerUtility;
import net.parnassoft.playutilities.UrlUtility;
import net.parnassoft.playutilities.annotations.Interceptor;
import play.Logger;
import play.modules.reverseproxy.annotations.Check;

import play.mvc.*;

@Interceptor
public class SecureInterceptor extends Controller {

    @Before(unless={"login", "authenticate", "logout"})
    static void checkAccess() throws Throwable {
        // Authentify

        Scope.Session session = Scope.Session.current();
        Scope.Flash flash = Scope.Flash.current();
        Http.Request request = Http.Request.current();

        if(!session.contains("username")) {
            flash.put("url", "GET".equals(request.method) ? request.url : "/"); // seems a good default
            //render("@reverseproxy.SecureController.login"); // renders views/SecureController/login.html
            //SecureController.login(); <---- DO NOT Invoke this way
            Logger.debug("NEEDS SESSIONS...");
            UrlUtility.redirectByReverseRouting("reverseproxy.secure.SecureController.login");
        }
        // Checks
        Check check = ControllerUtility.getActionAnnotation(Check.class);
        if(check != null) {
            SecureController.check(check);
        }
        check = ControllerUtility.getControllerInheritedAnnotation(Check.class);
        if(check != null) {
            SecureController.check(check);
        }
    }
}
