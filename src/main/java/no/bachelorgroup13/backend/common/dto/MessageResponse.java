package no.bachelorgroup13.backend.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Data transfer object for simple message responses.
 * Used to return string messages in API responses.
 */
@Data
@AllArgsConstructor
public class MessageResponse {
    @Schema(description = "The message to be returned.")
    private String message;
}
