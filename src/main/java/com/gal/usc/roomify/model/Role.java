package com.gal.usc.roomify.model;


import jakarta.persistence.*;

import java.util.Set;

@Entity
@Table(name = "roles")
@SuppressWarnings("unused")
public class Role {
    @Id
    private String rolename;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_hierarchy",
            joinColumns = @JoinColumn(name = "role"),
            inverseJoinColumns = @JoinColumn(name = "includes"))
    private Set<Role> includes;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role"),
            inverseJoinColumns = @JoinColumn(name = "permission"))
    private Set<Permission> permissions;

    public Role() { }

    public String getRolename() {
        return rolename;
    }

    public Role setRolename(String rolename) {
        this.rolename = rolename;
        return this;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }

    public Role setPermissions(Set<Permission> permissions) {
        this.permissions = permissions;
        return this;
    }

    public Set<Role> getIncludes() {
        return includes;
    }

    public Role setIncludes(Set<Role> includes) {
        this.includes = includes;
        return this;
    }
}
