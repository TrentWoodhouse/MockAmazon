package Controllers;

import Entities.Response;
import Enums.Status;
import Utils.Global;

public class Controller {
    public Response execute(String command) {
        String[] exArr = command.trim().split("\\s+");
        try {
            switch(exArr[0].toLowerCase()) {
                case "menu":
                    return menu();
                case "route":
                    return route();
                case "back":
                    return back();
                default:
                    return new Response("No command \"" + exArr[0] + "\" found.", Status.ERROR);
            }
        }
        catch (IndexOutOfBoundsException e) {
            return new Response("Missing parameter " + e.getMessage() + " for command \"" + exArr[0] + "\"", Status.ERROR);
        }
        catch (Exception e) {
            return new Response(e.getMessage(), Status.ERROR);
        }
    }

    public Response menu() {
        Global.io.print("menu:\t\t\t\t\tlists all actions that can be performed\n" +
                "route:\t\t\t\t\tlists the current route\n" +
                "back:\t\t\t\t\tgo back");
        return new Response("", Status.NOACTION);
    }

    public Response route() {
        Global.io.print(Global.router.getPathPretty());
        return new Response("", Status.NOACTION);
    }

    public Response back() {
        Global.router.goBack();
        return new Response("", Status.NOACTION);
    }
}
