package com.tokio.financialtransfer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Application Context Tests")
class FinancialTransferApplicationTest {

    @Test
    @DisplayName("Should load application context successfully")
    void shouldLoadApplicationContextSuccessfully() {}
}
