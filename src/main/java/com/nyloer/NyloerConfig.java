package com.nyloer;

import java.awt.Color;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Keybind;


@ConfigGroup(NyloerConfig.GROUP)
public interface NyloerConfig extends Config
{
	String GROUP = "nyloer";

	@ConfigSection(
		name = "General Settings",
		position = 0,
		description = "General settings",
		closedByDefault = false
	)
	String generalSettings = "generalSettings";

	@ConfigItem(
		position = 0,
		keyName = "showNylocasSymbol",
		name = "Show Nylocas Symbol",
		description = "Displays selected symbol instead of wave.",
		section = generalSettings
	)
	default boolean showNylocasSymbol()
	{
		return true;
	}

	@ConfigItem(
		position = 1,
		keyName = "meleeNylocasSymbol",
		name = "Melee Symbol",
		description = "Symbol of melee nylocas.",
		section = generalSettings
	)
	default String meleeNylocasSymbol()
	{
		return "I";
	}

	@ConfigItem(
		position = 2,
		keyName = "rangeNylocasSymbol",
		name = "Range Symbol",
		description = "Symbol of range nylocas.",
		section = generalSettings
	)
	default String rangeNylocasSymbol()
	{
		return "T";
	}

	@ConfigItem(
		position = 3,
		keyName = "mageNylocasSymbol",
		name = "Mage Symbol",
		description = "Symbol of mage nylocas.",
		section = generalSettings
	)
	default String mageNylocasSymbol()
	{
		return "H";
	}

	@ConfigItem(
		position = 4,
		keyName = "showNylocasWave",
		name = "Show Nylocas Wave",
		description = "Display wave on nylocas. \"Show nylocas symbol\" must be disabled.",
		section = generalSettings
	)
	default boolean showNylocasWave()
	{
		return false;
	}

	@ConfigItem(
		position = 5,
		keyName = "wavePrefix",
		name = "Wave Spawn Prefix",
		description = "Prefix for spawned nylocas from waves.",
		section = generalSettings
	)
	default String wavePrefix()
	{
		return "";
	}

	@ConfigItem(
		position = 6,
		keyName = "splitPrefix",
		name = "Split Spawn Prefix",
		description = "Prefix for spawned nylocas from splits.",
		section = generalSettings
	)
	default String splitPrefix()
	{
		return "s";
	}

	@ConfigItem(
		position = 7,
		keyName = "tileHighlightWidth",
		name = "Tile Highlight Width",
		description = "Tile highlight width",
		section = generalSettings
	)
	default double tileHighlightWidth()
	{
		return 2;
	}

	// ------------------------------------------------------------

	@ConfigSection(
		name = "Font Settings",
		position = 2,
		description = "Fonts settings",
		closedByDefault = true
	)
	String fontsSettings = "fontsSettings";

	@ConfigItem(
		position = 0,
		keyName = "fontsType",
		name = "Font Type",
		description = "Nylocas wave number font type.",
		section = fontsSettings
	)
	default NyloerFonts fontsType()
	{
		return NyloerFonts.ARIAL;
	}

	@ConfigItem(
		position = 1,
		keyName = "fontsBold",
		name = "Bold",
		description = "Nylocas wave number fonts will be bold.",
		section = fontsSettings
	)
	default boolean fontsBold()
	{
		return true;
	}

	@ConfigItem(
		position = 2,
		keyName = "fontsSize",
		name = "Font Size",
		description = "Nylocas wave number size.",
		section = fontsSettings
	)
	default int fontsSize()
	{
		return 12;
	}

	@ConfigItem(
		position = 3,
		keyName = "splitFontsType",
		name = "Split Font Type",
		description = "Nylocas wave number font type (split)",
		section = fontsSettings
	)
	default NyloerFonts splitFontsType()
	{
		return NyloerFonts.ARIAL;
	}

	@ConfigItem(
		position = 4,
		keyName = "splitFontsBold",
		name = "Split Bold",
		description = "Nylocas wave number fonts will be bold (split)",
		section = fontsSettings
	)
	default boolean splitFontsBold()
	{
		return true;
	}

	@ConfigItem(
		position = 5,
		keyName = "splitFontsSize",
		name = "Split Font Size",
		description = "Nylocas wave number size (split)",
		section = fontsSettings
	)
	default int splitFontsSize()
	{
		return 12;
	}

	@Alpha
	@ConfigItem(
		position = 6,
		keyName = "meleeNylocasColor",
		name = "Melee Color",
		description = "Color of melee nylocas.",
		section = fontsSettings
	)
	default Color meleeNylocasColor()
	{
		return Color.WHITE;
	}

	@Alpha
	@ConfigItem(
		position = 7,
		keyName = "meleeNylocasOutlineColor",
		name = "Melee Outline Color",
		description = "Outline color of melee nylocas.",
		section = fontsSettings
	)
	default Color meleeNylocasOutlineColor()
	{
		return Color.BLACK;
	}

	@Alpha
	@ConfigItem(
		position = 8,
		keyName = "rangeNylocasColor",
		name = "Range Color",
		description = "Color of range nylocas.",
		section = fontsSettings
	)
	default Color rangeNylocasColor()
	{
		return Color.GREEN;
	}

	@Alpha
	@ConfigItem(
		position = 9,
		keyName = "rangeNylocasOutlineColor",
		name = "Range Outline Color",
		description = "Outline color of range nylocas.",
		section = fontsSettings
	)
	default Color rangeNylocasOutlineColor()
	{
		return Color.BLACK;
	}

	@Alpha
	@ConfigItem(
		position = 10,
		keyName = "mageNylocasColor",
		name = "Mage Color",
		description = "Color of mage nylocas.",
		section = fontsSettings
	)
	default Color mageNylocasColor()
	{
		return Color.CYAN;
	}

	@Alpha
	@ConfigItem(
		position = 11,
		keyName = "mageNylocasOutlineColor",
		name = "Mage Outline Color",
		description = "Outline color of mage nylocas.",
		section = fontsSettings
	)
	default Color mageNylocasOutlineColor()
	{
		return Color.BLACK;
	}

	@ConfigItem(
		position = 12,
		keyName = "makeDarkerHotkey",
		name = "Make Darker Hotkey",
		description = "Makes color darker for present nylocas in the room.",
		section = fontsSettings
	)
	default Keybind makeDarkerHotkey()
	{
		return Keybind.NOT_SET;
	}

	/*
	 * TLDR defaults:
	 * Solo: 31
	 * Duo: 29
	 * Trio: 28
	 * 4s: 27 offset 4
	 * 5s: 26
	 */
	// --- Scale 1 ---
	@ConfigItem(keyName = "darkerWave1", name = "", description = "", hidden = true)
	default int darkerWave1() { return 31; }

	@ConfigItem(keyName = "darkerWave1", name = "", description = "", hidden = true)
	void setDarkerWave1(int v);

	@ConfigItem(keyName = "darkerWaveOffset1", name = "", description = "", hidden = true)
	default int darkerWaveOffset1() { return 0; }

	@ConfigItem(keyName = "darkerWaveOffset1", name = "", description = "", hidden = true)
	void setDarkerWaveOffset1(int v);

	// --- Scale 2 ---
	@ConfigItem(keyName = "darkerWave2", name = "", description = "", hidden = true)
	default int darkerWave2() { return 29; }

	@ConfigItem(keyName = "darkerWave2", name = "", description = "", hidden = true)
	void setDarkerWave2(int v);

	@ConfigItem(keyName = "darkerWaveOffset2", name = "", description = "", hidden = true)
	default int darkerWaveOffset2() { return 0; }

	@ConfigItem(keyName = "darkerWaveOffset2", name = "", description = "", hidden = true)
	void setDarkerWaveOffset2(int v);

	// --- Scale 3 ---
	@ConfigItem(keyName = "darkerWave3", name = "", description = "", hidden = true)
	default int darkerWave3() { return 28; }

	@ConfigItem(keyName = "darkerWave3", name = "", description = "", hidden = true)
	void setDarkerWave3(int v);

	@ConfigItem(keyName = "darkerWaveOffset3", name = "", description = "", hidden = true)
	default int darkerWaveOffset3() { return 0; }

	@ConfigItem(keyName = "darkerWaveOffset3", name = "", description = "", hidden = true)
	void setDarkerWaveOffset3(int v);

	// --- Scale 4 ---
	@ConfigItem(keyName = "darkerWave4", name = "", description = "", hidden = true)
	default int darkerWave4() { return 27; }

	@ConfigItem(keyName = "darkerWave4", name = "", description = "", hidden = true)
	void setDarkerWave4(int v);

	@ConfigItem(keyName = "darkerWaveOffset4", name = "", description = "", hidden = true)
	default int darkerWaveOffset4() { return 4; }

	@ConfigItem(keyName = "darkerWaveOffset4", name = "", description = "", hidden = true)
	void setDarkerWaveOffset4(int v);

	// --- Scale 5 ---
	@ConfigItem(keyName = "darkerWave5", name = "", description = "", hidden = true)
	default int darkerWave5() { return 26; }

	@ConfigItem(keyName = "darkerWave5", name = "", description = "", hidden = true)
	void setDarkerWave5(int v);

	@ConfigItem(keyName = "darkerWaveOffset5", name = "", description = "", hidden = true)
	default int darkerWaveOffset5() { return 0; }

	@ConfigItem(keyName = "darkerWaveOffset5", name = "", description = "", hidden = true)
	void setDarkerWaveOffset5(int v);

	@ConfigItem(
		position = 15,
		keyName = "customFontConfig",
		name = "Font config",
		description = "Config settings wave:melee/range/mage:color",
		section = fontsSettings
	)
	default String customFontConfig()
	{
		return "0:mage:#00FFFF";
	}


	// ------------------------------------------------------------
	@ConfigSection(
		name = "Role Swapper",
		position = 3,
		description = "Role Swapper Settings",
		closedByDefault = true
	)
	String roleSwapperSettings = "roleSwapperSettings";

	@ConfigItem(
		keyName = "mageRoleSwaps",
		name = "Mage Swaps",
		description = "Custom swaps for mage role",
		section = roleSwapperSettings,
		position = 0
	)
	default String mageRoleSwaps()
	{
		return "attack,nylocas ischyros*260*\n" +
			"attack,nylocas ischyros*162*\n" +
			"attack,nylocas toxobolos*260*\n" +
			"attack,nylocas toxobolos*162*\n" +
			"attack,nylocas hagios*260*\n" +
			"attack,nylocas hagios*162*";
	}

	@ConfigItem(
		keyName = "mageRoleShiftSwaps",
		name = "Mage Shift Swaps",
		description = "Custom shift swaps for mage role",
		section = roleSwapperSettings,
		position = 1
	)
	default String mageRoleShiftSwaps()
	{
		return "/walk here,*\n" +
			"/attack,nylocas hagios*162*";
	}

	@ConfigItem(
		keyName = "mageHighlightMageTiles",
		name = "Highlight Mage Tiles",
		description = "Highlights mage nylo tiles when mage role is selected.",
		section = roleSwapperSettings,
		position = 2
	)
	default boolean mageHighlightMageTiles()
	{
		return false;
	}

	@ConfigItem(
		keyName = "mageHighlightRangeTiles",
		name = "Highlight Range Tiles",
		description = "Highlights range nylo tiles when mage role is selected.",
		section = roleSwapperSettings,
		position = 3
	)
	default boolean mageHighlightRangeTiles()
	{
		return false;
	}

	@ConfigItem(
		keyName = "mageHighlightMeleeTiles",
		name = "Highlight Melee Tiles",
		description = "Highlights melee nylo tiles when mage role is selected.",
		section = roleSwapperSettings,
		position = 4
	)
	default boolean mageHighlightMeleeTiles()
	{
		return false;
	}

	@ConfigItem(
		keyName = "rangeRoleSwaps",
		name = "Range Swaps",
		description = "Custom swaps for range role",
		section = roleSwapperSettings,
		position = 5
	)
	default String rangeRoleSwaps()
	{
		return "attack,nylocas ischyros*260*\n" +
			"attack,nylocas ischyros*162*\n" +
			"attack,nylocas hagios*260*\n" +
			"attack,nylocas hagios*162*\n" +
			"attack,nylocas toxobolos*260*\n" +
			"attack,nylocas toxobolos*162*";
	}

	@ConfigItem(
		keyName = "rangeRoleShiftSwaps",
		name = "Range Shift Swaps",
		description = "Custom shift swaps for range role",
		section = roleSwapperSettings,
		position = 6
	)
	default String rangeRoleShiftSwaps()
	{
		return "/walk here,*\n" +
			"/attack,nylocas toxobolos*162*";
	}

	@ConfigItem(
		keyName = "rangeHighlightMageTiles",
		name = "Highlight Mage Tiles",
		description = "Highlights mage nylo tiles when range role is selected.",
		section = roleSwapperSettings,
		position = 7
	)
	default boolean rangeHighlightMageTiles()
	{
		return false;
	}

	@ConfigItem(
		keyName = "rangeHighlightRangeTiles",
		name = "Highlight Range Tiles",
		description = "Highlights range nylo tiles when range role is selected.",
		section = roleSwapperSettings,
		position = 8
	)
	default boolean rangeHighlightRangeTiles()
	{
		return false;
	}

	@ConfigItem(
		keyName = "rangeHighlightMeleeTiles",
		name = "Highlight Melee Tiles",
		description = "Highlights melee nylo tiles when range role is selected.",
		section = roleSwapperSettings,
		position = 9
	)
	default boolean rangeHighlightMeleeTiles()
	{
		return false;
	}

	@ConfigItem(
		keyName = "meleeRoleSwaps",
		name = "Melee Swaps",
		description = "Custom swaps for melee role",
		section = roleSwapperSettings,
		position = 10
	)
	default String meleeRoleSwaps()
	{
		return "attack,nylocas hagios*269*\n" +
			"attack,nylocas hagios*162*\n" +
			"attack,nylocas toxobolos*260*\n" +
			"attack,nylocas toxobolos*162*\n" +
			"attack,nylocas ischyros*260*\n" +
			"attack,nylocas ischyros*162*";
	}

	@ConfigItem(
		keyName = "meleeRoleShiftSwaps",
		name = "Melee Shift Swaps",
		description = "Custom shift swaps for melee role",
		section = roleSwapperSettings,
		position = 11
	)
	default String meleeRoleShiftSwaps()
	{
		return "/walk here,*\n" +
			"/attack,nylocas ischyros*162*";
	}

	@ConfigItem(
		keyName = "meleeHighlightMageTiles",
		name = "Highlight Mage Tiles",
		description = "Highlights mage nylo tiles when melee role is selected.",
		section = roleSwapperSettings,
		position = 12
	)
	default boolean meleeHighlightMageTiles()
	{
		return false;
	}

	@ConfigItem(
		keyName = "meleeHighlightRangeTiles",
		name = "Highlight Range Tiles",
		description = "Highlights range nylo tiles when melee role is selected.",
		section = roleSwapperSettings,
		position = 13
	)
	default boolean meleeHighlightRangeTiles()
	{
		return false;
	}

	@ConfigItem(
		keyName = "meleeHighlightMeleeTiles",
		name = "Highlight Melee Tiles",
		description = "Highlights melee nylo tiles when melee role is selected.",
		section = roleSwapperSettings,
		position = 14
	)
	default boolean meleeHighlightMeleeTiles()
	{
		return false;
	}

	@ConfigItem(
		keyName = "customRoleSwaps",
		name = "Custom Swaps",
		description = "Custom swaps for Custom role",
		section = roleSwapperSettings,
		position = 15
	)
	default String customRoleSwaps()
	{
		return "attack,nylocas toxobolos*260*\n" +
			"attack,nylocas toxobolos*162*\n" +
			"attack,nylocas hagios*260*\n" +
			"attack,nylocas hagios*162*\n" +
			"attack,nylocas ischyros*260*\n" +
			"attack,nylocas ischyros*162*";
	}

	@ConfigItem(
		keyName = "customRoleShiftSwaps",
		name = "Custom Shift Swaps",
		description = "Custom shift swaps for Custom role",
		section = roleSwapperSettings,
		position = 16
	)
	default String customRoleShiftSwaps()
	{
		return "/walk here,*\n" +
			"/attack,nylocas hagios*162*";
	}

	@ConfigItem(
		keyName = "customHighlightMageTiles",
		name = "Highlight Mage Tiles",
		description = "Highlights mage nylo tiles when custom role is selected.",
		section = roleSwapperSettings,
		position = 17
	)
	default boolean customHighlightMageTiles()
	{
		return false;
	}

	@ConfigItem(
		keyName = "customHighlightRangeTiles",
		name = "Highlight Range Tiles",
		description = "Highlights range nylo tiles when custom role is selected.",
		section = roleSwapperSettings,
		position = 18
	)
	default boolean customHighlightRangeTiles()
	{
		return false;
	}

	@ConfigItem(
		keyName = "customHighlightMeleeTiles",
		name = "Highlight Melee Tiles",
		description = "Highlights melee nylo tiles when custom role is selected.",
		section = roleSwapperSettings,
		position = 19
	)
	default boolean customHighlightMeleeTiles()
	{
		return false;
	}

	@ConfigItem(
		keyName = "displayMageTilesAsTrueTiles",
		name = "True tiles for mage",
		description = "True tiles will be displayed for magers",
		section = roleSwapperSettings,
		position = 20
	)
	default boolean displayMageTilesAsTrueTiles()
	{
		return false;
	}

	@ConfigItem(
		keyName = "displayRangeTilesAsTrueTiles",
		name = "True tiles for range",
		description = "True tiles will be displayed for rangers",
		section = roleSwapperSettings,
		position = 21
	)
	default boolean displayRangeTilesAsTrueTiles()
	{
		return false;
	}

	@ConfigItem(
		keyName = "displayMeleeTilesAsTrueTiles",
		name = "True tiles for melees",
		description = "True tiles will be displayed for melees",
		section = roleSwapperSettings,
		position = 22
	)
	default boolean displayMeleeTilesAsTrueTiles()
	{
		return false;
	}

	@ConfigItem(
		keyName = "previousRole",
		name = "",
		description = "",
		hidden = true
	)
	default String previousRole()
	{
		return "";
	}

	@ConfigItem(
		keyName = "previousRole",
		name = "",
		description = "",
		hidden = true
	)
	void setPreviousRole(String role);
}
