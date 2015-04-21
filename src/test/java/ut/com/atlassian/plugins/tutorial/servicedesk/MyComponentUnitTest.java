package ut.com.atlassian.plugins.tutorial.servicedesk;

import org.junit.Test;
import com.atlassian.plugins.tutorial.servicedesk.MyPluginComponent;
import com.atlassian.plugins.tutorial.servicedesk.MyPluginComponentImpl;

import static org.junit.Assert.assertEquals;

public class MyComponentUnitTest
{
    @Test
    public void testMyName()
    {
        MyPluginComponent component = new MyPluginComponentImpl(null);
        assertEquals("names do not match!", "myComponent",component.getName());
    }
}