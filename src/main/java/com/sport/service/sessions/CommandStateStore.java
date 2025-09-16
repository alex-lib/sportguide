package com.sport.service.sessions;
import org.springframework.stereotype.Component;

@Component
public class CommandStateStore {

	private static final long TTL_SECONDS = 300;

	private final RedisSessionStore store;

	public CommandStateStore(RedisSessionStore store) {
		this.store = store;
	}

	private String cmdKey(Long userId) {
		return "cmd:" + userId;
	}

	public void setCurrentCommand(Long userId, String command) {
		store.put(cmdKey(userId), command, TTL_SECONDS);
	}

	public String getCurrentCommand(Long userId) {
		return store.get(cmdKey(userId), String.class);
	}

	public void clearCurrentCommand(Long userId) {
		store.delete(cmdKey(userId));
	}
}