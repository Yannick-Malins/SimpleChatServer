package com.yannick.SimpleChatService.api;

import com.yannick.SimpleChatService.model.Message;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class MessagesAPIDelegateImpl implements MessagesApiDelegate{

    List<Message> messages = new ArrayList<>();

    @Override
    public ResponseEntity<List<Message>> getMessages() {
        return new ResponseEntity(messages, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Message> sendMessage(Message body) {
        body.setSender(SecurityContextHolder.getContext().getAuthentication().getName());
        body.setId(UUID.randomUUID().toString());
        body.setTime(OffsetDateTime.now());
        body.setContent("encrypted:"+body.getContent());
        messages.add(body);
        return new ResponseEntity<>(body,HttpStatus.CREATED);
    }
}
