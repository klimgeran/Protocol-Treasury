package com.github.klimgeran.symbol.discord.treasury.event;

import java.net.URI;
import java.time.Duration;

import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;

import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;

public abstract class MessageListener {

    public Mono<Void> processCommand(Message eventMessage) {
    	
    	
    	 WebSocketClient client = new ReactorNettyWebSocketClient();
    	 client.execute(URI.create("ws://cola-potatochips:3001/ws"), 
    			 session -> session.receive().map(WebSocketMessage::getPayloadAsText).log().then());//.map(WebSocketMessage::getPayloadAsText).log();
    			 
                 
         /*client.execute(
           URI.create("ws://cola-potatochips:3001/ws"), 
           session -> session.send(
             Mono.just(session.textMessage("event-spring-reactive-client-websocket")))
             .thenMany(session.receive()
               .map(WebSocketMessage::getPayloadAsText)
               .log())
             .then())
             .block(Duration.ofSeconds(10L));*/
    	
        return Mono.just(eventMessage)
           .filter(message -> message.getAuthor().map(user -> !user.isBot()).orElse(false))
           .filter(message -> message.getContent().equalsIgnoreCase("!symbol"))
           .flatMap(Message::getChannel)
           .flatMap(channel -> channel.createMessage("hello world!"))
           .then();
    }
}
