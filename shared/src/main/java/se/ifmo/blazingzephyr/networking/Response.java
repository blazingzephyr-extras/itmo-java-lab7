package se.ifmo.blazingzephyr.networking;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import se.ifmo.blazingzephyr.model.OrganizationWithId;

public final class Response implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final boolean success;
    private final String message;
    private final List<OrganizationWithId> data;

    private Response(boolean success, String message, List<OrganizationWithId> data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static Response ok(String message) {
        return new Response(true, message, null);
    }

    public static Response ok(String message, List<OrganizationWithId> data) {
        return new Response(true, message, data);
    }

    public static Response error(String message) {
        return new Response(false, message, null);
    }

    public boolean isSuccess() { return this.success; }
    public String getMessage() { return this.message; }
    public List<OrganizationWithId> getData() { return this.data; }

    @Override
    public String toString() {
        return "Response{success=" + this.success + ", message='" + this.message + "'}";
    }
}
