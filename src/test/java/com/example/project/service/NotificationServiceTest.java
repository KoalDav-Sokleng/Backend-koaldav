package com.example.project.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NotificationServiceTest {

    @Test
    void resolveDefaultEmail_returnsGmailWhenBlank() {
        NotificationService service = new NotificationService(null, null, null, null, null, null, null);

        assertEquals("sreyleng143@gmail.com", service.resolveDefaultEmail(""));
        assertEquals("sreyleng143@gmail.com", service.resolveDefaultEmail("   "));
        assertEquals("someone@gmail.com", service.resolveDefaultEmail("someone@gmail.com"));
    }
}
