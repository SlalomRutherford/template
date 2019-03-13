package testComponent;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class BootTest {
    private Boot boot = new Boot();

    @Test
    public void testCorrect() {
        assertEquals(boot.testFunc(), "correct");
    }
}