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
    List<UserBook> findByUserIdAndShelfName(Long userId, String shelfName);
    List<UserBook> findByBookId(Long bookId);
    
    @Query("SELECT ub FROM UserBook ub WHERE ub.user.id = :userId AND ub.book.id = :bookId")
    List<UserBook> findByUserIdAndBookId(@Param("userId") Long userId, @Param("bookId") Long bookId);
    
    @Query("SELECT ub FROM UserBook ub WHERE ub.user.id = :userId AND ub.book.id = :bookId AND ub.shelfName = :shelfName")
    Optional<UserBook> findByUserIdAndBookIdAndShelfName(@Param("userId") Long userId, @Param("bookId") Long bookId, @Param("shelfName") String shelfName);
    
    @Query("SELECT DISTINCT ub.shelfName FROM UserBook ub WHERE ub.user.id = :userId")
    List<String> findDistinctShelfNamesByUserId(@Param("userId") Long userId);
}
