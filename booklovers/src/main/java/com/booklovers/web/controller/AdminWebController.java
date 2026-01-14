package com.booklovers.web.controller;

import com.booklovers.dto.AuthorDto;
import com.booklovers.dto.BookDto;
import com.booklovers.dto.ReviewDto;
import com.booklovers.dto.UserDto;
import com.booklovers.exception.ResourceNotFoundException;
import com.booklovers.service.author.AuthorService;
import com.booklovers.service.book.BookService;
import com.booklovers.service.review.ReviewService;
import com.booklovers.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminWebController {
    
    private final BookService bookService;
    private final UserService userService;
    private final AuthorService authorService;
    private final ReviewService reviewService;
    
    @GetMapping
    public String adminPanel(Model model) {
        List<BookDto> books = bookService.getAllBooks();
        List<UserDto> users = userService.getAllUsers();
        List<AuthorDto> authors = authorService.getAllAuthors();
        List<ReviewDto> reviews = reviewService.getAllReviews();
        model.addAttribute("books", books);
        model.addAttribute("users", users);
        model.addAttribute("authors", authors);
        model.addAttribute("reviews", reviews);
        return "admin";
    }
    
    // ========== BOOK MANAGEMENT ==========
    
    @GetMapping("/books/{id}/edit")
    public String editBookForm(@PathVariable Long id, Model model) {
        BookDto book = bookService.getBookById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book", id));
        List<AuthorDto> authors = authorService.getAllAuthors();
        model.addAttribute("bookDto", book);
        model.addAttribute("authors", authors);
        return "edit-book";
    }
    
    @PostMapping("/books/{id}/edit")
    public String updateBook(@PathVariable Long id, @Valid @ModelAttribute BookDto bookDto, 
                            BindingResult result, RedirectAttributes redirectAttributes, Model model) {
        if (result.hasErrors()) {
            List<AuthorDto> authors = authorService.getAllAuthors();
            model.addAttribute("authors", authors);
            return "edit-book";
        }
        bookService.updateBook(id, bookDto);
        redirectAttributes.addFlashAttribute("success", "Książka została zaktualizowana!");
        return "redirect:/admin";
    }
    
    @PostMapping("/books/{id}/delete")
    public String deleteBook(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        bookService.deleteBook(id);
        redirectAttributes.addFlashAttribute("success", "Książka została usunięta!");
        return "redirect:/admin";
    }
    
    @PostMapping("/users/{id}/block")
    public String blockUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userService.blockUser(id);
        redirectAttributes.addFlashAttribute("success", "Użytkownik został zablokowany!");
        return "redirect:/admin";
    }
    
    @PostMapping("/users/{id}/unblock")
    public String unblockUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userService.unblockUser(id);
        redirectAttributes.addFlashAttribute("success", "Użytkownik został odblokowany!");
        return "redirect:/admin";
    }
    
    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userService.deleteUser(id);
        redirectAttributes.addFlashAttribute("success", "Użytkownik został usunięty!");
        return "redirect:/admin";
    }
    
    @GetMapping("/authors/add")
    public String addAuthorForm(Model model) {
        model.addAttribute("authorDto", new AuthorDto());
        return "add-author";
    }
    
    @PostMapping("/authors/add")
    public String addAuthor(@Valid @ModelAttribute AuthorDto authorDto, BindingResult result, 
                           RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "add-author";
        }
        authorService.createAuthor(authorDto);
        redirectAttributes.addFlashAttribute("success", "Autor został dodany!");
        return "redirect:/admin";
    }
    
    @GetMapping("/authors/{id}/edit")
    public String editAuthorForm(@PathVariable Long id, Model model) {
        AuthorDto author = authorService.getAuthorById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author", id));
        model.addAttribute("authorDto", author);
        return "edit-author";
    }
    
    @PostMapping("/authors/{id}/edit")
    public String updateAuthor(@PathVariable Long id, @Valid @ModelAttribute AuthorDto authorDto,
                              BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "edit-author";
        }
        authorService.updateAuthor(id, authorDto);
        redirectAttributes.addFlashAttribute("success", "Autor został zaktualizowany!");
        return "redirect:/admin";
    }
    
    @PostMapping("/authors/{id}/delete")
    public String deleteAuthor(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        authorService.deleteAuthor(id);
        redirectAttributes.addFlashAttribute("success", "Autor został usunięty!");
        return "redirect:/admin";
    }
    
    @PostMapping("/reviews/{id}/delete")
    public String deleteReview(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        reviewService.deleteReviewAsAdmin(id);
        redirectAttributes.addFlashAttribute("success", "Recenzja została usunięta!");
        return "redirect:/admin";
    }
}
