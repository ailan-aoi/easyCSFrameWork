package com.mec.CS_Frame_Work.core;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;

import com.mec.action.ActionExecutor;
import com.my.util.core.ArguementMarker;
import com.my.util.core.MecCipher;

class ServerConversation extends Communication {
	private Server server;
	private String clientId;
	private int keyLen;

	ServerConversation(Socket socket, Server server) throws IOException {
		super(socket);
		this.server = server;
		this.keyLen = IConversation.SECRET_KEY_LENGTH;
	}

	void setClientId(String clientId) {
		this.clientId = clientId;
	}

	private String getClientSecretId(byte[] binMessage) {
		byte[] key = new byte[keyLen];
		byte[] binMess = new byte[binMessage.length-keyLen];
		for (int i = 0; i < keyLen; i++) {
			key[i] = binMessage[i];
		}
		for (int i = 0; i < binMess.length; i++) {
			binMess[i] = binMessage[keyLen + i];
		}
		byte[] text = MecCipher.encrypt(binMess, key);

		return new String(text);
	}

	void dealIAm(NetMessage message, byte[] binMessage) {
		String secretId = getClientSecretId(binMessage);
		if (clientId == null || !clientId.equals(secretId)) {
			sent(new NetMessage().setCommand(ENetCommnad.ILLEGAL_USER));
			close();
		} else {
			sent(new NetMessage().setCommand(ENetCommnad.CONNECT_SUCCESS));
			synchronized (Server.class) {
				server.addConversationToFormalPool(clientId, this);
				server.getServerAction().dealLogin(clientId);
			}
		}
		synchronized (Server.class) {
			server.removeTemporaryConversation(clientId);
		}
		
	}
	
	class RequestWorker implements Runnable{
		private NetMessage netMessage;
		
		RequestWorker(NetMessage netMessage) {
			this.netMessage = netMessage;
			new Thread(this).start();
		}

		@Override
		public void run() {
			String action = netMessage.getAction();
			String paraJson = netMessage.getPara();
			String request = action.split("-")[0];
			try {
				Object obj = ActionExecutor.executorRequest(request, paraJson);
				ArguementMarker am = new ArguementMarker();
				am.addParam("result", obj);
				sent(new NetMessage().setCommand(ENetCommnad.RESPONSE).setAction(action).setPara(am.toString()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	void dealRequest(NetMessage netMessage) {
		new RequestWorker(netMessage);
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
	void dealNetMessage(NetMessage netMessage, byte[] bytes) {
		try {
			CommandDistributor.distributorCommand(this, netMessage, bytes);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
		}
	}

	@Override
	void peerAbnormalDrop() {
		server.removeTemporaryConversation(clientId);
		server.removeConversation(clientId);
		
		server.getServerAction().dealpeerAbnormalDrop(clientId);
	}
}
