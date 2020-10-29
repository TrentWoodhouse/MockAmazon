package Controllers;

import Classes.*;
import Entities.Response;
import Enums.Status;
import Utils.Global;
import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONObject;

public class AdminController extends Controller {

    @Override
    public Response execute(String command) {
        String[] exArr = command.trim().split("\\s+");
        try {
            switch(exArr[0].toLowerCase()) {
                case "menu":
                    return menu();
                case "viewreports":
                    return viewReports();
                case "reviewreport":
                    return reviewReport();
                case "flaglisting":
                    return flagListing();
                case "listlistings":
                    return listListings();
                case "listflags":
                    return listFlags();
                case "getmostflags":
                    return getMostFlags();
                case "reviewflags":
                    return reviewFlags();
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
        Global.io.print("viewReports:\t\t\tview all active reports");
        Global.io.print("reviewReport:\t\t\tresolve active reports");
        Global.io.print("flagListing:\t\t\tflag a listing for breaking Congo policies");
        Global.io.print("listListings:\t\t\tdisplay all listings");
        Global.io.print("listFlags:\t\t\tlist all flags for a given listing");
        Global.io.print("getMostFlags:\t\t\tdisplay the most flagged listing's flags");
        Global.io.print("reviewFlags:\t\t\treview a given listings flags to determine its fate");
        return super.menu();
    }

    public Response viewReports(){
        try{
            JSONArray deliveryReports = new JSONArray(Global.sendGet("/deliveryReport?admin=xyz").getMessage());

            //display all reports involving an admin (older than a day, or with a seller message)
            for(int i=0 ; i<deliveryReports.length() ; i++){
                JSONObject json = deliveryReports.getJSONObject(i);
                Buyer buyer = new Gson().fromJson(Global.sendGet("/buyer?id=" + json.get("buyer")).getMessage(), Buyer.class);
                Seller seller = new Gson().fromJson(Global.sendGet("/seller?id=" + json.get("seller")).getMessage(), Seller.class);
                JSONArray jsonArray = new JSONArray(Global.sendGet("/listing?id=" + json.get("listing")).getMessage());
                Listing listing = new Gson().fromJson(jsonArray.getJSONObject(0).toString(), Listing.class);
                Global.io.print("---------------------------------------------------------");
                Global.io.print("Report ID: " + json.get("id"));
                Global.io.print("Listing: " + listing.name);
                Global.io.print("Buyer: " + buyer.name);
                Global.io.print("Seller: " + seller.name);
                Global.io.print("---------------------------------------------------------");
            }

            return new Response("");
        } catch(Exception e){
            return new Response("Failed to load reports", Status.ERROR);
        }
    }

    public Response reviewReport(){
        try{
            //gather info to display to the admin
            String reportID = Global.io.inlineQuestion("What report number do you want to respond to? : ");
            JSONArray jsonArray = new JSONArray(Global.sendGet("/deliveryReport?id=" + reportID).getMessage());
            DeliveryReport deliveryReport = new Gson().fromJson(jsonArray.getJSONObject(0).toString(), DeliveryReport.class);
            Buyer buyer = new Gson().fromJson(Global.sendGet("/buyer?id=" + deliveryReport.buyer).getMessage(), Buyer.class);
            Seller seller = new Gson().fromJson(Global.sendGet("/seller?id=" + deliveryReport.buyer).getMessage(), Seller.class);
            Order order = new Gson().fromJson(Global.sendGet("/order/" + deliveryReport.order).getMessage(), Order.class);
            jsonArray = new JSONArray(Global.sendGet("/listing?id=" + deliveryReport.listing).getMessage());
            Listing listing = new Gson().fromJson(jsonArray.getJSONObject(0).toString(), Listing.class);

            //display info to the admin
            Global.io.print("-----------------Listing Information---------------------");
            Global.io.print("Name: " + listing.name);
            Global.io.print("Description: " + listing.description);
            Global.io.print("Cost: " + listing.cost);
            Global.io.print("Maximum Delivery Time (days-hours): " + listing.maxDelivery);
            Global.io.print("------------------Order Information----------------------");
            Global.io.print("Maximum Shipping Time: " + order.endDate);
            Global.io.print("-----------------Report Information----------------------");
            Global.io.print("Submission Date: " + deliveryReport.date);
            Global.io.print("Buyer: " + buyer.name);
            Global.io.print("Buyer Message: " + deliveryReport.buyerMessage);
            Global.io.print("------------------");
            Global.io.print("Seller: " + seller.name);
            Global.io.print("Seller Message: " + deliveryReport.sellerMessage);
            Global.io.print("---------------------------------------------------------");
            Global.io.print("");

            //complete the Response (refund/no refund)
            String response = Global.io.inlineQuestion("Accept this Report? (yes or no) - ");
            if(response.equals("yes")){
                buyer.balance += listing.cost;
                seller.balance -= listing.cost;
                Global.sendPatch("/buyer", new Gson().toJson(buyer));
                Global.sendPatch("/seller", new Gson().toJson(seller));
                Global.sendDelete("/order?id=" + order.id);
            }
            deliveryReport.appealed = true;

            Global.sendPatch("/deliveryReport", new Gson().toJson(deliveryReport));

            return new Response("Report successfully resolved");
        } catch(Exception e){
            return new Response("Failed to complete response", Status.ERROR);
        }
    }

    public Response listListings(){
        try {
            Listing[] listings = new Gson().fromJson(Global.sendGet("/listing?all=xyz").getMessage(), Listing[].class);
            for(Listing l : listings){
                Global.io.print("------------------------------------------");
                Global.io.print("id: " + l.id);
                Global.io.print("Name: " + l.name);
                Global.io.print("------------------------------------------");
            }

            return new Response("");
        } catch(Exception e){
            return new Response("Failed to list Listings", Status.ERROR);
        }
    }

    public Response listFlags(){
        try {
            String listingNumber = Global.io.inlineQuestion("What listing's flags do you want to see? (id) - ");
            Flag[] flags = new Gson().fromJson(Global.sendGet("/flag?listing=" + listingNumber).getMessage(), Flag[].class);
            for(Flag f : flags){
                User user = new Gson().fromJson(Global.sendGet("/user?id=" + f.user).getMessage(), User.class);
                Global.io.print("------------------------------------------");
                Global.io.print("id: " + f.id);
                Global.io.print("User: " + user.name);
                Global.io.print("Reason: " + f.reason);
                Global.io.print("------------------------------------------");
            }

            return new Response("");
        } catch(Exception e){
            return new Response("Failed to list flags");
        }
    }

    public Response getMostFlags(){
        try {
            Flag[] flags = new Gson().fromJson(Global.sendGet("/flag?most=xyz").getMessage(), Flag[].class);
            for(Flag f : flags){
                User user = new Gson().fromJson(Global.sendGet("/user?id=" + f.user).getMessage(), User.class);
                Global.io.print("------------------------------------------");
                Global.io.print("id: " + f.id);
                Global.io.print("User: " + user.name);
                Global.io.print("Reason: " + f.reason);
                Global.io.print("------------------------------------------");
            }

            Listing[] listing = new Gson().fromJson(Global.sendGet("/listing?id=" + flags[flags.length-1].listing).getMessage(), Listing[].class);
            return new Response("Listing with the most Flags: " + listing[0].name + ", id: " + listing[0].id);
        } catch(Exception e){
            return new Response("Failed to list flags");
        }
    }

    public Response flagListing(){
        //make the flag
        Flag flag = new Flag();
        flag.id = 0;
        flag.user = Global.currUser.id;
        String listingName = Global.io.inlineQuestion("What listing do you want to report? (give the name) - ");

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

    public Response reviewFlags(){
        try{
            String listingId = Global.io.inlineQuestion("What listing do you want to review? (give the id) - ");
            Listing[] listing = new Gson().fromJson(Global.sendGet("/listing?id=" + listingId).getMessage(), Listing[].class);

            Global.io.print("-----------------Listing Information---------------------");
            Global.io.print("Name: " + listing[0].name);
            Global.io.print("Description: " + listing[0].description);
            Global.io.print("Cost: " + listing[0].cost);
            Global.io.print("Maximum Delivery Time (days-hours): " + listing[0].maxDelivery);
            Global.io.print("------------------------Flags----------------------------");
            Flag[] flags = new Gson().fromJson(Global.sendGet("/flag?listing=" + listingId).getMessage(), Flag[].class);
            for(Flag f : flags){
                User user = new Gson().fromJson(Global.sendGet("/user?id=" + f.user).getMessage(), User.class);
                Global.io.print("id: " + f.id);
                Global.io.print("User: " + user.name);
                Global.io.print("Credibility: " + user.credibility);
                Global.io.print("Reason: " + f.reason);
                Global.io.print("------------------------------------------");
            }

            //complete the Response (refund/no refund)
            String response = Global.io.inlineQuestion("Remove the listing? (yes or no) - ");
            if(response.equals("yes")){
                Global.sendDelete("/listing?id=" + listingId);
            }

            for(Flag f : flags){
                User user = new Gson().fromJson(Global.sendGet("/user?id=" + f.user).getMessage(), User.class);
                if(response.equals("yes")) {
                    user.credibility = (float) Math.min(user.credibility+0.2, 5.0);
                } else {
                    user.credibility = (float) Math.max(user.credibility-0.3, 0.0);
                }
                Global.sendPatch("/user", new Gson().toJson(user));
                Global.sendDelete("/flag?id=" + f.id);
            }

            return new Response("Completed review successfully");
        } catch(Exception e){
            return new Response("Failed to complete Flag", Status.ERROR);
        }
    }
}
