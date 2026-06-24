package se.ifmo.blazingzephyr.model;

import java.io.Serializable;
import java.sql.Date;

public record UserGroupRelation(long userId, long groupId, Date addedAt) implements Serializable { }
