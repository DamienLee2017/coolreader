package com.dotcool.reader.callback;

public class CallbackEventData implements ICallbackEventData {
	protected String message;
	protected String source;

	public CallbackEventData() {};

	public CallbackEventData(String message) {
		this.message = message;
	}

	public CallbackEventData(String message, String source) {
		this.message = message;
		this.source = source;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getSource() {
		return source;
	}
}
