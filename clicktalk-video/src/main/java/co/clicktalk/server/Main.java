package co.clicktalk.server;

import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;

public class Main {

  public static void main(String[] args) throws IOException {
    IoAcceptor acceptor = new NioSocketAcceptor();

    ProtocolCodecFactory codecFactory = new TextLineCodecFactory(Charset.forName("UTF-8"));
    acceptor.getFilterChain().addLast("logger", new LoggingFilter());
    acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(codecFactory));
    acceptor.setHandler(new CachingSessionHandler());
    acceptor.getSessionConfig().setReadBufferSize(2048);
    acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);
    acceptor.bind(new InetSocketAddress(5556));
  }
}
