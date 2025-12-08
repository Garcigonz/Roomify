package com.gal.usc.roomify.repository;

import com.gal.usc.roomify.model.Role;
import com.gal.usc.roomify.model.Sala;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends MongoRepository<@NonNull Role, @NonNull String> {
    Role findByRolename(String rolename);
}