package mprower;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AppTest {
    @Test
    public void testPortDefault() {
        int port = HelloWorld.getPort();
        assertTrue(port > 0);
    }
}
