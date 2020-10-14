package Controllers;

import Entities.Response;

public interface Controller {
    public Response execute(String command);
}
