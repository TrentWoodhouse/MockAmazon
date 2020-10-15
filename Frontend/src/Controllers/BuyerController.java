package Controllers;

import Classes.Message;
import Entities.Response;
import Enums.Status;
import Utils.Global;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Date;
import org.json.JSONArray;
import java.math.BigDecimal;
import java.util.ArrayList;
import com.google.gson.Gson;


public class BuyerController extends Controller {

    @Override
    public Response execute(String command) {
        String[] exArr = command.trim().split("\\s+");
        try {
            switch(exArr[0].toLowerCase()) {
                case "menu":
                    return menu();
                case "givefeedback":
                    return giveFeedback(Integer.parseInt(exArr[1]));
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
        }
        catch (IndexOutOfBoundsException e) {
            return new Response("Missing parameter " + e.getMessage() + " for command \"" + exArr[0] + "\"", Status.ERROR);
        }
        catch (Exception e) {
            return new Response(e.getMessage(), Status.ERROR);
        }
    }

    @Override
    public Response menu() {
        Global.io.print("giveFeedback [listingId]: give feedback on a particular listing");
        Global.io.print("sendMessage:\t\t\tsend a message to a particular user");
        Global.io.print("viewMessages:\t\t\tview all messages from a user to you");
        Global.io.print("search:\t\t\t\t\tsearch available product listings");
        Global.io.print("viewCart:\t\t\t\tview all items in your cart");
        Global.io.print("addToCart [id]:\t\t\tadd an item to your cart");
        return super.menu();
    }

    public Response giveFeedback(int id) {
        try {
            JSONArray listingArray = new JSONArray(Global.sendGet("/listing?id=" + id).getMessage());
            if (listingArray.length() != 1) {
                throw new RuntimeException("The listing doesn't exist");
            }
            JSONObject listing = listingArray.getJSONObject(0);
            JSONObject rating = new JSONObject();

            String name = listing.getString("name");
            String fullDescription = listing.getString("description");
            String description = fullDescription.substring(0, Math.min(fullDescription.length(), 30)) + "...";
            String cost = "$" + Double.toString(listing.getDouble("cost"));

            Global.io.print(new String[]{name, description, cost});
            Global.io.print("Enter your feedback:");
            rating.put("title", Global.io.inlineQuestion("Name:"));
            rating.put("message", Global.io.inlineQuestion("Description:"));
            rating.put("ratedVal", Global.io.inlineQuestion("Rated value (1.0 - 5.0):"));

            return Global.sendPost("/rating", rating.toString());
        }
        catch(Exception e) {
            return new Response("The listing cannot be given a rating: " + e.getMessage(), Status.ERROR);
        }
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
            Response in = Global.sendGet("/seller?name="+ receiver);
            int val = 0;
            if (!(in.getStatus() == Status.ERROR)) {        //they are a seller
                val = Integer.parseInt(String.valueOf(new JSONObject(in.getMessage()).get("id")));
            } else {                                        //they are a buyer or nonexistent
                in = Global.sendGet("/buyer?name=" + receiver);
                if (!(in.getStatus() == Status.ERROR)) {    //they are a buyer
                    val = Integer.parseInt(String.valueOf(new JSONObject(in.getMessage()).get("id")));
                } else {                                    //they are nonexistent
                    return new Response("Message Failed to Send (Recipient doesn't exist)");
                }
            }
            m.receiver = val;
            System.out.println("Val=" + val);
        } catch (JSONException e){
            return new Response("Message Failed to Send");
        }

        m.message = message;
        m.timeSent = new Date().toString();

        Response inputLine = Global.sendPost("/message", new Gson().toJson(m));

        if(inputLine.getStatus() == Status.ERROR){
            return new Response("Message Failed to Send");
        }

        return new Response("Message Successfully Sent");
    }
    public Response viewMessages(){

        Response inputLine = Global.sendGet("/message?id="+Global.currUser.id);
        //Get an array of messages (jsonArray)
        JSONArray jsonArray;
        try {
            jsonArray = new JSONArray(inputLine.getMessage());

            //send each message
            for(int i=0 ; i<jsonArray.length() ; i++){
                JSONObject m = jsonArray.getJSONObject(i);

                String sender = new JSONObject(Global.sendGet("/user?id=" + m.get("sender")).getMessage()).getString("name");
                String receiver = new JSONObject(Global.sendGet("/user?id=" + m.get("receiver")).getMessage()).getString("name");

                System.out.println("-------------------------------------------------------");
                System.out.println("Time Sent: " + m.get("timeSent"));
                System.out.println("Sender:    " + sender);
                System.out.println("Receiver:  " + receiver);
                System.out.println("");
                System.out.println("Message:   " + m.get("message"));
                System.out.println("-------------------------------------------------------");
            }

        } catch (Exception e){
            System.out.println(e);
            return new Response("Failed to Load Messages");
        }

        return new Response(" - End of Messages - ");
    }

    public ArrayList<JSONObject> search(ArrayList<JSONObject> listings, String term) {
        ArrayList<JSONObject> results = new ArrayList<>();

        return results;
    }

    public Response searchProducts() {
        // TODO if buyer wants to add an item to cart, simply pass item id to addToCart()
        Global.io.print("Enter a search term or type \"advancedSearch\"");
        String term = Global.io.inlineQuestion("$");
        ArrayList<JSONObject> listings = new ArrayList<>();
        ArrayList<JSONObject> results = new ArrayList<>();
        try {

        } catch(Exception e) {
            return new Response("The search was unsuccessful", Status.ERROR);
        }

        if (term.toLowerCase() == "advancedsearch") {
            Global.io.print("Enter one of the following: \"Price less than x\", \"Price greater than x\", or \"Rating greater than x\"");
            String advanced = Global.io.inlineQuestion("$");
            Global.io.print("Now enter a search term");
            term = Global.io.inlineQuestion("$");
            ArrayList<JSONObject> validListings = new ArrayList<>();
            switch(advanced.toLowerCase()) {
                case "price less than x":
                    // get all listings w/ price lower than x
                    //BigDecimal.valueOf()
                    results = search(validListings, term);
                case "price greater than x":

                case "rating greater than x":

            }
        } else {
            results = search(listings, term);
        }
        return new Response("Found " + results.size() + " products that fit the criteria");
    }

    public Response viewCart() {
        try {
            JSONObject user = new JSONObject(Global.sendGet("/buyer?name=" + Global.currUser.name).getMessage());
            JSONArray cart = user.getJSONArray("cart");
            ArrayList<Integer> cartInt = new ArrayList<>();
            JSONArray orders = user.getJSONArray("orders");

            // checks contents of the cart and displays items and prices, as well as total price
            Global.io.print("Cart\n----------");
            float total = 0;
            if(cart.length() == 0) {
                Global.io.print("Cart is empty");
            } else {
                for (int i = 1; i <= cart.length(); i++) {
                    JSONObject listing = new JSONObject(Global.sendGet("/listing?id=" + cart.getInt(i-1)).getMessage());
                    cartInt.add(cart.getInt(i-1));
                    Global.io.print("Item " + i + ": " + listing.getString("name") + ", $" + listing.get("cost"));
                    total += BigDecimal.valueOf(listing.getDouble("cost")).floatValue();
                }
            }
            Global.io.print("----------\nTotal: $" + total + "\n");
            Global.io.print("Hit the Enter key to return to the main console or \"purchase\" to buy the items in your cart");
            String answer = Global.io.inlineQuestion("$");

            // add to orders for buyer and listings if purchased
            if (answer.toLowerCase().equals("purchase")) {
                for (int i = 0; i < cartInt.size(); i++) {
                    // TODO Update Orders for buyer and listings
                }
            }
        } catch(Exception e) {
            return new Response("The view cart query failed", Status.ERROR);
        }
        return new Response("Left cart...");
    }

    public Response addToCart(int id) {
        // TODO
        try {
            JSONObject user = new JSONObject(Global.sendGet("/buyer?name=" + Global.currUser.name).getMessage());
            JSONArray cart = user.getJSONArray("cart");

            // recreates the cart as an ArrayList of Integers
            ArrayList<Integer> newCart = new ArrayList<>();
            for (int i = 0; i < cart.length(); i++) {
                newCart.add(cart.getInt(i));
            }
            newCart.add(id);

            user.remove("cart");
            user.put("cart", newCart);
            // TODO Replace the old cart w/ the new
        } catch(Exception e) {
            return new Response("The add to cart query failed", Status.ERROR);
        }
        return new Response("R U HERE");
    }
}
