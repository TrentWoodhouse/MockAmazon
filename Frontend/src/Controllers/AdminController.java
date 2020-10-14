package Controllers;

import Entities.Response;
import Enums.Status;

public class AdminController implements Controller {

    public Response execute(String command) {
        String[] exArr = command.trim().split("\\s+");
        try {
            switch(exArr[0]) {
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
}
