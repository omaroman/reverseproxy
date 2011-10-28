package jobs;

import play.Play;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.test.Fixtures;

@OnApplicationStart
public class Bootstrap extends Job {

    @Override
    public void doJob() {
        if (models.User.count() == 0) {
            Fixtures.loadModels("data.yml");
        }
    }
}
