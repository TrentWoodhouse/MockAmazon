import Controllers.Controller;
import Entities.Response;
import Enums.UserType;
import Utils.*;
import Controllers.*;

public class Main {
    public static void main(String[] args) {
        //Initialize routes
        Global.router.addPath("admin", new AdminController());
        Global.router.addPath("seller", new SellerController());
        Global.router.addPath("buyer", new BuyerController());

        //TODO make authentication just a bit more secure
        boolean authenticated = false;
        UserType userType = UserType.BUYER;
        while(!authenticated) {
            String response = Global.io.question("Are you a [buyer], a [seller], or an [admin]?");
            for (UserType u : UserType.values()) {
                if (u.name().toLowerCase().equals(response.toLowerCase())) {
                    authenticated = true;
                    userType = u;
                }
            }
        }

        switch(userType) {
            case BUYER:
                Global.router.goTo("buyer");
                break;
            case SELLER:
                Global.router.goTo("seller");
                break;
            case ADMIN:
                Global.router.goTo("admin");
                break;
            default:
                return;
        }

        Controller userController = Global.router.getController();

        Global.io.print("You have logged in as [" + userType.name().toLowerCase() + "].");
        Global.io.print("Type the command \"menu\" to see all actions that can be performed");
        while(true) {
            String input = Global.io.prompt();
            Response response = userController.execute(input);
            switch(response.getStatus()) {
                case OK:
                    Global.io.print(response.getMessage());
                    break;
                case NOACTION:
                    break;
                case ERROR:
                default:
                    Global.io.error(response.getMessage());
            }
        }
    }
}
