package com.nyloer.npc;

import com.nyloer.NyloerConfig;
import com.nyloer.fonts.CustomFontConfig;
import java.awt.Color;
import java.awt.Font;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;

public class NyloerNpc
{
	@Getter private NPC npc;
	@Getter private int id;
	@Getter private int lastId;
	@Getter private NyloType type;
	@Getter private int index;
	@Getter private NyloSize size;
	@Getter @Setter private boolean isAlive;
	@Getter private boolean isSplit;
	@Getter private String spawn;
	@Getter private int waveSpawned;
	@Getter public int tickSpawned;
	@Getter private int ticksAlive;
	@Getter private String nyloerSymbol;
	@Getter private Color color;
	@Getter private Color outlineColor;
	@Getter private boolean colorDarker;
	@Getter private Font font;
	@Getter private String fontConfigKey;

	private final NyloerConfig config;
	private final CustomFontConfig customFontConfig;

	public NyloerNpc(NPC npc, Client client, NyloerConfig config, CustomFontConfig customFontConfig, int waveNumber, int lastWaveTickSpawned)
	{
		this.config = config;
		this.customFontConfig = customFontConfig;
		this.npc = npc;
		this.id = npc.getId();
		this.lastId = npc.getId();
		this.index = npc.getIndex();
		this.isAlive = true;
		this.spawn = findSpawn(npc, client);
		this.isSplit = spawn.equals("SPLIT");
		this.tickSpawned = client.getTickCount();
		this.colorDarker = false;
		this.waveSpawned = (isSplit && tickSpawned > lastWaveTickSpawned) ? waveNumber + 1 : waveNumber;
		this.ticksAlive = 0;
		updateStyle(id);
		configureFonts();
		this.size = NyloSize.fromNpcSize(npc.getComposition().getSize());
	}

	public void incrementTicks()
	{
		++ticksAlive;
		if (ticksAlive == 52)
		{
			isAlive = false;
		}
		if (lastId != npc.getId())
		{
			lastId = npc.getId();
			updateStyle(npc.getId());
		}
	}

	/** Called when this NPC is the first of a new wave; bumps waveSpawned and refreshes style. */
	public void bumpWave()
	{
		++waveSpawned;
		updateStyle(id);
	}

	private void updateStyle(int npcId)
	{
		type = NyloType.fromId(npcId);
		if (type == null)
		{
			fontConfigKey = null;
			color = Color.WHITE;
			outlineColor = Color.BLACK;
			isAlive = false;
			return;
		}

		fontConfigKey = waveSpawned + "-" + type.configKey();
		switch (type)
		{
			case MELEE:
				color = config.meleeNylocasColor();
				outlineColor = config.meleeNylocasOutlineColor();
				nyloerSymbol = config.meleeNylocasSymbol();
				break;
			case RANGE:
				color = config.rangeNylocasColor();
				outlineColor = config.rangeNylocasOutlineColor();
				nyloerSymbol = config.rangeNylocasSymbol();
				break;
			case MAGE:
				color = config.mageNylocasColor();
				outlineColor = config.mageNylocasOutlineColor();
				nyloerSymbol = config.mageNylocasSymbol();
				break;
		}

		Color customColor = customFontConfig.getColor(fontConfigKey);
		if (customColor != null)
		{
			color = customColor;
		}
		if (colorDarker)
		{
			color = color.darker().darker();
		}
	}

	private void configureFonts()
	{
		int fontStyle;
		if (isSplit)
		{
			fontStyle = config.splitFontsBold() ? Font.BOLD : Font.PLAIN;
			this.font = new Font(config.splitFontsType().toString(), fontStyle, config.splitFontsSize());
		}
		else
		{
			fontStyle = config.fontsBold() ? Font.BOLD : Font.PLAIN;
			this.font = new Font(config.fontsType().toString(), fontStyle, config.fontsSize());
		}
	}

	private static String findSpawn(NPC npc, Client client)
	{
		WorldPoint wp = WorldPoint.fromLocalInstance(client, npc.getLocalLocation());
		int x = wp.getRegionX();
		int y = wp.getRegionY();
		if (x == 17 && y == 25) return "WEST NORTH";
		if (x == 17 && y == 24) return "WEST SOUTH";
		if (x == 31 && y == 9)  return "SOUTH WEST";
		if (x == 32 && y == 9)  return "SOUTH EAST";
		if (x == 46 && y == 24) return "EAST SOUTH";
		if (x == 46 && y == 25) return npc.getComposition().getSize() == 1 ? "EAST NORTH" : "EAST BIG";
		if (x == 18 && y == 25) return "WEST BIG";
		if (x == 32 && y == 10) return "SOUTH BIG";
		if (x == 47 && y == 25) return "EAST BIG 30";
		return "SPLIT";
	}
}
