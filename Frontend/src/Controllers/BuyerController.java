package Controllers;

import Entities.Response;
import Enums.Status;
import Utils.Global;

public class BuyerController extends Controller {

    @Override
    public Response execute(String command) {
        String[] exArr = command.trim().split("\\s+");
        try {
            switch(exArr[0].toLowerCase()) {
                case "menu":
                    return menu();
                case "giveFeedback":
                    return giveFeedback();
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
        return super.menu();
    }

    public Response giveFeedback() {
        //TODO
        return new Response("You have given feedback. Thanks!");
    }
}
