package Entities;

import Enums.Status;

public class Response {
    private String message;
    private Status status;

    public Response(String message) {
        this.message = message;
        this.status = Status.OK;
    }

    public Response(String message, Status status) {
        this.message = message;
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public Status getStatus() {
        return status;
    }
}
