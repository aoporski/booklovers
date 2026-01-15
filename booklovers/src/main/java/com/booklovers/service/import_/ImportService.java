package com.booklovers.service.import_;

import com.booklovers.dto.UserDataExportDto;

public interface ImportService {
    void importUserDataFromJson(Long userId, String jsonData);
    void importUserDataFromCsv(Long userId, String csvData);
}
