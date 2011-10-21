package controllers;

import play.*;
import play.modules.reverseproxy.annotations.GlobalSwitchScheme;
import play.modules.reverseproxy.annotations.SchemeType;
import play.modules.reverseproxy.annotations.SwitchScheme;
import play.mvc.*;

import java.util.*;

import models.*;

@GlobalSwitchScheme(type = SchemeType.HTTP)
public class ApplicationController extends Controller {

    public static void insecureIndex() {
        render();
    }

    @SwitchScheme(type = SchemeType.HTTPS)
    public static void secureIndex() {
        render();
    }

}