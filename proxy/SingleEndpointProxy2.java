//java -cp $CLASSPATH:commons-math-2.2.jar EndpointProxy 159.90.11.58 7007 159.90.11.193 8890 False 3 1.0 1

import java.io.*;
import java.net.*;
import java.util.*;
//import HTTPClient.*;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.AbstractHttpMessage;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.Header;
import org.apache.http.util.EntityUtils;
import org.apache.http.impl.io.ChunkedInputStream;
//import org.apache.commons.httpclient.ChunkedInputStream;

public class SingleEndpointProxy2 extends Thread {

    private String endpointAddress = null;
    private String proxyAddress = null;
    private int proxyPort;
    private int endpointPort = 80;
    private Socket connectedClient = null;
    private int nr = 0;
    private int nc = 0;
    private boolean show = true;
    private String graph = null;

    public SingleEndpointProxy2 (String addressE, int portE, String addressP, int portP, String g) {

        this.endpointAddress = addressE;
        this.proxyAddress = addressP;
        this.proxyPort = portP;
        this.endpointPort = portE;
        this.graph = g;
    }

    public synchronized void increaseNC() {
        nc++;
    }

    public synchronized void increaseNR(int d) {
        nr += d;
    }

    public synchronized void show() {
        if (show) {
            System.out.println(nr+" "+nc);
            show = false;
        }
    }

    public void run() {

        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                show();
            }
        });

        ServerSocket ss = null;
        boolean ready = false;
        while (!ready) {
            try {
                ss = new ServerSocket (this.proxyPort, 50, InetAddress.getByName(this.proxyAddress));
                ready = true;
            } catch (java.io.IOException e) {
                //Thread.sleep(5);
                //System.err.println("Problems to set up proxy at address "+this.endpointAddress+" using port "+this.proxyPort);
                //e.printStackTrace();
                //System.exit(1);
            }
        }

      while (true) {

        BufferedReader inFromClient = null;
        DataOutputStream outToClient = null;
        CloseableHttpResponse rsp = null;
        boolean b = false;
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            this.connectedClient = ss.accept();
            //System.out.println("serversocket constructor with parameters: "+this.proxyPort+" and "+this.endpointAddress);
            //this.connectedClient = new ServerSocket (this.proxyPort, 5, InetAddress.getByName(this.endpointAddress)).accept();
            //System.out.println("httpconnection constructor with parameters: "+this.endpointAddress+" and "+this.endpointPort);
            //uri = new URIBuilder().setScheme("http").setHost(this.endpointAddress).setPath("/sparql").setPort(this.endpointPort);
            //System.out.println("httpconnection created");
            ////System.out.println( "Client " + cc.getInetAddress() 
            ////                 + ":" + cc.getPort() + " is connected");
            increaseNC();
            inFromClient = new BufferedReader(
                                      new InputStreamReader(
                                      this.connectedClient.getInputStream()));
            outToClient = new DataOutputStream(this.connectedClient.getOutputStream());
            while (!inFromClient.ready()) {
                Thread.sleep(1);
            }
            String requestString = inFromClient.readLine();
            //System.out.println("requestString: "+requestString);
            String headerLine = requestString;
            StringTokenizer tokenizer = new StringTokenizer(headerLine);
            String httpMethod = tokenizer.nextToken();
            String httpQueryString = tokenizer.nextToken();

            //System.out.println("method: "+httpMethod);
 
            HashMap<String, String> map = new HashMap<String, String>();
            String postQuery = read(inFromClient, map);
            //inFromClient.close();
            System.out.println("postQuery: "+postQuery);
                    //httpQueryString = httpQueryString.substring(6);
            URIBuilder uriB = new URIBuilder().setScheme("http").setHost(this.endpointAddress).setPort(this.endpointPort);
            setParameters(uriB, httpQueryString);
            if (this.graph != null) {
                uriB.setParameter("default-graph-uri", this.graph);
            }
            if (httpMethod.equals("GET")) {
                URI uri = uriB.build();
                HttpGet httpGet = new HttpGet(uri);
                addHeaders(httpGet, map);
                //System.out.println(httpGet.getURI());
                rsp = httpclient.execute(httpGet);
            } else if (httpMethod.equals("POST")) {
                uriB.setParameter("query", java.net.URLDecoder.decode(postQuery, "UTF-8"));
                URI uri = uriB.build();
                HttpPost httpPost = new HttpPost(uri);
                addHeaders(httpPost, map);    
                rsp = httpclient.execute(httpPost);
            } else {	
                sendResponse(404, "<b>The Requested resource not found ....", outToClient);
            }
            write(rsp, outToClient, "\r\n");
            rsp.close();
            outToClient.close();
            this.connectedClient.close();
            httpclient.close();
            inFromClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println("Connection closed!");
        //System.out.println("Se han enviado "+nr+" resultados hasta ahora");
        //System.out.println("Se han hecho "+nc+" contactos a esta fuente hasta ahora");
      }
    }

    public void addHeaders(AbstractHttpMessage message, HashMap<String, String> map) {

        for (String variable : map.keySet()) {
            String value = map.get(variable);
            message.addHeader(variable, value);
        }
    }

    public String read (BufferedReader inFromClient, HashMap<String, String> map) throws Exception {

        String header = "";
        int length = 0;
        String q = "";
        String requestString2 = inFromClient.readLine();

        while (!(requestString2 == null) && !(requestString2.equals(""))) {
            //System.out.println("requestString2: "+requestString2+". size: "+requestString2.length());
            //System.out.println("linea: "+requestString2);
            if (   requestString2.startsWith("Content-Length:") 
                || requestString2.startsWith("content-length:")) {
                length = Integer.parseInt(requestString2.substring(16));
            } else {
                if ((!requestString2.startsWith("Accept:") || requestString2.startsWith("Accept: application/sparql-results+xml"))&&!requestString2.endsWith("; charset=utf-8")) {
                    header = header + (requestString2 + "\n");
                }
                if (requestString2.endsWith("; charset=utf-8")) {
                    header = header + (requestString2.substring(0, requestString2.length()-15) + "\n");
                }
            }
            requestString2 = inFromClient.readLine();
        }
        //System.out.println("reading ended");
        if (length > 0) {
            q = getQuery(length, inFromClient);
        }

        //System.out.println("query: "+q);
        getHeaderForm(header, map);
        return q;
    }

    private String getQuery(int l, BufferedReader inFromClient) throws Exception {

        char[] cbuf = new char[l];
        int k = l;
        int m = 6;
        String s = "";
        do {
            int c = inFromClient.read(cbuf, 0, k);
            //System.out.println("c: "+c);

            if ((k == l) && (c >=m)) {
                s = new String(cbuf, m, (c-m));
            } else if (k == l) {
                m =  m - c;
            } else {
                s = s + new String(cbuf, 0, c);
            }
            k = k - c;
            //System.out.println("readed s: "+s);
            
        } while (k > 0);
        if (s.startsWith("n=SPARQL&query=")) {
            s = s.substring(15);
        }
        int i = s.indexOf("&infer=");
        if (i > 0) {
            s = s.substring(0, i);
        }
        //if (c != l) {
        //    throw new Exception("consulta incompleta!!");
        //}
        //System.out.println("s: "+s);
        //responseBuffer.append(s);
        return s;
    }

    private void getHeaderForm (String s, HashMap<String, String> map) {

        //System.out.println("s: "+s);
        String[] ss = s.split("\n");
        for (int i = 0; i < ss.length; i++) {
            //System.out.println("ss[i]: "+ss[i]);
            String[] ss2 = ss[i].split(": ");
            if (ss2.length>1) {
                map.put(ss2[0], ss2[1]);
            }
            //System.out.println("incluyendo "+ss2[0]+" con valor "+ss2[1]);
        }
    }

    public void setParameters(URIBuilder uri, String httpQueryString) throws Exception {

        int i = httpQueryString.indexOf("?");
        boolean b  = i == -1;
        i = b ? httpQueryString.length() : i;
        //System.out.println("httpQueryString: "+httpQueryString);
        String path = httpQueryString.substring(0, i);
        //System.out.println("path: "+path);
        uri.setPath(path);
        if (b) return;
        StringTokenizer st = new StringTokenizer(httpQueryString.substring(i+1), " \n\t\r&");
        //uri.setPath(path);
        while (st.hasMoreTokens()) {
            String variableValue = st.nextToken();
            StringTokenizer st2 = new StringTokenizer(variableValue, "=");
            String variable = st2.nextToken();
            String value = st2.nextToken();
            value = java.net.URLDecoder.decode(value, "UTF-8");
            //System.out.println(value);
            uri.setParameter(variable, value);
        }
    }

    public int write2(String h, HttpResponse r, DataOutputStream outToClient) throws Exception {
      int c = 0;
      try {
        outToClient.writeBytes(h);
        outToClient.writeBytes("\n");
        String resp = "";
        //System.out.println("headers: "+h);
        //System.out.println("r: "+r);
        //System.out.println("r.getEntity: "+r.getEntity());

        InputStream is = r.getEntity().getContent();
        //int l = (int) r.getEntity().getContentLength();
        //System.out.println("size: "+l);
        String cache = "";
        while (true) { 
            byte[] data = new byte[16384];
            int m = is.read(data);
            if (m<0) {
                break;
            }
            outToClient.writeBytes(Integer.toHexString(m)+"\r\n");
            outToClient.write(data, 0, m);
            outToClient.writeBytes("\r\n");
            //outToClient.flush();
            //System.out.println(m+" bytes written2");
            int j = 0;
            String s = cache+(new String(data));
            //System.out.println(s);
            while (j >= 0) {
                j = s.indexOf("<result>");
                if (j >=0) {
                    c++;
                    s = s.substring(j+1);
                }
            }
            cache = s.substring(Math.max(s.length()-7, 0));
        }
        ////System.out.println("se han enviado "+data.length+" bytes, y "+c+" resultados");
        //outToClient.flush();
      } catch (Exception e) {
        e.printStackTrace();
        //System.out.println("EndpointProxy:244");
        //System.out.println("ocurrio una excepcion, no se escribira la respuesta!");
      }
      outToClient.writeBytes(Integer.toHexString(0)+"\r\n");
      outToClient.writeBytes("\r\n");
      outToClient.flush();
      outToClient.close();
      //System.out.println("write2 ended");
      return c;
    }
// sep: for anapsid \r\n for fedX \n
    public void  write(HttpResponse r, DataOutputStream outToClient, String sep) throws Exception {
        r.removeHeaders("Connection");
        r.addHeader("Connection", "close"); 
        boolean isChunked = (r.getFirstHeader("Transfer-Encoding") != null) && r.getFirstHeader("Transfer-Encoding").getValue().equalsIgnoreCase("chunked");
        boolean isXML = r.getFirstHeader("Content-Type").getValue().indexOf("xml")>=0;
        boolean isJSON = r.getFirstHeader("Content-Type").getValue().indexOf("json")>=0;

        HeaderIterator it = r.headerIterator();
        outToClient.writeBytes(r.getStatusLine().toString()+"\r\n");
        while (it.hasNext()) {
            Header h = it.nextHeader();
            outToClient.writeBytes(h.toString()+"\r\n");
        }
        outToClient.writeBytes(sep);

        InputStream is = r.getEntity().getContent();
        BufferedInputStream bis = new BufferedInputStream(is);
        int d = 0;
        String cache = "";
        int m = 0;
        byte[] data = new byte[4096];
        while ((m = bis.read(data)) != -1) {
            //System.out.println(m+" bytes read");
            if (isChunked) {
                outToClient.writeBytes(Integer.toHexString(m)+"\r\n");
            }
            outToClient.write(data, 0, m);
            if (isChunked) {
                outToClient.writeBytes("\r\n");
            }
            //System.out.println(m+" bytes written");
            String s = cache+(new String(data, 0, m));
            //System.out.println("s:\n"+s);
            if (isJSON) {
                 cache = countTuplesJSON(s);
            } else if (isXML) {
                 cache = countTuplesXML(s);
            }
            //System.out.println("cache: "+cache);
        }
        if (isChunked) {
            outToClient.writeBytes(Integer.toHexString(0)+"\r\n");
            outToClient.writeBytes("\r\n");
        }
        if (isJSON && (cache.length()==0)) {
            //System.out.println("cache: "+cache);
            //System.out.println("decreasing 1");
            increaseNR(-1);
        }
        outToClient.flush();
        outToClient.close();
        //System.out.println("write ended");
    }

    public String countTuplesJSON(String s) {
        int d = 0;
        int j = 0;
        int k = s.length();
        do {
            k = s.length();
            s = s.replaceAll("}(\\s)+}", "}}");
        } while(s.length()!=k);

        while (j >= 0) {
            //System.out.println("s: "+s);
            j = s.indexOf("}}");
            if (j >=0) {
                d++;
                s = s.substring(j+2);
            }
        }
        //System.out.println("increasing value by: "+d);
        increaseNR(d);
        return s.substring(Math.max(s.lastIndexOf("}"), 0));
    }
    public  String countTuplesXML(String s) {
        int c = 0;
        int j = 0;
        while (j >= 0) {
            j = s.indexOf("<result>");
            if (j >=0) {
                c++;
                s = s.substring(j+1);
            }
        }
        increaseNR(c);
        return s.substring(Math.max(s.length()-7, 0));
    }

    public String getHeaders(HttpResponse rsp) throws Exception {

        String h = "";

        HeaderIterator it = rsp.headerIterator();
        while (it.hasNext()) {
            Header header = it.nextHeader();
            //System.out.println("header: "+header.toString());
            //System.out.println("header.getName: "+header.getName());
            if (!header.getName().equalsIgnoreCase("Connection")) { 
                h += header.toString()+"\r\n";
            }
        }
        h = h + "Connection: close\r\n";
        //if (!query.startsWith("ASK")) {
        //    h = h + "Content-Length: 808\r\n";
        //}
        //System.out.println("rsp.getStatusLine().getProtocolVersion(): "+rsp.getStatusLine().getProtocolVersion());
         //System.out.println("rsp.getStatusLine().getStatusCode(): "+rsp.getStatusLine().getStatusCode());
        return rsp.getStatusLine().getProtocolVersion()+" "+rsp.getStatusLine().getStatusCode()+" " 
                +(rsp.getStatusLine().getStatusCode()==200?"OK":"Not Found")+"\r\n"+h;
    }

    public void sendResponse (int statusCode, String responseString, DataOutputStream outToClient) throws Exception {

        System.out.println("SENDING RESPONSE!!");
        String statusLine = null;
        String serverdetails = "Server: Java HTTPServer\r\n";
        String contentLengthLine = null;
        String contentTypeLine = "Content-Type: text/html\r\n";

        if (statusCode == 200)
            statusLine = "HTTP/1.1 200 OK" + "\r\n";
        else
            statusLine = "HTTP/1.1 404 Not Found" + "\r\n";

        contentLengthLine = "Content-Length: " + responseString.length() + "\r\n";

        System.out.println(statusLine);
        outToClient.writeBytes(statusLine);
        System.out.println(serverdetails);
        outToClient.writeBytes(serverdetails);
        System.out.println(contentTypeLine);
        outToClient.writeBytes(contentTypeLine);
        System.out.println(contentLengthLine);
        outToClient.writeBytes(contentLengthLine);
        outToClient.writeBytes("Connection: close\r\n");
        outToClient.writeBytes("\r\n");

        outToClient.writeBytes(responseString);

        outToClient.close();
    }

    /*
     * 
     */
    public static void main (String args[]) throws Exception {

        String endpointAddress = args[0];
        int endpointPort = Integer.parseInt(args[1]);
        String proxyAddress = args[2];
        int proxyPort = Integer.parseInt(args[3]);
        String graph = null;
        if (args.length>4) {
            graph = args[4];
        }
        (new SingleEndpointProxy2(endpointAddress, endpointPort, proxyAddress, proxyPort, graph)).start();
    }
}

