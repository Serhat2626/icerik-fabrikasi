package com.icerikfabrikasi.platform.common.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * GlobalExceptionHandler testleri.
 *
 * <p>Standalone MockMvc kurulumu kullanır — Spring context bağımlılığı yoktur.
 * Bu sayede @WebMvcTest dilim sorunlarından etkilenmez.</p>
 *
 * <p>500 yanıtının iç bilgileri (stack trace, exception sınıf adı, dosya yolu,
 * komut, stdout/stderr/rawLog, secret/token, ham sağlayıcı verisi) açığa
 * çıkarmadığını doğrular.</p>
 */
class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestExceptionController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    /**
     * Test amaçlı exception fırlatan controller.
     */
    @RestController
    @RequestMapping("/api/test-exceptions")
    static class TestExceptionController {

        @GetMapping("/not-found")
        public void throwNotFound() {
            throw new NotFoundException("Kayıt", "test-id-123");
        }

        @GetMapping("/validation")
        public void throwValidation() {
            throw new ValidationException("Alan değeri geçersiz");
        }

        @GetMapping("/business")
        public void throwBusiness() {
            throw new BusinessException("İş kuralı ihlal edildi");
        }

        @GetMapping("/unexpected")
        public void throwUnexpected() {
            throw new RuntimeException(
                    "NullPointerException at com.icerikfabrikasi.platform.SomeService.process(SomeService.java:42) " +
                    "secret=sk-abc123 token=tok-xyz789 " +
                    "path=/internal/storage/key/file.dat " +
                    "stdout=raw-output stderr=error-output rawLog=internal-log " +
                    "command=ProcessBuilder[/bin/sh] " +
                    "provider_payload={\"model\":\"gpt-4\",\"api_key\":\"sk-secret\"}"
            );
        }
    }

    @Test
    void notFound_shouldReturn404WithMessage() throws Exception {
        mockMvc.perform(get("/api/test-exceptions/not-found")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Kayıt bulunamadı: test-id-123")));
    }

    @Test
    void validation_shouldReturn400WithMessage() throws Exception {
        mockMvc.perform(get("/api/test-exceptions/validation")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Alan değeri geçersiz")));
    }

    @Test
    void business_shouldReturn409WithMessage() throws Exception {
        mockMvc.perform(get("/api/test-exceptions/business")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("İş kuralı ihlal edildi")));
    }

    @Test
    void unexpected_shouldReturn500_andNotExposeInternals() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/test-exceptions/unexpected")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Beklenmeyen bir hata oluştu. Lütfen daha sonra tekrar deneyin.")))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();

        // Stack trace açığa çıkmamalı
        assertFalse(responseBody.contains("NullPointerException"),
                "Yanıt exception sınıf adı içermemeli");
        assertFalse(responseBody.contains(".java:"),
                "Yanıt dosya yolu/satır numarası içermemeli");
        assertFalse(responseBody.contains("SomeService"),
                "Yanıt iç sınıf adları içermemeli");

        // Secret/token açığa çıkmamalı
        assertFalse(responseBody.contains("sk-abc123"),
                "Yanıt secret içermemeli");
        assertFalse(responseBody.contains("tok-xyz789"),
                "Yanıt token içermemeli");
        assertFalse(responseBody.contains("sk-secret"),
                "Yanıt API key içermemeli");

        // Dosya yolu / storage key açığa çıkmamalı
        assertFalse(responseBody.contains("/internal/storage"),
                "Yanıt storage key içermemeli");

        // Komut / stdout / stderr / rawLog açığa çıkmamalı
        assertFalse(responseBody.contains("ProcessBuilder"),
                "Yanıt komut bilgisi içermemeli");
        assertFalse(responseBody.contains("raw-output"),
                "Yanıt stdout içermemeli");
        assertFalse(responseBody.contains("error-output"),
                "Yanıt stderr içermemeli");
        assertFalse(responseBody.contains("internal-log"),
                "Yanıt rawLog içermemeli");

        // Ham sağlayıcı verisi açığa çıkmamalı
        assertFalse(responseBody.contains("provider_payload"),
                "Yanıt sağlayıcı payload içermemeli");
        assertFalse(responseBody.contains("gpt-4"),
                "Yanıt model bilgisi içermemeli");
    }
}
