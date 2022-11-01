package org.sral.functions.ocr

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "sral.functions.ocr")
class Configuration {
    var tesseractDataPath: String = "/usr/share/tesseract-ocr/4.00/tessdata"
    var defaultLanguage: String = "eng"
    var defaultFileType: String = "pdf"
}
