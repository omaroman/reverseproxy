package models;

import java.text.SimpleDateFormat;
import java.util.Date;
import play.Play;
import play.i18n.Messages;

public class PlayConf {

    public String softname;
    public String[] softlangs;
    public String softrun;
    public Date softdate;

    public PlayConf()
    {
        softname = Play.configuration.getProperty("application.name", "");
        softlangs = Play.configuration.getProperty("application.langs", "").split(",");
        softrun = new SimpleDateFormat(Messages.get("softwaretime")).format(new Date(Play.startedAt));
        softdate = new Date(Play.startedAt);
    }
}
