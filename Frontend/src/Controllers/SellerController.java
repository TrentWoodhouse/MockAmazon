package Controllers;

import Entities.Response;
import Enums.Status;
import Utils.Global;

import java.util.ArrayList;

public class SellerController extends Controller {

    @Override
    public Response execute(String command) {
        String[] exArr = command.trim().split("\\s+");
        try {
            switch(exArr[0].toLowerCase()) {
                case "showListings":
                    return showListings();
                case "createListing":
                    return createListing();
                case "editListing":
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
                "editListing [id]:\t\tedit listing with the given id");
        return super.menu();
    }

    public Response showListings() {
        //TODO
        return new Response("Listing 1\nListing 2");
    }

    public Response createListing() {
        //TODO
        return new Response("Created new listing");
    }

    public Response editListing(int id) {
        //TODO
        return new Response("Listing " + id + " successfully edited");
    }
}
