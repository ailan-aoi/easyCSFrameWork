package com.mec.CS_Frame_Work.core;

public class NetMessage {
	static final int ST = 0;
	static final int BIN = 1;
	
	private int type;
	private int byteCount;
	private ENetCommnad command;
	private String action;
	private String para;
	
	NetMessage() {
	}
	
//	规定：每一个成员之间需要用"&"来进分隔.
	NetMessage(String message) {
		String[] fields = message.split("&");
		type = Integer.valueOf(fields[0]);
		if (type == BIN) {
			byteCount = Integer.valueOf(fields[1]);
		}
		command = ENetCommnad.valueOf(fields[2]);
		action = fields[3] == " " ? null : fields[3];
		para = fields[4] == " " ? null : fields[4];
	}
	
	int getType() {
		return type;
	}
	
	NetMessage setType(int type) {
		this.type = type;
		
		return this;
	}
	
	int getByteCount() {
		return byteCount;
	}
	
	NetMessage setByteCount(int byteCount) {
		this.byteCount = byteCount;
		
		return this;
	}
	
	ENetCommnad getCommand() {
		return command;
	}
	
	NetMessage setCommand(ENetCommnad command) {
		this.command = command;
		
		return this;
	}
	
	String getAction() {
		return action;
	}
	
	NetMessage setAction(String action) {
		this.action = action;
		
		return this;
	}
	
	String getPara() {
		return para;
	}
	
	NetMessage setPara(String para) {
		this.para = para;
		
		return this;
	}
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer().append(type).append("&");
		
		buffer.append(byteCount).append("&").append(command)
		.append("&").append(action == null ? " " : action)
		.append("&").append(para == null ? " " : para);
		
		return buffer.toString();
	}
}
