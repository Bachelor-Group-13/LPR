package no.bachelorgroup13.backend.features.licenseplate.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.File;
import java.io.IOException;
import java.util.List;
import no.bachelorgroup13.backend.features.licenseplate.dto.PlateDto;
import no.bachelorgroup13.backend.features.licenseplate.service.LicensePlateService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/license-plate")
@Tag(name = "License Plate", description = "Endpoints for license plate recognition.")
public class LicensePlateController {
    private final LicensePlateService computerVisionService;

    public LicensePlateController(LicensePlateService computerVisionService) {
        this.computerVisionService = computerVisionService;
    }

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

    static class LicensePlatesResponse {
        private List<PlateDto> license_plates;

        public LicensePlatesResponse(List<PlateDto> plates) {
            this.license_plates = plates;
        }

        public List<PlateDto> getLicense_plates() {
            return license_plates;
        }

        public void setLicense_plates(List<PlateDto> license_plates) {
            this.license_plates = license_plates;
        }
    }
}
