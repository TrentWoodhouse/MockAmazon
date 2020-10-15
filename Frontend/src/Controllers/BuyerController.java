package Controllers;

import Classes.Message;
import Entities.Response;
import Enums.Status;
import Utils.Global;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class BuyerController extends Controller {

    @Override
    public Response execute(String command) {
        String[] exArr = command.trim().split("\\s+");
        //try {
            switch(command/*exArr[0].toLowerCase()*/) {
                case "menu":
                    return menu();
                case "giveFeedback":
                    return giveFeedback();
                case "sendMessage":
                    return sendMessage();
                case "viewMessages":
                    return viewMessages();
                default:
                    return super.execute(command);
            }
        /*}
        catch (IndexOutOfBoundsException e) {
            return new Response("Missing parameter " + e.getMessage() + " for command \"" + exArr[0] + "\"", Status.ERROR);
        }
        catch (Exception e) {
            return new Response(e.getMessage(), Status.ERROR);
        }*/
    }

    @Override
    public Response menu() {
        Global.io.print("giveFeedback:\t\t\tgive feedback on a particular listing");
        Global.io.print("sendMessage:\t\t\tsend a message to a particular user");
        Global.io.print("viewMessages:\t\t\tview all messages from a user to you");
        return super.menu();
    }

    public Response giveFeedback() {
        //TODO
        return new Response("You have given feedback. Thanks!");
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
            String in = Global.sendGet("/seller" + "?name=" + receiver).getMessage();
            int val = 0;
            if (in == null || !in.equals("")) {
                val = Integer.parseInt(String.valueOf(new JSONObject(in).get("id")));
            } else {
                in = Global.sendGet("/buyer" + "?name=" + receiver).getMessage();
                if (in == null || !in.equals("")) {
                    val = Integer.parseInt(String.valueOf(new JSONObject(in).get("id")));
                } else {
                    return new Response("Message Failed to Send (Recipient doesn't exist)", Status.ERROR);
                }
            }
            m.receiver = val;
            System.out.println("Val=" + val);
        } catch (Exception e){
            System.out.println(e);
        }

        m.message = message;
        m.timeSent = new Date().toString();
        System.out.println(new Gson().toJson(m));

        String inputLine = Global.sendPost("/message", new Gson().toJson(m).toString()).getMessage();

        if(inputLine.equals("")){
            return new Response("Message Failed to Send");
        }

        return new Response("Message Successfully Sent");
    }
    public Response viewMessages(){

        return new Response(" - End of Messages - ");
    }
}