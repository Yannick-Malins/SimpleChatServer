# SimpleChatServer

Simple in-memory chat server for UI prototypes

## Structure
Uses basic auth, three users a & b & c are hardcoded in ChatSecurityConfiguration.java, with password password

API is defined in src/main/resources/swagger.yaml

endpoints:
    Get users (hardcoded list of 3 users)
    Get a specific user from ID
    Create a room giving a name and userID
    Get all rooms you are in
    Post a message to a room you are in (passing roomId and message content)
    Get all messages from a room you are in (passing roomId)

## How to Use
### Build & Run
mvn spring-boot:run
### Test
import SimpleChatServer.postman_collection.json

You can test the "user" feature of rooms and messages by overriding the auth for the POST service to users a/b/c
