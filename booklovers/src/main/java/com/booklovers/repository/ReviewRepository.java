package com.booklovers.repository;

import com.booklovers.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByBookId(Long bookId);
    List<Review> findByUserId(Long userId);
    
    @Query("SELECT r FROM Review r WHERE r.user.id = :userId AND r.book.id = :bookId")
    Optional<Review> findByUserIdAndBookId(@Param("userId") Long userId, @Param("bookId") Long bookId);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.book.id = :bookId")
    Long countByBookId(@Param("bookId") Long bookId);
    
    @Query("SELECT r.user.id FROM Review r WHERE r.id = :reviewId")
    Optional<Long> findUserIdByReviewId(@Param("reviewId") Long reviewId);
    
    @Query("SELECT r FROM Review r JOIN FETCH r.user WHERE r.id = :reviewId")
    Optional<Review> findByIdWithUser(@Param("reviewId") Long reviewId);
    
    @Modifying
    @Query(value = "DELETE FROM reviews WHERE id = :reviewId", nativeQuery = true)
    int deleteReviewById(@Param("reviewId") Long reviewId);
}
