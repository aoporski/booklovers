package com.booklovers.service.stats;

import com.booklovers.dto.StatsDto;
import com.booklovers.dto.UserStatsDto;

public interface StatsService {
    StatsDto getGlobalStats();
    UserStatsDto getUserStats(Long userId);
}
