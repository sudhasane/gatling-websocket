package websocket;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.ws;

public class StompTest extends Simulation {
    {

        HttpProtocolBuilder httpProtocol = http
                .baseUrl("http://localhost:8080")
                .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8") // Here are the common headers
                .doNotTrackHeader("1")
                .acceptLanguageHeader("en-US,en;q=0.5")
                .acceptEncodingHeader("gzip, deflate")
                .userAgentHeader("Gatling")
                .wsBaseUrl("ws://localhost:8080");
        String serverId = "10000001";
        String sessionId = "thisisnewsession1";
        String transport = "websocket";

        ScenarioBuilder scn = scenario("WebSocket Scenario")
                .exec(ws("open socket connection").connect("/gs-guide-websocket")
                       .await(10)
                        .on(ws.checkTextMessage("checkConnection")
                                .check(regex("CONNECTED"))))

                .exec(ws("subscribe")
                        .sendText("[\"SUBSCRIBE\\nid:sub-0\\ndestination:/topic/greetings\\n\\n\\u0000\"]"))

                .exec(ws("publish message")
                .sendText("[\"SEND\\ndestination:/app/hello\\ncontent-length:15\\n\\n{\\\"name\\\":\\\"Tom\\\"}\\u0000\"]")
                .await(10).on(ws.checkTextMessage("checkNotification").check(regex("MESSAGE\\\\ndestination:\\/topic\\/greetings\\\\ncontent-type:application\\/json;charset=UTF-8\\\\nsubscription:sub-0\\\\nmessage-id:[\\w\\d-]*\\\\ncontent-length:\\d*\\\\n\\\\n\\{\\\\\"content\\\\\":\\\\\"Hello, Tom!\\\\\"\\}\\\\u0000"))))

                .exec(ws("close socket connection").close());

        setUp(scn.injectOpen(atOnceUsers(1))).protocols(httpProtocol);
    }

}
