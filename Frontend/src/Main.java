import Classes.User;
import Controllers.Controller;
import Entities.Response;
import Enums.UserType;
import Utils.*;
import Controllers.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.gson.Gson;
import org.json.JSONObject;

public class Main {
    public static void main(String[] args) throws IOException {
        //Initialize routes
        Global.router.addPath("admin", new AdminController());
        Global.router.addPath("seller", new SellerController());
        Global.router.addPath("buyer", new BuyerController());



        boolean authenticated = false;
        UserType userType = UserType.BUYER;
        while(!authenticated) {
            String response = Global.io.question("Are you a [buyer], a [seller], or an [admin]?");
            for (UserType u : UserType.values()) {
                if (u.name().toLowerCase().equals(response.toLowerCase())) {
                    userType = u;
                    String name = Global.io.inlineQuestion(" Username:");
                    String pass = Global.io.inlineQuestion(" Password");

                    //Add an HTTP connection
                    try {
                        URL url = new URL("http://localhost:8080/user?name="+name);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");     //insecure, I know
                        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        String inputLine = in.readLine();
                        in.close();

                        //create a json object, and verify the password
                        JSONObject json = new JSONObject(inputLine);
                        if(json.get("password").equals(pass)){
                            authenticated = true;
                            Global.currUser = new Gson().fromJson(inputLine, User.class);
                        } else {
                            Global.io.error("Incorrect Password");
                        }

                    } catch(Exception e){
                        //System.out.println(e);
                        Global.io.error("Incorrect Username");
                    }
                }
            }
        }

        switch(userType) {
            case BUYER:
                Global.router.goTo("buyer");
                break;
            case SELLER:
                Global.router.goTo("seller");
                break;
            case ADMIN:
                Global.router.goTo("admin");
                break;
            default:
                return;
        }

        Controller userController = Global.router.getController();

        Global.io.print("You have logged in as [" + userType.name().toLowerCase() + "].");
        Global.io.print("Type the command \"menu\" to see all actions that can be performed");
        while(true) {
            String input = Global.io.prompt();
            Response response = userController.execute(input);
            switch(response.getStatus()) {
                case OK:
                    Global.io.print(response.getMessage());
                    break;
                case NOACTION:
                    break;
                case ERROR:
                default:
                    Global.io.error(response.getMessage());
            }
        }
    }
}
