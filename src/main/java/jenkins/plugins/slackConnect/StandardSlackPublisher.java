package jenkins.plugins.slackConnect;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;

import org.json.JSONObject;
import org.json.JSONArray;

import java.util.logging.Level;
import java.util.logging.Logger;

import jenkins.model.Jenkins;
import hudson.ProxyConfiguration;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;

public class StandardSlackPublisher implements SlackPublisher {

    private static final Logger logger = Logger.getLogger(StandardSlackPublisher.class.getName());

    private String host = "slack.com";
    private String teamDomain;
    private String token;
    private String[] roomIds;

    public StandardSlackPublisher(String teamDomain, String token, String roomId) {
        super();
        setTeamDomain(teamDomain);
        setToken(token);
        setRoomIds(roomId.split(",; "));
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getHost() {
        return host;
    }

    public String getTeamDomain() {
        return teamDomain;
    }

    public String getToken() {
        return token;
    }

    public String[] getRoomIds() {
        return roomIds;
    }

    public void setTeamDomain(String teamDomain) {
        this.teamDomain = teamDomain;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setRoomIds(String[] roomIds) {
        this.roomIds = roomIds;
    }

    public String publish(String message) {
        return publish(message, "warning");
    }

    public String publish(String message, String color) {
        String url = getUrl();

        String response = "No room to notify.";
        for (String roomId : getRoomIds()) {
            logger.info("Posting: to " + roomId + " on " + getTeamDomain() + " using " + url + ": " + message + " " + color);

            PostMethod post = new PostMethod(url);
            HttpClient client = getHttpClient();

            try {
                JSONObject field = new JSONObject();
                field.put("short", false);
                field.put("value", message);

                JSONArray fields = new JSONArray();
                fields.put(field);

                JSONObject attachment = new JSONObject();
                attachment.put("fallback", message);
                attachment.put("color", color);
                attachment.put("fields", fields);
                JSONArray attachments = new JSONArray();
                attachments.put(attachment);

                JSONObject json = new JSONObject();
                json.put("channel", roomId);
                json.put("attachments", attachments);

                post.addParameter("payload", json.toString());
                post.getParams().setContentCharset("UTF-8");

                int responseCode = client.executeMethod(post);
                response = post.getResponseBodyAsString();
                if (responseCode != HttpStatus.SC_OK) {
                    logger.log(Level.WARNING, "Slack post may have failed. Response: " + response);
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error posting to Slack", e);
                response = "Error posting to Slack [" + e.toString() + "]";
            } finally {
                logger.info("Posting succeeded");
                post.releaseConnection();
            }
        }
        return response;
    }

    protected HttpClient getHttpClient() {
        HttpClient client = new HttpClient();
        if (Jenkins.getInstance() != null) {
            ProxyConfiguration proxy = Jenkins.getInstance().proxy;
            if (proxy != null) {
                client.getHostConfiguration().setProxy(proxy.name, proxy.port);
                String username = proxy.getUserName();
                String password = proxy.getPassword();
                // Consider it to be passed if username specified. Sufficient?
                if (username != null && !"".equals(username.trim())) {
                    logger.info("Using proxy authentication (user=" + username + ")");
                    // http://hc.apache.org/httpclient-3.x/authentication.html#Proxy_Authentication
                    // and
                    // http://svn.apache.org/viewvc/httpcomponents/oac.hc3x/trunk/src/examples/BasicAuthenticationExample.java?view=markup
                    client.getState().setProxyCredentials(AuthScope.ANY,
                            new UsernamePasswordCredentials(username, password));
                }
            }
        }
        return client;
    }

    protected String getUrl() {
        return "https://" + getTeamDomain() + "." + getHost() + "/services/hooks/jenkins-ci?token=" + getToken();
    }
}
