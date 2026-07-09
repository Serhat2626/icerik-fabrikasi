package com.icerikfabrikasi.platform.common.api;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ApiResponse factory method testleri.
 */
class ApiResponseTest {

    @Test
    void success_withData_shouldSetFieldsCorrectly() {
        ApiResponse<String> response = ApiResponse.success("test-data");

        assertTrue(response.isSuccess());
        assertEquals("test-data", response.getData());
        assertNull(response.getMessage());
        assertNull(response.getErrors());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void success_withDataAndMessage_shouldSetFieldsCorrectly() {
        ApiResponse<String> response = ApiResponse.success("test-data", "İşlem başarılı");

        assertTrue(response.isSuccess());
        assertEquals("test-data", response.getData());
        assertEquals("İşlem başarılı", response.getMessage());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void error_withMessage_shouldSetFieldsCorrectly() {
        ApiResponse<Void> response = ApiResponse.error("Bir hata oluştu");

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertEquals("Bir hata oluştu", response.getMessage());
        assertNull(response.getErrors());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void error_withFieldErrors_shouldIncludeErrors() {
        List<FieldError> fieldErrors = List.of(
                FieldError.builder().field("isim").message("İsim boş olamaz").build(),
                FieldError.builder().field("email").message("Geçersiz e-posta").build()
        );

        ApiResponse<Void> response = ApiResponse.error("Validasyon hatası", fieldErrors);

        assertFalse(response.isSuccess());
        assertEquals("Validasyon hatası", response.getMessage());
        assertNotNull(response.getErrors());
        assertEquals(2, response.getErrors().size());
        assertEquals("isim", response.getErrors().get(0).getField());
        assertEquals("İsim boş olamaz", response.getErrors().get(0).getMessage());
    }

    @Test
    void success_withNullData_shouldHandleGracefully() {
        ApiResponse<Object> response = ApiResponse.success(null);

        assertTrue(response.isSuccess());
        assertNull(response.getData());
        assertNotNull(response.getTimestamp());
    }
}
