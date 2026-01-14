package com.booklovers.web.controller;

import com.booklovers.dto.AuthorDto;
import com.booklovers.dto.BookDto;
import com.booklovers.dto.RatingDto;
import com.booklovers.dto.ReviewDto;
import com.booklovers.dto.UserDto;
import com.booklovers.exception.ResourceNotFoundException;
import com.booklovers.service.author.AuthorService;
import com.booklovers.service.book.BookService;
import com.booklovers.service.rating.RatingService;
import com.booklovers.service.review.ReviewService;
import com.booklovers.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
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
                .orElseThrow(() -> new ResourceNotFoundException("Book", id));
        
        List<ReviewDto> reviews = reviewService.getReviewsByBookId(id);
        List<RatingDto> ratings = ratingService.getRatingsByBookId(id);
        
        Map<Long, Integer> ratingMap = ratings.stream()
                .collect(Collectors.toMap(
                        RatingDto::getUserId,
                        RatingDto::getValue,
                        (existing, replacement) -> existing
                ));
        
        reviews.forEach(review -> {
            if (review.getUserId() != null && ratingMap.containsKey(review.getUserId())) {
                review.setRatingValue(ratingMap.get(review.getUserId()));
            }
        });
        
        UserDto currentUser = null;
        List<String> userShelves = List.of();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() 
            && !authentication.getName().equals("anonymousUser")) {
            try {
                currentUser = userService.getCurrentUser();
                userShelves = bookService.getUserShelves(currentUser.getId());
            } catch (Exception e) {
                log.debug("Could not get current user: {}", e.getMessage());
            }
        }
        
        model.addAttribute("book", book);
        model.addAttribute("reviews", reviews);
        model.addAttribute("reviewDto", new ReviewDto());
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("userShelves", userShelves);
        return "book-details";
    }
    
    @GetMapping("/my-books")
    public String myBooksPage(Model model, @RequestParam(required = false) String shelf) {
        UserDto currentUser = userService.getCurrentUser();
        List<String> shelves = bookService.getUserShelves(currentUser.getId());
        List<String> defaultShelves = bookService.getDefaultShelves();
        
        List<BookDto> books;
        if (shelf != null && !shelf.isEmpty()) {
            books = bookService.getUserBooksByShelf(currentUser.getId(), shelf);
        } else {
            books = bookService.getUserBooks(currentUser.getId());
        }
        
        model.addAttribute("books", books);
        model.addAttribute("shelves", shelves);
        model.addAttribute("defaultShelves", defaultShelves);
        model.addAttribute("currentShelf", shelf);
        return "my-books";
    }
    
    @GetMapping("/my-books/shelves/{shelfName}")
    public String shelfBooksPage(@PathVariable String shelfName, Model model) {
        UserDto currentUser = userService.getCurrentUser();
        List<BookDto> books = bookService.getUserBooksByShelf(currentUser.getId(), shelfName);
        List<String> shelves = bookService.getUserShelves(currentUser.getId());
        List<String> defaultShelves = bookService.getDefaultShelves();
        
        model.addAttribute("books", books);
        model.addAttribute("shelves", shelves);
        model.addAttribute("defaultShelves", defaultShelves);
        model.addAttribute("currentShelf", shelfName);
        return "my-books";
    }
    
    @PostMapping("/my-books/shelves/create")
    public String createShelf(@RequestParam String shelfName, RedirectAttributes redirectAttributes) {
        if (shelfName == null || shelfName.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Nazwa półki nie może być pusta");
            return "redirect:/my-books";
        }
        
        List<String> defaultShelves = bookService.getDefaultShelves();
        if (defaultShelves.contains(shelfName.trim())) {
            redirectAttributes.addFlashAttribute("error", "Ta półka już istnieje jako domyślna");
            return "redirect:/my-books";
        }
        
        redirectAttributes.addFlashAttribute("success", "Półka '" + shelfName.trim() + "' została utworzona. Dodaj książki, aby zobaczyć ją na liście.");
        return "redirect:/my-books";
    }
    
    @PostMapping("/my-books/shelves/{shelfName}/delete")
    public String deleteShelf(@PathVariable String shelfName, RedirectAttributes redirectAttributes) {
        try {
            UserDto currentUser = userService.getCurrentUser();
            bookService.deleteShelf(currentUser.getId(), shelfName);
            redirectAttributes.addFlashAttribute("success", "Półka '" + shelfName + "' została usunięta");
        } catch (com.booklovers.exception.BadRequestException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            log.error("Error deleting shelf: ", e);
            redirectAttributes.addFlashAttribute("error", "Wystąpił błąd podczas usuwania półki: " + e.getMessage());
        }
        return "redirect:/my-books";
    }
    
    @PostMapping("/my-books/{bookId}/move")
    public String moveBookToShelf(@PathVariable Long bookId, 
                                  @RequestParam String fromShelf,
                                  @RequestParam String toShelf,
                                  RedirectAttributes redirectAttributes) {
        try {
            bookService.moveBookToShelf(bookId, fromShelf, toShelf);
            redirectAttributes.addFlashAttribute("success", "Książka została przeniesiona z '" + fromShelf + "' do '" + toShelf + "'");
        } catch (com.booklovers.exception.ConflictException e) {
            redirectAttributes.addFlashAttribute("error", "Książka już znajduje się w tej półce");
        } catch (Exception e) {
            log.error("Error moving book: ", e);
            redirectAttributes.addFlashAttribute("error", "Wystąpił błąd podczas przenoszenia książki: " + e.getMessage());
        }
        return "redirect:/my-books?shelf=" + toShelf;
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
        bookService.createBook(bookDto);
        redirectAttributes.addFlashAttribute("success", "Książka została dodana pomyślnie!");
        return "redirect:/books";
    }
    
    @PostMapping("/books/{id}/add-to-library")
    public String addBookToLibrary(@PathVariable Long id, @RequestParam(defaultValue = "Moja biblioteczka") String shelfName, RedirectAttributes redirectAttributes) {
        try {
            bookService.addBookToUserLibrary(id, shelfName);
            redirectAttributes.addFlashAttribute("success", "Książka została dodana do półki '" + shelfName + "'!");
        } catch (com.booklovers.exception.ConflictException e) {
            redirectAttributes.addFlashAttribute("error", "Książka już znajduje się w tej półce");
        } catch (Exception e) {
            log.error("Error adding book to library: ", e);
            redirectAttributes.addFlashAttribute("error", "Wystąpił błąd podczas dodawania książki: " + e.getMessage());
        }
        return "redirect:/books/" + id;
    }
    
    @PostMapping("/books/{id}/remove-from-library")
    public String removeBookFromLibrary(@PathVariable Long id, @RequestParam String shelfName, RedirectAttributes redirectAttributes) {
        try {
            bookService.removeBookFromUserLibrary(id, shelfName);
            redirectAttributes.addFlashAttribute("success", "Książka została usunięta z półki '" + shelfName + "'");
        } catch (Exception e) {
            log.error("Error removing book from library: ", e);
            redirectAttributes.addFlashAttribute("error", "Wystąpił błąd podczas usuwania książki: " + e.getMessage());
        }
        return "redirect:/my-books?shelf=" + shelfName;
    }
    
    @PostMapping("/books/{id}/reviews")
    public String addReview(@PathVariable Long id, @Valid @ModelAttribute ReviewDto reviewDto, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Błąd walidacji recenzji");
            return "redirect:/books/" + id;
        }
        
        try {
            reviewService.createReview(id, reviewDto);
            
            if (reviewDto.getRatingValue() != null && reviewDto.getRatingValue() >= 1 && reviewDto.getRatingValue() <= 5) {
                try {
                    reviewService.createRatingAfterReview(id, reviewDto.getRatingValue());
                } catch (Exception e) {
                    log.warn("Could not create rating for review: {}", e.getMessage());
                }
            }
            
            redirectAttributes.addFlashAttribute("success", "Recenzja została dodana!");
        } catch (com.booklovers.exception.ConflictException e) {
            redirectAttributes.addFlashAttribute("error", "Już dodałeś recenzję do tej książki. Możesz edytować istniejącą recenzję.");
        } catch (Exception e) {
            log.error("Error adding review: ", e);
            redirectAttributes.addFlashAttribute("error", "Wystąpił błąd podczas dodawania recenzji: " + e.getMessage());
        }
        
        return "redirect:/books/" + id;
    }
    
    @GetMapping("/books/{bookId}/reviews/{reviewId}/edit")
    public String editReviewForm(@PathVariable Long bookId, @PathVariable Long reviewId, Model model) {
        ReviewDto review = reviewService.getReviewById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", reviewId));
        BookDto book = bookService.getBookById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", bookId));
        
        UserDto currentUser = userService.getCurrentUser();
        if (currentUser.getRole() != com.booklovers.entity.User.Role.ADMIN && !review.getUserId().equals(currentUser.getId())) {
            throw new com.booklovers.exception.ForbiddenException("You can only edit your own reviews");
        }
        
        model.addAttribute("book", book);
        model.addAttribute("reviewDto", review);
        return "edit-review";
    }
    
    @PostMapping("/books/{bookId}/reviews/{reviewId}/edit")
    public String updateReview(@PathVariable Long bookId, @PathVariable Long reviewId, 
                               @Valid @ModelAttribute ReviewDto reviewDto, BindingResult result,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Błąd walidacji recenzji");
            return "redirect:/books/" + bookId + "/reviews/" + reviewId + "/edit";
        }
        
        try {
            UserDto currentUser = userService.getCurrentUser();
            ReviewDto review = reviewService.getReviewById(reviewId)
                    .orElseThrow(() -> new ResourceNotFoundException("Review", reviewId));
            
            if (currentUser.getRole() == com.booklovers.entity.User.Role.ADMIN) {
                reviewService.updateReviewAsAdmin(reviewId, reviewDto);
            } else if (review.getUserId().equals(currentUser.getId())) {
                reviewService.updateReview(reviewId, reviewDto);
            } else {
                throw new com.booklovers.exception.ForbiddenException("You can only edit your own reviews");
            }
            
            if (reviewDto.getRatingValue() != null && reviewDto.getRatingValue() >= 1 && reviewDto.getRatingValue() <= 5) {
                try {
                    reviewService.createRatingAfterReview(bookId, reviewDto.getRatingValue());
                } catch (Exception e) {
                    log.warn("Could not update rating for review: {}", e.getMessage());
                }
            }
            
            redirectAttributes.addFlashAttribute("success", "Recenzja została zaktualizowana!");
        } catch (com.booklovers.exception.ForbiddenException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            log.error("Error updating review: ", e);
            redirectAttributes.addFlashAttribute("error", "Wystąpił błąd podczas aktualizacji recenzji: " + e.getMessage());
        }
        
        return "redirect:/books/" + bookId;
    }
    
    @PostMapping("/books/{bookId}/reviews/{reviewId}/delete")
    public String deleteReview(@PathVariable Long bookId, @PathVariable Long reviewId,
                               RedirectAttributes redirectAttributes) {
        try {
            UserDto currentUser = userService.getCurrentUser();
            ReviewDto review = reviewService.getReviewById(reviewId)
                    .orElseThrow(() -> new ResourceNotFoundException("Review", reviewId));
            
            if (currentUser.getRole() == com.booklovers.entity.User.Role.ADMIN) {
                reviewService.deleteReviewAsAdmin(reviewId);
            } else if (review.getUserId().equals(currentUser.getId())) {
                reviewService.deleteReview(reviewId);
            } else {
                throw new com.booklovers.exception.ForbiddenException("You can only delete your own reviews");
            }
            
            redirectAttributes.addFlashAttribute("success", "Recenzja została usunięta!");
        } catch (com.booklovers.exception.ForbiddenException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            log.error("Error deleting review: ", e);
            redirectAttributes.addFlashAttribute("error", "Wystąpił błąd podczas usuwania recenzji: " + e.getMessage());
        }
        
        return "redirect:/books/" + bookId;
    }
}
