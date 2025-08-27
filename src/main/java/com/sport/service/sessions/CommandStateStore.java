package com.sport.service.sessions;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

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

	private String selKey(Long userId) {
		return "sel:" + userId;
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

	public List<String> getSelections(Long userId) {
		List<String> sel = store.get(selKey(userId), List.class);
		return sel != null ? sel : new ArrayList<>();
	}

	public void addSelection(Long userId, String value) {
		List<String> sel = getSelections(userId);
		sel.add(value);
		store.put(selKey(userId), sel, TTL_SECONDS);
	}

	public void clearSelections(Long userId) {
		store.delete(selKey(userId));
	}
}