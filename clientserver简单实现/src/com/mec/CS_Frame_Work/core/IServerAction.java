package com.mec.CS_Frame_Work.core;

public interface IServerAction {
		void dealpeerAbnormalDrop(String clientId);
		void dealOffline(String clientId);
		void dealLogin(String clientId);
}
