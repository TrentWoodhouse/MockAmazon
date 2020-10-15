import Classes.Buyer;
import Classes.User;
import Controllers.Controller;
import Entities.Response;
import Enums.UserType;
import Utils.*;
import Controllers.*;
import Utils.Global;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import com.google.gson.Gson;
import org.json.JSONObject;

public class Main {
    public static void main(String[] args) throws IOException {
        //Initialize routes
        Global.router.addPath("admin", new AdminController());
        Global.router.addPath("seller", new SellerController());
        Global.router.addPath("buyer", new BuyerController());


        //Intro!
        System.out.println("                                                                                ");
        System.out.println("                                      %%%%%                                     ");
        System.out.println("                                   %%%%%%%%%%%                                  ");
        System.out.println("                             ######### %%% #########                            ");
        System.out.println("                           #############(#############                          ");
        System.out.println("                          ##############(##############                         ");
        System.out.println("                      ((((((((( ### ((((((((( ### (((((((((                     ");
        System.out.println("                   ((((((((((((((#(((((((((((((#(((((((((((((                   ");
        System.out.println("               %%%%%%   ((((  %%%%%%  (((((  %%%%%%  ((((   %%%%%               ");
        System.out.println("            %%%%%%%%%%%#   %%%%%%%%%%%%   #%%%%%%%%%%%  %%%%%%%%%%%%            ");
        System.out.println("      %%%%%%%%%%   %%%%%%%%%%      %%%%%%%%%      %%%%%%%%%     %%%%%#%%%%%     ");
        System.out.println("   %%%            %%          %% %%%         %%  %%         %% %%          %%%  ");
        System.out.println("  /%*            %%            %%%            %,%%           .%%             %% ");
        System.out.println("  %%             %%            %%%             %%             %%             %% ");
        System.out.println("   %%             %%          %%%%             %%%           %%%%          %%%  ");
        System.out.println("     %%%%%%%%%%%   #%%%%%%%%%%% %%             %  %%%%%%%%%%% %% %%%%%%%%%%%    ");
        System.out.println("                                                              %%                ");
        System.out.println("                                                             *%%                ");
        System.out.println("                                                 %%%        %%%                 ");
        System.out.println("                                                    %%%%%%%%%                   ");
        System.out.println("                                                                                ");
        System.out.println("                                Welcome to Congo!                               ");
        System.out.println("                                                                                ");

        boolean authenticated = false;
        UserType userType = null;
        while(!authenticated) {
            String response = Global.io.question("Are you a [buyer], a [seller], an [admin], or [new]?");
            if(response.equals("new")){     //make a new account
                String inputLine = null;
                do {
                    String name = Global.io.inlineQuestion("New Username: ");
                    String password = Global.io.inlineQuestion("New Password: ");
                    String role = Global.io.inlineQuestion("New Role (buyer or seller): ");

                    User tmp;
                    if (role.equals("buyer")) {
                        tmp = new Buyer(new ArrayList<Integer>());
                        tmp.id = 4;
                        tmp.name = name;
                        tmp.password = password;
                    } else {
                        tmp = new User();
                        tmp.id = 4;
                        tmp.name = name;
                        tmp.password = password;
                    }


                    if(role.equals("buyer") || role.equals("seller")) {

                        try {
                            inputLine = Global.sendPost("/user", new Gson().toJson(tmp).toString()).getMessage();

                            inputLine = Global.sendGet("/user?name=" + name).getMessage();
                            JSONObject json = new JSONObject(inputLine);
                            long id = json.getLong("id");
                            tmp.id = id;

                            inputLine = Global.sendPost("/" + role, new Gson().toJson(tmp).toString()).getMessage();

                        } catch(Exception e){
                            continue;
                        }

                        if (inputLine.equals("Successfully Sent User") || inputLine.equals("Successfully Sent Buyer") || inputLine.equals("Successfully Sent Seller")) {
                            Global.io.print("Successfully Created Your Account!");

                        } else {
                            Global.io.error("Failed to Create Your Account... Please Try Again");
                        }
                    }  else {
                        Global.io.error("Please Enter 'buyer' or 'seller' For a Role");
                    }
                } while(inputLine != null && !inputLine.equals("Successfully Sent User") && !inputLine.equals("Successfully Sent Buyer") && !inputLine.equals("Successfully Sent Seller"));

            } else {                        //log in an existing account
                for (UserType u : UserType.values()) {
                    if (u.name().toLowerCase().equals(response.toLowerCase())) {
                        userType = u;
                    }
                }
                if(userType != null){
                    String name = Global.io.inlineQuestion("Username: ");
                    String pass = Global.io.inlineQuestion("Password: ");

                    //Add an HTTP connection
                    try {
                        String inputLine = Global.sendGet("/"+ response +"?name=" + name).getMessage();

                        //create a json object, and verify the password
                        JSONObject json = new JSONObject(inputLine);
                        if (json.get("password").equals(pass)) {
                            authenticated = true;
                            Global.currUser = new Gson().fromJson(inputLine, User.class);
                        } else {
                            Global.io.error("Incorrect Password");
                        }

                    } catch (Exception e) {
                        //System.out.println(e);
                        Global.io.error("Incorrect Username");
                    }
                } else {
                    Global.io.error(" Please enter 'buyer', 'seller', 'admin', or 'new'");
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

        Global.io.print("You have logged in as " + Global.currUser.name + ".");
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
