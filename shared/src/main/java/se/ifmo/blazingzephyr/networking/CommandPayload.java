package se.ifmo.blazingzephyr.networking;

import se.ifmo.blazingzephyr.model.OrganizationData;
import se.ifmo.blazingzephyr.model.OrganizationType;

import java.io.Serial;
import java.io.Serializable;

public sealed interface CommandPayload extends Serializable
        permits CommandPayload.None,
                CommandPayload.WithOrganization,
                CommandPayload.WithId,
                CommandPayload.WithIdAndOrganization,
                CommandPayload.WithOrganizationType,
                CommandPayload.WithName,
                CommandPayload.WithScriptName {

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
}