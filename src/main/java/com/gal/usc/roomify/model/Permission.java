package com.gal.usc.roomify.model;

import jakarta.persistence.*;

@SuppressWarnings("unused")
@Entity
@Table(name = "permissions")
public class Permission {
    @Id
    private String id;
    private String resource;
    private String action;

    public Permission() {}

    public String getId() {
        return this.id;
    }

    public Permission setId(String id) {
        this.id = id;
        return this;
    }

    public String getResource() {
        return resource;
    }

    public Permission setResource(String resource) {
        this.resource = resource;
        return this;
    }

    public String getAction() {
        return action;
    }

    public Permission setAction(String action) {
        this.action = action;
        return this;
    }

    @Override
    public String toString() {
        return resource+":"+action;
    }

    public String getName() {
        return this.toString();
    }

}
