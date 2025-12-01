package com.gal.usc.roomify.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@SuppressWarnings("unused")
@Document(collection = "permissions")
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

    public String getName() {
        return this.toString();
    }

    @Override
    public String toString() {
        return resource + ":" + action;
    }
}