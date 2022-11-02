package org.sral.functions.ocr.fn

import com.google.common.base.Stopwatch
import net.sourceforge.tess4j.Tesseract
import org.springframework.context.annotation.Bean
import org.springframework.integration.support.MessageBuilder
import org.springframework.messaging.Message
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import org.sral.functions.ocr.Configuration
import java.io.File

@Service
class OpticalCharacterRecognition {

    object Headers {
        const val FILE_TYPE = "x-ocr-file-type"
        const val LANGUAGE = "x-ocr-language"
    }

    @Bean
    fun ocr(config: Configuration) : (Message<MultipartFile>) -> Message<String> {
        return { input ->
            val tesseract = Tesseract()

            tesseract.setVariable("debug_file", config.debugFile)

            val resolution = if (input.headers.containsKey("x-ocr-resolution")) {
                if (input.headers["x-ocr-resolution"] is Int) {
                    input.headers["x-ocr-resolution"] as Int
                } else {
                    input.headers["x-ocr-resolution"].toString().toInt()
                }
            } else {
                config.defaultResolution
            }

            tesseract.setVariable("user_defined_dpi", resolution.toString())
            tesseract.setDatapath(config.tesseractDataPath)
            val fileType = if (input.headers.containsKey(Headers.FILE_TYPE)) {
                when (val value = input.headers[Headers.FILE_TYPE]) {
                    is String -> {
                        value
                    }

                    is List<*> -> {
                        value.first() as String
                    }

                    else -> {
                        config.defaultFileType
                    }
                }
            } else {
                config.defaultFileType
            }

            val language = if (input.headers.containsKey(Headers.LANGUAGE)) {
                when (val value = input.headers[Headers.LANGUAGE]) {
                    is String -> {
                        value
                    }

                    is List<*> -> {
                        value.first() as String
                    }

                    else -> {
                        config.defaultLanguage
                    }
                }
            } else {
                config.defaultLanguage
            }
            tesseract.setLanguage(language)

            val tempFile = File.createTempFile("for_ocr", ".$fileType").apply {
                writeBytes(input.payload.bytes)
            }

            try {
                val stopwatch = Stopwatch.createStarted()
                val result = tesseract.doOCR(tempFile)
                stopwatch.stop()

                MessageBuilder.withPayload(result)
                    .setHeader("x-request-id", input.headers.id)
                    .setHeader("x-processing-time-ms", stopwatch.elapsed().toMillis())
                    .build()
            } catch (e: Throwable) {
                println("Error: $e")
                MessageBuilder.withPayload("Error doing OCR on the input.")
                    .setHeader("request-id", input.headers.id)
                    .build()
            } finally {
                tempFile.delete()
            }
        }
    }
}