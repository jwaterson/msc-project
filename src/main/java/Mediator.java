import java.util.concurrent.ConcurrentHashMap;

/**
 *
 */
public class Mediator {
    public static ConcurrentHashMap<Long, String> map;

    private Mediator() {
        map = new ConcurrentHashMap<>();
    }

}

