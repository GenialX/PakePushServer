package me.pake.push.util;

import java.net.Socket;

import me.pake.push.conf.DeveloperConf;
import me.pake.push.conf.ServerConf;

import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.impl.DefaultBHttpClientConnection;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpProcessorBuilder;
import org.apache.http.protocol.HttpRequestExecutor;
import org.apache.http.protocol.RequestConnControl;
import org.apache.http.protocol.RequestContent;
import org.apache.http.protocol.RequestExpectContinue;
import org.apache.http.protocol.RequestTargetHost;
import org.apache.http.protocol.RequestUserAgent;
import org.apache.http.util.EntityUtils;

/**
 * Elemental example for executing multiple GET requests sequentially.
 */
public class HttpGet {

    public String get(String httpHost, String uri, int port) throws Exception {
        HttpProcessor httpproc = HttpProcessorBuilder.create()
            .add(new RequestContent())
            .add(new RequestTargetHost())
            .add(new RequestConnControl())
            .add(new RequestUserAgent(ServerConf.USER_AGENT))
            .add(new RequestExpectContinue(true)).build();

        HttpRequestExecutor httpexecutor = new HttpRequestExecutor();

        HttpCoreContext coreContext = HttpCoreContext.create();
        HttpHost host = new HttpHost(httpHost, port);
        coreContext.setTargetHost(host);

        DefaultBHttpClientConnection conn = new DefaultBHttpClientConnection(8 * 1024);
        ConnectionReuseStrategy connStrategy = DefaultConnectionReuseStrategy.INSTANCE;
        
        String responseString = "";
        
        try {
                if (!conn.isOpen()) {
                    Socket socket = new Socket(host.getHostName(), host.getPort());
                    conn.bind(socket);
                }
                BasicHttpRequest request = new BasicHttpRequest("GET", uri);
                
//if(DeveloperConf.CONSOLE_INFO) System.out.println(">> Request URI: " + request.getRequestLine().getUri());

                httpexecutor.preProcess(request, httpproc, coreContext);
                HttpResponse response = httpexecutor.execute(request, conn, coreContext);
                httpexecutor.postProcess(response, httpproc, coreContext);
                responseString = EntityUtils.toString(response.getEntity());
//if(DeveloperConf.CONSOLE_INFO)  System.out.println(responseString);
//if(DeveloperConf.CONSOLE_INFO)  System.out.println("<< Response: " + response.getStatusLine());
                if (!connStrategy.keepAlive(response, coreContext)) {
                    conn.close();
                } else {
//if(DeveloperConf.CONSOLE_INFO) System.out.println("Connection kept alive...");
                }

        } finally {
            conn.close();
        }
		return responseString;
    }

}
