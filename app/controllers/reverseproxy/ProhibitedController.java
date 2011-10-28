/**
 * Author: OMAROMAN
 * Date: 10/27/11
 * Time: 4:30 PM
 */
package controllers.reverseproxy;

import play.mvc.Controller;
import play.mvc.Http;

public class ProhibitedController extends Controller {

    public static void prohibited() {
        //403 Forbidden
        Http.Response.current().status = Http.StatusCode.FORBIDDEN;
        render();
    }
}
