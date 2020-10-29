package Controllers;

import Classes.*;
import Entities.Response;
import Enums.Status;
import Utils.Global;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Date;

public class SellerController extends Controller {

    @Override
    public Response execute(String command) {
        String[] exArr = command.trim().split("\\s+");
        //Global.io.print(getNotifications().getMessage());
        try {
            switch(exArr[0].toLowerCase()) {
                case "showlistings":
                    return showListings();
                case "createlisting":
                    return createListing();
                case "editlisting":
                    return editListing(Integer.parseInt(exArr[1]));
                case "sendmessage":
                    return sendMessage();
                case "viewmessages":
                    return viewMessages();
                case "viewreports":
                    return viewReports();
                case "respondtoreport":
                    return respondToReport();
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
        Global.io.print("showListings:\t\t\tshows all your posted listings\n" +
                "createListing:\t\t\tcreate a new listing\n" +
                "editListing [id]:\t\tedit listing with the given id");
        Global.io.print("sendMessage:\t\t\tsend a message to a particular user");
        Global.io.print("viewMessages:\t\t\tview all messages from a user to you");
        Global.io.print("viewReports:\t\t\tview all reports affecting your products");
        Global.io.print("respondToReport:\t\t\trespond to reports affecting your products");

        return super.menu();
    }

    public Response showListings() {
        //TODO
        return new Response("Listing 1\nListing 2");
    }

    public String chooseCategory(String choice) {
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
            return chooseCategory(Global.io.inlineQuestion("Please enter a valid choice: "));
        }
    }

    public Response createListing() {
        JSONObject listing = new JSONObject();

        Global.io.print("Creating a new listing:");
        try {
            listing.put("name", Global.io.inlineQuestion("Name:"));
            listing.put("description", Global.io.inlineQuestion("Description:"));
            Global.io.print("Choose the category that best fits the product by entering one of the following numbers:" +
                    "\n1. Apparel\t\t\t\t\tClothing, Shoes, Accessories, etc." +
                    "\n2. Beauty/Personal Cart\t\tMakeup, Shampoo, Deodorant, etc." +
                    "\n3. Electronics\t\t\t\tComputers, Phones, Cameras, etc." +
                    "\n4. Entertainment\t\t\tMovies, Video Games, Books, etc." +
                    "\n5. Food Products\t\t\tGroceries, Baking supplies, etc." +
                    "\n6. Furniture\t\t\t\tCouches, Desks, Mattresses, etc." +
                    "\n7. Household Products\t\tCleaning supplies, Tools, Dishes, etc." +
                    "\n8. Toys/Games\t\t\t\tBoard Games, Legos, Dolls, etc." +
                    "\n9. Miscellaneous\t\t\tAnything that doesn't fit one of the 8 main categories");
            listing.put("category", chooseCategory(Global.io.inlineQuestion("")));
            listing.put("cost", Global.io.inlineQuestion("Cost:"));
            listing.put("seller", Global.currUser.id);
            listing.put("maxDelivery", Global.io.inlineQuestion("Maximum Delivery time (days-hours):"));
            String jsonString = listing.toString();

            return Global.sendPost("/listing", jsonString);
        }
        catch(Exception e) {
            return new Response("The listing was not created successfully", Status.ERROR);
        }
    }

    public Response editListing(int id) {
        try {
            JSONArray listingArray = new JSONArray(Global.sendGet("/listing?id=" + id).getMessage());
            if (listingArray.length() != 1) {
                throw new RuntimeException("The listing doesn't exist");
            }
            JSONObject listing = listingArray.getJSONObject(0);
            String name = listing.getString("name");
            String fullDescription = listing.getString("description");
            String description = fullDescription.substring(0, Math.min(fullDescription.length(), 30)) + "...";
            String costVal = Double.toString(listing.getDouble("cost"));
            String cost = "$" + costVal;

            Global.io.print("Edit listing:");
            listing.put("name", Global.io.inlineQuestion("Name [" + name + "]:", name));
            listing.put("description", Global.io.inlineQuestion("Description [" + description + "]:", fullDescription));
            listing.put("cost", Global.io.inlineQuestion("Cost [" + cost + "]:", costVal));
            String jsonString = listing.toString();

            return Global.sendPatch("/listing", jsonString);
        }
        catch(Exception e) {
            return new Response("The listing cannot be edited: " + e.getMessage(), Status.ERROR);
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
            Response in = Global.sendGet("/seller?name=" + receiver);
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

    public Response viewReports(){
        try{
            JSONArray deliveryReports = new JSONArray(Global.sendGet("/deliveryReport?seller=" + Global.currUser.id).getMessage());

            for(int i=0 ; i<deliveryReports.length() ; i++){
                JSONObject json = deliveryReports.getJSONObject(i);
                Buyer buyer = new Gson().fromJson(Global.sendGet("/buyer?id=" + json.get("buyer")).getMessage(), Buyer.class);
                JSONArray jsonArray = new JSONArray(Global.sendGet("/listing?id=" + json.get("listing")).getMessage());
                Listing listing = new Gson().fromJson(jsonArray.getJSONObject(0).toString(), Listing.class);
                Global.io.print("---------------------------------------------------------");
                Global.io.print("Report ID: " + json.get("id"));
                Global.io.print("Listing: " + listing.name);
                Global.io.print("Buyer: " + buyer.name);
                Global.io.print("---------------------------------------------------------");
            }

            return new Response("");
        } catch(Exception e){
            e.printStackTrace();
            return new Response("Failed to load reports", Status.ERROR);
        }
    }

    public Response respondToReport(){
        try {
            //gather info to display to the seller
            String reportID = Global.io.inlineQuestion("What report number do you want to respond to? : ");
            JSONArray jsonArray = new JSONArray(Global.sendGet("/deliveryReport?id=" + reportID).getMessage());
            DeliveryReport deliveryReport = new Gson().fromJson(jsonArray.getJSONObject(0).toString(), DeliveryReport.class);
            Buyer buyer = new Gson().fromJson(Global.sendGet("/buyer?id=" + deliveryReport.buyer).getMessage(), Buyer.class);
            Order order = new Gson().fromJson(Global.sendGet("/order?id=" + deliveryReport.order).getMessage(), Order.class);
            jsonArray = new JSONArray(Global.sendGet("/listing?id=" + deliveryReport.listing).getMessage());
            Listing listing = new Gson().fromJson(jsonArray.getJSONObject(0).toString(), Listing.class);

            //display info to the seller
            Global.io.print("-----------------Listing Information---------------------");
            Global.io.print("Name: " + listing.name);
            Global.io.print("Description: " + listing.description);
            Global.io.print("Cost: " + listing.cost);
            Global.io.print("Maximum Delivery Time (days-hours): " + listing.maxDelivery);
            Global.io.print("------------------Order Information----------------------");
            Global.io.print("Maximum Shipping Time: " + order.endDate);
            Global.io.print("-----------------Report Information----------------------");
            Global.io.print("Seller: " + buyer.name);
            Global.io.print("Seller Message: " + deliveryReport.buyerMessage);
            Global.io.print("---------------------------------------------------------");
            Global.io.print("");

            //get and send the response
            deliveryReport.sellerMessage = Global.io.inlineQuestion("Response : ");

            return Global.sendPatch("/deliveryReport", new Gson().toJson(deliveryReport));
        } catch(Exception e){
            e.printStackTrace();
            return new Response("Failed to respond to the report", Status.ERROR);
        }
    }

    public Response getNotifications(){
        try {
            JSONArray deliveryReports = new JSONArray(Global.sendGet("/deliveryReport?seller=" + Global.currUser.id).getMessage());

            if(deliveryReports.length() == 1) return new Response("There is " + deliveryReports.length() + " report to appeal");
            else if(deliveryReports.length() > 1) return new Response("There are " + deliveryReports.length() + " reports to appeal");
            else return new Response("No new Notifications");

        } catch(Exception e){
            return new Response("Failed to load notifications", Status.ERROR);
        }
    }
}
