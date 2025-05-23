package no.bachelorgroup13.backend.features.licenseplate.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.File;
import java.io.IOException;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import no.bachelorgroup13.backend.features.licenseplate.dto.PlateDto;
import no.bachelorgroup13.backend.features.licenseplate.service.LicensePlateService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller for license plate recognition operations.
 * Handles image upload and license plate detection.
 */
@RestController
@RequestMapping("/license-plate")
@Tag(name = "License Plate", description = "Endpoints for license plate recognition.")
public class LicensePlateController {
    private final LicensePlateService computerVisionService;

    /**
     * Constructs a new LicensePlateController.
     * @param computerVisionService Service for license plate recognition
     */
    public LicensePlateController(LicensePlateService computerVisionService) {
        this.computerVisionService = computerVisionService;
    }

    /**
     * Recognizes license plates from an uploaded image.
     * @param image Multipart image file
     * @return List of recognized license plates or error message
     */
    @Operation(summary = "Recognize license plate from image")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> recognizePlate(@RequestParam("image") MultipartFile image) {
        try {
            File tempFile = File.createTempFile("image", ".jpg");
            image.transferTo(tempFile);

            List<PlateDto> plates = computerVisionService.getLicensePlates(tempFile);

            return ResponseEntity.ok(new LicensePlatesResponse(plates));

        } catch (IOException | IllegalStateException | InterruptedException e) {
            return ResponseEntity.status(500)
                    .body("Error recognizing license plate:" + e.getMessage());
        }
    }

    /**
     * Response wrapper for license plate recognition results.
     */
    @Setter
    @Getter
    static class LicensePlatesResponse {
        /**
         *  Gets and sets the list of recognized license plates.
         */
        private List<PlateDto> license_plates;

        /**
         * Creates a new LicensePlatesResponse.
         * @param plates List of recognized license plates
         */
        public LicensePlatesResponse(List<PlateDto> plates) {
            this.license_plates = plates;
        }
    }
}
