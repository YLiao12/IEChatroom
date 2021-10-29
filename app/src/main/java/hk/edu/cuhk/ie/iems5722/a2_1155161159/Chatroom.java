package hk.edu.cuhk.ie.iems5722.a2_1155161159;

public class Chatroom {
    private int chatroomId;
    private String chatroomName;

    public Chatroom(int chatroomId, String chatroomName) {
        this.chatroomId = chatroomId;
        this.chatroomName = chatroomName;
    }

    public int getChatroomId() {
        return chatroomId;
    }

    public void setChatroomId(int chatroomId) {
        this.chatroomId = chatroomId;
    }

    public String getChatroomName() {
        return chatroomName;
    }

    public void setChatroomName(String chatroomName) {
        this.chatroomName = chatroomName;
    }
}
