package com.yannick.SimpleChatService;

import com.yannick.SimpleChatService.api.RoomsApiController;
import com.yannick.SimpleChatService.api.UserApiController;
import com.yannick.SimpleChatService.api.UsersApiController;
import com.yannick.SimpleChatService.model.Message;
import com.yannick.SimpleChatService.model.Room;
import com.yannick.SimpleChatService.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class ChatApplicationTests {

    @Autowired
    RoomsApiController roomsApiController;
    @Autowired
    UserApiController userApiController;
    @Autowired
    UsersApiController usersApiController;

    @Test
    void contextLoads() {
        assertNotNull(roomsApiController);
        assertNotNull(userApiController);
        assertNotNull(usersApiController);
    }

    @Test
    void users() {
        ResponseEntity<List<User>> respUsers = usersApiController.getUsers();
        assertEquals(respUsers.getStatusCode(), HttpStatus.OK);
        assertNotNull(respUsers.getBody());
        assertEquals(respUsers.getBody().size(), 3);
        respUsers.getBody().stream().forEach(user -> {
            ResponseEntity<User> respUser = userApiController.getUser(user.getId());
            assertEquals(respUser.getStatusCode(), HttpStatus.OK);
            assertEquals(respUser.getBody().getName(), user.getName());
        });
    }

    @WithMockUser(username = "a")
    @Test
    void rooms() {
        int currentSize = roomsApiController.getRooms().getBody().size();
        List<String> userIds = usersApiController.getUsers().getBody().stream().map(user -> user.getId()).collect(Collectors.toList());
        Room room = new Room();
        room.setName("courgette");
        room.setParticipants(userIds);
        assertEquals(roomsApiController.createRoom(room).getStatusCode(), HttpStatus.CREATED);
        assertEquals(roomsApiController.getRooms().getBody().size(), currentSize + 1);

    }

    @WithMockUser(username = "a")
    @Test
    void messages() {
        List<String> userIds = usersApiController.getUsers().getBody().stream().map(user -> user.getId()).collect(Collectors.toList());
        Room room = new Room();
        room.setName("aubergine");
        room.setParticipants(userIds);
        room = roomsApiController.createRoom(room).getBody();
        Message message = new Message();
        message.setContent("hello guys");
        assertEquals(roomsApiController.sendMessage(room.getId(),message).getStatusCode(), HttpStatus.CREATED);
        ResponseEntity<List<Message>> resp = roomsApiController.getMessages(room.getId(), null);
        assertEquals(resp.getStatusCode(), HttpStatus.OK);
        assertEquals(resp.getBody().size(), 1);
    }

}
