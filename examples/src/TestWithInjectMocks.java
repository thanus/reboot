package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

public class TestWithInjectMocks {
  @InjectMocks
  private A a;
  @Mock
  private B b;
  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private C c;
  @Spy
  private D d = new D();
  
  @Test
  public void testSomething() {
    assertThat(1, 1);
  }
}
