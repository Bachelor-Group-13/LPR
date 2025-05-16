package no.bachelorgroup13.backend.features.licenseplate.azurecv;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Azure Cognitive Services.
 * Stores endpoint and subscription key for license plate recognition.
 */
@ConfigurationProperties(prefix = "azure.cognitiveservices")
public class LicensePlateProperties {
    private String endpoint;
    private String key;

    /**
     * Gets the Azure Cognitive Services endpoint URL.
     * @return Endpoint URL
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the Azure Cognitive Services endpoint URL.
     * @param endpoint Endpoint URL
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * Gets the Azure Cognitive Services subscription key.
     * @return Subscription key
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets the Azure Cognitive Services subscription key.
     * @param key Subscription key
     */
    public void setKey(String key) {
        this.key = key;
    }
}
