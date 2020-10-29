package Controllers;

import Classes.*;
import Entities.Response;
import Enums.Status;
import Utils.Global;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import org.json.JSONArray;
import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;

import com.google.gson.Gson;


public class BuyerController extends Controller {

    @Override
    public Response execute(String command) {
        String[] exArr = command.trim().split("\\s+");
        try {
            //Global.io.print(getNotifications().getMessage());
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
                case "reportorder":
                    return reportOrder();
                case "recommendation":
                    return getRecommendation();
                case "checkprime":
                    return getPrimeStatus();
                case "manageprime":
                    return managePrime();
                case "flaglisting":
                    return flagListing();
                case "checkcredibility":
                    return checkCredibility();
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
        Global.io.print("reportOrder:\t\t\treport one of your active orders");
        Global.io.print("recommendation:\t\t\tget a product recommendation");
        Global.io.print("checkPrime:\t\t\t\tcheck if you are currently a member of Congo Prime");
        Global.io.print("managePrime:\t\t\tregister or unregister for Congo Prime");
        Global.io.print("flagListing:\t\t\tflag a listing for breaking Congo policies");
        Global.io.print("checkCredibility:\t\tcheck your standing with the Congo community");
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
            rating.put("title", Global.io.inlineQuestion("Title:"));
            rating.put("message", Global.io.inlineQuestion("Message:"));
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

    /**
     * Helper function to search the JSONArray for matches
     */
    public ArrayList<JSONObject> search(JSONArray listings, String term) {
        ArrayList<JSONObject> results = new ArrayList<>();
        try {
            if (term == "") {
                for (int i = 0; i < listings.length(); i++) {
                    results.add(listings.getJSONObject(i));
                }
                return results;
            } else {
                for (int i = 0; i < listings.length(); i++) {
                    JSONObject temp = listings.getJSONObject(i);
                    if (temp.getString("name").toLowerCase().contains(term) || temp.getString("description").toLowerCase().contains(term)) {
                        results.add(listings.getJSONObject(i));
                    }
                }
                return results;
            }
        } catch(Exception e) {
            return null;
        }
    }

    public JSONArray categorySearch() {
        JSONArray ret = new JSONArray();
        Global.io.print("Choose on of the following: (Enter the corresponding number)" +
                "\n1. Apparel\t\t\t\t\tClothing, Shoes, Accessories, etc." +
                "\n2. Beauty/Personal Cart\t\tMakeup, Shampoo, Deodorant, etc." +
                "\n3. Electronics\t\t\t\tComputers, Phones, Cameras, etc." +
                "\n4. Entertainment\t\t\tMovies, Video Games, Books, etc." +
                "\n5. Food Products\t\t\tGroceries, Baking supplies, etc." +
                "\n6. Furniture\t\t\t\tCouches, Desks, Mattresses, etc." +
                "\n7. Household Products\t\tCleaning supplies, Tools, Dishes, etc." +
                "\n8. Toys/Games\t\t\t\tBoard Games, Legos, Dolls, etc." +
                "\n9. Miscellaneous\t\t\tAnything that doesn't fit one of the 8 main categories");
        String choice = getCategory(Global.io.inlineQuestion(""));
        ArrayList<JSONObject> r = getAllOfCategory(choice);
        try {
            for (int i = 0; i < r.size(); i++) {
                    ret.put(r.get(i));
            }
        } catch (Exception e) {

        }
        return ret;
    }

    /**
     * Helper function to get search results when buyer chooses advanced search
     */
    public ArrayList<JSONObject> advancedSearch(JSONArray listings, String term) {
        JSONArray validListings = new JSONArray();
        try {
            Global.io.print("Enter one of the following (Enter the corresponding number):" +
                    "\n1.Price less than x" +
                    "\n2.Price greater than x" +
                    "\n3.Category");
            String advanced = Global.io.inlineQuestion("$");
            if (advanced.equals("3")) {
                validListings = categorySearch();
                Global.io.print("Now enter a search term");
                term = Global.io.inlineQuestion("$");
            } else {
                Global.io.print("Now enter your x (what the prices should be less than or greater than)");
                String criteria = Global.io.inlineQuestion("$");
                Global.io.print("Now enter a search term");
                term = Global.io.inlineQuestion("$");
                if (advanced.equals("1")) {
                    // get all listings w/ price lower than x
                    for (int i = 0; i < listings.length(); i++) {
                        if (BigDecimal.valueOf(listings.getJSONObject(i).getDouble("cost")).floatValue() <= Float.parseFloat(criteria)) {
                            validListings.put(listings.getJSONObject(i));
                        }
                    }
                } else if (advanced.equals("2")){
                    for (int i = 0; i < listings.length(); i++) {
                        if (BigDecimal.valueOf(listings.getJSONObject(i).getDouble("cost")).floatValue() >= Float.parseFloat(criteria)) {
                            validListings.put(listings.getJSONObject(i));
                        }
                    }
                }
            }
        } catch (JSONException e) {

        }
        return search(validListings, term);
    }

    /**
     * Function to search for products based on certain criteria
     */
    public Response searchProducts() {
        Global.io.print("Enter a search term or type \"advancedSearch\"");
        String term = Global.io.inlineQuestion("$").toLowerCase();
        JSONArray listings;
        ArrayList<JSONObject> results = new ArrayList<>();
        try {
            listings = new JSONArray(Global.sendGet("/listing?all=xyz").getMessage());

        if (term.equalsIgnoreCase("advancedsearch")) {
            results = advancedSearch(listings, term);
        } else {
            results = search(listings, term);
        }
        for (int i = 1; i<=results.size(); i++) {
            Global.io.print("Item " + i + ": " + results.get(i-1).getString("name") + ", Description: " + results.get(i-1).getString("description") + ", Cost: " + results.get(i-1).get("cost"));
        }
        Global.io.print("Found " + results.size() + " products that fit the criteria");
        if (results.size() == 0) {
            return new Response("Search complete");
        }
        Global.io.print("Would you like to add one of these products to the cart? (y/n)");
        String response = Global.io.inlineQuestion("$");
        if (response.equalsIgnoreCase("y")) {
            Global.io.print("Which one? Enter the number");
            String number = Global.io.inlineQuestion("$");
            return addToCart(results.get(Integer.parseInt(number)-1).getInt("id"));
        }
        return new Response("Nothing added to cart");
        } catch(Exception e) {
            return new Response("The search was unsuccessful", Status.ERROR);
        }
    }

    /**
     * displays what is currently in the cart
     */
    public Response viewCart() {
        try {
            JSONObject user = new JSONObject(Global.sendGet("/buyer?name=" + Global.currUser.name).getMessage());
            JSONArray cart = user.getJSONArray("cart");
            ArrayList<Integer> cartInt = new ArrayList<>();
            JSONArray orders = user.getJSONArray("orders");

            // checks contents of the cart and displays items and prices, as well as total price
            Global.io.print("Cart\t\tPrice\t\t\tItem\n-------------------------------------------------------------------");
            float total = 0;
            if(cart.length() == 0) {
                Global.io.print("Cart is empty");
            } else {
                for (int i = 1; i <= cart.length(); i++) {
                    JSONArray arr = new JSONArray(Global.sendGet("/listing?id=" + cart.getInt(i-1)).getMessage());
                    JSONObject listing = arr.getJSONObject(0);
                    cartInt.add(cart.getInt(i-1));
                    Global.io.print("Item " + i + ":\t\t$" + listing.get("cost") + "\t\t\t" + listing.getString("name"));
                    total += BigDecimal.valueOf(listing.getDouble("cost")).floatValue();
                }
            }
            Global.io.print("-------------------------------------------------------------------\nTotal: $" + total + "\n");
            Global.io.print(user.toString());
            Global.io.print("Hit the Enter key to return to the main console or \"purchase\" to buy the items in your cart");
            String answer = Global.io.inlineQuestion("$");

            // add to orders for buyer and listings if purchased
            if (answer.toLowerCase().equals("purchase")) {
                Buyer buyer = new Gson().fromJson(user.toString(), Buyer.class);
                for (int i = 0; i < cartInt.size(); i++) {
                    Listing[] listing = new Gson().fromJson(Global.sendGet("/listing?id="+cartInt.get(i)).getMessage(), Listing[].class);
                    buyer.credibility = (float) Math.min(buyer.credibility+0.2, 5.0);
                    buyer.balance -= listing[0].cost;

                    // add order to buyer
                    ArrayList<Integer> newCart = new ArrayList<>();
                    ArrayList<Integer> newOrders = new ArrayList<>();
                    JSONArray add = user.getJSONArray("categories");
                    ArrayList<Integer> newCategories = new ArrayList<>();
                    for (int j = 0; j < add.length(); j++) {
                        newCategories.add(add.getInt(j));
                    }
                    String category = new JSONArray(Global.sendGet("/listing?id="+cartInt.get(i)).getMessage()).getJSONObject(0).getString("category");
                    int index = getIndex(category);
                    newCategories.set(index, newCategories.get(index)+1);
                    for (int j = 0; j < user.getJSONArray("orders").length(); j++) {
                        newOrders.add(user.getJSONArray("orders").getInt(j));
                    }
                    newOrders.add(cartInt.get(i));
                    user.remove("cart");
                    user.remove("orders");
                    user.put("cart", newCart);
                    user.put("orders", newOrders);
                    user.remove("categories");
                    user.put("categories", newCategories);

                    //get the maximum delivery date
                    DateFormat format = new SimpleDateFormat("dd-hh", Locale.ENGLISH);
                    Date tmpDate = format.parse(listing[0].maxDelivery);
                    Date date = new Date();
                    Calendar c = Calendar.getInstance();
                    c.setTime(date);
                    c.add(Calendar.DATE, tmpDate.getDay());
                    c.add(Calendar.HOUR, tmpDate.getHours());
                    date = c.getTime();

                    Order order = new Order();
                    order.id = 0;
                    order.endDate = date.toString();
                    order.listings = new ArrayList<>();
                    order.listings.add(cartInt.get(i));

                    Global.sendPatch("/buyer", user.toString()).getMessage();
                    Global.sendPost("/order", new Gson().toJson(order));
                }
                Global.sendPatch("/buyer", new Gson().toJson(buyer));
                return new Response("Purchase Successful");
            }
        } catch(Exception e) {
            e.printStackTrace();
            return new Response("The view cart query failed", Status.ERROR);
        }
        return new Response("Left cart...");
    }

    /**
     * adds an item to the users cart
     *  Implemented by the search function so as to avoid confusion with the listing id's
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
            Global.sendPatch("/buyer", user.toString());
            return new Response("Item added to your cart");
        } catch(Exception e) {
            return new Response("The add to cart query failed", Status.ERROR);
        }
    }

    /**
     * Helper function to pick a recommendation category
     */
    public String weightedCategoryPicker() {
        try {
            JSONArray add = new JSONObject(Global.sendGet("/buyer?name=" + Global.currUser.name).getMessage()).getJSONArray("categories");
            ArrayList<Integer> categories = new ArrayList<>();
            int total = 0;
            for (int j = 0; j < add.length(); j++) {
                categories.add(add.getInt(j));
                total += add.getInt(j);
            }
            int r = ThreadLocalRandom.current().nextInt(1, total+1);
            for (int j = 0; j < categories.size(); j++) {
                r = r - categories.get(j);
                if (r <= 0) {
                    return getCategory(Integer.toString(j+1));
                }
            }
        } catch (Exception e) {
            return "";
        }
        return "";
    }

    /**
     * Helper function that implements a recommendation based on previous purchase history
     */
    public Response getPurchaseRecommendation(){
        ArrayList<JSONObject> options = getAllOfCategory(weightedCategoryPicker());
        if (options.size() == 0) {
            return new Response("There are no items available in that category");
        }
        int r = ThreadLocalRandom.current().nextInt(0, options.size());
        JSONObject result = options.get(r);
        try {
            Global.io.print("Item:\t\t\t" + result.getString("name") +
                    "\nDescription:\t" + result.getString("description") +
                    "\nPrice:\t\t\t" + result.getDouble("cost"));
            String choice = Global.io.inlineQuestion("Would you like to add this item to you cart? (y/n)\n");
            if (choice.equalsIgnoreCase("y")) {
                return addToCart(result.getInt("id"));
            } else {
                return new Response("Item was not added to the cart");
            }
        } catch (Exception e) {
            return new Response("Error generating recommendation");
        }
    }

    /**
     * Helper function that converts the numerical choice into its corresponding category
     */
    public String getCategory(String choice) {
        if (choice.equals("1")) {
            return "apparel";
        } else if (choice.equals("2")) {
            return "beauty/personal care";
        } else if (choice.equals("3")) {
            return "electronics";
        } else if (choice.equals("4")) {
            return "entertainment";
        } else if (choice.equals("5")) {
            return "food products";
        } else if (choice.equals("6")) {
            return "furniture";
        } else if (choice.equals("7")) {
            return "household products";
        } else if (choice.equals("8")) {
            return "toys/games";
        } else if (choice.equals("9")) {
            return "miscellaneous";
        } else {
            return getCategory(Global.io.inlineQuestion("Please enter a valid choice: "));
        }
    }

    /**
     * Helper function that converts the category into its corresponding number
     */
    public int getIndex(String choice) {
        if (choice.equals("apparel")) {
            return 0;
        } else if (choice.equals("beauty/personal care")) {
            return 1;
        } else if (choice.equals("electronics")) {
            return 2;
        } else if (choice.equals("entertainment")) {
            return 3;
        } else if (choice.equals("food products")) {
            return 4;
        } else if (choice.equals("furniture")) {
            return 5;
        } else if (choice.equals("household products")) {
            return 6;
        } else if (choice.equals("toys/games")) {
            return 7;
        } else {
            return 8;
        }
    }

    /**
     * Returns an ArrayList of JSONObjects, where the contents are all of the Objects that match the category field
     *  provided
     */
    public ArrayList<JSONObject> getAllOfCategory(String category) {
        ArrayList<JSONObject> results = new ArrayList<>();
        try {
            JSONArray listings = new JSONArray(Global.sendGet("/listing?all=xyz").getMessage());
            for (int i = 0; i < listings.length(); i++) {
                JSONObject temp = listings.getJSONObject(i);
                if (temp.getString("category").equalsIgnoreCase(category)) {
                    results.add(listings.getJSONObject(i));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }

    /**
     * Helper function that implements a recommendation based on a chosen category
     */
    public Response getCategoryRecommendation() {
        Global.io.print("Choose on of the following: (Enter the corresponding number)" +
                "\n1. Apparel\t\t\t\t\tClothing, Shoes, Accessories, etc." +
                "\n2. Beauty/Personal Cart\t\tMakeup, Shampoo, Deodorant, etc." +
                "\n3. Electronics\t\t\t\tComputers, Phones, Cameras, etc." +
                "\n4. Entertainment\t\t\tMovies, Video Games, Books, etc." +
                "\n5. Food Products\t\t\tGroceries, Baking supplies, etc." +
                "\n6. Furniture\t\t\t\tCouches, Desks, Mattresses, etc." +
                "\n7. Household Products\t\tCleaning supplies, Tools, Dishes, etc." +
                "\n8. Toys/Games\t\t\t\tBoard Games, Legos, Dolls, etc." +
                "\n9. Miscellaneous\t\t\tAnything that doesn't fit one of the 8 main categories");
        String choice = getCategory(Global.io.inlineQuestion(""));
        ArrayList<JSONObject> options = getAllOfCategory(choice);
        if (options.size() == 0) {
            return new Response("There are no items available in that category");
        }
        int r = ThreadLocalRandom.current().nextInt(0, options.size());
        JSONObject result = options.get(r);
        try {
            Global.io.print("Item:\t\t\t" + result.getString("name") +
                    "\nDescription:\t" + result.getString("description") +
                    "\nPrice:\t\t\t" + result.getDouble("cost"));
            choice = Global.io.inlineQuestion("Would you like to add this item to you cart? (y/n)\n");
            if (choice.equalsIgnoreCase("y")) {
                return addToCart(result.getInt("id"));
            } else {
                return new Response("Item was not added to the cart");
            }
        } catch (Exception e) {
            return new Response("Error generating recommendation");
        }
    }

    /**
     * Gets a recommendation, either baser on previous purchases or based on a chose category
     */
    public Response getRecommendation() {
        try {
            JSONObject buyer = new JSONObject(Global.sendGet("/buyer?id=" + Global.currUser.id));
            String choice = Global.io.inlineQuestion("Chose one of the following: (Enter the corresponding number)" +
                    "\n1.Get a recommendation based on previous purchases" +
                    "\n2.Get a recommendation by choosing a category\n");
            if (choice.equals("1")) {
                return getPurchaseRecommendation();
            } else {
                return getCategoryRecommendation();
            }
        } catch (Exception e) {
            return new Response("Failed to get recommendation");
        }
    }

    public Response getPrimeStatus() {
        try {
            JSONObject user = new JSONObject(Global.sendGet("/buyer?name=" + Global.currUser.name).getMessage());
            int congo = user.getInt("congo");
            if (congo == 0 ) {
                return new Response("You are not currently a Congo Prime member");
            } else {
                return new Response("You are a Congo Prime member");
            }
        } catch (Exception e) {
            return new Response("Failed to fetch Prime status");
        }
    }

    /**
     * Allows the user to register or unregister for Congo Prime
     */
    public Response managePrime() {
        try {
            JSONObject user = new JSONObject(Global.sendGet("/buyer?name=" + Global.currUser.name).getMessage());
            int congo = user.getInt("congo");
            if (congo == 0) {
                String choice = Global.io.inlineQuestion("Would you like to register for Congo Prime? (y/n)\n");
                if (choice.equalsIgnoreCase("y")) {
                    congo = 1;
                    user.remove("congo");
                    user.put("congo", congo);
                    Global.sendPatch("/buyer", user.toString());
                    return new Response("You have been registered for Congo Prime");
                } else {
                    return new Response("You have not been registered for Congo Prime");
                }
            } else {
                String choice = Global.io.inlineQuestion("Would you like to unregister for Congo Prime? (y/n)\n");
                if (choice.equalsIgnoreCase("y")) {
                    congo = 0;
                    user.remove("congo");
                    user.put("congo", congo);
                    Global.sendPatch("/buyer", user.toString());
                    return new Response("You have been unregistered for Congo Prime");
                } else {
                    return new Response("You have not been unregistered for Congo Prime");
                }
            }
        } catch (Exception e) {
            return new Response("Failed to properly manage Prime registration");
        }
    }

    public Response reportOrder(){
        try{

            //get the objects to fill the report
            String orderID = Global.io.inlineQuestion("What order number do you want to report? : ");
            Order order = new Gson().fromJson(Global.sendGet("/order?id="+orderID).getMessage(), Order.class);
            JSONArray jsonArray = new JSONArray(Global.sendGet("/listing?id=" + order.listings.get(0)).getMessage());
            Listing listing = new Gson().fromJson(jsonArray.getJSONObject(0).toString(), Listing.class);

            //if order is overdue, automatically refund
            DateFormat format = new SimpleDateFormat("EEE MMM dd hh:mm:ss z yyyy", Locale.ENGLISH);
            Date date = format.parse(order.endDate);
            if(new Date().after(date)){
                Buyer buyer = new Gson().fromJson(Global.sendGet("/buyer?id="+Global.currUser.id).getMessage(), Buyer.class);
                Seller seller = new Gson().fromJson(Global.sendGet("/seller?id="+listing.seller).getMessage(), Seller.class);

                buyer.balance += listing.cost;
                seller.balance -= listing.cost;
                Global.sendPatch("/buyer", new Gson().toJson(buyer));
                Global.sendPatch("/seller", new Gson().toJson(seller));
                Global.sendDelete("/order?id=" + order.id);

                return new Response("The order is overdue, a refund has been deposited in your account");
            }

            //create the basis for the report
            DeliveryReport report = new DeliveryReport();
            report.id = 0;
            report.buyer = Global.currUser.id;
            report.seller = listing.seller;
            report.listing = order.listings.get(0);
            report.order = order.id;
            report.date = new Date().toString();
            report.buyerMessage = Global.io.inlineQuestion("Report Message: ");
            report.sellerMessage = "";
            report.appealed = false;

            String json = new Gson().toJson(report);
            return Global.sendPost("/deliveryReport", json);
        } catch(Exception e){
            return new Response("Failed to create report", Status.ERROR);
        }
    }

    public Response flagListing(){
        //make the flag
        Flag flag = new Flag();
        flag.id = 0;
        flag.user = Global.currUser.id;
        String listingName = Global.io.inlineQuestion("What listing do you want to report? (give the name) - ");

        if(new Gson().fromJson(Global.sendGet("/user?id=" + Global.currUser.id).getMessage(), User.class).credibility < 2.0) return new Response("Your credibility is too low to flag", Status.ERROR);

        try{
            //get the listing id
            Listing[] listing = new Gson().fromJson(Global.sendGet("/listing?name=" + listingName).getMessage(), Listing[].class);

            flag.listing = listing[0].id;
            flag.reason = Global.io.inlineQuestion("Why should this listing be removed? - ");

            //send the flag
            return Global.sendPost("/flag", new Gson().toJson(flag));
        } catch(Exception e){
            return new Response("Failed to flag listing", Status.ERROR);
        }
    }

    public Response checkCredibility(){
        float cred = new Gson().fromJson(Global.sendGet("/user?id=" + Global.currUser.id).getMessage(), User.class).credibility;
        return new Response("Your Credibility is:" + cred);
    }

    public Response getNotifications(){
        try {
            JSONArray deliveryReports = new JSONArray(Global.sendGet("/deliveryReport?buyer=" + Global.currUser.id).getMessage());
            if(deliveryReports.length() > 0) return new Response(deliveryReports.length() + " reports have been resolved");
            else return new Response("No new Notifications");
        } catch(Exception e){
            return new Response("Failed to load notifications", Status.ERROR);
        }
    }
}
