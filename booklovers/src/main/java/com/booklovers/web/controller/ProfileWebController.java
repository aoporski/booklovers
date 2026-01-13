package com.booklovers.web.controller;

import com.booklovers.dto.UserDto;
import com.booklovers.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileWebController {
    
    private final UserService userService;
    
    @GetMapping
    public String profilePage(Model model) {
        try {
            UserDto user = userService.getCurrentUser();
            model.addAttribute("user", user);
            model.addAttribute("userDto", user);
            return "profile";
        } catch (Exception e) {
            return "redirect:/login";
        }
    }
    
    @PostMapping
    public String updateProfile(UserDto userDto) {
        try {
            userService.updateUser(userDto);
            return "redirect:/profile?updated";
        } catch (Exception e) {
            return "redirect:/profile?error";
        }
    }
}
