/*
 * Copyright (C) 2010 Copyright 2010 Google Inc.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.
 *
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package com.googlecode.gwtquake.server;

import java.io.IOException;
import java.net.InetAddress;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import com.googlecode.gwtquake.shared.common.Compatibility;
import com.googlecode.gwtquake.shared.common.NetworkAddress;
import com.googlecode.gwtquake.shared.sys.QuakeSocket;
import com.googlecode.gwtquake.shared.sys.QuakeSocketFactory;
import com.googlecode.gwtquake.shared.util.Lib;

public class ServerWebSocketFactoryImpl implements QuakeSocketFactory {
  private ServletContextHandler context;

  public ServerWebSocketFactoryImpl(ServletContextHandler context) {
    this.context = context;
  }

  public QuakeSocket bind(boolean server) {
    if (server) {
      return new ServerWebSocketImpl(context);
    }
    return null;
  }
}

class ServerWebSocketImpl implements QuakeSocket {
  public static ServerWebSocketImpl INSTANCE;

  public Map<String, MyWebSocket> sockets = new HashMap<String, MyWebSocket>();

//  private Server server;
//  private final int port;

  static final LinkedList<Msg> msgQueue = new LinkedList<Msg>();

  public ServerWebSocketImpl(ServletContextHandler context) {
    INSTANCE = this;

    ServletHolder holder = new ServletHolder("ws", new WebSocketServlet() {
      @Override
      public void configure(WebSocketServletFactory factory) {
        factory.register(MyWebSocket.class);
      }
    });
    context.addServlet(holder, "/ws/*");
  }

  public void close() {
//    server.destroy();
//    server = null;
    sockets = null;
  }

  public int receive(NetworkAddress fromAddr, byte[] buf) throws IOException {
    synchronized (msgQueue) {
      if (msgQueue.isEmpty()) {
        return -1;
      }

      Msg msg = msgQueue.removeFirst();
      String data = msg.data;

      fromAddr.ip = new byte[4];
      System.arraycopy(msg.fromIp, 0, fromAddr.ip, 0, 4);
      fromAddr.port = msg.fromPort;

      int len = Compatibility.stringToBytes(data, buf);
//      System.out.println("receiving " + Lib.hexDump(buf, len, true));
      return len;
    }
  }

  public void send(NetworkAddress toAddr, byte[] data, int len) throws IOException {
    String targetAddress = InetAddress.getByAddress(toAddr.ip).getHostAddress() + ":" + toAddr.port;

    MyWebSocket target = sockets.get(targetAddress);

    if (target == null) {
      System.out.println("Trying to send message to " + toAddr.toString()
          + "; address not found. Available addresses: " + sockets.keySet());
      return;
    }

//    System.out.println("sending to " + targetAddress + ": " + Lib.hexDump(data, len, true));

    target.sendMessage(Compatibility.bytesToString(data, len));
  }

  public void Shutdown() {
//    try {
//      server.stop();
//    } catch (Exception e) {
//    }
  }
}
