package jenkins.plugins.slackConnect;

import org.jvnet.hudson.test.JenkinsRule;
import org.junit.Before;
import org.junit.Test;

public class StandardSlackPublisherTest {

    /**
     * Publish should generally not re-throw exceptions, or it will cause a build job to fail at end.
     */
    @Test
    public void publishWithBadHostShouldNotRethrowExceptions() {
        StandardSlackPublisher service = new StandardSlackPublisher("foo", "token", "#general");
        service.setHost("hostvaluethatwillcausepublishtofail");
        service.publish("message");
    }

    /**
     * Use a valid host, but an invalid team domain
     */
    @Test
    public void invalidTeamDomainShouldFail() {
        StandardSlackPublisher service = new StandardSlackPublisher("my", "token", "#general");
        service.publish("message");
    }

    /**
     * Use a valid team domain, but a bad token
     */
    @Test
    public void invalidTokenShouldFail() {
        StandardSlackPublisher service = new StandardSlackPublisher("tinyspeck", "token", "#general");
        service.publish("message");
    }
}
