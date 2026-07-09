package com.icerikfabrikasi.platform;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Uygulama context'inin başarıyla yüklendiğini doğrular.
 */
@SpringBootTest
@ActiveProfiles("test")
class IcerikFabrikasiApplicationTest {

    @Test
    void contextLoads() {
        // Context başarıyla yüklenirse test geçer
    }
}
