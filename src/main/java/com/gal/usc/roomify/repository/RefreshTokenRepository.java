package com.gal.usc.roomify.repository;

import com.gal.usc.roomify.model.RefreshToken;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@SuppressWarnings("UnusedReturnValue")
@NullMarked
@Repository
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
    Collection<RefreshToken> deleteAllByUsername(String username);
    Optional<RefreshToken> findByToken(String token);
}
