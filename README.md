# Useful Functions as a Service

A small Proof of Concept showing the use of Spring Cloud Function and Spring Cloud Stream.

## Prerequisite

* Tesseract + language packs.
* RabbitMQ bound to localhost:5672 with default credentials (guest/guest).
Tune the values in `application.properties` accordingly.

## Usage

```
mvn spring-boot:run
```

```shell
curl -v -F body=@for_ocr.png -H "X-ocr-file-type: png" -H "X-ocr-language: nor" http://localhost:8080/functions/ocr
# -> will output the OCR'ed text directly.
```
