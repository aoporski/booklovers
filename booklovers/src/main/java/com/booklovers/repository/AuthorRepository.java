package com.booklovers.repository;

import com.booklovers.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {
    Optional<Author> findByFirstNameAndLastName(String firstName, String lastName);
    
    @Query("SELECT a FROM Author a WHERE a.firstName LIKE %:query% OR a.lastName LIKE %:query%")
    List<Author> searchAuthors(@Param("query") String query);
    
    List<Author> findByFirstNameContainingIgnoreCase(String firstName);
    List<Author> findByLastNameContainingIgnoreCase(String lastName);
}
