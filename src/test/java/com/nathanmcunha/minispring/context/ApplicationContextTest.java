package com.nathanmcunha.minispring.context;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.nathanmcunha.minispring.context.myanother.MyAnotherComponentTest;
import java.lang.reflect.InvocationTargetException;
import org.junit.jupiter.api.Test;

public class ApplicationContextTest {

  @Test
  void testApplicationContext() {
    ApplicationContext context = new ApplicationContext();
    assertNotNull(context);
  }

  @Test
  void miniApplicationContextShouldThrowUnsupportedOperationException()
      throws InstantiationException,
          IllegalAccessException,
          IllegalArgumentException,
          InvocationTargetException,
          NoSuchMethodException,
          SecurityException {
    ApplicationContext context = new ApplicationContext();
    context.MiniApplicationContext(DummyConfig.class);
    MyTestComponent component = context.getBean(MyTestComponent.class);
    MyAnotherComponentTest anotherComponentTest = context.getBean(MyAnotherComponentTest.class);
    assertNotNull(component, "MyTestComponent should be discovered and registered.");
    assertNotNull(anotherComponentTest, "MyTestComponent should be discovered and registered.");
  }
}
