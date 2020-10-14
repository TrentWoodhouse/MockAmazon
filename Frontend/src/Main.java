import Controllers.Controller;
import Entities.Response;
import Enums.UserType;
import Utils.IO;
import Controllers.*;

public class Main {
    public static void main(String[] args) {
        IO io = new IO();

        //TODO make authentication just a bit more secure
        boolean authenticated = false;
        UserType userType = UserType.BUYER;
        while(!authenticated) {
            String response = io.question("Are you a [buyer], a [seller], or an [admin]?");
            for (UserType u : UserType.values()) {
                if (u.name().toLowerCase().equals(response.toLowerCase())) {
                    authenticated = true;
                    userType = u;
                }
            }
        }

        Controller userController;
        switch(userType) {
            case BUYER:
                userController = new BuyerController();
                break;
            case SELLER:
                userController = new SellerController();
                break;
            case ADMIN:
                userController = new AdminController();
                break;
            default:
                return;
        }

        while(true) {
            String input = io.prompt();
            Response response = userController.execute(input);
            switch(response.getStatus()) {
                case OK:
                    io.print(response.getMessage());
                    break;
                case ERROR:
                default:
                    io.error(response.getMessage());
            }
        }
    }
}
