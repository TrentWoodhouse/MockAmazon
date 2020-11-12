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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class SellerController extends Controller {

    @Override
    public Response execute(String command) {
        String[] exArr = command.trim().split("\\s+");
        //Global.io.print(getNotifications().getMessage());
        try {
            switch(exArr[0].toLowerCase()) {
                case "editdescription":
                    return editDescription();
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
                case "getunitssold":
                    return getUnitsSold(exArr.length > 1 ? Integer.parseInt(exArr[1]) : 0);
                case "getsales":
                    return getSales(exArr.length > 1 ? Integer.parseInt(exArr[1]) : 0);
                case "makedeal":
                    return makeDeal(exArr.length > 1 ? Integer.parseInt(exArr[1]) : 0);
                case "viewlistingviews":
                    return viewListingViews(Integer.parseInt(exArr[1]), exArr[2]);
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
        Global.io.print("editDescription:\t\tedit your seller description and add social media links");
        Global.io.print("showListings:\t\t\tshows all your posted listings\n" +
                "createListing:\t\t\tcreate a new listing\n" +
                "editListing [id]:\t\tedit listing with the given id");
        Global.io.print("sendMessage:\t\t\tsend a message to a particular user");
        Global.io.print("viewMessages:\t\t\tview all messages from a user to you");
        Global.io.print("viewReports:\t\t\tview all reports affecting your products");
        Global.io.print("respondToReport:\t\trespond to reports affecting your products");
        Global.io.print("getUnitsSold (id):\t\tgets all units sold from a listing, or all units sold by you if id is left blank");
        Global.io.print("getSales (id):\t\t\tgets total revenue from a listing, or all generated revenue if id is left blank");
        Global.io.print("viewListingViews [id] [type]:\ttype=total: shows all views for a listing\n\t\t\t\t\t\t\t" +
                "type=average: shows average views per day\n\t\t\t\t\t\t\t" +
                "type=percentage: shows what percentage of views result in a sale");
        Global.io.print("makeDeal (id):\t\t\tput listing(s) on sale.");

        return super.menu();
    }

    public Response showListings() {
        Listing[] listings = new Gson().fromJson(Global.sendGet("/listing?seller="+Global.currUser.id).getMessage(), Listing[].class);

        for(Listing l : listings){
            Global.io.print("------------------------------------------");
            Global.io.print("id: " + l.id);
            Global.io.print("Name: " + l.name);
            Global.io.print("Description: " + l.description);
            Global.io.print("Cost: " + l.cost);
            Global.io.print("------------------------------------------");
        }
        return new Response("");
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
            listing.put("orders", new ArrayList<Integer>());
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
            Order order = new Gson().fromJson(Global.sendGet("/order/" + deliveryReport.order).getMessage(), Order.class);
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

    public Response getUnitsSold(int id) {
        try {
            if (id > 0) {
                int unitsSold = 0;
                JSONArray orders = new JSONArray(Global.sendGet("/order").getMessage());
                for (int i = 0; i < orders.length(); i++) {
                    JSONObject order = orders.getJSONObject(i);
                    JSONArray listings = order.getJSONArray("listings");
                    for (int j = 0; j < listings.length(); j++) {
                        if(listings.getInt(j) == id) {
                            unitsSold++;
                            break;
                        }
                    }
                }
                return new Response(unitsSold + " units sold");
            }
            else {
                return new Response("20 units sold");
            }
        }
        catch(Exception e) {
            return new Response("An error occurred: " + e.getMessage(), Status.ERROR);
        }
    }

    public Response getSales(int id) {
        try {
            if (id > 0) {
                int unitsSold = 0;
                JSONArray orders = new JSONArray(Global.sendGet("/order").getMessage());
                for (int i = 0; i < orders.length(); i++) {
                    JSONObject order = orders.getJSONObject(i);
                    JSONArray listings = order.getJSONArray("listings");
                    for (int j = 0; j < listings.length(); j++) {
                        if (listings.getInt(j) == id) {
                            unitsSold++;
                            break;
                        }
                    }
                }
                JSONArray listingArray = new JSONArray(Global.sendGet("/listing?id=" + id).getMessage());
                if (listingArray.length() != 1) {
                    throw new RuntimeException("The listing doesn't exist");
                }
                JSONObject listing = listingArray.getJSONObject(0);
                return new Response("$" + (unitsSold * listing.getDouble("cost")) + " revenue generated");
            }
            else {
                double totalSales = 0;
                JSONArray listings = new JSONArray(Global.sendGet("/listing").getMessage());
                for(int i = 0; i < listings.length(); i++) {
                    if (listings.getJSONObject(i).getInt("seller") == Global.currUser.id) {
                        int unitsSold = 0;
                        JSONArray orders = new JSONArray(Global.sendGet("/order").getMessage());
                        for (int j = 0; j < orders.length(); j++) {
                            JSONObject order = orders.getJSONObject(j);
                            JSONArray listingIdArray = order.getJSONArray("listings");
                            for (int k = 0; k < listingIdArray.length(); k++) {
                                if (listingIdArray.getInt(k) == id) {
                                    unitsSold++;
                                    break;
                                }
                            }
                        }
                        JSONObject listing = listings.getJSONObject(i);
                        totalSales += unitsSold * listing.getDouble("cost");
                    }
                }
                return new Response("$" + totalSales + " revenue generated");
            }
        } catch (Exception e) {
            return new Response("An error occurred: " + e.getMessage(), Status.ERROR);
        }
    }

    public Response makeDeal(int id) {
        try {
            if (id > 0) {
                int percentage = Integer.parseInt(Global.io.question("What percentage should listing " + id + " be sold at? (1-100)"));
                while(percentage < 1 || percentage > 100) {
                    percentage = Integer.parseInt(Global.io.question("Please enter percentage between 1-100."));
                }
                JSONArray listingArray = new JSONArray(Global.sendGet("/listing?id=" + id).getMessage());
                if (listingArray.length() != 1) {
                    throw new RuntimeException("The listing doesn't exist");
                }
                JSONObject listing = listingArray.getJSONObject(0);
                listing.put("salePercentage", percentage / 100.0);
                Global.sendPatch("/listing?id=" + id, listing.toString());
                return new Response("Listing " + id + " is now being sold at " + percentage + "% of its original value.");
            }
            else {
                int percentage = Integer.parseInt(Global.io.question("What percentage should all listings be sold at? (1-100)"));
                while(percentage < 1 || percentage > 100) {
                    percentage = Integer.parseInt(Global.io.question("Please enter percentage between 1-100."));
                }
                JSONObject user = new JSONObject(Global.sendGet("/seller?name=" + Global.currUser.name).getMessage());
                user.put("salePercentage", percentage / 100.0);
                Global.sendPatch("/seller?name=" + Global.currUser.name, user.toString());
                return new Response("All listings are now being sold at " + percentage + "% of their original value.");
            }
        } catch (Exception e) {
            return new Response("An error occurred: " + e.getMessage(), Status.ERROR);
        }
    }

    public Response editDescription() {
        try {
            JSONObject seller = new JSONObject(Global.sendGet("/seller?id=" + Global.currUser.id).getMessage());
            String description = seller.getString("description");
            String partialDescription = description.substring(0, Math.min(seller.getString("description").length(), 30)) + "...";
            String website = seller.getString("website");
            String facebook = seller.getString("facebook");
            String instagram = seller.getString("instagram");

            Global.io.print("Edit description:");
            seller.put("description", Global.io.inlineQuestion("Seller Description [" + partialDescription + "]:", description));
            seller.put("website", Global.io.inlineQuestion("Website" + (website.equals("") ? ": " : " [" + website + "]: "), website));
            seller.put("facebook", Global.io.inlineQuestion("Facebook" + (facebook.equals("") ? ": " : " [" + facebook + "]: "), facebook));
            seller.put("instagram", Global.io.inlineQuestion("Instagram" + (instagram.equals("") ? ": " : " [" + instagram + "]: "), instagram));

            String jsonString = seller.toString();

            return Global.sendPatch("/seller?id=" + Global.currUser.id, jsonString);
        }
        catch(Exception e) {
            return new Response("The seller description cannot be edited: " + e.getMessage(), Status.ERROR);
        }
    }

    public Response viewListingViews(int id, String type) {
        try {
            JSONArray listingArray = new JSONArray(Global.sendGet("/listing?id=" + id).getMessage());
            if (listingArray.length() != 1) {
                throw new RuntimeException("The listing doesn't exist");
            }
            JSONObject listing = listingArray.getJSONObject(0);
            int views = listing.getInt("views");
            String name = listing.getString("name");

            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
            Date dateCreated = sdf.parse(listing.getString("dateCreated"));
            Global.io.print(name);
            switch(type){
                case "total":
                    return new Response(views + " total views", Status.OK);
                case "average":
                    double avg = views / ((double)(((new Date()).getTime() - dateCreated.getTime()) / (1000*60*60*24)) + 1);
                    return new Response(avg + " views per day", Status.OK);
                case "percentage":
                    int unitsSold = 0;
                    JSONArray orders = new JSONArray(Global.sendGet("/order").getMessage());
                    for (int i = 0; i < orders.length(); i++) {
                        JSONObject order = orders.getJSONObject(i);
                        JSONArray listings = order.getJSONArray("listings");
                        for (int j = 0; j < listings.length(); j++) {
                            if(listings.getInt(j) == id) {
                                unitsSold++;
                                break;
                            }
                        }
                    }
                    return new Response((double)unitsSold / views * 100 + "% of views result in a sale", Status.OK);
                default:
                    return new Response("There is no type \"" + type + "\" for viewListingViews. Please use total, average, or percentage", Status.ERROR);
            }
        }
        catch(Exception e) {
            return new Response("There was an error trying to view the listings view info: " + e.getMessage(), Status.ERROR);
        }
    }
}
