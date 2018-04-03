package com.googlecode.gwtquake.server;

public class Msg {
  public byte[] fromIp;
  public int fromPort;
  public String data;

  public Msg(byte[] fromIp, int fromPort, String data) {
    this.fromIp = fromIp;
    this.fromPort = fromPort;
    this.data = data;
  }
}
