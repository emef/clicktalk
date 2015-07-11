package co.clicktalk.server;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import java.rmi.server.UID;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CachingSessionHandler extends IoHandlerAdapter {
  private final Map<String, IoSession> sessionCache;

  public CachingSessionHandler() {
    this.sessionCache = new ConcurrentHashMap<String, IoSession>();
  }

  @Override
  public void sessionCreated(IoSession session) throws Exception {
    super.sessionCreated(session);
    String id = new UID().toString();
    session.setAttribute("id", id);
    session.write(String.format("ID %s", id));
    sessionCache.put(id, session);


  }

  @Override
  public void sessionClosed(IoSession session) throws Exception {
    super.sessionClosed(session);
    String id = (String)session.getAttribute("id");
    sessionCache.remove(id);
  }

  @Override
  public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
    cause.printStackTrace();
  }

  @Override
  public void messageReceived(IoSession session, Object message) throws Exception {
    String str = message.toString();

    String[] parts = str.split("\\s+", 3);
    if (parts.length != 3) {
      session.write("Invalid command");
      return;
    }

    if (parts[0].equals("SEND")) {
      String id = parts[1];
      String msg = parts[2];
      sessionCache.get(id).write(msg);
    }
  }

  @Override
  public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
    System.out.println("IDLE " + session.getIdleCount(status));
  }
}
