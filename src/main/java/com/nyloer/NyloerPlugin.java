package com.nyloer;

import com.google.inject.Provides;
import com.nyloer.ui.DarkerEntry;
import com.nyloer.fonts.CustomFontConfig;
import com.nyloer.npc.NyloSize;
import com.nyloer.npc.NyloType;
import com.nyloer.npc.NyloerNpc;
import com.nyloer.overlays.NyloerOverlay;
import com.nyloer.overlays.NyloerTileOverlay;
import com.nyloer.roleswapper.RoleSwapper;
import com.nyloer.stats.StatsHandler;
import com.nyloer.ui.NyloerSidePanel;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import lombok.Getter;
import net.runelite.api.Animation;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.Model;
import net.runelite.api.ModelData;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.Renderable;
import net.runelite.api.RuneLiteObject;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.callback.Hooks;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.NpcUtil;
import net.runelite.client.input.KeyListener;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PluginDescriptor(
	name = "Nyloer"
)
public class NyloerPlugin extends Plugin implements KeyListener
{
	public static final Logger log = LoggerFactory.getLogger(NyloerPlugin.class);

	private static final int NYLOCAS_REGION_ID = 13122;
	private static final int[] NYLOCAS_PILLAR_IDS = {8358, 10811, 10790};

	@Inject public Client client;
	@Inject public ClientThread clientThread;
	@Inject private ClientToolbar clientToolbar;
	@Inject private EventBus eventBus;
	@Inject public NyloerConfig config;
	@Inject public RoleSwapper roleSwapper;
	@Inject public StatsHandler statsHandler;
	@Inject public CustomFontConfig customFontConfig;
	@Inject private OverlayManager overlayManager;
	@Inject private NpcUtil npcUtil;
	@Inject private NyloerOverlay nyloerOverlay;
	@Inject public NyloerTileOverlay nyloerTileOverlay;
	@Inject private KeyManager keyManager;
	@Inject private Hooks hooks;

	public NyloerSidePanel sidePanel;
	public NavigationButton sidePanelButton;

	@Getter private boolean isNylocasRegion;
	@Getter private boolean isNylocasRegionLast;
	@Getter private boolean pillarsSpawned;
	@Getter private int wave1Tick;
	@Getter private int waveNumber;
	@Getter private int nylocasAliveCount;
	@Getter private final List<NyloerNpc> nyloers = new ArrayList<>();
	@Getter private final Map<Integer, NyloerNpc> nyloersIndexMap = new HashMap<>();
	// Persists wave/spawn assignments across despawn/respawn cycles to handle client-side NPC flicker.
	private final Map<Integer, Integer> nyloIndexWaveMap = new HashMap<>();
	private final Map<Integer, String> nyloIndexSpawnMap = new HashMap<>();
	/** Each rule: {tickThreshold, dimSmalls(1/0), dimBigs(1/0)}. A nylo is dimmed if its spawnTick <= tickThreshold and its size bit is set. */
	@Getter private final List<int[]> activeDimRules = new ArrayList<>();

	/** Each entry: {fireAtTick, dimWave, dimOffset, dimSmalls(1/0), dimBigs(1/0)} */
	private final List<int[]> pendingDarkerEvents = new ArrayList<>();
	private final Map<Integer, Integer> waveSpawnTicks = new HashMap<>();
	private int lastWaveTickSpawned;

	private static final int RULE_LIMIT_MODEL_THRESHOLD = 10;
	private static final int BIG_LIMIT_NPC_ID = 2793;
	private static final int SMALL_LIMIT_NPC_ID = 2794;
	private static final int BIG_LIMIT_STAND_ANIM_ID = 5852;
	private static final int BIG_LIMIT_WALK_ANIM_ID = 5848;
	private static final int BIG_LIMIT_ATTACK_ANIM_ID = 5849;
	private static final int SMALL_LIMIT_STAND_ANIM_ID = 5852;
	private static final int SMALL_LIMIT_WALK_ANIM_ID = 5856;
	private static final int SMALL_LIMIT_ATTACK_ANIM_ID = 5849;
	@Getter private boolean ruleLimitModelActive;
	private Model bigLimitModel;
	private Model smallLimitModel;
	private Animation bigLimitStandAnim;
	private Animation bigLimitWalkAnim;
	private Animation bigLimitAttackAnim;
	private Animation smallLimitStandAnim;
	private Animation smallLimitWalkAnim;
	private Animation smallLimitAttackAnim;
	private final Map<Integer, LimitModelEntry> ruleLimitModelObjects = new HashMap<>();
	private final Hooks.RenderableDrawListener ruleLimitModelHider = (renderable, ui) ->
	{
		if (!ruleLimitModelActive)
		{
			return true;
		}
		if (renderable instanceof NPC)
		{
			return !nyloersIndexMap.containsKey(((NPC) renderable).getIndex());
		}
		return true;
	};

	private static final class LimitModelEntry
	{
		final RuneLiteObject obj;
		final boolean big;
		LocalPoint lastLoc;
		boolean walking;

		LimitModelEntry(RuneLiteObject obj, boolean big)
		{
			this.obj = obj;
			this.big = big;
		}
	}

	// ---- lifecycle

	@Override
	protected void startUp()
	{
		createSidePanel();
		keyManager.registerKeyListener(this);
		eventBus.register(roleSwapper);
		eventBus.register(statsHandler);
		roleSwapper.reloadSwaps();
		hooks.registerRenderableDrawListener(ruleLimitModelHider);
		start();
	}

	@Override
	protected void shutDown()
	{
		removeSidePanel();
		stop();
		keyManager.unregisterKeyListener(this);
		eventBus.unregister(roleSwapper);
		eventBus.unregister(statsHandler);
		hooks.unregisterRenderableDrawListener(ruleLimitModelHider);
	}

	private void start()
	{
		log.debug("Starting Nyloer.");
		customFontConfig.parse(config);
		overlayManager.add(nyloerOverlay);
		overlayManager.add(nyloerTileOverlay);
		clientThread.invoke(this::refreshRuleLimitModel);
	}

	private void stop()
	{
		log.debug("Stopping Nyloer.");
		customFontConfig.getColorSettings().clear();
		overlayManager.remove(nyloerOverlay);
		overlayManager.remove(nyloerTileOverlay);
		clientThread.invoke(this::disableRuleLimitModel);
		reset();
	}

	private void reset()
	{
		log.debug("Resetting Nyloer.");
		waveNumber = 0;
		nylocasAliveCount = 0;
		activeDimRules.clear();
		pendingDarkerEvents.clear();
		waveSpawnTicks.clear();
		pillarsSpawned = false;
		lastWaveTickSpawned = 0;
		nyloers.clear();
		nyloerOverlay.nyloers.clear();
		nyloIndexWaveMap.clear();
		nyloIndexSpawnMap.clear();
	}

	// ---- side panel

	private void createSidePanel()
	{
		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "/ico.png");
		sidePanel = new NyloerSidePanel(client, this, config);
		sidePanelButton = NavigationButton.builder().tooltip("Nyloer").icon(icon).priority(6).panel(sidePanel).build();
		clientToolbar.addNavigation(sidePanelButton);
		sidePanel.startPanel();
	}

	private void removeSidePanel()
	{
		clientToolbar.removeNavigation(sidePanelButton);
	}

	// ---- key listener

	@Override
	public void keyTyped(KeyEvent e)
	{
	}

	@Override
	public void keyPressed(KeyEvent e)
	{
		if (config.makeDarkerHotkey().matches(e))
		{
			int t = client.getTickCount() - 1;
			t -= (t - wave1Tick) % 4;
			activeDimRules.add(new int[]{t, 1, 1});
		}
	}

	@Override
	public void keyReleased(KeyEvent e)
	{
	}

	// ---- events

	@Provides
	NyloerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(NyloerConfig.class);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals(NyloerConfig.GROUP))
		{
			return;
		}
		reloadFontConfig();
		clientThread.invoke(this::refreshRuleLimitModel);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		GameState state = event.getGameState();
		if (state == GameState.LOGIN_SCREEN || state == GameState.HOPPING)
		{
			stop();
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (client.getTickCount() % 5 == 0)
		{
			updateRegionDetection();
		}
		handleRegionTransition();
		isNylocasRegionLast = isNylocasRegion;

		if (!isNylocasRegion)
		{
			return;
		}

		processDarkerEvents();
		tickNyloers();
	}

	@Subscribe
	public void onClientTick(ClientTick event)
	{
		updateLimitModelLocations();
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned event)
	{
		if (!isNylocasRegion)
		{
			return;
		}
		NPC npc = event.getNpc();
		if (npc.getName() == null)
		{
			return;
		}

		if (!pillarsSpawned && ArrayUtils.contains(NYLOCAS_PILLAR_IDS, npc.getId()))
		{
			handlePillarSpawn();
			return;
		}
		if (NyloType.fromId(npc.getId()) != null)
		{
			registerNyloer(npc);
			if (ruleLimitModelActive)
			{
				NyloerNpc nyloer = nyloersIndexMap.get(npc.getIndex());
				if (nyloer != null)
				{
					addLimitModel(nyloer);
				}
			}
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned event)
	{
		if (!isNylocasRegion)
		{
			return;
		}
		removeNyloer(event.getNpc());
		removeLimitModel(event.getNpc().getIndex());
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (!isNylocasRegion || event.getType() != ChatMessageType.GAMEMESSAGE)
		{
			return;
		}
		handleGameMessage(event.getMessage());
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		if (!ruleLimitModelActive || !isNylocasRegion)
		{
			return;
		}
		if (!"Walk here".equals(event.getOption()))
		{
			return;
		}
		WorldPoint clicked = WorldPoint.fromScene(client, event.getActionParam0(), event.getActionParam1(), client.getPlane());
		for (NyloerNpc nyloer : nyloers)
		{
			if (!nyloer.isAlive())
			{
				continue;
			}
			NPC npc = nyloer.getNpc();
			WorldArea area = npc.getWorldArea();
			if (area == null || !area.contains2D(clicked))
			{
				continue;
			}
			if (menuHasEntryForNpc(npc.getIndex()))
			{
				continue;
			}
			NPCComposition comp = npc.getTransformedComposition();
			if (comp == null)
			{
				continue;
			}
			String[] actions = comp.getActions();
			if (actions == null)
			{
				continue;
			}
			String target = "<col=ffff00>" + npc.getName() + "</col>";
			if (npc.getCombatLevel() > 0)
			{
				target += "  (level-" + npc.getCombatLevel() + ")";
			}
			for (int i = actions.length - 1; i >= 0; i--)
			{
				String action = actions[i];
				if (action == null || action.isEmpty())
				{
					continue;
				}
				MenuAction type;
				switch (i)
				{
					case 0: type = MenuAction.NPC_FIRST_OPTION; break;
					case 1: type = MenuAction.NPC_SECOND_OPTION; break;
					case 2: type = MenuAction.NPC_THIRD_OPTION; break;
					case 3: type = MenuAction.NPC_FOURTH_OPTION; break;
					case 4: type = MenuAction.NPC_FIFTH_OPTION; break;
					default: continue;
				}
				client.createMenuEntry(-1)
					.setOption(action)
					.setTarget(target)
					.setIdentifier(npc.getIndex())
					.setType(type);
			}
		}
	}

	private boolean menuHasEntryForNpc(int npcIndex)
	{
		for (MenuEntry entry : client.getMenuEntries())
		{
			if (entry.getIdentifier() != npcIndex)
			{
				continue;
			}
			MenuAction type = entry.getType();
			if (type == MenuAction.NPC_FIRST_OPTION
				|| type == MenuAction.NPC_SECOND_OPTION
				|| type == MenuAction.NPC_THIRD_OPTION
				|| type == MenuAction.NPC_FOURTH_OPTION
				|| type == MenuAction.NPC_FIFTH_OPTION
				|| type == MenuAction.EXAMINE_NPC)
			{
				return true;
			}
		}
		return false;
	}

	// ---- event handlers

	private void reloadFontConfig()
	{
		customFontConfig.getColorSettings().clear();
		customFontConfig.parse(config);
	}

	private void updateRegionDetection()
	{
		isNylocasRegion = ArrayUtils.contains(client.getMapRegions(), NYLOCAS_REGION_ID);
	}

	private void handleRegionTransition()
	{
		if (isNylocasRegion && !isNylocasRegionLast)
		{
			start();
		}
		else if (!isNylocasRegion && isNylocasRegionLast)
		{
			stop();
		}
	}

	private void scheduleDarkerEvents()
	{
		int spawnTick = client.getTickCount();
		for (DarkerEntry entry : DarkerEntry.parseAll(config.darkerEntries()))
		{
			// triggerWave/triggerOffset decide when to execute; wave/offset select which nylos are dimmed.
			if (entry.triggerWave == waveNumber)
			{
				pendingDarkerEvents.add(new int[]{
					spawnTick + entry.triggerOffset,
					entry.wave,
					entry.offset,
					entry.dimSmalls ? 1 : 0,
					entry.dimBigs ? 1 : 0
				});
			}
		}
	}

	private void processDarkerEvents()
	{
		pendingDarkerEvents.removeIf(event ->
		{
			if (client.getTickCount() < event[0])
			{
				return false;
			}
			Integer dimSpawnTick = waveSpawnTicks.get(event[1]);
			if (dimSpawnTick == null)
			{
				return false; // dim wave hasn't spawned yet — keep waiting
			}
			activeDimRules.add(new int[]{dimSpawnTick + event[2], event[3], event[4]});
			return true;
		});
	}

	private void tickNyloers()
	{
		for (NyloerNpc nyloer : nyloers)
		{
			if (nyloer.getNpc().isDead())
			{
				nyloer.setAlive(false);
			}
			else if (nyloer.isAlive())
			{
				nyloer.incrementTicks();
			}
		}
	}

	private void handlePillarSpawn()
	{
		reset();
		sidePanel.resetStallsTable();
		pillarsSpawned = true;
	}

	private void registerNyloer(NPC npc)
	{
		Integer knownWave = nyloIndexWaveMap.get(npc.getIndex());
		if (knownWave != null)
		{
			// Flicker: NPC despawned and respawned with the same index. Restore the original
			// wave assignment without disturbing the global wave counter.
			NyloerNpc nyloer = new NyloerNpc(npc, client, config, customFontConfig, knownWave, lastWaveTickSpawned);
			nyloers.add(nyloer);
			nyloersIndexMap.put(nyloer.getIndex(), nyloer);
			nylocasAliveCount = nyloersIndexMap.size();
			return;
		}
		NyloerNpc nyloer = new NyloerNpc(npc, client, config, customFontConfig, waveNumber, lastWaveTickSpawned);
		if (!nyloer.getSpawn().equals("SPLIT") && client.getTickCount() - lastWaveTickSpawned >= 4)
		{
			handleNewWave(nyloer);
		}
		nyloers.add(nyloer);
		nyloersIndexMap.put(nyloer.getIndex(), nyloer);
		nylocasAliveCount = nyloersIndexMap.size();
		nyloIndexWaveMap.put(nyloer.getIndex(), nyloer.getWaveSpawned());
		nyloIndexSpawnMap.put(nyloer.getIndex(), nyloer.getSpawn());
	}

	private void handleNewWave(NyloerNpc nyloer)
	{
		lastWaveTickSpawned = client.getTickCount();
		++waveNumber;
		nyloer.bumpWave();
		waveSpawnTicks.put(waveNumber, client.getTickCount());
		if (waveNumber == 1)
		{
			wave1Tick = client.getTickCount();
		}
		scheduleDarkerEvents();
	}

	private void removeNyloer(NPC npc)
	{
		NyloerNpc nyloer = nyloersIndexMap.remove(npc.getIndex());
		if (nyloer != null)
		{
			nylocasAliveCount = nyloersIndexMap.size();
		}
	}

	private void handleGameMessage(String message)
	{
		if (message.equals("You have failed. The vampyres take pity on you and allow you to try again..."))
		{
			reset();
		}
	}

	private int countActiveDimRules()
	{
		String entries = config.darkerEntries();
		if (entries == null || entries.isEmpty())
		{
			return 0;
		}
		int count = 0;
		for (String token : entries.split(","))
		{
			if (!token.trim().isEmpty())
			{
				count++;
			}
		}
		return count;
	}

	private void refreshRuleLimitModel()
	{
		boolean shouldBeActive = countActiveDimRules() > RULE_LIMIT_MODEL_THRESHOLD;
		if (shouldBeActive == ruleLimitModelActive)
		{
			return;
		}
		if (shouldBeActive)
		{
			enableRuleLimitModel();
		}
		else
		{
			disableRuleLimitModel();
		}
	}

	private void enableRuleLimitModel()
	{
		ensureRuleLimitModelsLoaded();
		ruleLimitModelActive = true;
		for (NyloerNpc nyloer : nyloers)
		{
			if (nyloer.isAlive())
			{
				addLimitModel(nyloer);
			}
		}
	}

	private void disableRuleLimitModel()
	{
		ruleLimitModelActive = false;
		clearLimitModels();
	}

	private void ensureRuleLimitModelsLoaded()
	{
		if (bigLimitModel == null)
		{
			bigLimitModel = loadNpcModel(BIG_LIMIT_NPC_ID);
		}
		if (smallLimitModel == null)
		{
			smallLimitModel = loadNpcModel(SMALL_LIMIT_NPC_ID);
		}
		if (bigLimitStandAnim == null)
		{
			bigLimitStandAnim = client.loadAnimation(BIG_LIMIT_STAND_ANIM_ID);
		}
		if (bigLimitWalkAnim == null)
		{
			bigLimitWalkAnim = client.loadAnimation(BIG_LIMIT_WALK_ANIM_ID);
		}
		if (smallLimitStandAnim == null)
		{
			smallLimitStandAnim = client.loadAnimation(SMALL_LIMIT_STAND_ANIM_ID);
		}
		if (smallLimitWalkAnim == null)
		{
			smallLimitWalkAnim = client.loadAnimation(SMALL_LIMIT_WALK_ANIM_ID);
		}
		if (bigLimitAttackAnim == null)
		{
			bigLimitAttackAnim = client.loadAnimation(BIG_LIMIT_ATTACK_ANIM_ID);
		}
		if (smallLimitAttackAnim == null)
		{
			smallLimitAttackAnim = client.loadAnimation(SMALL_LIMIT_ATTACK_ANIM_ID);
		}
	}

	private Model loadNpcModel(int npcId)
	{
		NPCComposition comp = client.getNpcDefinition(npcId);
		if (comp == null)
		{
			return null;
		}
		int[] modelIds = comp.getModels();
		if (modelIds == null || modelIds.length == 0)
		{
			return null;
		}
		ModelData[] datas = new ModelData[modelIds.length];
		for (int i = 0; i < modelIds.length; i++)
		{
			datas[i] = client.loadModelData(modelIds[i]);
			if (datas[i] == null)
			{
				return null;
			}
		}
		short[] recolorFrom = comp.getColorToReplace();
		short[] recolorTo = comp.getColorToReplaceWith();
		if (recolorFrom != null && recolorTo != null)
		{
			int n = Math.min(recolorFrom.length, recolorTo.length);
			for (int i = 0; i < n; i++)
			{
				for (ModelData md : datas)
				{
					md.recolor(recolorFrom[i], recolorTo[i]);
				}
			}
		}
		ModelData merged = client.mergeModels(datas);
		int wScale = comp.getWidthScale();
		int hScale = comp.getHeightScale();
		if (wScale > 0 && hScale > 0 && (wScale != 128 || hScale != 128))
		{
			merged = merged.scale(wScale, hScale, wScale);
		}
		return merged.light();
	}

	private void addLimitModel(NyloerNpc nyloer)
	{
		if (nyloer == null)
		{
			return;
		}
		NPC npc = nyloer.getNpc();
		if (npc == null)
		{
			return;
		}
		ensureRuleLimitModelsLoaded();
		boolean big = nyloer.getSize() == NyloSize.BIG;
		Model model = big ? bigLimitModel : smallLimitModel;
		if (model == null)
		{
			return;
		}
		if (ruleLimitModelObjects.containsKey(npc.getIndex()))
		{
			return;
		}
		LocalPoint loc = npc.getLocalLocation();
		if (loc == null)
		{
			return;
		}
		RuneLiteObject obj = client.createRuneLiteObject();
		obj.setModel(model);
		Animation standAnim = big ? bigLimitStandAnim : smallLimitStandAnim;
		if (standAnim != null)
		{
			obj.setAnimation(standAnim);
			obj.setShouldLoop(true);
		}
		obj.setLocation(loc, client.getPlane());
		obj.setOrientation(npc.getCurrentOrientation());
		obj.setActive(true);
		LimitModelEntry entry = new LimitModelEntry(obj, big);
		entry.lastLoc = loc;
		ruleLimitModelObjects.put(npc.getIndex(), entry);
	}

	private void removeLimitModel(int npcIndex)
	{
		LimitModelEntry entry = ruleLimitModelObjects.remove(npcIndex);
		if (entry != null)
		{
			entry.obj.setActive(false);
		}
	}

	private void clearLimitModels()
	{
		for (LimitModelEntry entry : ruleLimitModelObjects.values())
		{
			entry.obj.setActive(false);
		}
		ruleLimitModelObjects.clear();
	}

	private void updateLimitModelLocations()
	{
		if (!ruleLimitModelActive || ruleLimitModelObjects.isEmpty())
		{
			return;
		}
		Iterator<Map.Entry<Integer, LimitModelEntry>> it = ruleLimitModelObjects.entrySet().iterator();
		while (it.hasNext())
		{
			Map.Entry<Integer, LimitModelEntry> e = it.next();
			LimitModelEntry entry = e.getValue();
			NyloerNpc nyloer = nyloersIndexMap.get(e.getKey());
			if (nyloer == null || !nyloer.isAlive())
			{
				entry.obj.setActive(false);
				it.remove();
				continue;
			}
			NPC npc = nyloer.getNpc();
			LocalPoint loc = npc.getLocalLocation();
			boolean moving = false;
			if (loc != null)
			{
				moving = entry.lastLoc != null
					&& (entry.lastLoc.getX() != loc.getX() || entry.lastLoc.getY() != loc.getY());
				entry.walking = moving;
				entry.lastLoc = loc;
				entry.obj.setLocation(loc, client.getPlane());
			}
			entry.obj.setOrientation(npc.getCurrentOrientation());

			boolean attacking = npc.getAnimation() != -1;
			Animation desired;
			if (attacking)
			{
				desired = entry.big ? bigLimitAttackAnim : smallLimitAttackAnim;
			}
			else if (moving)
			{
				desired = entry.big ? bigLimitWalkAnim : smallLimitWalkAnim;
			}
			else
			{
				desired = entry.big ? bigLimitStandAnim : smallLimitStandAnim;
			}
			if (desired != null && entry.obj.getAnimation() != desired)
			{
				entry.obj.setAnimation(desired);
				entry.obj.setShouldLoop(true);
				entry.obj.setFinished(false);
			}
		}
	}

}
