package com.nyloer;

import com.google.inject.Provides;
import com.nyloer.ui.DarkerEntry;
import com.nyloer.fonts.CustomFontConfig;
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
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import lombok.Getter;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.callback.ClientThread;
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

	// ---- lifecycle

	@Override
	protected void startUp()
	{
		createSidePanel();
		keyManager.registerKeyListener(this);
		eventBus.register(roleSwapper);
		eventBus.register(statsHandler);
		roleSwapper.reloadSwaps();
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
	}

	private void start()
	{
		log.debug("Starting Nyloer.");
		customFontConfig.parse(config);
		overlayManager.add(nyloerOverlay);
		overlayManager.add(nyloerTileOverlay);
	}

	private void stop()
	{
		log.debug("Stopping Nyloer.");
		customFontConfig.getColorSettings().clear();
		overlayManager.remove(nyloerOverlay);
		overlayManager.remove(nyloerTileOverlay);
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

}
