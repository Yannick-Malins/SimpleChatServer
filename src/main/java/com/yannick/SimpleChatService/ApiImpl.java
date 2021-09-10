package com.yannick.SimpleChatService;

import com.yannick.SimpleChatService.api.MessagesApiDelegate;
import com.yannick.SimpleChatService.api.RoomsApiDelegate;
import com.yannick.SimpleChatService.api.UserApiDelegate;
import com.yannick.SimpleChatService.api.UsersApiDelegate;
import com.yannick.SimpleChatService.model.Message;
import com.yannick.SimpleChatService.model.Room;
import com.yannick.SimpleChatService.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.NativeWebRequest;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ApiImpl implements MessagesApiDelegate, RoomsApiDelegate, UserApiDelegate, UsersApiDelegate {

    List<User> users;
    List<Room> rooms = new LinkedList<>();
    Map<String, List<Message>> messagesByRoom = new HashMap<>();

    public ApiImpl() {
        users = new LinkedList<>();
        for(String name:new String[]{"a", "b", "c"}) {
            User user = new User();
            user.setName(name);
            user.setId("user-"+UUID.randomUUID().toString());
            users.add(user);
        }
    }

    protected String myId() {
        String myName = SecurityContextHolder.getContext().getAuthentication().getName();
        return users.stream().filter(user -> user.getName().equals(myName)).findFirst().get().getId();
    }

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return Optional.empty();
    }

    @Override
    public ResponseEntity<User> getUser(String userId) {
        if(userId==null)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        Optional<User> opt = users.stream().filter(user -> user.getId().equals(userId)).findAny();
        if(opt.isEmpty())
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        else
            return new ResponseEntity<>(opt.get(),HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<User>> getUsers() {
        return new ResponseEntity<>(users,HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<Message>> getMessages(String roomId, OffsetDateTime fromTime) {
        if (roomId == null)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        if (rooms.stream().noneMatch(lRoom -> (lRoom.getId().equals(roomId) && lRoom.getParticipants().contains(myId()))))
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
    public ResponseEntity<Message> sendMessage(Message message) {
        if (message.getContent() == null
                || message.getRoomId() == null
                || rooms.stream().noneMatch(room -> (room.getId().equals(message.getRoomId()) && room.getParticipants().contains(myId()))))
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        message.setSenderId(myId());
        message.setId("message-"+UUID.randomUUID().toString());
        message.setTime(OffsetDateTime.now());
        message.setContent("encrypted:" + message.getContent());
        messagesByRoom.get(message.getRoomId()).add(message);
        return new ResponseEntity<>(message, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Room> createRoom(Room room) {
        if (room.getName() == null
                || room.getParticipants() == null
                || room.getParticipants().isEmpty()
                || !room.getParticipants().contains(myId())
                || !room.getParticipants().stream().allMatch(userId -> users.stream().anyMatch(user->user.getId().equals(userId))))
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        room.setId("room-"+UUID.randomUUID().toString());
        rooms.add(room);
        messagesByRoom.put(room.getId(), new LinkedList<>());
        return new ResponseEntity<>(room, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<List<Room>> getRooms() {
        List<Room> filteredRooms = rooms.stream().filter(room -> room.getParticipants().contains(myId())).collect(Collectors.toList());
        return new ResponseEntity<>(filteredRooms, HttpStatus.OK);
    }
}
