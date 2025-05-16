package no.bachelorgroup13.backend.features.licenseplate.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object for license plate recognition results.
 * Contains the recognized text and bounding box coordinates.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlateDto {
    @Schema(description = "The recognized license plate text.")
    private String text;

    @Schema(description = "The confidence score of the recognition.")
    private List<Integer> bbox;
}
