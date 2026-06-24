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
    private final List<Serializable> args;

    private Response(boolean success, String message, List<OrganizationWithId> data, List<Serializable> args) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.args = args;
    }

    public static Response ok(String message) {
        return new Response(true, message, null, List.of());
    }

    public static Response ok(String message, Serializable... args) {
        return new Response(true, message, null, List.of(args));
    }

    public static Response ok(String message, List<OrganizationWithId> data) {
        return new Response(true, message, data, List.of());
    }

    public static <T extends Serializable> Response okList(String message, List<T> list) {
        return new Response(true, message, null, List.copyOf(list));
    }

    public static Response error(String message) {
        return new Response(false, message, null, List.of());
    }

    public static Response error(String message, Serializable... args) {
        return new Response(false, message, null, List.of(args));
    }

    public boolean isSuccess() { return this.success; }
    public String getMessage() { return this.message; }
    public List<OrganizationWithId> getData() { return this.data; }
    public List<Serializable> getArgs() { return this.args; }

    @Override
    public String toString() {
        return "Response{success=" + this.success + ", message='" + this.message
            + "', data=" + this.data + ", args=" + this.args + "}";
    }
}
