package jenkins.plugins.slackConnect;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.json.JSONArray;
import org.json.JSONObject;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.jvnet.hudson.test.JenkinsRule;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import org.mockito.MockSettings;
import static org.mockito.Mockito.*;
import org.mockito.stubbing.Answer;
import org.mortbay.util.ajax.JSON;

public class StandardSlackPublisherTest {

    /**
     * Publish with just a message should use "warning" level
     */
    @Test
    public void publishWithWarningByDefault() {
        StandardSlackPublisher service = mock(StandardSlackPublisher.class);
        when(service.publish(anyString())).thenCallRealMethod();
        service.publish("test");
        verify(service).publish(eq("test"), eq("warning"));
    }

    /**
     * Test we call Slack API properly
     */
    @Test
    public void checkSlackApiCallIsCorrect() throws URIException {
        String host = "slack.com";
        String team = "team";
        String token = "token";
        String room = "room";

        String message = "test message";
        String level = "warning";

        String expectedPayload = "{\"attachments\":[{\"color\":\"" + level
                + "\",\"fields\":[{\"short\":false,\"value\":\"" + message
                + "\"}],\"fallback\":\"" + message + "\"}],\"channel\":\""
                + room + "\"}";

        HttpClient client = mock(HttpClient.class);

        StandardSlackPublisher service = mock(StandardSlackPublisher.class);
        when(service.getHost()).thenReturn(host);
        when(service.getTeamDomain()).thenReturn(team);
        when(service.getToken()).thenReturn(token);
        when(service.getRoomIds()).thenReturn(room.split(",; "));
        when(service.getHttpClient()).thenReturn(client);
        when(service.getUrl()).thenReturn("URL");

        when(service.publish(anyString())).thenCallRealMethod();
        when(service.publish(anyString(), anyString())).thenCallRealMethod();

        service.publish(message, level);
        ArgumentCaptor<PostMethod> argument = ArgumentCaptor.forClass(PostMethod.class);

        try {
            verify(client).executeMethod(argument.capture());
        } catch (Exception e) {
            // do nothing
        }

        assertEquals("UTF-8", argument.getValue().getParams().getContentCharset());
        NameValuePair payload = argument.getValue().getParameter("payload");
        assertEquals(expectedPayload, payload.getValue());

        Map payloadMap = (Map) JSON.parse(payload.getValue());
        assertTrue(payloadMap.keySet().contains("attachments"));
        assertTrue(payloadMap.keySet().contains("channel"));
        payloadMap.entrySet()
                .forEach(
                        x -> {
                            switch((String)(((Map.Entry)x).getKey())) {
                                case "attachments":
                                    Arrays.asList(((Map.Entry)x).getValue())
                                            .forEach(
                                                    z -> {
                                                        Map attachment = (Map)(Array.get(z,0));
                                                        assertEquals(level,attachment.get("color"));
                                                        assertEquals(message,attachment.get("fallback"));
                                                        Map fields = (Map) (Array.get(attachment.get("fields"),0));
                                                        assertEquals(false,fields.get("short"));
                                                        assertEquals(message,fields.get("value"));
                                                    }
                                            );
                                    //assertEquals(attachments[0])
                                    break;
                                case "channel":
                                    assertEquals(room,payloadMap.get("channel"));
                                    break;
                            }
                        }
                );
        assertEquals(new URI("URL"),argument.getValue().getURI());
    }
}
