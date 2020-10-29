package Controllers;

import Classes.*;
import Entities.Response;
import Enums.Status;
import Utils.Global;
import com.google.gson.Gson;
import org.json.JSONArray;

public class AdminController extends Controller {

    @Override
    public Response execute(String command) {
        String[] exArr = command.trim().split("\\s+");
        try {
            switch(exArr[0].toLowerCase()) {
                case "menu":
                    return menu();
                case "reviewreports":
                    return reviewReports();
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
        Global.io.print("reviewReports:\t\t\tresolve active reports");
        return super.menu();
    }

    public Response reviewReports(){
        try{
            //gather info to display to the admin
            String reportID = Global.io.inlineQuestion("What report number do you want to respond to? : ");
            JSONArray jsonArray = new JSONArray(Global.sendGet("/deliveryReport?id=" + reportID).getMessage());
            DeliveryReport deliveryReport = new Gson().fromJson(jsonArray.getJSONObject(0).toString(), DeliveryReport.class);
            Buyer buyer = new Gson().fromJson(Global.sendGet("/buyer?id=" + deliveryReport.buyer).getMessage(), Buyer.class);
            Seller seller = new Gson().fromJson(Global.sendGet("/seller?id=" + deliveryReport.buyer).getMessage(), Seller.class);
            Order order = new Gson().fromJson(Global.sendGet("/order?id=" + deliveryReport.order).getMessage(), Order.class);
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
            deliveryReport.appealed = true;

            return Global.sendPatch("/deliveryReport", new Gson().toJson(deliveryReport));
        } catch(Exception e){
            e.printStackTrace();
            return new Response("Failed to complete response", Status.ERROR);
        }
    }
}
