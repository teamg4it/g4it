/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiuser.repository;

import com.soprasteria.g4it.backend.apiuser.modeldb.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * User repository to access user data in database.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by email.
     *
     * @param email the email.
     * @return the user.
     */
    Optional<User> findByEmail(final String email);

    /**
     * Find user by subject.
     *
     * @param subject the subject.
     * @return the user.
     */
    Optional<User> findBySub(final String subject);

    @Modifying
    @Query(value = """
            UPDATE g4it_user
            SET last_seen_release_note_id = (SELECT MAX(id) FROM release_notes)
            WHERE id = :userId
              AND EXISTS (SELECT 1 FROM release_notes)
            """, nativeQuery = true)
    int markLatestReleaseNoteAsSeen(@Param("userId") long userId);

    /**
     * Return all users from g4it_users where the first name, last name or email
     * address contains the text typed
     *
     * @param searchedName the searched string
     */
    @Query("""
            SELECT u FROM User u
            WHERE
            u.domain IN :domains AND
            (u.firstName ILIKE %:searchedName% OR u.lastName ILIKE %:searchedName% OR u.email ILIKE %:searchedName%)
            """)
    List<User> findBySearchedName(@Param("searchedName") String searchedName, @Param("domains") Set<String> domains);
    
}
