package controllers.reverseproxy.secure;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import net.parnassoft.playutilities.CookieUtility;
import net.parnassoft.playutilities.UrlUtility;
import play.Logger;
import play.Play;
import play.modules.reverseproxy.ReverseProxyUtility;
import play.modules.reverseproxy.annotations.*;
import play.modules.reverseproxy.annotations.Check;

import play.data.validation.*;
import play.libs.*;
import play.mvc.Controller;
import play.mvc.Http;
import play.utils.*;

//@GlobalSwitchScheme(type = SchemeType.HTTP)
public class SecureController extends Controller {

    private static String COOKIE_NAME = "rememberme";

    //private
    static void check(Check check) throws Throwable {
        for(String profile : check.value()) {
            boolean hasProfile = (Boolean)Security.invoke("check", profile);
            if(!hasProfile) {
                Security.invoke("onCheckFailed", profile);
            }
        }
    }

    // ~~~ Login
    @SwitchScheme(type = SchemeType.HTTPS)
    public static void login() throws Throwable {
        Logger.debug("SCOPE -> SecureController.login");
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
        Logger.debug("SCOPE -> SecureController.authenticate");
        // Check tokens
        Boolean allowed = (Boolean)Security.invoke("authenticate", username, password);
        if(Validation.hasErrors() || !allowed) {
            flash.keep("url");
            flash.error("secure.error");
            params.flash();
            // <<< DO invoke this way:
            login();
            //render("@reverseproxy.ProtectController.login"); // renders views/secure/SecureController/login
            // or
            //render("@login"); // renders views/secure/SecureController/login
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

    public static void logout() throws Throwable {
        Logger.debug("SCOPE -> SecureController.logout");
        Security.invoke("onDisconnect");
        session.clear();
        CookieUtility.deleteCookie("rememberme");
        ReverseProxyUtility.deleteReferredUrlCookie();
        Security.invoke("onDisconnected");
        flash.success("secure.logout");
        // <<< DO invoke this way
        login();
        // or
        // render("@login"); // renders views/secure/SecureController/login
    }

    // ~~~ Utils
    static void redirectToOriginalURL() throws Throwable {
        Security.invoke("onAuthenticated");
        String url = flash.get("url");
        if(url == null) {
            url = "/";
        }
//        redirect(url);    // <--- DO NOT Invoke this way
        UrlUtility.redirectToUriPattern(url);
    }

    public static class Security extends Controller {

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
         * This method checks that a profile is allowed to view this page/method. This method is called prior
         * to the method's controller annotated with the @Check method. 
         *
         * @param profile
         * @return true if you are allowed to execute this controller method.
         */
        static boolean check(String profile) {
            return true;
        }

        /**
         * This method returns the current connected username
         * @return
         */
        static String connected() {
            return session.get("username");
        }

        /**
         * Indicate if a user is currently connected
         * @return  true if the user is connected
         */
        static boolean isConnected() {
            return session.contains("username");
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

        /**
         * This method is called if a check does not succeed. By default it shows the not allowed page (the controller forbidden method).
         * @param profile
         */
        static void onCheckFailed(String profile) {
            forbidden();
        }

        private static Object invoke(String m, Object... args) throws Throwable {
            Class security = null;
            List<Class> classes = Play.classloader.getAssignableClasses(Security.class);
            if(classes.size() == 0) {
                security = Security.class;
            } else {
                security = classes.get(0);
            }
            try {
                return Java.invokeStaticOrParent(security, m, args);
            } catch(InvocationTargetException e) {
                throw e.getTargetException();
            }
        }

    }

}
