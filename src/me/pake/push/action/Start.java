package me.pake.push.action;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.security.KeyStore;
import java.util.Locale;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import me.pake.push.conf.DeveloperConf;
import me.pake.push.conf.ServerConf;
import me.pake.push.util.Log;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.nio.DefaultHttpServerIODispatch;
import org.apache.http.impl.nio.DefaultNHttpServerConnection;
import org.apache.http.impl.nio.DefaultNHttpServerConnectionFactory;
import org.apache.http.impl.nio.SSLNHttpServerConnectionFactory;
import org.apache.http.impl.nio.reactor.DefaultListeningIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.NHttpConnection;
import org.apache.http.nio.NHttpConnectionFactory;
import org.apache.http.nio.NHttpServerConnection;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.nio.protocol.BasicAsyncRequestConsumer;
import org.apache.http.nio.protocol.BasicAsyncResponseProducer;
import org.apache.http.nio.protocol.HttpAsyncExchange;
import org.apache.http.nio.protocol.HttpAsyncRequestConsumer;
import org.apache.http.nio.protocol.HttpAsyncRequestHandler;
import org.apache.http.nio.protocol.HttpAsyncService;
import org.apache.http.nio.protocol.UriHttpAsyncRequestHandlerMapper;
import org.apache.http.nio.reactor.IOEventDispatch;
import org.apache.http.nio.reactor.ListeningIOReactor;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpProcessorBuilder;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;
import org.json.JSONException;
import org.json.JSONStringer;

/**
 * HTTP/1.1 file server based on the non-blocking I/O model and capable of direct channel
 * (zero copy) data transfer.
 */
public class Start {

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
        	Log.record("Please specify document root directory", Log.NOTICE);
            System.exit(1);
        }
        // Document root directory
        File docRoot = new File(args[0]);
        int port = 1720;
        if (args.length >= 2) {
            port = Integer.parseInt(args[1]);
        }
        Log.record("Started...", Log.INFO); 
        // Create HTTP protocol processing chain
        HttpProcessor httpproc = HttpProcessorBuilder.create()
                .add(new ResponseDate())
                .add(new ResponseServer(ServerConf.USER_AGENT))
                .add(new ResponseContent())
                .add(new ResponseConnControl()).build();
        // Create request handler registry
        UriHttpAsyncRequestHandlerMapper reqistry = new UriHttpAsyncRequestHandlerMapper();
        // Register the default handler for all URIs
        reqistry.register("*", new HttpFileHandler(docRoot));
        // Create server-side HTTP protocol handler
        HttpAsyncService protocolHandler = new HttpAsyncService(httpproc, reqistry) {

            public void connected(final NHttpServerConnection conn) {
            	Log.record(conn + ": connection open",  Log.INFO);
                super.connected(conn);
            }

            @Override
            public void closed(final NHttpServerConnection conn) {
            	Log.record(conn + ": connectiong closed", Log.INFO);
                super.closed(conn);
            }

        };
        // Create HTTP connection factory
        NHttpConnectionFactory<DefaultNHttpServerConnection> connFactory;
        if (port == 8443) {
            // Initialize SSL context
            ClassLoader cl = Start.class.getClassLoader();
            URL url = cl.getResource("my.keystore");
            if (url == null) {
            	Log.record("Keystore not found", Log.EMERG);
                System.exit(1);
            }
            KeyStore keystore  = KeyStore.getInstance("jks");
            keystore.load(url.openStream(), "secret".toCharArray());
            KeyManagerFactory kmfactory = KeyManagerFactory.getInstance(
                    KeyManagerFactory.getDefaultAlgorithm());
            kmfactory.init(keystore, "secret".toCharArray());
            KeyManager[] keymanagers = kmfactory.getKeyManagers();
            SSLContext sslcontext = SSLContext.getInstance("TLS");
            sslcontext.init(keymanagers, null, null);
            connFactory = new SSLNHttpServerConnectionFactory(sslcontext,
                    null, ConnectionConfig.DEFAULT);
        } else {
            connFactory = new DefaultNHttpServerConnectionFactory(
                    ConnectionConfig.DEFAULT);
        }
        // Create server-side I/O event dispatch
        IOEventDispatch ioEventDispatch = new DefaultHttpServerIODispatch(protocolHandler, connFactory);
        // Set I/O reactor defaults
        IOReactorConfig config = IOReactorConfig.custom()
            .setIoThreadCount(1)
            .setSoTimeout(3000)
            .setConnectTimeout(3000)
            .build();
        // Create server-side I/O reactor
        ListeningIOReactor ioReactor = new DefaultListeningIOReactor(config);
        try {
            // Listen of the given port
            ioReactor.listen(new InetSocketAddress(port));
            // Ready to go!
            ioReactor.execute(ioEventDispatch);
        } catch (InterruptedIOException ex) {
        	Log.record("Interrupted", Log.INFO);
        } catch (IOException e) {
        	Log.record("I/O error: " + e.getMessage(), Log.INFO);
        }
        	Log.record("Shutdown", Log.INFO);
    }

    static class HttpFileHandler implements HttpAsyncRequestHandler<HttpRequest> {

        public HttpFileHandler(final File docRoot) {
            super();
        }

        public HttpAsyncRequestConsumer<HttpRequest> processRequest(
                final HttpRequest request,
                final HttpContext context) {
            // Buffer request content in memory for simplicity
            return new BasicAsyncRequestConsumer();
        }

        public void handle(
                final HttpRequest request,
                final HttpAsyncExchange httpexchange,
                final HttpContext context) throws HttpException, IOException {
            HttpResponse response = httpexchange.getResponse();
            handleInternal(request, response, context);
            httpexchange.submitResponse(new BasicAsyncResponseProducer(response));
        }

        private void handleInternal(
                final HttpRequest request,
                final HttpResponse response,
                final HttpContext context) throws HttpException, IOException {
        	
            HttpCoreContext coreContext = HttpCoreContext.adapt(context);
            
            String method = request.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH);
            if (!method.equals("GET")) {
            	Log.record("Invalid method: " + method, Log.INFO);
                throw new MethodNotSupportedException(method + " method not supported");
            }
            
            String target = request.getRequestLine().getUri();
            
            // Start push work
            PushWorker pw = new PushWorker(target);
            Thread t = new Thread(pw);
            t.start();
            
            // Response
            NHttpConnection conn = coreContext.getConnection(NHttpConnection.class);
            response.setStatusCode(HttpStatus.SC_OK);
            JSONStringer json = new JSONStringer();
            try {
				json.object().key("msg").value("ok").endObject();
			} catch (JSONException e) {
				e.printStackTrace();
			}
            NStringEntity entity = new NStringEntity(json.toString(), ContentType.create("text/json", "UTF-8"));
            json = null;
            response.setEntity(entity);
            Log.record(conn + "", Log.INFO);
            
        }

    }

}
