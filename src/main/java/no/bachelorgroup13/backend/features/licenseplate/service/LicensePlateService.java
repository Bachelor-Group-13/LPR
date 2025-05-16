package no.bachelorgroup13.backend.features.licenseplate.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import no.bachelorgroup13.backend.features.licenseplate.azurecv.LicensePlateProperties;
import no.bachelorgroup13.backend.features.licenseplate.azurecv.model.AnalyzeResult;
import no.bachelorgroup13.backend.features.licenseplate.azurecv.model.Line;
import no.bachelorgroup13.backend.features.licenseplate.azurecv.model.ReadResponse;
import no.bachelorgroup13.backend.features.licenseplate.azurecv.model.ReadResult;
import no.bachelorgroup13.backend.features.licenseplate.dto.PlateDto;
import org.springframework.stereotype.Service;

@Service
public class LicensePlateService {

    // Regex for plates (e.g. AB12345)
    private static final Pattern PLATE_REGEX = Pattern.compile("(?i)^[A-Z]{2}[- ]?\\d{5}$");

    private final String endpoint;
    private final String subscriptionKey;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LicensePlateService(LicensePlateProperties properties) {
        this.endpoint = properties.getEndpoint();
        this.subscriptionKey = properties.getKey();
    }

    /**
     * Method to POST the image to Azure, poll for the read results, extract plates
     * via regex
     */
    public List<PlateDto> getLicensePlates(File imageFile)
            throws IOException, InterruptedException {
        // Send image & get operation location
        String operationLocation = sendImageAndGetOperationLocation(imageFile);

        // Poll for the result
        ReadResponse readResponse = pollReadResult(operationLocation);

        // Extract license plates
        return extractPlatesFromResponse(readResponse);
    }

    /**
     * POSTs the image to the Azure "read/analyze" endpoint, returns the
     * "Operation-Location" header.
     */
    private String sendImageAndGetOperationLocation(File imageFile) throws IOException {
        URI analyzeUri = URI.create(endpoint + "/vision/v3.2/read/analyze");
        HttpURLConnection connection = (HttpURLConnection) analyzeUri.toURL().openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Ocp-Apim-Subscription-Key", subscriptionKey);
        connection.setRequestProperty("Content-Type", "application/octet-stream");
        connection.setDoOutput(true);

        // Write image bytes
        try (FileInputStream inputStream = new FileInputStream(imageFile);
                OutputStream outputStream = connection.getOutputStream()) {
            byte[] buffer = new byte[4096];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
        }

        int responseCode = connection.getResponseCode();
        // 202 => we expect an Operation-Location header
        if (responseCode != 202) {
            String errorMessage = readStream(connection.getErrorStream());
            throw new IOException(
                    "Failed to send image to Azure. HTTP " + responseCode + ": " + errorMessage);
        }

        String operationLocation = connection.getHeaderField("Operation-Location");
        if (operationLocation == null || operationLocation.isEmpty()) {
            throw new IOException("Failed to get operation location from Azure response");
        }
        return operationLocation;
    }

    /**
     * Polls the operation location until the status is no longer "notStarted" or
     * "running".
     */
    private ReadResponse pollReadResult(String operationLocation)
            throws IOException, InterruptedException {
        int maxAttempts = 10;
        int attempt = 0;
        long backoffMs = 1000; // Start with 1 second

        while (attempt < maxAttempts) {
            URI operationUri = URI.create(operationLocation);
            HttpURLConnection connection =
                    (HttpURLConnection) operationUri.toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Ocp-Apim-Subscription-Key", subscriptionKey);

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                String errorMessage = readStream(connection.getErrorStream());
                throw new IOException(
                        "Error polling read result. HTTP " + responseCode + ": " + errorMessage);
            }

            String json = readStream(connection.getInputStream());
            ReadResponse response = objectMapper.readValue(json, ReadResponse.class);

            if (!"notStarted".equalsIgnoreCase(response.getStatus())
                    && !"running".equalsIgnoreCase(response.getStatus())) {
                return response;
            }

            attempt++;
            if (attempt < maxAttempts) {
                Thread.sleep(backoffMs);
                backoffMs =
                        Math.min(backoffMs * 2, 10000); // Double the backoff time, max 10 seconds
            }
        }
        throw new IOException("Max polling attempts reached");
    }

    /**
     * Extracts lines from the response, applies the plate regex, and returns any
     * matches.
     */
    private List<PlateDto> extractPlatesFromResponse(ReadResponse readResponse) {
        List<PlateDto> plates = new ArrayList<>();

        if ("succeeded".equalsIgnoreCase(readResponse.getStatus())
                && readResponse.getAnalyzeResult() != null) {

            AnalyzeResult analyzeResult = readResponse.getAnalyzeResult();
            if (analyzeResult.getReadResults() != null) {
                for (ReadResult readResult : analyzeResult.getReadResults()) {
                    if (readResult.getLines() != null) {
                        for (Line line : readResult.getLines()) {
                            String candidate = line.getText();
                            // remove spaces, dashes, colons
                            candidate = candidate.replaceAll("\\s+", "");
                            candidate = candidate.replaceAll("-", "");
                            candidate = candidate.replaceAll(":", "");

                            if (PLATE_REGEX.matcher(candidate).matches()) {
                                plates.add(
                                        new PlateDto(
                                                candidate,
                                                Arrays.stream(line.getBoundingBox())
                                                        .boxed()
                                                        .collect(Collectors.toList())));
                            }
                        }
                    }
                }
            }
        }

        return plates;
    }

    /** Reads an InputStream into a string */
    private String readStream(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return "";
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            return builder.toString();
        }
    }
}
