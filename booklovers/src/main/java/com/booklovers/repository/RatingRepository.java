package com.booklovers.repository;

import com.booklovers.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    List<Rating> findByBookId(Long bookId);
    List<Rating> findByUserId(Long userId);
    
    @Query("SELECT r FROM Rating r WHERE r.user.id = :userId AND r.book.id = :bookId")
    Optional<Rating> findByUserIdAndBookId(@Param("userId") Long userId, @Param("bookId") Long bookId);
    
    @Query("SELECT AVG(r.value) FROM Rating r WHERE r.book.id = :bookId")
    Double getAverageRatingByBookId(@Param("bookId") Long bookId);
    
    @Query("SELECT COUNT(r) FROM Rating r WHERE r.book.id = :bookId")
    Long countByBookId(@Param("bookId") Long bookId);
}
