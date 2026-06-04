package se.ifmo.blazingzephyr.model;

import java.io.Serializable;

public class OrganizationWithId implements Serializable {
    
    private final long id;
    private final OrganizationData data;
    private final String owner;

    public OrganizationWithId(long id, OrganizationData data, String owner) {
        this.id = id;
        this.data = data;
        this.owner = owner;
    }

    public long getId() { return this.id; }
    public OrganizationData getData() { return this.data; }
    public String getOwner() { return this.owner; }
}
