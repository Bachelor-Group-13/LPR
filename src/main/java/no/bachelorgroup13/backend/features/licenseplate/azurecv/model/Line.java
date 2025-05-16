package no.bachelorgroup13.backend.features.licenseplate.azurecv.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a line of text detected in an image by Azure Computer Vision.
 * Contains the text content and its bounding box coordinates.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Line {
    private String text;

    @JsonProperty("boundingBox")
    private int[] boundingBox;

    /**
     * Gets the detected text content.
     * @return Text content
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the detected text content.
     * @param text Text content
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Gets the bounding box coordinates of the text.
     * @return Array of coordinates [x1, y1, x2, y2]
     */
    public int[] getBoundingBox() {
        return boundingBox;
    }

    /**
     * Sets the bounding box coordinates of the text.
     * @param boundingBox Array of coordinates [x1, y1, x2, y2]
     */
    public void setBoundingBox(int[] boundingBox) {
        this.boundingBox = boundingBox;
    }
}
