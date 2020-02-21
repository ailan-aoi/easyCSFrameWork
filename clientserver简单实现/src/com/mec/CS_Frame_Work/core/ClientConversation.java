package com.mec.CS_Frame_Work.core;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;

import com.mec.action.ActionExecutor;
import com.my.util.core.MecCipher;

public class ClientConversation extends Communication {
	private Client client;
	private String id;
	private int keylen;
	
	public ClientConversation(Socket socket, Client client) throws IOException {
		super(socket);
		this.client = client;
		this.keylen = IConversation.SECRET_KEY_LENGTH;
	}

	void dealOutOfRoom(NetMessage message) {
		close();
		client.getClientAction().OutOfRoom();
	}
	
	void dealConnectSuccess(NetMessage message) {
		System.out.println("开始执行连接成功后的事情");
		client.getClientAction().connectSuccess();
	}
	
	void dealTooFast(NetMessage message) {
		close();
		client.getClientAction().connectTooFast();
	}

	void dealResponse(NetMessage message) {
		new ResponseWorker(message);
	}
	
	void dealWhoAreYou(NetMessage message) {
		this.id = message.getPara();
		byte[] binMess = getSecertText(id, keylen);
		sent(new NetMessage().setCommand(ENetCommnad.I_AM).setType(NetMessage.BIN).setByteCount(binMess.length), binMess);
	}
	
	private byte[] getSecertText(String id, int keyLen) {
		byte[] key = MecCipher.getSecretKey(keyLen);
		byte[] text = id.getBytes();
		text = MecCipher.encrypt(text, key);
		int keyLength = key.length;
		byte[] binMessage = new byte[keyLength + text.length];
		for (int i = 0; i < keyLength; i++) {
			binMessage[i] = key[i];
		}
		for (int i = 0; i < text.length; i++) {
			binMessage[i + keyLength] = text[i];
		}

		return binMessage;
	}
	
	@Override
	void dealNetMessage(NetMessage netMessage) {
		try {
			CommandDistributor.distributorCommand(this, netMessage);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
		}
	}

	@Override
	void dealNetMessage(NetMessage netMessage,  byte[] bytes) {
		try {
			CommandDistributor.distributorCommand(this, netMessage, bytes);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
		}
	}

	void sentRequsest(String request, String response, String params) {
		String action = request + "-" + response;
		this.sent(new NetMessage().
				setCommand(ENetCommnad.REQUEST).
				setAction(action).setPara(params));
	}
	
	public String getId() {
		return id;
	}

	class ResponseWorker implements Runnable{
		private NetMessage message;
		
		ResponseWorker(NetMessage netMessage) {
			this.message = netMessage;
			new Thread(this).start();
		}

		@Override
		public void run() {
			String action = message.getAction();
			String paraJson = message.getPara();
			String response = action.split("-")[1];
			
			try {
				ActionExecutor.executorResponse(response, paraJson);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	void peerAbnormalDrop() {
		client.getClientAction().dealAbnormalDrop();
	}
}
