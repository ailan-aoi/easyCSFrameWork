package com.mec.CS_Frame_Work.core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

abstract class Communication implements Runnable{
	private Socket socket;
	private DataInputStream dis;
	DataOutputStream dos;
	private Object lock;
	private volatile boolean goon;
	private static int number;
	
	Communication(Socket socket) throws IOException {
		this.socket = socket;
		goon = true;
		lock = new Object();
		
		dis = new DataInputStream(socket.getInputStream());
		dos = new DataOutputStream(socket.getOutputStream());
		new Thread(this, "线程" + number++).start();
	}

	abstract void dealNetMessage(NetMessage netMessage);
	abstract void dealNetMessage(NetMessage netMessage, byte[] bytes);
	abstract void peerAbnormalDrop();
	
	byte[] readByteMessage(int bytesCount) throws IOException{
		byte[] bytes = new byte[bytesCount];

		dis.readFully(bytes);

		return bytes;
	}
	
	void sent(NetMessage message) {
		try {
			dos.writeUTF(message.toString());
		} catch (IOException e) {
			close();
		}
	}
	
	void sent(NetMessage message, byte[] bytes) {
		message.setType(NetMessage.BIN);
		message.setByteCount(bytes.length);
		try {
			dos.writeUTF(message.toString());
			if (bytes != null) {
				dos.write(bytes);
			}
		} catch (IOException e) {
			peerAbnormalDrop();
			close();
		}
	}
	
	@Override
	public void run() {
		String message = "";
		synchronized (lock) {
			lock.notify();
		}
		while(goon) {
			try {
				message = dis.readUTF();
				NetMessage netMessage = new NetMessage(message);
				if (netMessage.getType() == NetMessage.BIN) {
					byte[] bytes = readByteMessage(netMessage.getByteCount());
					dealNetMessage(netMessage, bytes);
				} else {
					dealNetMessage(netMessage);
				}
			} catch (IOException e) {
				if (goon = true) {
					peerAbnormalDrop();
				}
				goon = false;
			}
		}
		close();
		
	}
	
	void close() {
		goon = false;
		if (dis != null) {
			try {
				dis.close();
			} catch (IOException e) {
			} finally {
				dis = null;
			}
		}
		if (dos != null) {
			try {
				dos.close();
			} catch (IOException e) {
			} finally {
				dos = null;
			}
		}
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
			} finally {
				socket = null;
			}
		}
	}
}
