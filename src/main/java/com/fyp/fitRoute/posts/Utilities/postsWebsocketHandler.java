package com.fyp.fitRoute.posts.Utilities;

import com.fyp.fitRoute.posts.Entity.coordinates;
import com.fyp.fitRoute.posts.Entity.route;
import com.fyp.fitRoute.security.Utilities.JwtUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class postsWebsocketHandler extends TextWebSocketHandler {
    private final Map<String, WebSocketSession> conectedUsers = new HashMap();
    @Autowired
    private JwtUtils jwtUtils;


    public postsWebsocketHandler(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

        String authHeader = ""+session.getHandshakeHeaders().get("Authorization");
        String jwt = (authHeader.substring(7)).trim();
        String name = jwtUtils.extractUsername(jwt);

        conectedUsers.put(name, session);
    }


    public String sendFeed(String name, postResponse post) throws IOException {
        JSONObject json = new JSONObject();
        json.put("id", post.getId());
        json.put("likes", post.getLikes());
        json.put("comments", post.getComments());
        json.put("username", post.getUsername());
        json.put("profilePic", post.getProfilePic());
        json.put("description", post.getDescription());
        json.put("tags", new JSONArray(post.getTags()));
        json.put("images", new JSONArray(post.getImages()));
        json.put("category", post.getCategory());
        json.put("createdAt", post.getCreatedAt());
        json.put("updatedAt", post.getUpdatedAt());
        json.put("like", post.isLike());

        // Nested route object
        JSONObject routeJson = new JSONObject();
        route route = post.getRoute();
        routeJson.put("id", route.getId());
        routeJson.put("distance", route.getDistance());
        routeJson.put("time", route.getTime());

        // Coordinates array
        JSONArray coordinatesJson = new JSONArray();
        for (coordinates coord : route.getCoordinates()) {
            JSONObject coordJson = new JSONObject();
            coordJson.put("longitude", coord.getLongitude());
            coordJson.put("latitude", coord.getLatitude());
            coordinatesJson.put(coordJson);
        }
        routeJson.put("coordinates", coordinatesJson);
        routeJson.put("createdAt", route.getCreatedAt());
        routeJson.put("updatedAt", route.getUpdatedAt());

        json.put("route", routeJson);
        if (checkConnection(name))
            conectedUsers.get(name).sendMessage(new TextMessage(json.toString()));
        else
            throw new RuntimeException("You are not connected to delivery socket");
        return post.getId();
    }

    public boolean checkConnection(String name){
        return conectedUsers.containsKey(name);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        conectedUsers.entrySet().removeIf(entry -> entry.getValue().getId().equals(session.getId()));
    }
}

