package no.bachelorgroup13.backend.features.licenseplate.azurecv.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * Represents the analysis result from Azure Computer Vision API.
 * Contains a list of read results from the image analysis.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnalyzeResult {
    private List<ReadResult> readResults;

    /**
     * Gets the list of read results from the analysis.
     * @return List of read results
     */
    public List<ReadResult> getReadResults() {
        return readResults;
    }

    /**
     * Sets the list of read results from the analysis.
     * @param readResults List of read results
     */
    public void setReadResults(List<ReadResult> readResults) {
        this.readResults = readResults;
    }
}
