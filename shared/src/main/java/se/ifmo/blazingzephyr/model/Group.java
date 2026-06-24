package se.ifmo.blazingzephyr.model;

import java.io.Serializable;
import java.sql.Date;

public record Group(long id, String name, Date createdAt) implements Serializable { }
