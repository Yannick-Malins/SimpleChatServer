# SimpleChatServer

Simple in-memory chat server for UI prototypes

## Structure
Uses basic auth, two users a & b are hardcoded in ChatSecurityConfiguration.java, with password password

API is defined in src/main/resources/swagger.yaml

Only two endpoints: GET all the messages in the room, and POST a new message to the room.

Only the message content is taken from the POST input, the ID, timestamp and user are injected server side

## How to Use
### Build & Run
mvn spring-boot:run
### Test
import SimpleChatServer.postman_collection.json

You can test the "user" feature by overriding the auth for the POST service from user a to b, and then seeing these users appear in the message list
