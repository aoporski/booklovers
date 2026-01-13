package com.booklovers.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_books", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "book_id", "shelf_name"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBook {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_book_seq")
    @SequenceGenerator(name = "user_book_seq", sequenceName = "user_book_seq", allocationSize = 1)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;
    
    @Column(name = "shelf_name", nullable = false)
    @Builder.Default
    private String shelfName = "Moja biblioteczka"; // Domyślna kategoria
    
    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime addedAt = LocalDateTime.now();
}
