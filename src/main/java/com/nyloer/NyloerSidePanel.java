package com.nyloer;


import com.nyloer.stats.Stall;
import com.nyloer.stats.Stats;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionListener;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

import javax.inject.Inject;
import javax.swing.*;
import javax.swing.border.LineBorder;


public class NyloerSidePanel extends PluginPanel
{
	@Inject
	private Client client;
	@Inject
	private NyloerPlugin plugin;
	@Inject
	private NyloerConfig config;
	@Inject
	private ConfigManager configManager;
	private final int MAX_SCALE = 5;

	static final Font tableTitleFont;
	static final Font buttonFont;
	static final Font tableFont;
	static final Font tableHeaderFont;

	JButton buttonMageSwaps;
	JButton buttonRangeSwaps;
	JButton buttonMeleeSwaps;
	JButton buttonCustomSwaps;

	JTable stallsTable;
	DefaultTableModel stallsTableModel;
	JScrollBar stallsTableScrollBar;

	JTable statsTable;
	DefaultTableModel statsTableModel;
	JScrollBar statsTableScrollBar;

	static {
		tableTitleFont = new Font(NyloerFonts.RUNESCAPE.toString(), Font.PLAIN, 16);
		buttonFont = new Font(NyloerFonts.DIALOG.toString(), Font.PLAIN, 12);
		tableFont = new Font(NyloerFonts.DIALOG.toString(), Font.PLAIN, 12);
		tableHeaderFont = new Font(NyloerFonts.DIALOG.toString(), Font.PLAIN, 12);
	}

	public void startPanel()
	{
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		final JPanel layout = new JPanel();
		BoxLayout boxLayout = new BoxLayout(layout, BoxLayout.Y_AXIS);
		layout.setLayout(boxLayout);
		add(layout, BorderLayout.NORTH);

		layout.add(Box.createVerticalGlue());
		JPanel swapsFrame = createRoleSwapsFrame();
		layout.add(swapsFrame);

		layout.add(Box.createVerticalGlue());
		JPanel scalePane = createScalePane();
		layout.add(scalePane);

		layout.add(Box.createVerticalGlue());
		JScrollPane stallsPane = createStallsPane();
		layout.add(stallsPane);

		layout.add(Box.createVerticalGlue());
		JPanel statsPane = createRecentStatsFrame();
		layout.add(statsPane);
	}

	public void addStall(Stall stall)
	{
		String aliveDisplay = stall.getAliveCount() + "/" + stall.getCapSize();
		stallsTableModel.addRow(new Object[]{stall.getWave(), aliveDisplay, stall.getTotalStalls()});
		stallsTableScrollBar.setValue(stallsTableScrollBar.getMaximum() + 100);
	}

	public void addStats(Stats stats)
	{
		statsTableModel.insertRow(
			0,
			new Object[]{
				stats.totalTime,
				stats.bossTime,
				stats.wavesTime,
				stats.stallCountPre != 0 ? stats.stallCountPre : "",
				stats.stallCount1to12 != 0 ? stats.stallCount1to12 : "",
				stats.stallCount13to19 != 0 ? stats.stallCount13to19 : "",
				stats.stallCount21 != 0 ? stats.stallCount21 : "",
				stats.stallCount22to27 != 0 ? stats.stallCount22to27 : "",
				stats.stallCount28 != 0 ? stats.stallCount28 : "",
				stats.stallCount29 != 0 ? stats.stallCount29 : "",
				stats.stallCount30 != 0 ? stats.stallCount30 : "",
				stats.bigsAlive22 != -1 ? stats.bigsAlive22 : "",
				stats.bigsAlive29 != -1 ? stats.bigsAlive29 : "",
				stats.bigsAlive30 != -1 ? stats.bigsAlive30 : "",
				stats.bigsAlive31 != -1 ? stats.bigsAlive31 : ""
			}
		);
	}

	public void resetStallsTable()
	{
		if (stallsTableModel.getRowCount() > 0)
		{
			for (int i = stallsTableModel.getRowCount() - 1; i > -1; i--)
			{
				stallsTableModel.removeRow(i);
			}
		}
	}

	private JPanel createScalePane() {
		JPanel swapsPanel = new JPanel(new GridLayout(0, 1, 0, 5));
		TitledBorder border = BorderFactory.createTitledBorder(new LineBorder(Color.BLACK), "Dim Settings");
		border.setTitleFont(tableTitleFont);
		swapsPanel.setBorder(border);

		for (int i = 1; i <= MAX_SCALE; i++) {
			final int scale = i;
			JPanel scalePanel = new JPanel(new GridBagLayout());

			TitledBorder scaleBorder = BorderFactory.createTitledBorder(new LineBorder(Color.GRAY), "Scale: " + scale);
			scaleBorder.setTitleFont(tableTitleFont.deriveFont(12f));
			scalePanel.setBorder(scaleBorder);

			addConfigRow(scalePanel, 0, "Wave:", 0, 31,
					val -> plugin.updateDarkerWave(scale, val),
					"darkerWave" + scale,
					() -> plugin.fetchDarkerWave(scale)
			);

			addConfigRow(scalePanel, 1, "Offset:", 0, 31,
					val -> plugin.updateDarkerWaveOffset(scale, val),
					"darkerWaveOffset" + scale,
					() -> plugin.fetchDarkerWaveOffset(scale)
			);

			swapsPanel.add(scalePanel);
		}
		return swapsPanel;
	}
	private void addConfigRow(JPanel parent, int gridY, String labelText, int min, int max,
							  IntConsumer onUpdate, String configKey, IntSupplier defaultFetcher) {

		// 1. Setup Constraints
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(2, 5, 2, 5);
		gbc.gridy = gridY;

		// 2. Add Label
		gbc.gridx = 0;
		gbc.weightx = 0.3;
		parent.add(new JLabel(labelText), gbc);

		// 3. Create Spinner
		JSpinner spinner = new JSpinner(new SpinnerNumberModel(defaultFetcher.getAsInt(), min, max, 1));
		spinner.addChangeListener(e -> onUpdate.accept((int) spinner.getValue()));

		// 4. Add Reset Logic
		// We reuse your existing addReset helper, passing the specific logic for this row
		addReset(spinner, e -> {
			configManager.unsetConfiguration(NyloerConfig.GROUP, configKey);
			// After unsetting, fetching again returns the default value from Config interface
			spinner.setValue(defaultFetcher.getAsInt());
		});

		// 5. Add Spinner
		gbc.gridx = 1;
		gbc.weightx = 0.7;
		parent.add(spinner, gbc);
	}
	private void addReset(JSpinner spinner, ActionListener listener)
	{
		JMenuItem resetItem = new JMenuItem("Reset to Default");
		resetItem.addActionListener(listener);
		JPopupMenu popup = new JPopupMenu();
		popup.add(resetItem);
		spinner.setComponentPopupMenu(popup);
		((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setComponentPopupMenu(popup);
	}

	private JPanel createRoleSwapsFrame()
	{
		JPanel swapsFrame = new JPanel();
		swapsFrame.setLayout(new GridLayout(2, 2));
		TitledBorder border = BorderFactory.createTitledBorder(new LineBorder(Color.BLACK), "Role Swaps");
		border.setTitleFont(tableTitleFont);
		swapsFrame.setBorder(border);

		buttonMageSwaps = new JButton("Mage");
		buttonRangeSwaps = new JButton("Range");
		buttonMeleeSwaps = new JButton("Melee");
		buttonCustomSwaps = new JButton("Custom");
		buttonMageSwaps.setPreferredSize(new Dimension(40, 40));
		buttonRangeSwaps.setPreferredSize(new Dimension(40, 40));
		buttonMeleeSwaps.setPreferredSize(new Dimension(40, 40));
		buttonCustomSwaps.setPreferredSize(new Dimension(40, 40));
		buttonMageSwaps.setFocusable(false);
		buttonRangeSwaps.setFocusable(false);
		buttonMeleeSwaps.setFocusable(false);
		buttonCustomSwaps.setFocusable(false);
		buttonMageSwaps.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		buttonRangeSwaps.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		buttonMeleeSwaps.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		buttonCustomSwaps.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		buttonMageSwaps.addActionListener(e -> _configureMageSwaps());
		buttonRangeSwaps.addActionListener(e -> _configureRangeSwaps());
		buttonMeleeSwaps.addActionListener(e -> _configureMeleeSwaps());
		buttonCustomSwaps.addActionListener(e -> _configureCustomSwaps());
		swapsFrame.add(buttonMageSwaps);
		swapsFrame.add(buttonMeleeSwaps);
		swapsFrame.add(buttonRangeSwaps);
		swapsFrame.add(buttonCustomSwaps);
		switch (config.previousRole())
		{
			case "mage":
				_configureMageSwaps();
				break;
			case "range":
				_configureRangeSwaps();
				break;
			case "melee":
				_configureMeleeSwaps();
				break;
			case "custom":
				_configureCustomSwaps();
				break;
			default:
				_resetRolesSelection();
		}

		return swapsFrame;
	}

	private JScrollPane createStallsPane()
	{
		stallsTable = new JTable();

		String[] columnNames = {"Wave", "Alive", "Total"};
		stallsTableModel = new DefaultTableModel(columnNames, 0);
		stallsTable.setModel(stallsTableModel);
		stallsTable.getTableHeader().setReorderingAllowed(false);
		stallsTable.setDefaultEditor(Object.class, null);
		stallsTable.setPreferredScrollableViewportSize(new Dimension(0, 325));
		stallsTable.setRowHeight(25);
		stallsTable.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		stallsTable.setFont(tableFont);
		stallsTable.getTableHeader().setFont(tableHeaderFont);
		stallsTable.setRowSelectionAllowed(false);
		stallsTable.setCellSelectionEnabled(false);
		stallsTable.setShowGrid(false);
		stallsTable.setFillsViewportHeight(true);

		DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer();
		cellRenderer.setVerticalAlignment(JLabel.CENTER);
		cellRenderer.setHorizontalAlignment(JLabel.CENTER);
		for (int columnIndex = 0; columnIndex < stallsTableModel.getColumnCount(); columnIndex++)
		{
			stallsTable.getColumnModel().getColumn(columnIndex).setCellRenderer(cellRenderer);
		}

		JScrollPane scrollPane = new JScrollPane(
			stallsTable,
			JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
			JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
		);
		TitledBorder border = BorderFactory.createTitledBorder(new LineBorder(Color.BLACK), "Stalls");
		border.setTitleFont(tableTitleFont);
		scrollPane.setBorder(border);
		scrollPane.setBackground(ColorScheme.DARK_GRAY_COLOR);
		stallsTableScrollBar = scrollPane.getVerticalScrollBar();

		return scrollPane;
	}

	private JPanel createRecentStatsFrame()
	{
		JPanel statsFrame = new JPanel();
		BoxLayout boxLayout = new BoxLayout(statsFrame, BoxLayout.Y_AXIS);
		statsFrame.setLayout(boxLayout);
		TitledBorder border = BorderFactory.createTitledBorder(new LineBorder(Color.BLACK), "Recent Times");
		border.setTitleFont(tableTitleFont);
		statsFrame.setBorder(border);

		statsTable = new JTable();
		String[] columnNames = {"Room", "Boss", "Waves", "Pre", "1-12", "13-19", "21", "22-27", "28", "29", "30", "bigs22", "bigs29", "bigs30", "bigs31"};
		statsTableModel = new DefaultTableModel(columnNames, 0);
		statsTable.setModel(statsTableModel);
		statsTable.getColumnModel().getColumn(0).setPreferredWidth(60);
		statsTable.getColumnModel().getColumn(1).setPreferredWidth(60);
		statsTable.getColumnModel().getColumn(2).setPreferredWidth(60);
		statsTable.getColumnModel().getColumn(3).setPreferredWidth(30);
		statsTable.getColumnModel().getColumn(4).setPreferredWidth(40);
		statsTable.getColumnModel().getColumn(5).setPreferredWidth(40);
		statsTable.getColumnModel().getColumn(6).setPreferredWidth(30);
		statsTable.getColumnModel().getColumn(7).setPreferredWidth(40);
		statsTable.getColumnModel().getColumn(8).setPreferredWidth(30);
		statsTable.getColumnModel().getColumn(9).setPreferredWidth(30);
		statsTable.getColumnModel().getColumn(10).setPreferredWidth(30);
		statsTable.getColumnModel().getColumn(11).setPreferredWidth(40);
		statsTable.getColumnModel().getColumn(12).setPreferredWidth(40);
		statsTable.getColumnModel().getColumn(13).setPreferredWidth(40);
		statsTable.getColumnModel().getColumn(14).setPreferredWidth(40);

		statsTable.getTableHeader().setReorderingAllowed(false);
		statsTable.setDefaultEditor(Object.class, null);
		statsTable.setPreferredScrollableViewportSize(new Dimension(0, 325));
		statsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		statsTable.setRowHeight(25);
		statsTable.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		statsTable.setFont(tableFont);
		statsTable.getTableHeader().setFont(tableHeaderFont);
		statsTable.setRowSelectionAllowed(false);
		statsTable.setCellSelectionEnabled(false);
		statsTable.setShowGrid(false);
		statsTable.setFillsViewportHeight(true);

		DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer();
		cellRenderer.setVerticalAlignment(JLabel.CENTER);
		cellRenderer.setHorizontalAlignment(JLabel.CENTER);
		for (int columnIndex = 0; columnIndex < statsTableModel.getColumnCount(); columnIndex++)
		{
			statsTable.getColumnModel().getColumn(columnIndex).setCellRenderer(cellRenderer);
		}

		JScrollPane scrollPane = new JScrollPane(
			statsTable,
			JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
			JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS
		);
		scrollPane.setBackground(ColorScheme.DARK_GRAY_COLOR);
		statsTableScrollBar = scrollPane.getVerticalScrollBar();
		statsFrame.add(scrollPane);

		JPanel statsButtonFrame = new JPanel();
		statsButtonFrame.setLayout(new GridLayout(1, 2));
		JButton buttonCopyTable = new JButton("Copy");
		JButton buttonClearTable = new JButton("Clear");
		buttonCopyTable.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		buttonClearTable.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		buttonCopyTable.setFocusable(false);
		buttonClearTable.setFocusable(false);
		buttonCopyTable.setPreferredSize(new Dimension(40, 25));
		buttonClearTable.setPreferredSize(new Dimension(40, 25));
		buttonCopyTable.setFont(buttonFont);
		buttonClearTable.setFont(buttonFont);
		buttonCopyTable.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		buttonClearTable.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		buttonCopyTable.addActionListener(e -> _copyToClipboard(statsTable));
		buttonClearTable.addActionListener(e -> _resetStatsTable());
		statsButtonFrame.add(buttonCopyTable);
		statsButtonFrame.add(buttonClearTable);
		statsFrame.add(Box.createRigidArea(new Dimension(0, 5)));
		statsFrame.add(statsButtonFrame);

		return statsFrame;
	}

	private void _copyToClipboard(JTable table)
	{
		StringBuffer sbf = new StringBuffer();
		table.selectAll();
		int numCols = table.getSelectedColumnCount();
		int numRows = table.getSelectedRowCount();
		int[] selectedRows = table.getSelectedRows();
		int[] selectsColumns = table.getSelectedColumns();
		table.clearSelection();
		for (int i = 0; i < numCols; i++)
		{
			sbf.append(statsTable.getModel().getColumnName(i));
			sbf.append("\t");
		}
		sbf.append("\n");
		for (int i = 0; i < numRows; i++)
		{
			for (int j = 0; j < numCols; j++)
			{
				sbf.append(table.getValueAt(selectedRows[i], selectsColumns[j]));
				if (j < numCols - 1)
				{
					sbf.append("\t");
				}
			}
			sbf.append("\n");
		}
		StringSelection data = new StringSelection(sbf.toString());
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(data, data);
	}

	private void _resetStatsTable()
	{
		if (statsTableModel.getRowCount() > 0)
		{
			for (int i = statsTableModel.getRowCount() - 1; i > -1; i--)
			{
				statsTableModel.removeRow(i);
			}
		}
	}

	private void _configureMageSwaps()
	{
		NyloerPlugin.log.debug("Configuring mage swaps.");
		String currentRole = plugin.roleSwapper.getCurrentRole();
		_resetRolesSelection();
		if ((currentRole == null) || (!currentRole.equals("mage")))
		{
			config.setPreviousRole("mage");
			plugin.roleSwapper.setCurrentRole("mage");
			plugin.nyloerTileOverlay.setRenderMage(config.mageHighlightMageTiles());
			plugin.nyloerTileOverlay.setRenderRange(config.mageHighlightRangeTiles());
			plugin.nyloerTileOverlay.setRenderMelee(config.mageHighlightMeleeTiles());

			buttonMageSwaps.setForeground(Color.CYAN);
			buttonMageSwaps.setFont(buttonFont);
		}
	}

	private void _configureRangeSwaps()
	{
		NyloerPlugin.log.debug("Configuring range swaps.");
		String currentRole = plugin.roleSwapper.getCurrentRole();
		_resetRolesSelection();
		if ((currentRole == null) || (!currentRole.equals("range")))
		{
			config.setPreviousRole("range");
			plugin.roleSwapper.setCurrentRole("range");
			plugin.nyloerTileOverlay.setRenderMage(config.rangeHighlightMageTiles());
			plugin.nyloerTileOverlay.setRenderRange(config.rangeHighlightRangeTiles());
			plugin.nyloerTileOverlay.setRenderMelee(config.rangeHighlightMeleeTiles());

			buttonRangeSwaps.setForeground(Color.GREEN);
			buttonRangeSwaps.setFont(buttonFont);
		}
	}

	private void _configureMeleeSwaps()
	{
		NyloerPlugin.log.debug("Configuring melee swaps.");
		String currentRole = plugin.roleSwapper.getCurrentRole();
		_resetRolesSelection();
		if ((currentRole == null) || (!currentRole.equals("melee")))
		{
			config.setPreviousRole("melee");
			plugin.roleSwapper.setCurrentRole("melee");
			plugin.nyloerTileOverlay.setRenderMage(config.meleeHighlightMageTiles());
			plugin.nyloerTileOverlay.setRenderRange(config.meleeHighlightRangeTiles());
			plugin.nyloerTileOverlay.setRenderMelee(config.meleeHighlightMeleeTiles());

			buttonMeleeSwaps.setForeground(Color.WHITE);
			buttonMeleeSwaps.setFont(buttonFont);
		}
	}

	private void _configureCustomSwaps()
	{
		NyloerPlugin.log.debug("Configured custom role swaps.");
		String currentRole = plugin.roleSwapper.getCurrentRole();
		_resetRolesSelection();
		if ((currentRole == null) || (!currentRole.equals("custom")))
		{
			config.setPreviousRole("custom");
			plugin.roleSwapper.setCurrentRole("custom");
			plugin.nyloerTileOverlay.setRenderMage(config.customHighlightMageTiles());
			plugin.nyloerTileOverlay.setRenderRange(config.customHighlightRangeTiles());
			plugin.nyloerTileOverlay.setRenderMelee(config.customHighlightMeleeTiles());

			buttonCustomSwaps.setForeground(Color.MAGENTA);
			buttonCustomSwaps.setFont(buttonFont);
		}
	}

	private void _resetRolesSelection()
	{
		config.setPreviousRole("");
		plugin.roleSwapper.setCurrentRole(null);

		plugin.nyloerTileOverlay.setRenderMage(false);
		plugin.nyloerTileOverlay.setRenderRange(false);
		plugin.nyloerTileOverlay.setRenderMelee(false);

		buttonMageSwaps.setForeground(Color.GRAY);
		buttonRangeSwaps.setForeground(Color.GRAY);
		buttonMeleeSwaps.setForeground(Color.GRAY);
		buttonCustomSwaps.setForeground(Color.GRAY);
		buttonMageSwaps.setFont(buttonFont);
		buttonRangeSwaps.setFont(buttonFont);
		buttonMeleeSwaps.setFont(buttonFont);
		buttonCustomSwaps.setFont(buttonFont);
	}
}