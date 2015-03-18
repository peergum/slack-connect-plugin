package jenkins.plugins.slackConnect;

public interface SlackPublisher {
    String publish(String message);

    String publish(String message, String color);
}
