package com.nyloer.npc;

public enum NyloType
{
	MELEE(new int[]{8342, 8345, 8348, 8351, 10774, 10777, 10780, 10783, 10791, 10794, 10797, 10800}),
	RANGE(new int[]{8343, 8346, 8349, 8352, 10775, 10778, 10781, 10784, 10792, 10795, 10798, 10801}),
	MAGE(new int[]{8344, 8347, 8350, 8353, 10776, 10779, 10782, 10785, 10793, 10796, 10799, 10802});

	private final int[] ids;

	NyloType(int[] ids)
	{
		this.ids = ids;
	}

	/** Returns the matching type for the given NPC ID, or null if not a nylocas. */
	public static NyloType fromId(int id)
	{
		for (NyloType type : values())
		{
			for (int npcId : type.ids)
			{
				if (npcId == id)
				{
					return type;
				}
			}
		}
		return null;
	}

	/** Lower-case name used as part of font config keys (e.g. "melee", "range", "mage"). */
	public String configKey()
	{
		return name().toLowerCase();
	}
}
