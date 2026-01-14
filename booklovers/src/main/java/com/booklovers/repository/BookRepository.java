package com.booklovers.repository;

import com.booklovers.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    Optional<Book> findByIsbn(String isbn);
    List<Book> findByTitleContainingIgnoreCase(String title);
    List<Book> findByAuthorContainingIgnoreCase(String author);
    
    @Query("SELECT b FROM Book b WHERE " +
           "LOWER(b.title) LIKE LOWER(:query) OR " +
           "LOWER(b.author) LIKE LOWER(:query) OR " +
           "LOWER(COALESCE(b.isbn, '')) LIKE LOWER(:query)")
    List<Book> searchBooks(@Param("query") String query);
    
    @Query("SELECT COUNT(b) FROM Book b WHERE b.authorEntity.id = :authorId")
    Long countByAuthorId(@Param("authorId") Long authorId);
}
