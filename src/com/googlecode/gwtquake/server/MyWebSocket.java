package com.googlecode.gwtquake.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.LinkedList;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

public class MyWebSocket extends WebSocketAdapter {
  private LinkedList<String> outQueue = new LinkedList<String>();
  private Session outbound;
  private byte[] fromIp;
  private int fromPort;

  public MyWebSocket() {
  }

  @Override
  public void onWebSocketConnect(Session outbound) {
    this.outbound = outbound;
    InetSocketAddress addr = outbound.getRemoteAddress();
    fromIp = addr.getAddress().getAddress();
    fromPort = addr.getPort();
    String from = String.format("%d.%d.%d.%d:%d", fromIp[0], fromIp[1], fromIp[2], fromIp[3], fromPort);
    ServerWebSocketImpl.INSTANCE.sockets.put(from, this);

    if (!outQueue.isEmpty()) {
      for (String msg : outQueue) {
        sendMessage(msg);
      }
      outQueue.clear();
    }

    System.out.println("onConnect");
  }

  @Override
  public void onWebSocketClose(int statusCode, String reason) {
    System.out.println("onClose");
  }

  @Override
  public void onWebSocketText(String message) {
    synchronized (ServerWebSocketImpl.msgQueue) {
      ServerWebSocketImpl.msgQueue.add(new Msg(fromIp, fromPort, message));
    }
  }

  public void sendMessage(String msg) {
    if (outbound == null) {
      outQueue.add(msg);
      return;
    }

    try {
      outbound.getRemote().sendString(msg);
    } catch (IOException e) {
      System.out.println("sendMessage failed: " + e.getMessage());
      outQueue.add(msg);
      outbound = null;
    }
  }
}
