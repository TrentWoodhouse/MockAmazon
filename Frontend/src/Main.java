import Controllers.Controller;
import Entities.Response;
import Enums.UserType;
import Utils.IO;
import Controllers.SellerController;

public class Main {
    public static void main(String[] args) {
        IO io = new IO();

        Controller userController;
        switch(UserType.SELLER) {
            case BUYER:
                //TODO userController = new BuyerController();
                break;
            case SELLER:
                userController = new SellerController();
                break;
            case ADMIN:
                //TODO userController = new BuyerController();
                break;
            default:
                return;
        }

        userController = new SellerController(); //TODO remove this when other controllers are implemented;

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
