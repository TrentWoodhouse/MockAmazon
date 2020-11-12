package Controllers;

import Classes.*;
import Entities.Response;
import Enums.Status;
import Utils.Global;
import com.ebay.sdk.ApiContext;
import com.ebay.sdk.ApiCredential;
import com.ebay.sdk.helper.ConsoleUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.*;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.net.URI;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import org.json.JSONArray;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.google.gson.Gson;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class BuyerController extends Controller {
    private static DecimalFormat df = new DecimalFormat("0.00");

    @Override
    public Response execute(String command) {
        String[] exArr = command.trim().split("\\s+");
        try {
            //Global.io.print(getNotifications().getMessage());
            JSONObject user = new JSONObject(Global.sendGet("/buyer?name=" + Global.currUser.name).getMessage());
            switch(exArr[0].toLowerCase()) {
                case "menu":
                    return menu();
                case "givefeedback":
                    return giveFeedback(Integer.parseInt(exArr[1]));
                case "sendmessage":
                    return sendMessage();
                case "viewmessages":
                    return viewMessages();
                case "viewseller":
                    return viewSeller(Integer.parseInt(exArr[1]));
                case "search":
                    return searchProducts();
                case "viewcart":
                    return viewCart();
                case "clearcart":
                    return clearCart();
                case "reportorder":
                    return reportOrder();
                case "recommendation":
                    return getRecommendation();
                case "checkprime":
                    return checkPrimeStatus();
                case "checkrewards":
                    if (getPrimeStatus() == 1) {
                        return checkRewards();
                    } else {
                        return super.execute(command);
                    }
                case "manageprime":
                    return managePrime();
                case "flaglisting":
                    return flagListing();
                case "checkcredibility":
                    return checkCredibility();
                case "vieworders":
                    return viewOrders();
                case "subscribe":
                    return subscribe(Integer.parseInt(exArr[1]));
                case "viewsubscriptions":
                    return viewSubscriptions();
                case "browsinghistory" :
                    return viewBrowsingHistory();
                case "purchasehistory":
                    return viewPurchaseHistory();
                case "compareprices":
                    return comparePrices();
                case "trackshipments":
                    return trackShipments();
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
        Global.io.print("giveFeedback [listingId]:\tgive feedback on a particular listing");
        Global.io.print("viewSeller [sellerId]:\t\tview a seller and open links");
        Global.io.print("sendMessage:\t\t\t\tsend a message to a particular user");
        Global.io.print("viewMessages:\t\t\t\tview all messages from a user to you");
        Global.io.print("search:\t\t\t\t\t\tsearch available product listings");
        Global.io.print("viewCart:\t\t\t\t\tview all items in your cart");
        Global.io.print("clearCart:\t\t\t\t\tempties your cart");
        Global.io.print("reportOrder:\t\t\t\treport one of your active orders");
        Global.io.print("recommendation:\t\t\t\tget a product recommendation");
        Global.io.print("checkPrime:\t\t\t\t\tcheck if you are currently a member of Congo Prime");
        Global.io.print("managePrime:\t\t\t\tregister or unregister for Congo Prime");
        if (getPrimeStatus() == 1) {
            Global.io.print("checkRewards:\t\t\t\tcheck what rewards you have earned as a Congo Prime member");
        }
        Global.io.print("flagListing:\t\t\tflag a listing for breaking Congo policies");
        Global.io.print("checkCredibility:\t\tcheck your standing with the Congo community");
        Global.io.print("viewOrders:\t\t\t\tview the current orders of your account");
        Global.io.print("subscribe:\t\t\t\tsubscribe to a specific item on a regular basis for a reduced price");
        Global.io.print("viewSubscriptions:\t\tview the current subscriptions for your account");
        Global.io.print("browsingHistory:\t\tview the 5 most recent items in your browsing history");
        Global.io.print("purchaseHistory:\t\tview the 5 most recent items in your purchase history");
        Global.io.print("comparePrices:\t\tview top prices of similar listings on other retail sites");
        Global.io.print("trackShipments:\t\tview shipments related to your account.");
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
            JSONObject listing = results.get(i-1);
            Global.io.print("Item " + i + ": " + listing.getString("name"));
        }
        Global.io.print("Found " + results.size() + " products that fit the criteria");
        if (results.size() == 0) {
            return new Response("Search complete");
        }
        Global.io.print("\nType the number of the item you would like to view or hit enter if you don't want to view one");
        String response = Global.io.inlineQuestion("$");
        int choice = 0;
        if (response != "") {
            try {
                choice = Integer.parseInt(response);
            } catch (NumberFormatException e) {
                Global.io.print("Invalid input, returning to menu...");
                return new Response("Nothing added to cart");
            }
            if (choice <= 0 || choice > results.size()) {
                Global.io.print("Invalid input, returning to menu...");
                return new Response("Nothing added to cart");
            } else {
                JSONObject listing = results.get(choice-1);
                listing.put("views", listing.getInt("views") + 1);
                Global.sendPatch("/listing", listing.toString());

                // Update browsing history
                JSONObject user = new JSONObject(Global.sendGet("/buyer?name=" + Global.currUser.name).getMessage());
                JSONArray hist = user.getJSONArray("browsingHistory");
                int[] history = new int[] {0,0,0,0,0};
                for (int i = 0; i < history.length; i++) {
                    history[i] = hist.getInt(i);
                }
                history = updateHistory(history, choice);
                user.remove("browsingHistory");
                user.put("browsingHistory", history);
                Global.sendPatch("/buyer", user.toString());

                double biggestSale = listing.getDouble("salePercentage");
                JSONObject seller = new JSONObject(Global.sendGet("/seller?id=" + listing.getInt("seller")).getMessage());
                if (biggestSale > seller.getDouble("salePercentage")) {
                    biggestSale = seller.getDouble("salePercentage");
                }
                String saleText = biggestSale != 1 ? " (" + Math.round((1 - biggestSale) * 100) + "% off)" : "";
                double salePrice = listing.getDouble("cost")*biggestSale;
                df.setRoundingMode(RoundingMode.DOWN);

                Global.io.print("Name: " + listing.get("name") + "\nDescription: " + listing.get("description") + "\nCost: " + df.format(salePrice) + saleText + "\n\nWould you like to add this item to your cart? (y/n)");
                response = Global.io.inlineQuestion("$");
                if (response.equalsIgnoreCase("y")) {
                    return addToCart(listing.getInt("id"));
                }
            }
        }
        return new Response("Nothing added to cart");
        } catch(Exception e) {
            return new Response("The search was unsuccessful", Status.ERROR);
        }
    }

    /**
     * Helper function to print the cart for normal members
     */
    public float cartPrint(JSONObject user, ArrayList<Integer> cartInt) {
        try {
            JSONArray cart = user.getJSONArray("cart");
            JSONArray orders = user.getJSONArray("orders");

            // checks contents of the cart and displays items and prices, as well as total price
            Global.io.print("Cart\t\tPrice\t\t\tSale\t\t\tFinal\t\t\tItem\n---------------------------------------------------------------------------------------");
            float total = 0;
            float save = 0;
            if (cart.length() == 0) {
                Global.io.print("Cart is empty");
            } else {
                for (int i = 1; i <= cart.length(); i++) {
                    JSONArray arr = new JSONArray(Global.sendGet("/listing?id=" + cart.getInt(i - 1)).getMessage());
                    JSONObject listing = arr.getJSONObject(0);
                    double biggestSale = listing.getDouble("salePercentage");
                    JSONObject seller = new JSONObject(Global.sendGet("/seller?id=" + listing.getInt("seller")).getMessage());
                    if (biggestSale > seller.getDouble("salePercentage")) {
                        biggestSale = seller.getDouble("salePercentage");
                    }
                    double originalPrice = listing.getDouble("cost");
                    double salePrice = originalPrice*biggestSale;
                    String printSale = "";
                    if (biggestSale == 1.0) {
                        printSale = "None";
                    } else {
                        printSale = Double.toString(biggestSale*100) + "%";
                    }
                    cartInt.add(cart.getInt(i - 1));
                    df.setRoundingMode(RoundingMode.DOWN);
                    Global.io.print("Item " + i + ":\t\t$" + originalPrice + "\t\t\t" + printSale + "\t\t\t$" + df.format(salePrice) + "\t\t\t" + listing.get("name"));
                    total += BigDecimal.valueOf(salePrice).floatValue();
                }
                save = BigDecimal.valueOf(total*.15).floatValue();
            }
            System.out.format("---------------------------------------------------------------------------------------\nTotal: $%.2f\n", total);
            System.out.format("\nYou could save $%.2f on this purchase if you join CongoPrime\n\n", save);
            return total;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Helper function to print the cart for Prime members
     */
    public float congoCartPrint(JSONObject user, ArrayList<Integer> cartInt) {
        try {
            JSONArray cart = user.getJSONArray("cart");
            JSONArray orders = user.getJSONArray("orders");
            Global.io.print("Cart\t\tCongo Price\t\tSale\t\t\tFinal\t\t\tItem\n---------------------------------------------------------------------------------------");
            float total = 0;
            float saved = 0;
            if (cart.length() == 0) {
                Global.io.print("Cart is empty");
            } else {
                for (int i = 1; i <= cart.length(); i++) {
                    JSONArray arr = new JSONArray(Global.sendGet("/listing?id=" + cart.getInt(i - 1)).getMessage());
                    JSONObject listing = arr.getJSONObject(0);
                    double biggestSale = listing.getDouble("salePercentage");
                    JSONObject seller = new JSONObject(Global.sendGet("/seller?id=" + listing.getInt("seller")).getMessage());
                    if (biggestSale > seller.getDouble("salePercentage")) {
                        biggestSale = seller.getDouble("salePercentage");
                    }
                    double originalPrice = listing.getDouble("cost");
                    double salePrice = originalPrice*biggestSale;
                    String printSale = "";
                    if (biggestSale == 1.0) {
                        printSale = "None";
                    } else {
                        printSale = Double.toString(biggestSale*100) + "%";
                    }
                    cartInt.add(cart.getInt(i - 1));
                    df.setRoundingMode(RoundingMode.DOWN);
                    Global.io.print("Item " + i + ":\t\t$" + df.format(originalPrice*.85) + "\t\t\t" + printSale + "\t\t\t$" + df.format(salePrice*.85) + "\t\t\t" + listing.get("name"));
                    total += BigDecimal.valueOf(salePrice).floatValue();
                }
                saved = BigDecimal.valueOf(total*.15).floatValue();
                total = BigDecimal.valueOf(total*.85).floatValue();
            }
            System.out.format("---------------------------------------------------------------------------------------\nTotal: $%.2f\n", total);
            System.out.format("\nYou're saving $%.2f on this purchase thanks to CongoPrime\n\n", saved);
            return total;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * displays what is currently in the cart
     */
    public Response viewCart() {
        try {
            JSONObject user = new JSONObject(Global.sendGet("/buyer?name=" + Global.currUser.name).getMessage());
            ArrayList<Integer> cartInt = new ArrayList<>();
            float total;

            // congoPrime
            double thisPurchase = 0;
            double points = user.getDouble("primePoints");
            double rewards = user.getDouble("rewardsCash");

            // checks contents of the cart and displays items and prices, as well as total price
            if (user.getInt("congo") == 0) {
                total = cartPrint(user, cartInt);
            } else {
                total = congoCartPrint(user, cartInt);
            }
            if (total == 0.0) {
                return new Response("Leaving Cart...");
            }
            Global.io.print("Hit the Enter key to return to the main console or \"purchase\" to buy the items in your cart");
            String answer = Global.io.inlineQuestion("$");

            // add to orders for buyer and listings if purchased
            if (answer.toLowerCase().equals("purchase")) {
                for (int i = 0; i < cartInt.size(); i++) {
                    user = new JSONObject(Global.sendGet("/buyer?name=" + Global.currUser.name).getMessage());

                    Listing[] listing = new Gson().fromJson(Global.sendGet("/listing?id="+cartInt.get(i)).getMessage(), Listing[].class);
                    float cred = Float.parseFloat(String.valueOf(user.get("credibility")));
                    float balance = Float.parseFloat(String.valueOf(user.get("balance")));
                    user.remove("credibility");
                    user.remove("balance");
                    balance -= listing[0].cost;
                    cred = (float) Math.min(cred+0.2, 5.0);
                    user.put("credibility", cred);
                    user.put("balance", balance);

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

                    //update purchase history
                    JSONArray hist = user.getJSONArray("purchaseHistory");
                    int[] history = new int[] {0,0,0,0,0};
                    for (int j = 0; j < history.length; j++) {
                        history[j] = hist.getInt(j);
                    }
                    history = updateHistory(history, cartInt.get(i));
                    user.remove("purchaseHistory");
                    user.put("purchaseHistory", history);

                    //get the maximum delivery date
                    DateFormat format = new SimpleDateFormat("dd-hh", Locale.ENGLISH);
                    Date tmpDate = format.parse(listing[0].maxDelivery);
                    Date date = new Date();
                    Calendar c = Calendar.getInstance();
                    c.setTime(date);
                    c.add(Calendar.DATE, tmpDate.getDay());
                    c.add(Calendar.HOUR, tmpDate.getHours());
                    date = c.getTime();

                    //set order fields
                    Order order = new Order();
                    order.id = 0;
                    order.endDate = date.toString();
                    order.listings = new ArrayList<>();
                    order.listings.add(cartInt.get(i));

                    Global.sendPost("/order", new Gson().toJson(order));
                    //buyer.orders.add(orderID);
                    Global.sendPatch("/buyer", user.toString());
                }
                if (user.getInt("congo") == 1) {
                    thisPurchase = BigDecimal.valueOf(total).doubleValue() * .10;
                    points += thisPurchase;
                    rewards += thisPurchase;
                    user.remove("primePoints");
                    user.remove("rewardsCash");
                    user.put("primePoints", points);
                    user.put("rewardsCash", rewards);
                    Global.sendPatch("/buyer", user.toString());
                    System.out.format("You earned %.2f rewards cash on this purchase%n", thisPurchase);
                }
                return new Response("Purchase Successful");
            }
        } catch(Exception e) {
            e.printStackTrace();
            return new Response("The view cart query failed", Status.ERROR);
        }
        return new Response("Leaving cart...");
    }

    public Response clearCart() {
        try {
            JSONObject user = new JSONObject(Global.sendGet("/buyer?name=" + Global.currUser.name).getMessage());
            user.remove("cart");
            user.put("cart", new ArrayList<Integer>());
            Global.sendPatch("/buyer", user.toString());
            return new Response("Cart has been cleared");
        } catch (Exception e) {
            return new Response("Error, Cart not cleared");
        }
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
            return new Response("You have not made any purchases");
        }
        int r = ThreadLocalRandom.current().nextInt(0, options.size());
        JSONObject result = options.get(r);
        try {
            Global.io.print("Item:\t\t\t" + result.getString("name") +
                    "\nDescription:\t" + result.getString("description") +
                    "\nPrice:\t\t\t" + result.getDouble("cost"));

            // Update browsing history
            JSONObject user = new JSONObject(Global.sendGet("/buyer?name=" + Global.currUser.name).getMessage());
            JSONArray hist = user.getJSONArray("browsingHistory");
            int[] history = new int[] {0,0,0,0,0};
            for (int i = 0; i < history.length; i++) {
                history[i] = hist.getInt(i);
            }
            history = updateHistory(history, result.getInt("id"));
            user.remove("browsingHistory");
            user.put("browsingHistory", history);
            Global.sendPatch("/buyer", user.toString());

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

            // Update browsing history
            JSONObject user = new JSONObject(Global.sendGet("/buyer?name=" + Global.currUser.name).getMessage());
            JSONArray hist = user.getJSONArray("browsingHistory");
            int[] history = new int[] {0,0,0,0,0};
            for (int i = 0; i < history.length; i++) {
                history[i] = hist.getInt(i);
            }
            history = updateHistory(history, result.getInt("id"));
            user.remove("browsingHistory");
            user.put("browsingHistory", history);
            Global.sendPatch("/buyer", user.toString());

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

    public Response checkPrimeStatus() {
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

    public int getPrimeStatus() {
        try {
            JSONObject user = new JSONObject(Global.sendGet("/buyer?name=" + Global.currUser.name).getMessage());
            int congo = user.getInt("congo");
            return congo;
        } catch (Exception e) {
            return 0;
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

    /**
     * checks to see if the user is a member of Congo Prime
     */
    public Response checkRewards() {
        try {
            JSONObject user = new JSONObject(Global.sendGet("/buyer?name=" + Global.currUser.name).getMessage());
            double points = user.getDouble("primePoints");
            double rewards = user.getDouble("rewardsCash");
            System.out.format("You currently have $%.2f in rewards cash%n%nYou have earned a total of $%.2f to date%n", rewards, points);
            return new Response("");
        } catch (Exception e) {
            return new Response("Failed to fetch rewards");
        }
    }

    //TODO Finish This
    public Response subscribe(int id) {
        // see if item id exists
        try {
            JSONArray arr = new JSONArray(Global.sendGet("/listing?id=" + id).getMessage());
            JSONObject listing = arr.getJSONObject(0);
        } catch (Exception e) {
            return new Response("There is no item with that ID");
        }
        String ans = Global.io.inlineQuestion("How frequently would you like this item? (Enter the corresponding number)" +
                "\n1. Once per week\n2. Once per month\n3. Once per year\n");
        int freq = 0;
        String frequency = "";
        try {
            freq = Integer.parseInt(ans);
            if (freq == 1) {
                frequency = "week";
            } else if (freq == 2) {
                frequency = "month";
            } else if (freq == 3) {
                frequency = "year";
            } else {
                return new Response("Subscription not added due to invalid entry, returning to menu...");
            }
        } catch (Exception e) {
            return new Response("Subscription not added due to invalid entry, returning to menu...");
        }
        try {
            JSONObject user = new JSONObject(Global.sendGet("/buyer?name=" + Global.currUser.name).getMessage());
            JSONArray subscriptions = user.getJSONArray("subscriptions");
            ArrayList<JSONObject> newSubs = new ArrayList<>();
            for (int i = 0; i < subscriptions.length(); i++) {
                newSubs.add(subscriptions.getJSONObject(i));
            }
            JSONObject newSub = new JSONObject();
            newSub.put("id", id);
            newSub.put("frequency", frequency);

            Date date = new Date();
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            if (freq == 1) {
                c.add(Calendar.DAY_OF_YEAR, 7);
                newSub.put("discount", .99);
            } else if (freq == 2) {
                c.add(Calendar.MONTH, 1);
                newSub.put("discount", .97);
            } else if (freq == 3) {
                c.add(Calendar.YEAR, 1);
                newSub.put("discount", .95);
            }
            date = c.getTime();

            newSub.put("next", date.toString());

            newSubs.add(newSub);

            user.remove("subscriptions");
            user.put("subscriptions", newSubs);

            Global.sendPatch("/buyer", user.toString());

            return new Response("Subscription successfully added");
        } catch (Exception e){
            return new Response("Failed to add subscription");
        }
    }

    //TODO Test This
    public Response viewSubscriptions() {
        try {
            JSONObject user = new JSONObject(Global.sendGet("/buyer?name=" + Global.currUser.name).getMessage());
            JSONArray subs = user.getJSONArray("subscriptions");
            if (subs.length() == 0) {
                Global.io.print("You have no current subscriptions");
            } else {
                Global.io.print("Current Subscriptions: ");
                Global.io.print("------------------------------------------------------------------------------------");
                for (int i = 0; i < subs.length(); i++) {
                    JSONObject sub = subs.getJSONObject(i);
                    JSONArray arr = new JSONArray(Global.sendGet("/listing?id=" + sub.get("id")).getMessage());
                    JSONObject listing = arr.getJSONObject(0);
                    double biggestSale = listing.getDouble("salePercentage");
                    JSONObject seller = new JSONObject(Global.sendGet("/seller?id=" + listing.getInt("seller")).getMessage());
                    if (biggestSale > seller.getDouble("salePercentage")) {
                        biggestSale = seller.getDouble("salePercentage");
                    }
                    double price = listing.getDouble("cost")*biggestSale;

                    if (user.getInt("congo") == 1) {
                        price = price*.85;
                    }
                    price = price * sub.getDouble("discount");
                    Global.io.print("Item:\t\t\t" + sub.get("name")
                            + "\nPrice:\t\t\t" + price + "(" + ((1-sub.getDouble("discount"))*100) + "% discount)"
                            + "\nFrequency:\t\tOnce per " + sub.get("frequency")
                            + "\nNext Order Date:\t" + sub.get("next")
                            + "\n------------------------------------------------------------------------------------");
                }
            }
            return new Response("\nReturning to menu...");
        } catch (Exception e) {
            return new Response("Failed to fetch current subscriptions");
        }
    }

    /**
     * Shifts everything in the given history int array to the left (discarding the last one) and inserts the toAdd int
     *  into index 0
     */
    public int[] updateHistory(int[] history, int toAdd) {
        int[] ret = history;
        for (int i = ret.length-2; i >= 0; i--) {
            ret[i+1] = ret[i];
        }
        ret[0] = toAdd;
        return ret;
    }

    public Response viewBrowsingHistory() {
        try {
            JSONObject user = new JSONObject(Global.sendGet("/buyer?name=" + Global.currUser.name).getMessage());
            JSONArray history = user.getJSONArray("browsingHistory");
            Global.io.print("Browsing History (Most recent first):");
            if (history.getInt(0) == 0) {
                Global.io.print("No items in browsing history");
            } else {
                for (int i = 0; i < history.length(); i++) {
                    if (history.getInt(i) == 0) {
                        break;
                    } else {
                        JSONArray arr = new JSONArray(Global.sendGet("/listing?id=" + history.get(i)).getMessage());
                        JSONObject listing = arr.getJSONObject(0);
                        Global.io.print((i + 1) + ") " + listing.get("name"));
                    }
                }
            }
        } catch (Exception e) {
            return new Response("Failed to get browsing history");
        }
        return new Response("\nReturning to menu...");
    }

    public Response viewPurchaseHistory() {
        try {
            JSONObject user = new JSONObject(Global.sendGet("/buyer?name=" + Global.currUser.name).getMessage());
            JSONArray history = user.getJSONArray("purchaseHistory");
            Global.io.print("Purchase History (Most recent first):");
            if (history.getInt(0) == 0) {
                Global.io.print("No items in purchase history");
            } else {
                for (int i = 0; i < history.length(); i++) {
                    if (history.getInt(i) == 0) {
                        break;
                    } else {
                        JSONArray arr = new JSONArray(Global.sendGet("/listing?id=" + history.get(i)).getMessage());
                        JSONObject listing = arr.getJSONObject(0);
                        Global.io.print((i + 1) + ") " + listing.get("name"));
                    }
                }
            }
        } catch (Exception e) {
            return new Response("Failed to get purchase history");
        }
        return new Response("\nReturning to menu...");
    }

    public Response reportOrder(){
        try{

            //get the objects to fill the report
            String orderID = Global.io.inlineQuestion("What order number do you want to report? : ");
            Order order = new Gson().fromJson(Global.sendGet("/order/"+orderID).getMessage(), Order.class);
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

    public Response viewOrders(){
        Buyer buyer = new Gson().fromJson(Global.sendGet("/buyer?id="+Global.currUser.id).getMessage(), Buyer.class);

        for(int i : buyer.orders){
            Order order = new Gson().fromJson(Global.sendGet("/order/"+i).getMessage(), Order.class);
            Global.io.print("------------------------------------------");
            Global.io.print("id: " + order.id);
            Global.io.print("End Date: " + order.endDate);
            Global.io.print("Listings: " + order.listings);
            Global.io.print("------------------------------------------");
        }
        return new Response("");
    }

    public static Response getNotifications(){
        try {
            JSONArray deliveryReports = new JSONArray(Global.sendGet("/deliveryReport?buyer=" + Global.currUser.id).getMessage());
            if(deliveryReports.length() > 0) return new Response(deliveryReports.length() + " reports have been resolved");
            else return new Response("No new Notifications");
        } catch(Exception e){
            return new Response("Failed to load notifications", Status.ERROR);
        }
    }

    public Response viewSeller(int id) {
        try {
            JSONObject seller = new JSONObject(Global.sendGet("/seller?id=" + id).getMessage());
            String description = seller.getString("description");
            String website = seller.getString("website");
            String facebook = seller.getString("facebook");
            String instagram = seller.getString("instagram");

            Global.io.print("View Seller");
            Global.io.print("====================================");
            if(!description.isEmpty()){
                Global.io.print(description);
            }
            else {
                Global.io.print("This seller has no description.");
            }
            if(!website.isEmpty()){
                if (Global.io.inlineQuestion("Navigate to seller's website? (yes/no)").equals("yes")) {
                    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                        Desktop.getDesktop().browse(new URI(website));
                    }
                    else {
                        Global.io.print(website);
                    }
                }
            }
            if(!facebook.isEmpty()){
                if (Global.io.inlineQuestion("Navigate to seller's facebook? (yes/no)").equals("yes")) {
                    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                        Desktop.getDesktop().browse(new URI(facebook));
                    }
                    else {
                        Global.io.print(facebook);
                    }
                }
            }
            if(!instagram.isEmpty()){
                if (Global.io.inlineQuestion("Navigate to seller's instagram? (yes/no)").equals("yes")) {
                    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                        Desktop.getDesktop().browse(new URI(instagram));
                    }
                    else {
                        Global.io.print(instagram);
                    }
                }
            }
            return new Response("Exiting view seller");
        }
        catch(Exception e) {
            return new Response("An error occurred viewing seller " + id + "'s info: " + e.getMessage(), Status.ERROR);
        }
    }



    public Response comparePrices(){
        try {
            Document doc = Jsoup.connect("https://www.ebay.com/globaldeals").userAgent("Mozilla/17.0").get();
            Elements tmp = doc.select("dne-itemtile-detail");

            for(Element card : tmp){

            }
        } catch(Exception e){
            e.printStackTrace();
        }

        return new Response("");
    }

    public Response trackShipments(){
        try {
            Buyer buyer = new Gson().fromJson(Global.sendGet("/buyer?id="+Global.currUser.id).getMessage(), Buyer.class);
            JSONObject json;

            for(int i : buyer.orders){
                Order order = new Gson().fromJson(Global.sendGet("/order/"+i).getMessage(), Order.class);
                if(order.shipmentID != null) json = getPackageData(order.shipmentID);
                else continue;
                //json = getPackageData("1Z933R1A1314152653");

                JSONObject jsonDetail = json.getJSONArray("trackDetails").getJSONObject(0);
                JSONObject jsonTracking = jsonDetail.getJSONArray("shipmentProgressActivities").getJSONObject(0);

                Global.io.print("------------------------------------------");
                Global.io.print("id: " + order.id);
                Global.io.print("Progress Percent: " + jsonDetail.getString("progressBarPercentage"));
                Global.io.print("Shipment Status: " + jsonDetail.getString("progressBarType"));

                Global.io.print("");
                if(jsonDetail.getString("progressBarType").equals("Delivered")){
                    Global.io.print("Delivered time: " + jsonDetail.getString("deliveredDate") + " at " + jsonDetail.getString("deliveredTime"));
                    Global.io.print("Left At: " + jsonDetail.getString("leftAt"));
                    Global.io.print("------");
                    Global.io.print("Last Tracked Data:");
                    Global.io.print("Location: " + jsonTracking.getString("location"));
                    Global.io.print("Time: " + jsonTracking.getString("date") + " at " + jsonTracking.getString("time"));
                    Global.io.print("Note: " + jsonTracking.getString("activityScan"));
                } else {
                    Global.io.print("Expected Delivery time: " + jsonDetail.getString("scheduledDeliveryDate") + " at " + jsonDetail.getString("scheduledDeliveryTime"));
                    Global.io.print("To Be Left At: " + jsonDetail.getString("leaveAt"));
                    Global.io.print("------");
                    Global.io.print("Last Tracked Data:");
                    Global.io.print("Location: " + jsonTracking.getString("location"));
                    Global.io.print("Time: " + jsonTracking.getString("date") + " at " + jsonTracking.getString("time"));
                    Global.io.print("Note: " + jsonTracking.getString("activityScan"));
                }

                Global.io.print("------------------------------------------");
            }

            Global.io.print("- End of Results -");
        } catch(Exception e){
            e.printStackTrace();
        }

        return new Response("");
    }

    private static ApiContext getApiContext() throws IOException {

        String input;
        ApiContext apiContext = new ApiContext();

        //set Api Token to access eBay Api Server
        ApiCredential cred = apiContext.getApiCredential();
        input = ConsoleUtil.readString("Enter your eBay Authentication Token: ");


        cred.seteBayToken(input);

        //set Api Server Url
        //input = "https://api.ebay.com/wsapi";
        input = "https://api.sandbox.ebay.com/wsapi";

        apiContext.setApiServerUrl(input);

        return apiContext;
    }

    private JSONObject getPackageData(String packageID){
        try{
            //send a get request to get verification data (in cookies)
            URL url = new URL("https://www.ups.com/track/api/Track/GetStatus");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            conn.connect();
            Map<String, List<String>> headerFields = conn.getHeaderFields();
            List<String> cookiesHeader = headerFields.get("Set-Cookie");
            String s = cookiesHeader.get(2).split(";|\\=")[1];
            in.close();


            //send a post with the requested tracking number
            String json = "{\"TrackingNumber\": [\"" + packageID + "\"]}";
            byte[] rawData = json.getBytes(StandardCharsets.UTF_8);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setInstanceFollowRedirects(false);
            conn.setConnectTimeout(5000);
            conn.setRequestMethod("POST");
            conn.setRequestProperty( "Content-Type", "application/json" );
            conn.addRequestProperty( "X-XSRF-TOKEN", "CfDJ8Jcj9GhlwkdBikuRYzfhrpLRf1xiYNvFzxiDFNTZnPX5LNVJiMIGFNe0WZfZl9er6Y0gNLny1j-4AikY7dbDEbOcRtAYubkNCwCNk6I34KKQcOhG_cNu3YZSMoaZKjGlbtEUDzWSoRSOEDybvzyJWMI");
            conn.setRequestProperty( "Content-Length", String.valueOf(rawData.length));
            conn.setRequestProperty( "Accept", "application/json");
            String string = "";
            for(String str : headerFields.get("Set-Cookie")) string += str;
            string = "ak_bmsc=FD79A496705FE5015CF87E04D1D5D5F9172A9E2C096F00002469AD5F2C0EFF28~plN7gWLr5RJd7jF4tv9zTHKad1gx0u2ABwOVCjJ7BEfqIvTcu0/tEgEqCXBDlLm2a/X18Mmni5JhL8QbPqqCiWMjeMCteWPvDtH4f+9XuCNTNflT+JY6+Kv8VFO1Xg0OK7HHS7+0YFTFTwCTqYeuyeDEq3cHkGAxhfB9eh/f6FiwWDKfKHNuehw+R4ZIJ8RvBWNLeBDyDmfJWDnyLqvWMLvM+T9rCZKAHkwCFqNz37sUM=; bm_sz=864D6A84804D84028E2263117A37FB9C~YAAQLJ4qF2mh5Lp1AQAAXLZivQmDaNTgJsilTefXxzIewyLUNDnD99fKKDbWGsoJr7IqQy3CXSJfPZeLk6EV4wj7Zs9qOBuj3UIV1mYM6TqsaZz8YLYyAo4s2hnPwYgWQP6nBLFG55PtvlF0uc8dih1quRSHxfW02b+nKh2KiT3QL1r16S3bXcMyXJls; X-CSRF-TOKEN=CfDJ8Jcj9GhlwkdBikuRYzfhrpKtYqzpGV0H7A9PC4S2FNaOc3f8ZxJZNGHpwhZO7yEhECHyV0qsdxKO7erWCFN_Dy_-cgPlk_B1THIsXcfsHOV_2W8CdwvGMaaR5QQhjt1bL65EIQ1g9RL36arJKeDyPS4; X-XSRF-TOKEN-ST=CfDJ8Jcj9GhlwkdBikuRYzfhrpJ9LsVqx1-Dr3dj6u4ZBic_YEdqLv_jCrD2POQZ4DSfxksfO3-X5Pvsl9mfYQTAjeXMIBMjcWU7IpPbdD_ImuGhdRb_ggpDyQvWrXNITCorLu0b9qUJ9XrKBDe7cVY89W0; _abck=F26C7EF97C98E3BD18D314B6F661729F~-1~YAAQLJ4qF9oJ5bp1AQAAKx+QvQRBJAaC+yPIPnKtMBTI+Bb636HzhdckiiV65OVHeetI5QLmutG355T+Eh/CkFRT7kFvbSDf33fqcQykvLuYfhsd01bZVs7O9F7nk5QJT7kEw767U6Lmh/KS+fKN9nMU76BQJ9bHrlCMacy7iO274ySz93CXr2wfzFdlRj2Ra0EPhRc23nDdDusKPq4il6HVSGwtESGCwF9BDCc5FMtpv3SBtalYJmMtPtnikHghtn6445ihvl61/QsYlOVwYQk5zxHHgdjxJJxS52xp2/Uz3jiVvNo+vsyhLJQlj3wrgZ00NA==~0~-1~-1";
            conn.setRequestProperty( "Cookie", string);
            conn.connect();
            OutputStream os = conn.getOutputStream();
            os.write(rawData);
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            os.close();

            //receive the json from the post
            String inputLine = in.readLine();
            in.close();
            conn.disconnect();

            return new JSONObject(inputLine);
        } catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
