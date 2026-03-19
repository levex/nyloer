package com.nyloer.npc;

public enum NyloSize
{
	SMALL,
	BIG;

	public static NyloSize fromNpcSize(int npcSize)
	{
		return npcSize == 1 ? SMALL : BIG;
	}
}
