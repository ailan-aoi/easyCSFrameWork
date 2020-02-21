package com.mec.CS_Frame_Work.core;

public interface ISpeaker {
	void SpeakOut(String message);
	
	boolean addLisenter(IListener listener);
	
	boolean removeListener(IListener listener);
}
