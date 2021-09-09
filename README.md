# SimpleChatServer

Simple in-memory chat server for UI prototypes

## Structure
Uses basic auth, three users a & b & c are hardcoded in ChatSecurityConfiguration.java, with password password

API is defined in src/main/resources/swagger.yaml

Four endpoints:
    Create a room
    Get all rooms you are in
    Post a message to a room you are in (Only the message content is taken from the POST input, the ID, timestamp and user are injected server side)
    Get all messages from a room you are in

## How to Use
### Build & Run
mvn spring-boot:run
### Test
import SimpleChatServer.postman_collection.json

You can test the "user" feature of rooms and messages by overriding the auth for the POST service to users a/b/c
