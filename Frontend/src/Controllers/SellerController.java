package Controllers;

import Entities.Response;
import Enums.Status;
import Utils.Global;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class SellerController extends Controller {

    @Override
    public Response execute(String command) {
        String[] exArr = command.trim().split("\\s+");
        try {
            switch(exArr[0].toLowerCase()) {
                case "showlistings":
                    return showListings();
                case "createlisting":
                    return createListing();
                case "editlisting":
                    return editListing(Integer.parseInt(exArr[1]));
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
                "createListing:\t\t\tcreate a new listing]n" +
                "editListing [id]:\t\tedit listing with the given id");
        return super.menu();
    }

    public Response showListings() {
        //TODO
        return new Response("Listing 1\nListing 2");
    }

    public Response createListing() {
        JSONObject listing = new JSONObject();

        Global.io.print("Creating a new listing:");
        try {
            listing.put("id", Global.io.inlineQuestion("Enter the listing's Product ID:")); //TODO remove this when automatic id is handled
            listing.put("name", Global.io.inlineQuestion("Name:"));
            listing.put("description", Global.io.inlineQuestion("Description:"));
            listing.put("cost", Global.io.inlineQuestion("Cost:"));
            listing.put("seller", Global.currUser.id);
            String jsonString = listing.toString();

            URL url = new URL("http://localhost:8080/listing");
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);
            try(OutputStream os = con.getOutputStream()) {
                byte[] input = jsonString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
            try(BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                return new Response(response.toString());
            }
        }
        catch(Exception e) {
            return new Response("The listing was not created successfully", Status.ERROR);
        }

    }

    public Response editListing(int id) {
        //TODO
        return new Response("Listing " + id + " successfully edited");
    }
}
