package com.aia.aspect;

public class CallTracker {

	private boolean called;

	public boolean isCalled() {
		return called;
	}

	public void resetCalled() {
		called=false;
	}
	
	protected void trackCall() {
		called=true;
	}

}
