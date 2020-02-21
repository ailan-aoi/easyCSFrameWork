package com.mec.CS_Frame_Work.core;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.my.util.core.PropertiesParser;
import com.my.util.core.TickTick;

public class Server implements Runnable, ISpeaker{
	private static final int DEFAULT_MAX_CLIENT_COUNT = 50;
	private static final int DEFAULT_MIN_CONNECT_TIME = 1000;

	private ServerSocket serverSocket;
	private int port;
	private volatile boolean goon;
	private boolean isStartup;
	private Object preTreatMentLock;
	private int maxClientCount;
	private int minConnectTime;
	
	private IServerAction serverAction;
	private Gson gson;
	private ConcurrentLinkedQueue<Socket> socketQueue;
	private Map<String, ServerConversation> clientFormalPool;
	private Map<String, TemporaryConversation> clientTemporaryPool;
	private List<IListener> listenerList;
	
	public Server() {
		this(0);
	}

	public Server(int port) {
		listenerList = new ArrayList<>();
		this.port = port;
		goon = false;
		isStartup = false;
		maxClientCount = DEFAULT_MAX_CLIENT_COUNT;
		minConnectTime = DEFAULT_MIN_CONNECT_TIME;
		preTreatMentLock = new Object();
		gson = new GsonBuilder().create();
	}

	public void initServer(String configPath) throws Exception {
		PropertiesParser.readConfig(configPath);
		String stringPort = PropertiesParser.getValueByKey("serverPort");
		if (stringPort == null) {
			throw new Exception("端口号不能为零！！！");
			}
		port = Integer.valueOf(stringPort);
		String StringClientcount = PropertiesParser.getValueByKey("maxClientCount");
		if (StringClientcount != null) {
			maxClientCount = Integer.valueOf(StringClientcount);
		}
		String StringConnectTime = PropertiesParser.getValueByKey("minConnectTime");
		if (StringConnectTime != null) {
			minConnectTime = Integer.valueOf(StringConnectTime);
		}
	}
	
	public void startup() throws Exception {
		if (isStartup) {
			SpeakOut("服务器已经启动，无需再次启动！！！");
			return;
		}
		if (port <= 1000 || port >65535) {
			throw new Exception("无效的端口值");
		}
		goon = true;
		SpeakOut("服务器正在启动...");
		isStartup = true;
		serverSocket = new ServerSocket(port);
		clientFormalPool = new ConcurrentHashMap<>();
		clientTemporaryPool = new ConcurrentHashMap<>();
		socketQueue = new ConcurrentLinkedQueue<>();
		new Thread(this, "服务器侦听线程").start();
		SpeakOut("服务器启动成功...");
	}
	
	public void shutdown() {
		if (!isStartup) {
			SpeakOut("服务器已经关闭，无需再次宕机！！");
		}
		if (!clientFormalPool.isEmpty() || !clientTemporaryPool.isEmpty()) {
			SpeakOut("仍有客户在线，不能宕机！");
		}
		SpeakOut("服务器已宕机");
		goon = false;
		close();
		isStartup = false;
	}
	
	@Override
	public void run() {
		Socket socket = null;
		new PreTreatment();
		synchronized (preTreatMentLock) {
			try {
				preTreatMentLock.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		SpeakOut("服务器的侦听线程开始工作！！");
		while (goon) {
			try {
				socket = serverSocket.accept();
				SpeakOut("侦听到客户端" + socket.getInetAddress().getHostAddress() + "上线");
				socketQueue.add(socket);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	class PreTreatment implements Runnable{
		
		public PreTreatment() {
			new Thread(this, "预处理线程").start();
		}
		
		@Override
		public void run() {
			SpeakOut("预处理线程已启动");
			Socket socket = null;
			ServerConversation conversation = null;
			String clientId = null;
			synchronized (preTreatMentLock) {
				preTreatMentLock.notify();
			}
			while (goon) {
				if (!socketQueue.isEmpty()) {
					socket = socketQueue.poll();
					if (socket == null) {
						continue;
					}
					
					try {
						conversation = new ServerConversation(socket, Server.this);
						if (isOutOfRoom(conversation) || (clientId = clientIsExist(socket, conversation)) == null) {
							continue;
						}
						conversation.setClientId(clientId);
						NetMessage message = new NetMessage().setCommand(ENetCommnad.WHO_ARE_YOU).setPara(clientId);
						conversation.dos.writeUTF(message.toString());
						SpeakOut("已发送验证请求！！！");
						clientTemporaryPool.put(clientId, new TemporaryConversation(conversation));
					} catch (IOException e) {
					}
				}
			}
		}
		
	}

	public TemporaryConversation removeTemporaryConversation(String clientId) {
		synchronized (Server.class) {
			return clientTemporaryPool.remove(clientId);
		}
	}
	
	public ServerConversation removeConversation(String clientId) {
		synchronized (Server.class) {
			return clientFormalPool.remove(clientId);
		}
	}

	public void addConversationToFormalPool(String clientId, ServerConversation serverConversation) {
		clientFormalPool.put(clientId, serverConversation);
	}
	
	public String clientIsExist(Socket socket, ServerConversation serverConversation) {
		String clientId = socket.getLocalAddress().getHostAddress() + (System.currentTimeMillis() / minConnectTime);
		
		if (clientFormalPool.containsKey(clientId) || clientTemporaryPool.containsKey(clientId)) {
			serverConversation.sent(new NetMessage().setCommand(ENetCommnad.TOO_FASY));
			serverConversation.close();
			
			return null;
		}
		
		return clientId;
	}
	
	public boolean isOutOfRoom(ServerConversation serverConversation) {
		int currentClientCount = clientFormalPool.size() + clientTemporaryPool.size();
		if (currentClientCount >= maxClientCount) {
			serverConversation.sent(new NetMessage().setCommand(ENetCommnad.OUT_OF_ROOM));
			serverConversation.close();
			
			return true;
		}
		
		return false;
	}
	
	class CleanerThread extends TickTick{

		public CleanerThread() {
		}

		@Override
		public void doSomething() {
			synchronized (Server.class) {
				Set<String> clientIdSet = clientTemporaryPool.keySet();
				for (String clientId : clientIdSet) {
					TemporaryConversation conversation = clientTemporaryPool.get(clientId);
					if (conversation.increaseCount() >= 2) {
						removeTemporaryConversation(clientId);
					}
				}
			}
		}
	}
	
	private void close() {
		goon = false;
		try {
			serverSocket.close();
		} catch (IOException e) {
		} finally {
			serverSocket = null;
		}
	}
	
	@Override
	public void SpeakOut(String message) {
		for (IListener listener : listenerList) {
			listener.dealSpeech(message);
		}
	}

	@Override
	public boolean addLisenter(IListener listener) {
		if (listenerList.contains(listener)) {
			return false;
		}
		
		listenerList.add(listener);
		return true;
	}

	@Override
	public boolean removeListener(IListener listener) {
		if (!listenerList.contains(listener)) {
			return true;
		}
		
		listenerList.remove(listener);
		return true;
	}

	IServerAction getServerAction() {
		return serverAction;
	}

	public void setport(int port) {
		this.port = port;
	}
	
	public void setServerAction(IServerAction serverAction) {
		this.serverAction = serverAction;
	}

	public Gson getGson() {
		return gson;
	}
	
	public boolean isServerStartup() {
		return isStartup;
	}

}
