package Utils;

import Classes.User;
import Entities.Response;
import Enums.Status;
import sun.net.www.protocol.https.HttpsURLConnectionImpl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Global {
    public static IO io = new IO();
    public static Router router = new Router();
    public static User currUser;
    public static String apiHost = "http://localhost:8080";
    public static Response sendPost(String route, String json){
        try {
            URL url = new URL(apiHost + route);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            byte[] out = json.getBytes(StandardCharsets.UTF_8);
            connection.setFixedLengthStreamingMode(out.length);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.connect();
            try {
                OutputStream os = connection.getOutputStream();
                os.write(out);
            } catch (Exception e) {
                return new Response(e.getMessage(), Status.ERROR);
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine = in.readLine();
            //System.out.println(inputLine);
            in.close();

            return new Response(inputLine);
        } catch(Exception e){
            return new Response(e.getMessage(), Status.ERROR);
        }
    }

    public static Response sendPatch(String route, String json){
        try {
            URL url = new URL(apiHost + route);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            final Object target;
            if (connection instanceof HttpsURLConnectionImpl) {
                final Field delegate = HttpsURLConnectionImpl.class.getDeclaredField("delegate");
                delegate.setAccessible(true);
                target = delegate.get(connection);
            } else {
                target = connection;
            }
            final Field f = HttpURLConnection.class.getDeclaredField("method");
            f.setAccessible(true);
            f.set(target, "PATCH");

            connection.setDoOutput(true);

            byte[] out = json.getBytes(StandardCharsets.UTF_8);
            connection.setFixedLengthStreamingMode(out.length);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.connect();
            try {
                OutputStream os = connection.getOutputStream();
                os.write(out);
            } catch (Exception e) {
                return new Response(e.getMessage(), Status.ERROR);
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine = in.readLine();
            //System.out.println(inputLine);
            in.close();

            return new Response(inputLine);
        } catch(Exception e){
            return new Response(e.getMessage(), Status.ERROR);
        }
    }

    public static Response sendGet(String route){
        try {
            URL url = new URL(apiHost + route);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");     //insecure, I know
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine = in.readLine();
            in.close();

            if(inputLine == null) return new Response("", Status.ERROR);
            return new Response(inputLine);
        } catch(Exception e){
            return new Response(e.getMessage(), Status.ERROR);
        }
    }

    public static Response sendDelete(String route){
        try {
            URL url = new URL(apiHost + route);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("DELETE");     //insecure, I know
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine = in.readLine();
            in.close();

            if(inputLine == null) return new Response("", Status.ERROR);
            return new Response(inputLine);
        } catch(Exception e){
            return new Response(e.getMessage(), Status.ERROR);
        }
    }
}
