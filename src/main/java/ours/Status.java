package ours;

import java.util.Objects;

/**
 * @author mvolkhart
 */
public class Status {

    public static final int FAIL = -1;
    public static final int PASS = 1;

    private final String message;
    private final int code;

    public Status(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int code() {
        return code;
    }

    public String message() {
        return message;
    }

    @Override
    public String toString() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Status)) {
            return false;
        }
        Status that = (Status) o;
        return that.code == code && Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, message);
    }
}
