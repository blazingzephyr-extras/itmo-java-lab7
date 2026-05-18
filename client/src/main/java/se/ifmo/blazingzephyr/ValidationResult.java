package se.ifmo.blazingzephyr;

import java.util.Optional;

import se.ifmo.blazingzephyr.networking.CommandPayload;
import se.ifmo.blazingzephyr.networking.CommandType;
import se.ifmo.blazingzephyr.networking.Request;

public record ValidationResult(Optional<ValidationError> error, Optional<Request> request) { 

    public static ValidationResult ok(CommandType type) {
        return new ValidationResult(Optional.empty(), Optional.of(new Request(type)));
    }

    public static ValidationResult ok(CommandType type, CommandPayload payload) {
        return new ValidationResult(Optional.empty(), Optional.of(new Request(type, payload)));
    }

    public static ValidationResult error(ValidationError error) {
        return new ValidationResult(Optional.of(error), Optional.empty());
    }

    public boolean isError() { return !this.error.isEmpty(); }
    public boolean isOk() { return this.error.isEmpty(); }
}
