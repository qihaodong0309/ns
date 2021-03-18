package messagepack;

import org.msgpack.annotation.Message;

/**
 * @author qihaodong
 */
@Message
public class UserInfo {

    private int id;
    private String name;

    public UserInfo() {

    }

    public UserInfo(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
