package com.rperry.states;

public class State<T extends BasicState> {
	private T stateRef;

	public State(T state) {
		this.stateRef = state;
	}
}
