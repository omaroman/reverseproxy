package controllers;

import controllers.deadbolt.Deadbolt;
import controllers.deadbolt.RestrictedResource;
import play.modules.reverseproxy.annotations.GlobalSwitchScheme;
import play.modules.reverseproxy.annotations.SchemeType;
import play.modules.reverseproxy.annotations.SwitchScheme;
import play.mvc.*;

import java.util.*;

import models.*;

@With({Deadbolt.class})
@GlobalSwitchScheme(type = SchemeType.HTTP)
public class ApplicationController extends Controller {

    public static void insecureIndex() {
        render();
    }

    @RestrictedResource(name = "controllers_ApplicationController_secureIndex")
    @SwitchScheme(type = SchemeType.HTTPS)
    public static void secureIndex() {
        render();
    }

}