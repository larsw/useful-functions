package org.sral.functions.ocr

import com.google.common.base.Stopwatch
import net.sourceforge.tess4j.Tesseract
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.integration.support.MessageBuilder
import org.springframework.messaging.Message
import org.springframework.web.multipart.MultipartFile
import java.io.File

@SpringBootApplication
class Application {

    object Headers {
        const val FILE_TYPE = "x-ocr-file-type"
        const val LANGUAGE = "x-ocr-language"
    }

    @Bean
    fun ocr(config: Configuration) : (Message<MultipartFile>) -> Message<String> {
        return { input ->
            val tesseract = Tesseract()
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

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
