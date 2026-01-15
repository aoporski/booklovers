package com.booklovers.repository;

import com.booklovers.entity.UserBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBookRepository extends JpaRepository<UserBook, Long> {
    List<UserBook> findByUserId(Long userId);
    
    @Query("SELECT ub FROM UserBook ub WHERE ub.user.id = :userId AND ub.shelfName = :shelfName")
    List<UserBook> findByUserIdAndShelfName(@Param("userId") Long userId, @Param("shelfName") String shelfName);
    
    List<UserBook> findByBookId(Long bookId);
    
    @Query("SELECT ub FROM UserBook ub WHERE ub.user.id = :userId AND ub.book.id = :bookId")
    List<UserBook> findByUserIdAndBookId(@Param("userId") Long userId, @Param("bookId") Long bookId);
    
    @Query("SELECT ub FROM UserBook ub WHERE ub.user.id = :userId AND ub.book.id = :bookId AND ub.shelfName = :shelfName")
    Optional<UserBook> findByUserIdAndBookIdAndShelfName(@Param("userId") Long userId, @Param("bookId") Long bookId, @Param("shelfName") String shelfName);
    
    @Query("SELECT DISTINCT ub.shelfName FROM UserBook ub WHERE ub.user.id = :userId")
    List<String> findDistinctShelfNamesByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(DISTINCT ub.book.id) FROM UserBook ub WHERE ub.user.id = :userId AND ub.book IS NOT NULL AND EXTRACT(YEAR FROM ub.addedAt) = :year")
    Long countBooksReadInYear(@Param("userId") Long userId, @Param("year") int year);
    
    @Query("SELECT COUNT(DISTINCT ub.user.id) FROM UserBook ub WHERE ub.book.id = :bookId AND ub.book IS NOT NULL")
    Long countReadersByBookId(@Param("bookId") Long bookId);
}
