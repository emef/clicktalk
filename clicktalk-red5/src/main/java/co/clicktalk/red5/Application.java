package co.clicktalk.red5;

import org.red5.server.adapter.ApplicationAdapter;

public class Application extends ApplicationAdapter {
  public int add(int a, int b) {
    return a + b;
  }
}
