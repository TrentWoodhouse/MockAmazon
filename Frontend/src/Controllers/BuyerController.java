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
                    return giveFeedback();
                case "sendmessage":
                    return sendMessage();
                case "viewmessages":
                    return viewMessages();
                case "search":
                    return searchProducts();
                case "viewcart":
                    return viewCart();
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
        Global.io.print("giveFeedback:\t\t\tgive feedback on a particular listing");
        Global.io.print("sendMessage:\t\t\tsend a message to a particular user");
        Global.io.print("viewMessages:\t\t\tview all messages from a user to you");
        Global.io.print("search:\t\t\t\tsearch available product listings");
        Global.io.print("viewCart:\t\t\tview all items in your cart");
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

    /**
     * Helper function to search the JSONArray for matches
     * @param listings
     * @param term
     * @return
     */
    public ArrayList<JSONObject> search(JSONArray listings, String term) {
        ArrayList<JSONObject> results = new ArrayList<>();
        try {
            for (int i = 0; i < listings.length(); i++) {
                JSONObject temp = listings.getJSONObject(i);
                if (temp.getString("name").toLowerCase().contains(term) || temp.getString("description").toLowerCase().contains(term)) {
                    results.add(listings.getJSONObject(i));
                }
            }
            return results;
        } catch(Exception e) {
            return null;
        }
    }

    /**
     * Function to search for products based on certain criteria
     * @return
     */
    public Response searchProducts() {
        Global.io.print("Enter a search term or type \"advancedSearch\"");
        String term = Global.io.inlineQuestion("$").toLowerCase();
        JSONArray listings;
        ArrayList<JSONObject> results = new ArrayList<>();
        try {
            listings = new JSONArray(Global.sendGet("/listing?all=xyz").getMessage());

        if (term.equalsIgnoreCase("advancedsearch")) {
            Global.io.print("Enter one of the following: \"Price less than x\" or \"Price greater than x\"");
            String advanced = Global.io.inlineQuestion("$");
            Global.io.print("Now enter your x (what the prices should be less than or greater than)");
            String criteria = Global.io.inlineQuestion("$");
            Global.io.print("Now enter a search term");
            term = Global.io.inlineQuestion("$");
            JSONArray validListings = new JSONArray();
            if (advanced.equalsIgnoreCase("price less than x")) {
                // get all listings w/ price lower than x
                for (int i = 0; i < listings.length(); i++) {
                    if (BigDecimal.valueOf(listings.getJSONObject(i).getDouble("cost")).floatValue() <= Float.parseFloat(criteria)) {
                           validListings.put(listings.getJSONObject(i));
                    }
                }
            } else {
                for (int i = 0; i < listings.length(); i++) {
                    if (BigDecimal.valueOf(listings.getJSONObject(i).getDouble("cost")).floatValue() >= Float.parseFloat(criteria)) {
                        validListings.put(listings.getJSONObject(i));
                    }
                }
            }

            results = search(validListings, term);
        } else {
            results = search(listings, term);
        }
        for (int i = 1; i<=results.size(); i++) {
            Global.io.print("Item " + i + ": " + results.get(i-1).getString("name") + ", Description: " + results.get(i-1).getString("description") + ", Cost: " + results.get(i-1).get("cost"));
        }
        Global.io.print("Found " + results.size() + " products that fit the criteria");
        Global.io.print("Would you like to add one of these products to the cart? (y/n)");
        String response = Global.io.inlineQuestion("$");
        if (response.equalsIgnoreCase("y")) {
            Global.io.print("Which one? Enter the number");
            String number = Global.io.inlineQuestion("$");
<<<<<<< HEAD
            addToCart(results.get(Integer.parseInt(number)-1).getInt("id"));
=======
            addToCart(results.get(Integer.parseInt(number)).getInt("id"));
>>>>>>> 1578950d928adbb1a91c522094ff7d21f55f8650
        }
        return new Response("");
        } catch(Exception e) {
            return new Response("The search was unsuccessful", Status.ERROR);
        }
    }

    /**
     * displays what is currently in the cart
     * @return
     */
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

                }
            }
        } catch(Exception e) {
            return new Response("The view cart query failed", Status.ERROR);
        }
        return new Response("Left cart...");
    }

    /**
     * adds an item to the users cart
     *  Implemented by the search function so as to avoid confusion with the listing id's
     * @param id
     * @return
     */
    public Response addToCart(int id) {
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
<<<<<<< HEAD
            return Global.sendPatch("/listing", user.toString());
        } catch(Exception e) {
            return new Response("The add to cart query failed", Status.ERROR);
        }
=======
            Global.sendPatch("/buyer?name=" +Global.currUser.name, user.toString());
        } catch(Exception e) {
            return new Response("The add to cart query failed", Status.ERROR);
        }
        return new Response("");
>>>>>>> 1578950d928adbb1a91c522094ff7d21f55f8650
    }
}
