package se.ifmo.blazingzephyr.model;

import java.io.Serializable;

public record User(long id, String login, String password) implements Serializable { }
