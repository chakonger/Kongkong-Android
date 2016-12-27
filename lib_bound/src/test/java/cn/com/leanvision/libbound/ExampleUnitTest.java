package cn.com.leanvision.libbound;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
  @Test
  public void addition_isCorrect() throws Exception {

    byte[] bytes = "jylx".getBytes("UTF-8");
    System.out.println(bytes.length);
    for (int i = 0; i < bytes.length; i++) {
      System.out.println(bytes[i]);
    }
    assertEquals(4, 2 + 2);
  }
}