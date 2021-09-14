package com.yannick.SimpleChatService;

import com.yannick.SimpleChatService.api.RoomsApiDelegate;
import com.yannick.SimpleChatService.api.UserApiDelegate;
import com.yannick.SimpleChatService.api.UsersApiDelegate;
import com.yannick.SimpleChatService.model.Message;
import com.yannick.SimpleChatService.model.Room;
import com.yannick.SimpleChatService.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.NativeWebRequest;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ApiImpl implements  RoomsApiDelegate, UserApiDelegate, UsersApiDelegate {

    Map<String, User> users;
    Map<String, Room> rooms = new HashMap<>();
    Map<String, List<Message>> messagesByRoom = new HashMap<>();

    public ApiImpl() {
        users = new HashMap<>();
        for (String name : new String[]{"a", "b", "c"}) {
            User user = new User();
            user.setName(name);
            user.setId("user-" + UUID.randomUUID());
            users.put(user.getId(), user);
        }
    }

    protected String myId() {
        String myName = SecurityContextHolder.getContext().getAuthentication().getName();
        return users.values().stream().filter(user -> user.getName().equals(myName)).findFirst().get().getId();
    }

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return Optional.empty();
    }

    @Override
    public ResponseEntity<User> getUser(String userId) {
        if (userId == null)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        User user = users.get(userId);
        if (user == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        else
            return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<User>> getUsers() {
        return new ResponseEntity<>(new ArrayList<>(users.values()), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<Message>> getMessages(String roomId, OffsetDateTime fromTime) {
        if (roomId == null)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        if (!rooms.containsKey(roomId) || !rooms.get(roomId).getParticipants().contains(myId()))
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        else {
            if (fromTime == null)
                return new ResponseEntity<>(messagesByRoom.get(roomId), HttpStatus.OK);
            else {
                List<Message> filteredMessages = messagesByRoom.get(roomId).stream()
                        .filter(message -> message.getTime().isAfter(fromTime))
                        .collect(Collectors.toList());
                return new ResponseEntity<>(filteredMessages, HttpStatus.OK);
            }
        }
    }

    @Override
    public ResponseEntity<Message> sendMessage(String roomId, Message message) {
        if (roomId==null
                || !rooms.containsKey(roomId)
                || !rooms.get(roomId).getParticipants().contains(myId())
                || message.getContent() == null)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        message.setSenderId(myId());
        message.setId("message-" + UUID.randomUUID());
        message.setTime(OffsetDateTime.now());
        message.setContent("encrypted:" + message.getContent());
        messagesByRoom.get(roomId).add(message);
        return new ResponseEntity<>(message, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Room> createRoom(Room room) {
        if (room.getName() == null
                || room.getParticipants() == null
                || room.getParticipants().isEmpty()
                || !room.getParticipants().contains(myId())
                || !users.keySet().containsAll(room.getParticipants()))
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        room.setId("room-" + UUID.randomUUID());
        rooms.put(room.getId(), room);
        messagesByRoom.put(room.getId(), new LinkedList<>());
        return new ResponseEntity<>(room, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<List<Room>> getRooms() {
        List<Room> filteredRooms = rooms.values().stream().filter(room -> room.getParticipants().contains(myId())).collect(Collectors.toList());
        return new ResponseEntity<>(filteredRooms, HttpStatus.OK);
    }
}
