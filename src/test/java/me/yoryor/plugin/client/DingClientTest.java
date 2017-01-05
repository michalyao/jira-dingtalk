package me.yoryor.plugin.client;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientOptions;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertTrue;

public class DingClientTest {
    private DingClient client;
    private String corpID = "ding1aeb3554483ce275";
    private String corpSecret = "Yu7PKg0Lhy8XVrgpys4Kq5_91bi6K6V-d3JnvgDD2PfqQ8lan3BXNGm4bXDX83FY";

    @Before
    public void init() throws Exception {
        HttpClientOptions options = new HttpClientOptions();
        options.setSsl(true);
        options.setTrustAll(true);
        options.setKeepAlive(false);
        options.setDefaultPort(443);
        options.setDefaultHost("oapi.dingtalk.com");
        options.setLogActivity(true);
        client = new DingClient(options);
    }

    @Test
    public void close() throws Exception {
        client.close();
        assertTrue(true);
    }

    @Test
    public void getToken() throws Exception {
        client.getToken(corpID, corpSecret).setHandler(stringAsyncResult -> {
            if (stringAsyncResult.succeeded()) {
                System.out.println(stringAsyncResult.result());
            }
        });
    }

    @Test
    public void corpMsgTo() throws Exception {
        HttpClientOptions options = new HttpClientOptions();
        options.setSsl(true);
        options.setTrustAll(true);
        options.setKeepAlive(true).setLogActivity(true);
        options.setDefaultPort(443);
        Vertx.vertx().createHttpClient(options).get("oapi.dingtalk.com", "/gettoken?corpid=ding1aeb3554483ce275&corpsecret=Yu7PKg0Lhy8XVrgpys4Kq5_91bi6K6V-d3JnvgDD2PfqQ8lan3BXNGm4bXDX83FY", response -> {
            response.bodyHandler(buffer -> System.out.println(buffer.length()));
        }).putHeader("Accept", "*").putHeader("reqId", requestId()).end();
    }

    private String requestId() {
        return UUID.fromString(UUID.nameUUIDFromBytes(UUID.randomUUID().toString().getBytes()).toString()).toString().replace("-", "");
    }

    @Test
    public void userMap() throws Exception {

    }

}