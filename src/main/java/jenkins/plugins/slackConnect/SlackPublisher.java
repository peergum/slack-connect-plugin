package jenkins.plugins.slackConnect;

public interface SlackPublisher {
    void publish(String message);

    void publish(String message, String color);
}
