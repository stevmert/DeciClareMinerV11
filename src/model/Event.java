package model;

import java.io.Serializable;

//TODO: use this instead of Activity in constraints!!! (=how DeciClare should be)
public class Event implements Serializable {

	private static final long serialVersionUID = 3080433760159276770L;

	private Activity act;

	public enum EventType {
		START, END, FAILURE, CANCELLATION
	}
	private EventType type;

	public Event(Activity act, EventType type) {
		super();
		this.act = act;
		this.type = type;
	}

	public Activity getAct() {
		return act;
	}

	public void setAct(Activity act) {
		this.act = act;
	}

	public EventType getType() {
		return type;
	}

	public void setType(EventType type) {
		this.type = type;
	}
}
