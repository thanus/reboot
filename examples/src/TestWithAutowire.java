package org.example;

import org.springframework.beans.factory.annotation.Autowired;

public class TestWithAutowire {
  @Autowired
  private A a;
  @Autowired
  private B b;
  
  @Test
  public void testSomething() {
    assertThat(1, 1);
  }
}
