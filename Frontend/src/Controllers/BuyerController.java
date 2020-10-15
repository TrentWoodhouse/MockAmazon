package Controllers;

import Classes.Message;
import Entities.Response;
import Enums.Status;
import Utils.Global;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;


public class BuyerController extends Controller {

    @Override
    public Response execute(String command) {
        String[] exArr = command.trim().split("\\s+");
        //try {
            switch(command/*exArr[0].toLowerCase()*/) {
                case "menu":
                    return menu();
                case "givefeedback":
                    return giveFeedback();
                case "sendmessage":
                    return sendMessage();
                case "viewmessages":
                    return viewMessages();
                case "search":
                    return searchProducts();
                case "viewcart":
                    return viewCart();
                case "addtocart":
                    return addToCart(Integer.parseInt(exArr[1]));
                default:
                    return super.execute(command);
            }
        /*}
        catch (IndexOutOfBoundsException e) {
            return new Response("Missing parameter " + e.getMessage() + " for command \"" + exArr[0] + "\"", Status.ERROR);
        }
        catch (Exception e) {
            return new Response(e.getMessage(), Status.ERROR);
        }*/
    }

    @Override
    public Response menu() {
        Global.io.print("giveFeedback:\t\t\tgive feedback on a particular listing");
        Global.io.print("sendMessage:\t\t\tsend a message to a particular user");
        Global.io.print("viewMessages:\t\t\tview all messages from a user to you");
        Global.io.print("search:\t\t\t\tsearch available product listings");
        Global.io.print("viewCart:\t\t\tview all items in your cart");
        Global.io.print("addToCart [id]:\t\t\tadd an item to your cart");
        return super.menu();
    }

    public Response giveFeedback() {
        //TODO
        return new Response("You have given feedback. Thanks!");
    }

    public Response sendMessage() {

        String receiver = Global.io.inlineQuestion("Receiver: ");
        String message = Global.io.inlineQuestion("Message: ");

        //make a message based on input (if possible)
        Message m = new Message();
        m.id = 0;
        m.sender = Integer.parseInt(String.valueOf(Global.currUser.id));

        //find the user mentioned
        try {
            String in = Global.sendGet("http://localhost:8080/seller" + "?name=" + receiver);
            int val = 0;
            if (in == null || !in.equals("")) {
                val = Integer.parseInt(String.valueOf(new JSONObject(in).get("id")));
            } else {
                in = Global.sendGet("http://localhost:8080/buyer" + "?name=" + receiver);
                if (in == null || !in.equals("")) {
                    val = Integer.parseInt(String.valueOf(new JSONObject(in).get("id")));
                } else {
                    return new Response("Message Failed to Send (Recipient doesn't exist)");
                }
            }
            m.receiver = val;
            System.out.println("Val=" + val);
        } catch (Exception e){
            System.out.println(e);
        }

        m.message = message;
        m.timeSent = new Date().toString();
        System.out.println(new Gson().toJson(m));

        String inputLine = Global.sendPost("http://localhost:8080/message", new Gson().toJson(m).toString());

        if(inputLine.equals("")){
            return new Response("Message Failed to Send");
        }

        return new Response("Message Successfully Sent");
    }
    public Response viewMessages(){

        return new Response(" - End of Messages - ");
    }

    public Response searchProducts() {
        // TODO
        Global.io.print("Enter a search term or type advancedSearch");
        String term = Global.io.inlineQuestion("");
        ArrayList<JSONObject> listings = new ArrayList<>();

        if (term.toLowerCase() == "advancedsearch") {

        } else {
            try {
                long id = 1;
                System.out.println((Global.sendGet("/listing").getMessage()));
            } catch(Exception e) {
                return new Response("The search was unsuccessful", Status.ERROR);
            }
        }
        return new Response("Found %d products that fit the criteria");
    }

    public Response viewCart() {
        // TODO
        try {
            JSONObject user = new JSONObject(Global.sendGet("/buyer?name=" + Global.currUser.name).getMessage());
            JSONArray cart = user.getJSONArray("cart");

            Global.io.print("Cart\n----------");
            float total = 0;
            if(cart.length() == 0) {
                Global.io.print("Cart is empty");
            } else {
                for (int i = 1; i <= cart.length(); i++) {
                    JSONObject listing = new JSONObject(Global.sendGet("/listing?id=" + cart.getInt(i-1)).getMessage());
                    Global.io.print("Item " + i + ": " + listing.getString("name") + ", " + listing.get("cost"));
                    total += BigDecimal.valueOf(listing.getDouble("cost")).floatValue();
                }
            }
            Global.io.print("----------\nTotal: " + total);
        } catch(Exception e) {
            return new Response("The cart query failed", Status.ERROR);
        }
        return new Response("");
    }

    public Response addToCart(int id) {
        // TODO
        return new Response("Added");
    }
}


