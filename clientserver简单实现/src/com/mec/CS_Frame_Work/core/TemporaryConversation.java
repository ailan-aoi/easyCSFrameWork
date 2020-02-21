package com.mec.CS_Frame_Work.core;

class TemporaryConversation {
	private ServerConversation serverConversation;
	private short count;
	
	public TemporaryConversation(ServerConversation serverConversation) {
		this.serverConversation = serverConversation;
	}

	ServerConversation getServerConversation() {
		return serverConversation;
	}

	short getCount() {
		return count;
	}
	
	int increaseCount() {
		return ++count;
	}
}
