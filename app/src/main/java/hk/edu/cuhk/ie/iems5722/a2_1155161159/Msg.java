package hk.edu.cuhk.ie.iems5722.a2_1155161159;

public class Msg {

    public static final int TYPE_RECEIVED = 0;
    public static final int TYPE_SENT = 1;
    String Msg;
    String Time;
    int Type;

    public Msg(String msg, int type, String time) {
        this.Msg = msg;
        this.Type = type;
        this.Time = time;
    }

    public String getMsg() {
        return Msg;
    }

    public void setMsg(String msg) {
        this.Msg = msg;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        this.Time = time;
    }

    public int getType() {
        return Type;
    }

    public void setType(int type) {
        this.Type = type;
    }
}
