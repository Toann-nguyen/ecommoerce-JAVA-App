# Ecommerce Chatbot Backend

Backend Spring Boot service for the customer support chatbot in the Android ecommerce application.

## Features

- REST API for chatbot communication
- Predefined responses for common customer queries
- Chat history storage with H2 database
- Integration with Firebase for authentication (optional)

## Prerequisites

- Java 17 or higher
- Maven 3.6+ or Gradle 7.0+

## How to Run

1. Clone the repository
2. Navigate to the project folder
3. Run the following command:

```bash
mvn spring-boot:run
```

The server will start on port 8080.

## API Endpoints

### Send a message to the chatbot

```
POST /api/chat
```

Request body:

```json
{
  "userId": "user123",
  "message": "Xin chào"
}
```

Response:

```json
{
  "response": "Xin chào! Tôi có thể giúp gì cho bạn?",
  "timestamp": "2023-05-15T14:30:45.123"
}
```

### Get chat history

```
GET /api/chat/history/{userId}
```

Response:

```json
[
  {
    "id": 1,
    "userId": "user123",
    "message": "Xin chào",
    "isUser": true,
    "timestamp": "2023-05-15T14:30:40.123",
    "sessionId": "abc-123-def-456"
  },
  {
    "id": 2,
    "userId": "user123",
    "message": "Xin chào! Tôi có thể giúp gì cho bạn?",
    "isUser": false,
    "timestamp": "2023-05-15T14:30:45.123",
    "sessionId": "abc-123-def-456"
  }
]
```

## Integration with Android App

The Android app communicates with this backend using Retrofit. Configure the base URL in the Android
app's `ChatbotRepository.java` file to match your server address.