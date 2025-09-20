# Fitness Web App
[![Ask DeepWiki](https://devin.ai/assets/askdeepwiki.png)](https://deepwiki.com/saurabhpund/fitness-webapp)

This project is a comprehensive fitness tracking application built using a microservices architecture. It allows users to register, track their various fitness activities, and receive personalized, AI-driven recommendations based on their performance.

The system is designed to be scalable and resilient, leveraging modern cloud-native patterns and technologies.

## Architecture

The application is decomposed into several independent microservices, each with a distinct responsibility:

*   **Config Server (`configserver`)**: A centralized service for managing the external configuration of all other microservices. It provides configuration properties from a native source (classpath resources).
*   **Eureka Server (`eureka`)**: Acts as a service registry. All other microservices register with Eureka, allowing them to dynamically discover and communicate with each other.
*   **API Gateway (`gateway`)**: The single entry point for all client requests. It handles routing, load balancing, and security. It integrates with Keycloak for OAuth2 authentication and includes a custom filter to synchronize user data from JWT tokens into the User Service upon first login.
*   **User Service (`userservice`)**: Manages user data, including registration and profile retrieval. It uses a PostgreSQL database for persistence.
*   **Activity Service (`activityservice`)**: Responsible for tracking users' fitness activities. It stores activity data in a MongoDB database. After tracking an activity, it publishes an event to a RabbitMQ queue for asynchronous processing.
*   **AI Service (`aiservice`)**: Listens for activity events from RabbitMQ. Upon receiving an event, it calls the Google Gemini API to generate detailed analysis and recommendations. These recommendations are then stored in a separate MongoDB database.

## Features

*   **Microservices Architecture**: A scalable and maintainable system built with Spring Boot and Spring Cloud.
*   **Centralized Configuration**: All service configurations are managed by the Spring Cloud Config Server.
*   **Service Discovery**: Services register with and discover each other using Netflix Eureka.
*   **Secure API Gateway**: A single entry point protected by Spring Security and Keycloak (OAuth2/JWT).
*   **User Management**: User registration and profile management with data stored in PostgreSQL.
*   **Automatic User Sync**: New users are automatically registered in the User Service from their Keycloak JWT token on their first API call.
*   **Activity Tracking**: Users can log various fitness activities like running, cycling, yoga, etc. Activity data is stored in MongoDB.
*   **Asynchronous Processing**: RabbitMQ is used for message-based communication between services, decoupling the AI analysis from the initial activity tracking.
*   **AI-Powered Recommendations**: Integration with Google Gemini API to provide users with intelligent feedback, improvement suggestions, and safety guidelines for their workouts.

## Technologies Used

*   **Backend**: Java 21, Spring Boot 3, Spring Cloud
*   **Service Discovery**: Netflix Eureka
*   **API Gateway**: Spring Cloud Gateway
*   **Configuration**: Spring Cloud Config
*   **Authentication**: Spring Security, OAuth 2.0, Keycloak
*   **Database**:
    *   PostgreSQL (`user-service`)
    *   MongoDB (`activity-service`, `ai-service`)
*   **Messaging**: RabbitMQ
*   **AI**: Google Gemini API
*   **Build Tool**: Maven

## System Setup and Installation

### Prerequisites

*   Java 21
*   Maven
*   PostgreSQL
*   MongoDB
*   RabbitMQ
*   Keycloak
*   Google Gemini API Key

### Configuration

1.  **Databases**:
    *   Create a PostgreSQL database named `fitness_user_db` for the `user-service`.
    *   Create a MongoDB database named `fitness` for the `activity-service`.
    *   Create a MongoDB database named `fitnessrecommendation` for the `ai-service`.
    *   Update the database credentials in `configserver/src/main/resources/config/` (`user-service.yml`, `activity-service.yml`, `ai-service.yml`) if they differ from the defaults.

2.  **Keycloak**:
    *   Set up a Keycloak instance.
    *   Create a new realm (e.g., `fitness-oauth2`).
    *   Create a client for the API Gateway.
    *   Create at least one user within the realm.

3.  **Gemini API Key**:
    *   Obtain an API key from Google AI Studio.
    *   Set the following environment variables. The application will read these via the Config Server.
        ```bash
        export GEMINI_API_URL=<Your_Gemini_API_URL>
        export GEMINI_API_KEY=<Your_Gemini_API_Key>
        ```
    *   The `ai-service.yml` file references these variables.

### Running the Application

The services must be started in a specific order to ensure proper initialization and dependency resolution. For each service, navigate to its root directory and run the command.

1.  **Config Server**
    ```bash
    cd configserver
    ./mvnw spring-boot:run
    ```
2.  **Eureka Server**
    ```bash
    cd eureka
    ./mvnw spring-boot:run
    ```
3.  **User Service**
    ```bash
    cd userservice
    ./mvnw spring-boot:run
    ```
4.  **Activity Service**
    ```bash
    cd activityservice
    ./mvnw spring-boot:run
    ```
5.  **AI Service**
    ```bash
    cd aiservice
    ./mvnw spring-boot:run
    ```
6.  **API Gateway**
    ```bash
    cd gateway
    ./mvnw spring-boot:run
    ```

## API Endpoints

All endpoints are accessed through the API Gateway running on `http://localhost:8080`. A valid bearer token from Keycloak is required in the `Authorization` header for all requests. The gateway will automatically add the `X-USER-ID` header based on the JWT's `sub` claim.

### Activity Service

*   `POST /api/activities`
    *   Tracks a new fitness activity.
    *   **Body**:
        ```json
        {
            "type": "RUNNING",
            "duration": 30,
            "caloriesBurned": 300,
            "startTime": "2024-05-21T10:00:00",
            "additionalMetrics": {
                "distance": 5.0,
                "avg_pace": "6:00/km"
            }
        }
        ```

*   `GET /api/activities`
    *   Retrieves a list of all activities for the authenticated user.

*   `GET /api/activities/{activityId}`
    *   Retrieves a specific activity by its ID.

### AI Service (Recommendation)

*   `GET /api/recommendation/user/{userId}`
    *   Retrieves all AI-generated recommendations for a specific user ID.

*   `GET /api/recommendation/activity/{activityId}`
    *   Retrieves the AI-generated recommendation for a specific activity ID.

### User Service

*   `GET /api/users/{userId}`
    *   Retrieves the public profile for a specific user ID. The gateway filter handles user creation, so direct registration via an endpoint is not typically needed by the end-user.
