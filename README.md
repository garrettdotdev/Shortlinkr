# Shortlinkr
### A simple URL shortener

## Overview
***Shortlinkr*** is a simple URL shortener built using Java and Spring Boot. It demonstrates how to design, implement, and test a scalable, maintainable web application with concurrency control and clean code practices. Note this application does not create live short links as written.

### What It Does
- Encode: Converts a long URL into a short, shareable link.
- Decode: Translates a shortened link back into the original URL.

The service processes HTTP POST requests via two endpoints:
- `/encode`: Accepts a long URL and returns a short URL.
- `/decode`: Accepts a short URL and returns the original long URL.

### How It Works

1. **User Request**: The user sends a POST request to the `/encode` endpoint with the original URL.
2. **Encoding Logic**: The application generates a short URL using Base64 encoding of the input URL and stores the mapping in an in-memory `ConcurrentHashMap`.
3. **Concurrency Control**: The application limits the number of simultaneous requests using a configurable semaphore, preventing overload.
4. **Validation and Error Handling**: The application ensures inputs are valid and provides user-friendly error messages.
5. **Response**: The application returns the short URL in the response body.

### Limitations
- **No persistent storage**: URL mappings are stored in memory and are lost when the application restarts.
- **No live short links**: The application does not create live short links but demonstrates the encoding and decoding logic.
- **Simplified encoding logic**: Base64 encoding, while effective for small-scale applications, could result in collisions or inefficiencies in larger-scale systems.
- **No authentication**: The application does not require authentication for encoding or decoding URLs.

### But Why Though?
1. **Core Spring Concepts**: Demonstrates the use of Spring Boot, dependency injection, RESTful APIs, and configuration management.
2. **Concurrency Control**: Demonstrates how to control simultaneous access to application resources using semaphore.
3. **Clean Code Practices**: Highlights best practices, including modular design, validation with `@Valid`, exception handling, and unit/integration testing.
4. **Real-World Design Patterns**: The architecture can be extended to include a persistent database, distributed caching, or load balancing, making it a good foundation for a more complex implementation.
5. And because...why not? Writing code is fun.

## Requirements
- Java 21 (Amazon Corretto 21)
- Maven

## Build and Run
```bash
mvn clean install
mvn spring-boot:run
```

## Usage

### Shorten a URL

To encode a long URL into a short URL, send a POST request to the `/encode` endpoint with the original URL in the request body.

#### CURL
```bash
curl -X POST http://localhost:8080/encode -H "Content-Type: application/json" -d '{"url": "https://www.google.com"}'
```

#### HTTPie
```bash
http POST http://localhost:8080/encode url=https://www.google.com
```

### Decoding a shortened URL
To decode a short URL back into the original URL, send a POST request to the `/decode` endpoint with the short URL in the request body.

#### CURL
```bash
curl -X POST http://localhost:8080/decode -H "Content-Type: application/json" -d '{"url": "http://short.est/abc123"}'
```

#### HTTPie
```bash
http POST http://localhost:8080/decode url=http://short.est/abc123
```

### Response
Both endpoints will return JSON with the encoded or decoded URL.

#### Example response from `/encode`
```json
{
  "url": "http://short.est/abc123"
}
```

#### Example response from `/decode`

```json
{
  "url": "https://www.google.com"
}
```

## Error Handling
The application handles various exceptions and returns appropriate HTTP status codes and error messages.
- 400 Bad Request: For invalid inputs or empty URL requests.
- 429 Too Many Requests: For too many requests in a short period.

#### Example error response
```json
{
  "error": "Invalid URL"
}
```

## Tests
To run the unit and integration tests, use:
```bash
mvn test
```
The test suite will veirfy the encoding and decoding logic, input validation, and concurrency control.