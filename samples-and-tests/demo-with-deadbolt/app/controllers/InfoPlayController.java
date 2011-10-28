package controllers;

import controllers.deadbolt.Deadbolt;
import controllers.deadbolt.RestrictedResource;
import models.JavaInfo;
import models.PlayConf;
import play.modules.reverseproxy.annotations.Check;
import play.modules.reverseproxy.annotations.SchemeType;
import play.modules.reverseproxy.annotations.SwitchScheme;
import play.mvc.Controller;
import play.mvc.With;

@With({Deadbolt.class})
//@GlobalSwitchScheme
public class InfoPlayController extends Controller {

    @SwitchScheme(type = SchemeType.HTTP, keepUrl = true)
	@RestrictedResource(name = "controllers_InfoPlayController_do")
    public static void showInfoPlay() {
        JavaInfo javainfo = new JavaInfo();
        PlayConf playconf = new PlayConf();
        render(javainfo, playconf);
    }

}
