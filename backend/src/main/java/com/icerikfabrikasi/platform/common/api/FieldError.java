package com.icerikfabrikasi.platform.common.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Alan bazlı hata bilgisi.
 * Validasyon hatalarında hangi alanın hangi nedenle geçersiz olduğunu belirtir.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldError {

    private String field;
    private String message;
}
