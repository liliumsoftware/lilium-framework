package ir.baho.framework.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.baho.framework.service.CurrentUser;
import ir.baho.framework.service.impl.SocketCurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

public abstract class AbstractSocketHandler extends AbstractWebSocketHandler {

    @Autowired
    protected ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        afterConnectionEstablished(session, new SocketCurrentUser(session));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        handleTextMessage(session, message, new SocketCurrentUser(session));
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        handleBinaryMessage(session, message, new SocketCurrentUser(session));
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        handleTransportError(session, exception, new SocketCurrentUser(session));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        afterConnectionClosed(session, status, new SocketCurrentUser(session));
    }

    protected void afterConnectionEstablished(WebSocketSession session, CurrentUser currentUser) throws Exception {
        super.afterConnectionEstablished(session);
    }

    protected void handleTextMessage(WebSocketSession session, TextMessage message, CurrentUser currentUser) throws Exception {
        super.handleTextMessage(session, message);
    }

    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message, CurrentUser currentUser) throws Exception {
        super.handleBinaryMessage(session, message);
    }

    protected void handleTransportError(WebSocketSession session, Throwable exception, CurrentUser currentUser) throws Exception {
        super.handleTransportError(session, exception);
    }

    protected void afterConnectionClosed(WebSocketSession session, CloseStatus status, CurrentUser currentUser) throws Exception {
        super.afterConnectionClosed(session, status);
    }

    protected void sendMessage(WebSocketSession session, Object message) throws Exception {
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
    }

    protected void sendMessage(WebSocketSession session, byte[] message) throws Exception {
        session.sendMessage(new BinaryMessage(message));
    }

    protected <V> V getMessage(TextMessage message, Class<V> clas) throws Exception {
        return objectMapper.readValue(message.getPayload(), clas);
    }

    protected String getParam(WebSocketSession session, String name) {
        if (session.getUri() != null) {
            UriComponents uriComponents = UriComponentsBuilder.fromUri(session.getUri()).build();
            return uriComponents.getQueryParams().toSingleValueMap().get(name);
        }
        return null;
    }

    protected List<String> getParams(WebSocketSession session, String name) {
        if (session.getUri() != null) {
            UriComponents uriComponents = UriComponentsBuilder.fromUri(session.getUri()).build();
            return uriComponents.getQueryParams().get(name);
        }
        return null;
    }

}
