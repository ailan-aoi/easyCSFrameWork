package com.mec.CS_Frame_Work.core;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.my.util.core.PropertiesParser;


public class Client {
	private Socket socket;
	private int serverPort;
	private String serverIp;
	private ClientConversation conversation;
	
	private List<IListener> listenerList;
	private IClientAction clientAction;
	
	public Client() {
		listenerList = new ArrayList<>();
	}
	
	public void initClient(String configPath) {
		PropertiesParser.readConfig(configPath);
		serverIp = PropertiesParser.getValueByKey("serverIp");
		String stringPort = PropertiesParser.getValueByKey("serverPort");
		if (stringPort != null) {
			serverPort = Integer.valueOf(stringPort);
		}
	}

	public boolean connectTOServer() throws Exception{
		if (serverIp == null || serverPort <= 1000 || serverPort >= 65535) {
			System.out.println(serverIp + ":" + serverPort);
			throw new Exception("服务器ip或端口号错误！！");
		}
		try {
			System.out.println("开始连接服务器！！！");
			socket = new Socket(serverIp, serverPort);
			conversation = new ClientConversation(socket, this);
			System.out.println("服务器连接成功!!!");
		} catch (IOException e) {
			return false;
		}
		return true;
	}
	
	public void sentRequest(String request, String response, String params) {
		conversation.sentRequsest(request, response, params);
	}
	
	public Client setClientAction(IClientAction clientAction) {
		this.clientAction = clientAction;
		
		return this;
	}
	
	public boolean addLisenter(IListener listener) {
		if (listenerList.contains(listener)) {
			return false;
		}
		
		listenerList.add(listener);
		return true;
	}

	public boolean removeListener(IListener listener) {
		if (!listenerList.contains(listener)) {
			return true;
		}
		
		listenerList.remove(listener);
		return true;
	}
	
	public IClientAction getClientAction() {
		return clientAction;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}
	
	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	public void setServerIp(String serverIp) {
		this.serverIp = serverIp;
	}

	public ClientConversation getConversation() {
		return conversation;
	}
	
	public void shutdown() {
		conversation.close();
	}
	
}
