/*
Copyright (C) 2010 Copyright 2010 Google Inc.

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/
package com.googlecode.gwtquake.server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.googlecode.gwtquake.shared.common.Compatibility;
import com.googlecode.gwtquake.shared.common.ResourceLoader;
import com.googlecode.gwtquake.shared.server.QuakeServer;
import com.googlecode.gwtquake.shared.sys.NET;

/**
 * This entry point runs a multiplayer-capable GwtQuake server.
 */
public class GwtQuakeServer {

  private static void printUsageAndDie() {
    System.err.println("usage: GwtQuakeServer [port] [quake args]");
    System.exit(-1);
  }

  public static void main(String[] args) throws Exception {
    String[] qargs = args;

    int port = 8080;
    if (args.length > 0) {
      try {
        port = Integer.parseInt(args[0]);
      } catch (NumberFormatException e) {
        System.err.println("Unable to parse port: " + args[0]);
        printUsageAndDie();
      }

      qargs = new String[args.length - 1];
      System.arraycopy(args, 1, qargs, 0, qargs.length);
    }

    Compatibility.impl = new CompatibilityImpl();
    ResourceLoader.impl = new ResourceLoaderImpl();

    ServletContextHandler context = createServer(port);
    NET.socketFactory = new ServerWebSocketFactoryImpl(context);
    QuakeServer.run(qargs);
  }

  private static ServletContextHandler createServer(int port) throws Exception {
    Server server = new Server(port);

    ServletContextHandler context = new ServletContextHandler();
    context.setResourceBase("war");
    context.addServlet(new ServletHolder(new GwtQuakeServlet()), "/GwtQuake.html");
    context.addServlet(new ServletHolder(new DefaultServlet()), "/*");
    server.setHandler(context);

    server.start();
    return context;
  }
}
