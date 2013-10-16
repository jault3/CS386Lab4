package unit;

import com.google.common.testing.EqualsTester;
import org.testng.annotations.Test;
import ours.Status;

import static org.testng.Assert.assertEquals;

/**
 * @author mvolkhart
 */
public class StatusTest {

    @Test
    public void toStringReturnsMessage() {
        String message1 = "test message 1";
        String message2 = "other test message";
        Status status1 = new Status(0, message1);
        Status status2 = new Status(0, message2);

        assertEquals(status1.toString(), message1);
        assertEquals(status2.toString(), message2);
    }

    @Test
    public void equalsWorks() {

        EqualsTester tester = new EqualsTester();

        tester.addEqualityGroup(new Status(0, "message"), new Status(0, "message"));
        tester.addEqualityGroup(new Status(1, "alone"));
        tester.addEqualityGroup(new Status(2, "message"));
        tester.addEqualityGroup(new Status(0, "other"));

        Status same = new Status(-1, "same");
        tester.addEqualityGroup(same, same);

        tester.testEquals();
    }
}
