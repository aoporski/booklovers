package com.booklovers.service.export;

import com.booklovers.dto.UserDataExportDto;

public interface ExportService {
    UserDataExportDto exportUserData(Long userId);
    String exportUserDataAsJson(Long userId);
    String exportUserDataAsCsv(Long userId);
}
