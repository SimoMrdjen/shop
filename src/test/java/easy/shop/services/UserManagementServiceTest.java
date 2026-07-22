package easy.shop.services;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;

class UserManagementServiceTest {

    @InjectMocks
    UserManagementService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() {
    }
    
    @Test
    void shouldReturnTrueIfBlank() {
        String value = "";
        assertEquals(true, service.isBlank(value));

        String nullValue = null;
        assertEquals(true, service.isBlank(nullValue));
    }

    @Test
    void shouldReturnFalseIfNotBlank() {
        String value = "a";
        assertEquals(false, service.isBlank(value));

    }
}