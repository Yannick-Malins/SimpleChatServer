package com.yannick.SimpleChatService;

import com.yannick.SimpleChatService.api.MessagesApiDelegate;
import com.yannick.SimpleChatService.api.RoomsApiDelegate;
import com.yannick.SimpleChatService.model.Message;
import com.yannick.SimpleChatService.model.Room;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.NativeWebRequest;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ApiImpl implements MessagesApiDelegate, RoomsApiDelegate {

    List<String> users = Arrays.asList("a", "b", "c");
    List<Room> rooms = new LinkedList<>();
    Map<String, List<Message>> messagesByRoom = new HashMap<>();

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return Optional.empty();
    }

    @Override
    public ResponseEntity<List<Message>> getMessages(String room, OffsetDateTime fromTime) {
        String myName = SecurityContextHolder.getContext().getAuthentication().getName();
        if (room == null)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        if (rooms.stream().noneMatch(lRoom -> (lRoom.getName().equals(room) && lRoom.getParticipants().contains(myName))))
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        else {
            if (fromTime == null)
                return new ResponseEntity<>(messagesByRoom.get(room), HttpStatus.OK);
            else {
                List<Message> filteredMessages = messagesByRoom.get(room).stream()
                        .filter(message -> message.getTime().isAfter(fromTime))
                        .collect(Collectors.toList());
                return new ResponseEntity<>(filteredMessages, HttpStatus.OK);
            }
        }
    }

    @Override
    public ResponseEntity<Message> sendMessage(Message message) {
        String myName = SecurityContextHolder.getContext().getAuthentication().getName();
        if (message.getContent() == null
                || message.getRoom() == null
                || rooms.stream().noneMatch(room -> (room.getName().equals(message.getRoom()) && room.getParticipants().contains(myName))))
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        message.setSender(myName);
        message.setId(UUID.randomUUID().toString());
        message.setTime(OffsetDateTime.now());
        message.setContent("encrypted:" + message.getContent());
        messagesByRoom.get(message.getRoom()).add(message);
        return new ResponseEntity<>(message, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Room> createRoom(Room room) {
        String myName = SecurityContextHolder.getContext().getAuthentication().getName();
        if (room.getName() == null
                || rooms.stream().anyMatch(lRoom -> lRoom.getName().equals(room.getName()))
                || room.getParticipants() == null
                || room.getParticipants().isEmpty()
                || !room.getParticipants().contains(myName)
                || !users.containsAll(room.getParticipants()))
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        rooms.add(room);
        messagesByRoom.put(room.getName(), new LinkedList<>());
        return new ResponseEntity<>(room, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<List<Room>> getRooms() {
        String myName = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Room> filteredRooms = rooms.stream().filter(room -> room.getParticipants().contains(myName)).collect(Collectors.toList());
        return new ResponseEntity<>(filteredRooms, HttpStatus.OK);
    }
}
