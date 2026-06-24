package se.ifmo.blazingzephyr.networking;

import java.io.Serial;
import java.io.Serializable;
import se.ifmo.blazingzephyr.model.OrganizationData;
import se.ifmo.blazingzephyr.model.OrganizationType;

public sealed interface CommandPayload extends Serializable
        permits CommandPayload.None,
                CommandPayload.WithOrganization,
                CommandPayload.WithId,
                CommandPayload.WithIdAndOrganization,
                CommandPayload.WithOrganizationType,
                CommandPayload.WithName,
                CommandPayload.WithScriptName,
                CommandPayload.StringArg,
                CommandPayload.LongArg,
                CommandPayload.TwoLongs {

    record None() implements CommandPayload {
        @Serial private static final long serialVersionUID = 1L;
    }

    /** ADD, ADD_IF_MAX, REMOVE_GREATER */
    record WithOrganization(OrganizationData organization) implements CommandPayload {
        @Serial private static final long serialVersionUID = 1L;
    }

    /** REMOVE_BY_ID */
    record WithId(long id) implements CommandPayload {
        @Serial private static final long serialVersionUID = 1L;
    }

    /** UPDATE */
    record WithIdAndOrganization(long id, OrganizationData organization) implements CommandPayload {
        @Serial private static final long serialVersionUID = 1L;
    }

    /** REMOVE_ALL_BY_TYPE */
    record WithOrganizationType(OrganizationType organizationType) implements CommandPayload {
        @Serial private static final long serialVersionUID = 1L;
    }

    /** FILTER_STARTS_WITH_FULL_NAME */
    record WithName(String name) implements CommandPayload {
        @Serial private static final long serialVersionUID = 1L;
    }

    /** EXECUTE_SCRIPT */
    record WithScriptName(String scriptName) implements CommandPayload {
        @Serial private static final long serialVersionUID = 1L;
    }

    /** CREATE_GROUP — имя новой группы */
    record StringArg(String value) implements CommandPayload {
        @Serial private static final long serialVersionUID = 1L;
    }

    /** DELETE_GROUP — ID группы */
    record LongArg(long value) implements CommandPayload {
        @Serial private static final long serialVersionUID = 1L;
    }

    /** ADD_USER_TO_GROUP, REMOVE_USER_FROM_GROUP — userId + groupId */
    record TwoLongs(long first, long second) implements CommandPayload {
        @Serial private static final long serialVersionUID = 1L;
    }
}
