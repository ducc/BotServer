package pw.sponges.botserver.event.events;

import pw.sponges.botserver.Client;
import pw.sponges.botserver.event.framework.Event;
import pw.sponges.botserver.framework.Network;
import pw.sponges.botserver.framework.Room;
import pw.sponges.botserver.framework.User;

/*public class ChatMessageEvent extends Event {

    private final Client client;
    private final String userId, username, room, roomName, message;
    private final UserRole role;

    public ChatMessageEvent(Client client, String userId, String username, String room, String roomName, String message, UserRole role) {
        this.client = client;
        this.userId = userId;
        this.username = username;
        this.room = room;
        this.roomName = roomName;
        this.message = message;
        this.role = role;
    }

    public Client getClient() {
        return client;
    }

    public String getRoom() {
        return room;
    }

    public String getRoomName() {
        return roomName;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String needsChecks() {
        return room;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public UserRole getRole() {
        return role;
    }
}
*/

public class ChatMessageEvent extends Event {

    private final Client client;
    private final Network network;
    private final Room room;
    private final User user;
    private final String message;

    public ChatMessageEvent(Client client, Network network, Room room, User user, String message) {
        this.client = client;
        this.network = network;
        this.room = room;
        this.user = user;
        this.message = message;
    }

    public Client getClient() {
        return client;
    }

    public Network getNetwork() {
        return network;
    }

    public Room getRoom() {
        return room;
    }

    public User getUser() {
        return user;
    }

    public String getMessage() {
        return message;
    }

    // TODO change how checks are managed
    @Override
    public String needsChecks() {
        return room.getId();
    }
}