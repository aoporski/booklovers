package com.booklovers.web.controller;

import com.booklovers.dto.AuthorDto;
import com.booklovers.dto.BookDto;
import com.booklovers.dto.RatingDto;
import com.booklovers.dto.ReviewDto;
import com.booklovers.dto.UserDto;
import com.booklovers.service.author.AuthorService;
import com.booklovers.service.book.BookService;
import com.booklovers.service.rating.RatingService;
import com.booklovers.service.review.ReviewService;
import com.booklovers.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class BookWebController {
    
    private final BookService bookService;
    private final ReviewService reviewService;
    private final RatingService ratingService;
    private final UserService userService;
    private final AuthorService authorService;
    
    @GetMapping("/books")
    public String booksPage(Model model, @RequestParam(required = false) String search) {
        List<BookDto> books;
        if (search != null && !search.isEmpty()) {
            books = bookService.searchBooks(search);
        } else {
            books = bookService.getAllBooks();
        }
        model.addAttribute("books", books);
        model.addAttribute("search", search);
        return "books";
    }
    
    @GetMapping("/books/{id}")
    public String bookDetails(@PathVariable Long id, Model model) {
        BookDto book = bookService.getBookById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));
        
        List<ReviewDto> reviews = reviewService.getReviewsByBookId(id);
        List<RatingDto> ratings = ratingService.getRatingsByBookId(id);
        
        // Mapowanie ocen do recenzji (po userId)
        Map<Long, Integer> ratingMap = ratings.stream()
                .collect(Collectors.toMap(
                        RatingDto::getUserId,
                        RatingDto::getValue,
                        (existing, replacement) -> existing
                ));
        
        // Dodaj oceny do recenzji
        reviews.forEach(review -> {
            if (review.getUserId() != null && ratingMap.containsKey(review.getUserId())) {
                review.setRatingValue(ratingMap.get(review.getUserId()));
            }
        });
        
        model.addAttribute("book", book);
        model.addAttribute("reviews", reviews);
        model.addAttribute("reviewDto", new ReviewDto());
        return "book-details";
    }
    
    @GetMapping("/my-books")
    public String myBooksPage(Model model) {
        try {
            UserDto currentUser = userService.getCurrentUser();
            List<BookDto> books = bookService.getUserBooks(currentUser.getId());
            model.addAttribute("books", books);
            return "my-books";
        } catch (Exception e) {
            return "redirect:/login";
        }
    }
    
    @GetMapping("/books/add")
    public String addBookForm(Model model) {
        List<AuthorDto> authors = authorService.getAllAuthors();
        model.addAttribute("bookDto", new BookDto());
        model.addAttribute("authors", authors);
        return "add-book";
    }
    
    @PostMapping("/books/add")
    public String addBook(@Valid @ModelAttribute BookDto bookDto, BindingResult result, RedirectAttributes redirectAttributes, Model model) {
        if (result.hasErrors()) {
            List<AuthorDto> authors = authorService.getAllAuthors();
            model.addAttribute("authors", authors);
            return "add-book";
        }
        try {
            bookService.createBook(bookDto);
            redirectAttributes.addFlashAttribute("success", "Książka została dodana pomyślnie!");
            return "redirect:/books";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Błąd podczas dodawania książki: " + e.getMessage());
            return "redirect:/books/add";
        }
    }
    
    @PostMapping("/books/{id}/add-to-library")
    public String addBookToLibrary(@PathVariable Long id, @RequestParam(defaultValue = "Moja biblioteczka") String shelfName, RedirectAttributes redirectAttributes) {
        try {
            bookService.addBookToUserLibrary(id, shelfName);
            redirectAttributes.addFlashAttribute("success", "Książka została dodana do biblioteczki!");
            return "redirect:/books/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Błąd podczas dodawania książki do biblioteczki: " + e.getMessage());
            return "redirect:/books/" + id;
        }
    }
    
    @PostMapping("/books/{id}/reviews")
    public String addReview(@PathVariable Long id, @Valid @ModelAttribute ReviewDto reviewDto, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Błąd walidacji recenzji");
            return "redirect:/books/" + id;
        }
        try {
            reviewService.createReview(id, reviewDto);
            
            // Utwórz ocenę w osobnej transakcji, jeśli została podana
            if (reviewDto.getRatingValue() != null && reviewDto.getRatingValue() >= 1 && reviewDto.getRatingValue() <= 5) {
                try {
                    reviewService.createRatingAfterReview(id, reviewDto.getRatingValue());
                } catch (Exception e) {
                    // Jeśli nie udało się utworzyć oceny, nie przerywaj - recenzja już jest zapisana
                    System.err.println("Warning: Could not create rating for review: " + e.getMessage());
                }
            }
            
            redirectAttributes.addFlashAttribute("success", "Recenzja została dodana!");
            return "redirect:/books/" + id;
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Masz już recenzję dla tej książki");
            return "redirect:/books/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Błąd podczas dodawania recenzji: " + e.getMessage());
            return "redirect:/books/" + id;
        }
    }
}
