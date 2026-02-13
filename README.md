# Couple Backend

A Spring Boot backend application with Firebase Authentication support for Google and Email authentication.

## Architecture

This project follows Clean Architecture principles with the following layers:

- **Domain Layer**: Core business entities and repository interfaces
  - `domain/model`: User and Couple entities
  - `domain/repository`: JPA repository interfaces

- **Application Layer**: Business logic and use cases
  - `application/service`: Authentication and user management services
  - `application/dto`: Data Transfer Objects for API communication

- **Infrastructure Layer**: External integrations and frameworks
  - `infrastructure/controller`: REST API controllers
  - `infrastructure/config`: Spring and Firebase configuration
  - `infrastructure/security`: Security configuration

## Features

- Firebase Authentication (Google OAuth and Email/Password)
- JWT token verification
- User profile management
- Couple relationship tracking
- H2 in-memory database for development
- Clean Architecture design

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- Firebase project with Authentication enabled

## Setup

1. **Firebase Configuration**

   Create a Firebase project and enable authentication methods:
   - Go to [Firebase Console](https://console.firebase.google.com/)
   - Create a new project or select an existing one
   - Enable Google Sign-In and Email/Password authentication
   - Download the service account key:
     - Go to Project Settings > Service Accounts
     - Click "Generate New Private Key"
     - Save the JSON file as `firebase-service-account.json`
   - Place the file in `src/main/resources/` directory

2. **Build the Project**

   ```bash
   mvn clean install
   ```

3. **Run the Application**

   ```bash
   mvn spring-boot:run
   ```

   The application will start on `http://localhost:8080`

## API Endpoints

### Authentication

#### POST /auth/firebase

Authenticate a user with Firebase ID token.

**Request Body:**
```json
{
  "idToken": "firebase-id-token-from-client"
}
```

**Response (Success - 200 OK):**
```json
{
  "user": {
    "id": 1,
    "firebaseUid": "firebase-user-id",
    "email": "user@example.com",
    "name": "John Doe",
    "photoUrl": "https://example.com/photo.jpg",
    "provider": "GOOGLE",
    "createdAt": "2024-01-01T12:00:00",
    "updatedAt": "2024-01-01T12:00:00"
  },
  "coupleState": {
    "coupleId": null,
    "coupleName": null,
    "hasPartner": false,
    "createdAt": null
  }
}
```

**Response (Error - 401 Unauthorized):**
```json
{
  "error": "Authentication failed",
  "message": "Invalid Firebase token"
}
```

## Authentication Flow

1. **Client (Android)** signs in using Firebase Authentication (Google or Email)
2. **Firebase** returns an ID token to the client
3. **Client** sends the ID token to backend: `POST /auth/firebase`
4. **Backend** verifies the token with Firebase Admin SDK
5. **Backend** creates or updates the user in the database
6. **Backend** returns user profile and couple state

## Database Schema

### Users Table
- `id`: Primary key (auto-generated)
- `firebase_uid`: Firebase user ID (unique)
- `email`: User email
- `name`: User display name
- `photo_url`: Profile picture URL
- `provider`: Authentication provider (GOOGLE or EMAIL)
- `couple_id`: Foreign key to couples table (nullable)
- `created_at`: Account creation timestamp
- `updated_at`: Last update timestamp

### Couples Table
- `id`: Primary key (auto-generated)
- `couple_name`: Name of the couple (nullable)
- `created_at`: Creation timestamp
- `updated_at`: Last update timestamp

## Development

### H2 Console

Access the H2 database console at: `http://localhost:8080/h2-console`

**Connection Details:**
- JDBC URL: `jdbc:h2:mem:coupledb`
- Username: `sa`
- Password: (leave empty)

### Running Tests

```bash
mvn test
```

## Security

- Firebase ID tokens are verified on every request to `/auth/firebase`
- CSRF protection is disabled for stateless API
- Session management is stateless (no server-side sessions)
- H2 console is enabled for development only (disable in production)

## Configuration

Main configuration is in `src/main/resources/application.properties`:

```properties
# Server
server.port=8080

# Database
spring.datasource.url=jdbc:h2:mem:coupledb
spring.jpa.hibernate.ddl-auto=update

# Firebase
firebase.service-account-key=classpath:firebase-service-account.json
```

## Production Deployment

For production deployment:

1. Replace H2 with a production database (PostgreSQL, MySQL, etc.)
2. Disable H2 console
3. Use environment variables for sensitive configuration
4. Enable HTTPS
5. Configure proper CORS settings
6. Set up proper logging and monitoring

## License

MIT License