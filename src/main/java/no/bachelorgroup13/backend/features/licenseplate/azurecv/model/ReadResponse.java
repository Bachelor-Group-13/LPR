package no.bachelorgroup13.backend.features.licenseplate.azurecv.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Response from Azure Computer Vision Read API.
 * Contains the status of the analysis and the analysis results.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReadResponse {
    private String status;
    private AnalyzeResult analyzeResult;

    /**
     * Gets the status of the analysis operation.
     * @return Analysis status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the status of the analysis operation.
     * @param status Analysis status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Gets the analysis results.
     * @return Analysis results
     */
    public AnalyzeResult getAnalyzeResult() {
        return analyzeResult;
    }

    /**
     * Sets the analysis results.
     * @param analyzeResult Analysis results
     */
    public void setAnalyzeResult(AnalyzeResult analyzeResult) {
        this.analyzeResult = analyzeResult;
    }
}
