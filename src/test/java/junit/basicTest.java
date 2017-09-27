package junit;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import com.csye6225.demo.bean.User;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertThat;

public class basicTest {

    //Single Object
    @Test
    public void testClassProperty() {

        User obj = new User(6, "Mk", "bcnx", "hgdhgd");

        assertThat(obj, hasProperty("userName"));

        assertThat(obj, hasProperty("userName", is("Mk")));

    }
}