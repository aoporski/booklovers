package com.booklovers.service.stats;

import com.booklovers.dto.BookStatsDto;
import com.booklovers.dto.StatsDto;
import com.booklovers.dto.UserStatsDto;

public interface StatsService {
    StatsDto getGlobalStats();
    UserStatsDto getUserStats(Long userId);
    BookStatsDto getBookStats(Long bookId);
}
