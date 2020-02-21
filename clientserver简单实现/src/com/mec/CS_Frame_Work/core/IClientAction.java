package com.mec.CS_Frame_Work.core;

public interface IClientAction {
	void OutOfRoom();
	void connectTooFast();
	void connectSuccess();
	void dealAbnormalDrop();
}
