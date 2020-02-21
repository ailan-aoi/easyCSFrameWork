package com.mec.CS_Frame_Work.core;

public enum ENetCommnad {
	/**
	 * 服务器已连接的客户端数达到规定的最大值。
	 */
	OUT_OF_ROOM,
	/**
	 * 客户端连接的过于频繁。
	 */
	TOO_FASY,
	/**
	 * 客户端连接到连接池，并且询问对方的身份.
	 */
	WHO_ARE_YOU,
	/**
	 *客户端发送的验证请求
	 */
	I_AM,
	/**
	 * 客户端非法用户
	 */
	ILLEGAL_USER,
	/**
	 * 客户端连接成功，应该进入正式池
	 */
	CONNECT_SUCCESS,
	/**
	 * 客户端向服务器请求的操作。
	 */
	REQUEST,
	/**
	 * 客户端得到服务器的回应后，应该做到的操作。
	 */
	RESPONSE,
}
