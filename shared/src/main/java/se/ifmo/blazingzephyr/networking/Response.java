package se.ifmo.blazingzephyr.networking;

import java.io.Serial;
import java.io.Serializable;

public final class Response implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final boolean success;
    private final String message;

    private Response(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static Response ok(String message) {
        return new Response(true, message);
    }

    public static Response error(String message) {
        return new Response(false, message);
    }

    public boolean isSuccess() { return this.success; }
    public String getMessage() { return this.message; }

    @Override
    public String toString() {
        return "Response{success=" + this.success + ", message='" + this.message + "'}";
    }
}
