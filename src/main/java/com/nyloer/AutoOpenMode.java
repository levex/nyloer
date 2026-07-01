package com.nyloer;

public enum AutoOpenMode
{
	OFF("Off"),
	ENTER_AREA("When entering Nylocas area"),
	ROOM_START("When starting the room"),
	BOTH("Both");

	private final String label;

	AutoOpenMode(String label)
	{
		this.label = label;
	}

	public boolean opensOnEnterArea()
	{
		return this == ENTER_AREA || this == BOTH;
	}

	public boolean opensOnRoomStart()
	{
		return this == ROOM_START || this == BOTH;
	}

	@Override
	public String toString()
	{
		return label;
	}
}
