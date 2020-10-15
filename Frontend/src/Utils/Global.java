package Utils;

import Classes.User;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Global {
    public static IO io = new IO();
    public static Router router = new Router();
    public static User currUser;

    public static String sendPost(String urlString, String json){
        try {
            URL url = new URL(urlString);
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
                return "";
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine = in.readLine();
            //System.out.println(inputLine);
            in.close();

            return inputLine;
        } catch(Exception e){
            return "";
        }
    }

    public static String sendGet(String urlString){
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");     //insecure, I know
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine = in.readLine();
            in.close();

            return inputLine;
        } catch(Exception e){
            return "";
        }
    }
}
