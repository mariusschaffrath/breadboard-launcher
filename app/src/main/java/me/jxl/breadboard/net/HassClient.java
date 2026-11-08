package me.jxl.breadboard.net;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class HassClient {
    private static final String TAG = "HassClient";
    public static final String WS_URL = "ws://192.168.178.131:8123/api/websocket";
    public static final String AUTH_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJkNzk0MWVhOWQ5OWU0YzhjOTc0YjU5NjE4N2Q1YjI0OSIsImlhdCI6MTc4NzkzMDIzNSwiZXhwIjoyMTAzMjkwMjM1fQ.Hx4GbbBbAPN9ToA2opU7wpyIZ5GV1bmgDNHNLswLG9M";

    public static class EntityState {
        public final String entityId;
        public final String state;
        public final JSONObject attributes;

        public EntityState(String entityId, String state, JSONObject attributes) {
            this.entityId = entityId;
            this.state = state;
            this.attributes = attributes != null ? attributes : new JSONObject();
        }

        public boolean isOn() {
            return "on".equalsIgnoreCase(state) || "open".equalsIgnoreCase(state) || "playing".equalsIgnoreCase(state);
        }

        public String getFriendlyName() {
            return attributes.optString("friendly_name", entityId);
        }

        public String getMediaTitle() {
            return attributes.optString("media_title", "");
        }

        public String getMediaArtist() {
            return attributes.optString("media_artist", "");
        }

        public String getEntityPicture() {
            return attributes.optString("entity_picture", "");
        }

        public double getTemperature() {
            return attributes.optDouble("temperature", 22.0);
        }

        public double getVolumeLevel() {
            return attributes.optDouble("volume_level", 0.5);
        }

        public int getBrightness() {
            return attributes.optInt("brightness", 0);
        }
    }

    public interface EntityListener {
        void onEntityUpdated(String entityId, EntityState state);
    }

    public interface ConnectionListener {
        void onConnected();
        void onDisconnected();
    }

    private static HassClient instance;

    public static synchronized HassClient getInstance() {
        if (instance == null) {
            instance = new HassClient();
        }
        return instance;
    }

    private WebSocketClient ws;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final AtomicInteger msgId = new AtomicInteger(1);
    private final ConcurrentHashMap<String, EntityState> states = new ConcurrentHashMap<>();
    private final List<EntityListener> entityListeners = Collections.synchronizedList(new ArrayList<>());
    private final List<ConnectionListener> connectionListeners = Collections.synchronizedList(new ArrayList<>());

    private boolean isConnected = false;
    private boolean shouldReconnect = true;

    private HassClient() {}

    public void start() {
        shouldReconnect = true;
        connect();
    }

    public void stop() {
        shouldReconnect = false;
        if (ws != null) {
            ws.close();
            ws = null;
        }
    }

    private void connect() {
        try {
            ws = new WebSocketClient(new URI(WS_URL)) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    Log.i(TAG, "WebSocket connected to Home Assistant");
                }

                @Override
                public void onMessage(String message) {
                    handleMessage(message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Log.w(TAG, "WebSocket closed: " + reason + " (code " + code + ")");
                    isConnected = false;
                    notifyDisconnected();
                    if (shouldReconnect) {
                        mainHandler.postDelayed(HassClient.this::connect, 3000);
                    }
                }

                @Override
                public void onError(Exception ex) {
                    Log.e(TAG, "WebSocket error: " + ex.getMessage());
                }
            };
            ws.connect();
        } catch (Exception e) {
            Log.e(TAG, "Failed to create WebSocketClient: " + e.getMessage());
            if (shouldReconnect) {
                mainHandler.postDelayed(this::connect, 3000);
            }
        }
    }

    private void handleMessage(String message) {
        try {
            JSONObject json = new JSONObject(message);
            String type = json.optString("type");

            if ("auth_required".equals(type)) {
                JSONObject auth = new JSONObject();
                auth.put("type", "auth");
                auth.put("access_token", AUTH_TOKEN);
                sendRaw(auth.toString());
            } else if ("auth_ok".equals(type)) {
                Log.i(TAG, "Authenticated with Home Assistant!");
                isConnected = true;
                notifyConnected();
                fetchInitialStates();
                subscribeEvents();
            } else if ("result".equals(type)) {
                if (json.optBoolean("success", false) && json.has("result")) {
                    Object res = json.get("result");
                    if (res instanceof JSONArray) {
                        JSONArray arr = (JSONArray) res;
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = arr.getJSONObject(i);
                            parseAndStoreState(obj);
                        }
                    }
                }
            } else if ("event".equals(type)) {
                JSONObject event = json.optJSONObject("event");
                if (event != null && "state_changed".equals(event.optString("event_type"))) {
                    JSONObject data = event.optJSONObject("data");
                    if (data != null && data.has("new_state")) {
                        JSONObject newState = data.optJSONObject("new_state");
                        if (newState != null) {
                            parseAndStoreState(newState);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing message: " + e.getMessage());
        }
    }

    private void parseAndStoreState(JSONObject obj) {
        String entityId = obj.optString("entity_id");
        String state = obj.optString("state");
        JSONObject attrs = obj.optJSONObject("attributes");

        EntityState entityState = new EntityState(entityId, state, attrs);
        states.put(entityId, entityState);

        notifyEntityUpdated(entityId, entityState);
    }

    private void fetchInitialStates() {
        try {
            JSONObject req = new JSONObject();
            req.put("id", msgId.getAndIncrement());
            req.put("type", "get_states");
            sendRaw(req.toString());
        } catch (Exception e) {
            Log.e(TAG, "Error requesting states: " + e.getMessage());
        }
    }

    private void subscribeEvents() {
        try {
            JSONObject req = new JSONObject();
            req.put("id", msgId.getAndIncrement());
            req.put("type", "subscribe_events");
            req.put("event_type", "state_changed");
            sendRaw(req.toString());
        } catch (Exception e) {
            Log.e(TAG, "Error subscribing events: " + e.getMessage());
        }
    }

    public void callService(String domain, String service, JSONObject serviceData) {
        try {
            JSONObject req = new JSONObject();
            req.put("id", msgId.getAndIncrement());
            req.put("type", "call_service");
            req.put("domain", domain);
            req.put("service", service);
            if (serviceData != null) {
                req.put("service_data", serviceData);
            }
            sendRaw(req.toString());
        } catch (Exception e) {
            Log.e(TAG, "Error calling service: " + e.getMessage());
        }
    }

    public void toggleLight(String entityId) {
        try {
            JSONObject data = new JSONObject();
            data.put("entity_id", entityId);
            callService("light", "toggle", data);
        } catch (Exception e) {}
    }

    public void toggleCover(String entityId) {
        try {
            JSONObject data = new JSONObject();
            data.put("entity_id", entityId);
            callService("cover", "toggle", data);
        } catch (Exception e) {}
    }

    public void mediaPlayPause(String entityId) {
        try {
            JSONObject data = new JSONObject();
            data.put("entity_id", entityId);
            callService("media_player", "media_play_pause", data);
        } catch (Exception e) {}
    }

    public void mediaNext(String entityId) {
        try {
            JSONObject data = new JSONObject();
            data.put("entity_id", entityId);
            callService("media_player", "media_next_track", data);
        } catch (Exception e) {}
    }

    public void mediaPrev(String entityId) {
        try {
            JSONObject data = new JSONObject();
            data.put("entity_id", entityId);
            callService("media_player", "media_previous_track", data);
        } catch (Exception e) {}
    }

    public void setVolume(String entityId, float volumeLevel) {
        try {
            JSONObject data = new JSONObject();
            data.put("entity_id", entityId);
            data.put("volume_level", Math.max(0f, Math.min(1f, volumeLevel)));
            callService("media_player", "volume_set", data);
        } catch (Exception e) {}
    }

    public void setBrightness(String entityId, int pct) {
        try {
            JSONObject data = new JSONObject();
            data.put("entity_id", entityId);
            data.put("brightness_pct", Math.max(0, Math.min(100, pct)));
            callService("light", "turn_on", data);
        } catch (Exception e) {}
    }

    public void triggerScene(String entityId) {
        try {
            JSONObject data = new JSONObject();
            data.put("entity_id", entityId);
            callService("scene", "turn_on", data);
        } catch (Exception e) {}
    }

    private void sendRaw(String text) {
        if (ws != null && ws.isOpen()) {
            ws.send(text);
        }
    }

    public EntityState getState(String entityId) {
        return states.get(entityId);
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void addEntityListener(EntityListener listener) {
        if (!entityListeners.contains(listener)) {
            entityListeners.add(listener);
        }
    }

    public void removeEntityListener(EntityListener listener) {
        entityListeners.remove(listener);
    }

    public void addConnectionListener(ConnectionListener listener) {
        if (!connectionListeners.contains(listener)) {
            connectionListeners.add(listener);
        }
    }

    public void removeConnectionListener(ConnectionListener listener) {
        connectionListeners.remove(listener);
    }

    private void notifyEntityUpdated(String entityId, EntityState state) {
        mainHandler.post(() -> {
            for (EntityListener l : entityListeners) {
                try {
                    l.onEntityUpdated(entityId, state);
                } catch (Exception e) {
                    Log.e(TAG, "Listener error: " + e.getMessage());
                }
            }
        });
    }

    private void notifyConnected() {
        mainHandler.post(() -> {
            for (ConnectionListener l : connectionListeners) {
                try {
                    l.onConnected();
                } catch (Exception e) {}
            }
        });
    }

    private void notifyDisconnected() {
        mainHandler.post(() -> {
            for (ConnectionListener l : connectionListeners) {
                try {
                    l.onDisconnected();
                } catch (Exception e) {}
            }
        });
    }
}
