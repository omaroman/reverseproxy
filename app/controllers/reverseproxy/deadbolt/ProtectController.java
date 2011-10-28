/**
 * Author: OMAROMAN
 * Date: 10/28/11
 * Time: 08:48 AM
 */

package controllers.reverseproxy.deadbolt;

import net.parnassoft.playutilities.CookieUtility;
import net.parnassoft.playutilities.UrlUtility;
import play.Logger;
import play.Play;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.libs.Crypto;
import play.modules.reverseproxy.ReverseProxyUtility;
import play.modules.reverseproxy.annotations.SchemeType;
import play.modules.reverseproxy.annotations.SwitchScheme;
import play.mvc.Controller;
import play.mvc.Http;
import play.utils.Java;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

//@GlobalSwitchScheme(type = SchemeType.HTTP)
public class ProtectController extends Controller {

    private static String COOKIE_NAME = "rememberme";

    // ~~~ Login
    @SwitchScheme(type = SchemeType.HTTPS)
    public static void signin() throws Throwable {
        Logger.debug("SCOPE -> ProtectController.signin");
        Http.Cookie remember = CookieUtility.getCookie(COOKIE_NAME);
        if(remember != null && remember.value.indexOf("-") > 0) {
            String sign = remember.value.substring(0, remember.value.indexOf("-"));
            String username = remember.value.substring(remember.value.indexOf("-") + 1);
            if(Crypto.sign(username).equals(sign)) {
                session.put("username", username);
                redirectToOriginalURL();
            }
        }
        //flash.keep("url");
        render();
    }

    @SwitchScheme(type = SchemeType.HTTPS)
    public static void authenticate(@Required String username, String password, boolean remember) throws Throwable {
        Logger.debug("SCOPE -> ProtectController.authenticate");
        // Check tokens
        Boolean allowed = (Boolean) Safety.invoke("authenticate", username, password);
        if(Validation.hasErrors() || !allowed) {
            flash.keep("url");
            flash.error("secure.error");
            params.flash();
//            signin();  // <--- DO NOT Invoke this way
            //render("@reverseproxy.deadbolt.ProtectController.signin"); // renders views/deadbolt/ProtectController/signin.html
            // or
            render("@signin"); // renders views/deadbolt/ProtectController/signin
        }
        // Mark user as connected
        session.put("username", username);
        // Remember if needed
        if(remember) {
            CookieUtility.createCookie(COOKIE_NAME, Crypto.sign(username) + "-" + username, "30d");
        }
        // Redirect to the original URL (or /)
        redirectToOriginalURL();
    }

    public static void signout() throws Throwable {
        Logger.debug("SCOPE -> ProtectController.signout");
        Safety.invoke("onDisconnect");
        session.clear();
        CookieUtility.deleteCookie("rememberme");
        ReverseProxyUtility.deleteReferredUrlCookie();
        Safety.invoke("onDisconnected");
        flash.success("protect.signout");
//        signin();  // <--- DO NOT Invoke this way
        render("@signin"); // renders views/deadbolt/ProtectController/signin
    }

    // ~~~ Utils
    static void redirectToOriginalURL() throws Throwable {
        Safety.invoke("onAuthenticated");
        String url = flash.get("url");
        if(url == null) {
            url = "/";
        }
//        redirect(url);    // <--- DO NOT Invoke this way
        UrlUtility.redirectToUriPattern(url);
    }

    public static class Safety extends Controller {

        /**
         * This method is called during the authentication process. This is where you check if
         * the user is allowed to log in into the system. This is the actual authentication process
         * against a third party system (most of the time a DB).
         *
         * @param username
         * @param password
         * @return true if the authentication process succeeded
         */
        static boolean authenticate(String username, String password) {
            return true;
        }

        /**
         * This method is called after a successful authentication.
         * You need to override this method if you with to perform specific actions (eg. Record the time the user signed in)
         */
        static void onAuthenticated() {
        }

         /**
         * This method is called before a user tries to sign off.
         * You need to override this method if you wish to perform specific actions (eg. Record the name of the user who signed off)
         */
        static void onDisconnect() {
        }

         /**
         * This method is called after a successful sign off.
         * You need to override this method if you wish to perform specific actions (eg. Record the time the user signed off)
         */
        static void onDisconnected() {
        }

        private static Object invoke(String m, Object... args) throws Throwable {
            Class safety;
            List<Class> classes = Play.classloader.getAssignableClasses(Safety.class);
            if(classes.size() == 0) {
                safety = Safety.class;
            } else {
                safety = classes.get(0);
            }
            try {
                return Java.invokeStaticOrParent(safety, m, args);
            } catch(InvocationTargetException e) {
                throw e.getTargetException();
            }
        }

    }

}
