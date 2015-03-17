package jenkins.plugins.slackConnect;

import hudson.model.AbstractBuild;

@SuppressWarnings("rawtypes")
public class InactiveNotifier implements FineGrainedNotifier {
    public void started(AbstractBuild r) {
    }

    public void deleted(AbstractBuild r) {
    }

    public void finalized(AbstractBuild r) {
    }

    public void completed(AbstractBuild r) {
    }
}
