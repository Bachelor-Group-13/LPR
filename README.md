# Inneparkert Backend

The backend application for Inneparkert, a smart parking management system developed as a bachelor project in collaboration with Twoday.

## Overview

This is the backend part of the Inneparkert system, built with Java and Spring Boot. It handles core business logic, user management, and serves REST APIs for the frontend.

## Features

- **REST API**

  - Endpoints for user and license plate management
  - Parking event processing
  - Integration with detection API

- **Authentication**

  - JWT-based authentication and authorization
  - Role-based access control

- **Database Integration**

  - PostgreSQL for persistent data storage

## Technical Details

### Built With

- **Framework**: Spring Boot
- **Language**: Java
- **Database**: PostgreSQL
- **Build Tool**: Maven
- **Security**: Spring Security, JWT

### Project Structure

```
backend/
├── .github/workflows/
│   └── build.yml
├── src/
│   └── main/java/no/
│       └── backend/
│           ├── common/
│           │   ├── config
│           │   └── dto
│           └── features/
│               ├── auth
│               ├── licenseplate
│               ├── push
│               ├── reservation
│               └── user
├── Dockerfile
└── pom.xml
```


## Getting Started

### Prerequisites

- Java 17+
- PostgreSQL
- Maven

### Installation

1. Clone the repository:

```bash
git clone https://github.com/Bachelor-Group-13/backend.git
cd backend
```

2. Configure your `.env` file:
```
spring.datasource.url=jdbc:postgresql://localhost:5432/inneparkert
spring.datasource.username=your_db_user
spring.datasource.password=your_db_password
jwt.secret=your_jwt_secret
```

3. Run the application:
```bash
mvn spring-boot:run
```

## Development
- Build: `mvn clean install`
- Run: `mvn spring-boot:run`
- Test: `mvn test`

##Prosject Status
This service is part of the Inneparkert system and provides APIs for frontend communication and parking management.

## Team

- Viljar Hoem-Olsen
- Thomas Åkre
- Sander Grimstad
