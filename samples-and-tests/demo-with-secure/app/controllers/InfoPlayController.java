package controllers;

import controllers.reverseproxy.secure.SecureInterceptor;
import models.JavaInfo;
import models.PlayConf;
import play.modules.reverseproxy.annotations.Check;
import play.modules.reverseproxy.annotations.SchemeType;
import play.modules.reverseproxy.annotations.SwitchScheme;
import play.mvc.Controller;
import play.mvc.With;

@With(SecureInterceptor.class)
//@GlobalSwitchScheme
public class InfoPlayController extends Controller {

    @SwitchScheme(type = SchemeType.HTTP, keepUrl = true)
	@Check("admin")
    public static void showInfoPlay() {
        play.Logger.debug("SCOPE -> showInfoPlay");
        JavaInfo javainfo = new JavaInfo();
        PlayConf playconf = new PlayConf();
        render(javainfo, playconf);
    }

}
