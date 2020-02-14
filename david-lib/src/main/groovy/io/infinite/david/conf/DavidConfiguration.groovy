package io.infinite.david.conf

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.ObjectMapper
import io.infinite.blackbox.BlackBox
import org.springframework.core.io.FileSystemResource

@BlackBox
class DavidConfiguration {

    @JsonIgnore
    static DavidConfiguration conf

    static initConfiguration(FileSystemResource davidConfigResource) {
        conf = new ObjectMapper().readValue(davidConfigResource.getFile().getText(), DavidConfiguration.class)
    }

    static initConfiguration(String jsonConfig) {
        conf = new ObjectMapper().readValue(jsonConfig, DavidConfiguration.class)
    }

    String botUsername

    String botToken

    String telegramProxyHost

    Integer telegramProxyPort

    Integer telegramAdminId

    Map<String, Object> resourceProperties = new HashMap<>()

    String pluginsDir

}
