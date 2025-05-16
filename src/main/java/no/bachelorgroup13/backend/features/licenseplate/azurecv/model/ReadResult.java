package no.bachelorgroup13.backend.features.licenseplate.azurecv.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * Represents a single read result from Azure Computer Vision.
 * Contains a list of detected text lines in the image.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReadResult {
    private List<Line> lines;

    /**
     * Gets the list of detected text lines.
     * @return List of text lines
     */
    public List<Line> getLines() {
        return lines;
    }

    /**
     * Sets the list of detected text lines.
     * @param lines List of text lines
     */
    public void setLines(List<Line> lines) {
        this.lines = lines;
    }
}
