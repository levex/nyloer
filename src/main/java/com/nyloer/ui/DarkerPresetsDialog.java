package com.nyloer.ui;

import com.nyloer.NyloerConfig;
import com.nyloer.fonts.NyloerFonts;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.MatteBorder;
import net.runelite.client.ui.ColorScheme;

public class DarkerPresetsDialog extends JDialog
{
	private final NyloerConfig config;
	private final Font buttonFont;
	private final Font tableFont;

	private final DefaultListModel<String> presetListModel = new DefaultListModel<>();
	private final JList<String> presetList = new JList<>(presetListModel);

	private final List<DarkerPreset> presets = new ArrayList<>();
	private final List<DarkerEntryRow> darkerRows = new ArrayList<>();

	private JPanel entriesPanel;
	private JButton addEntryBtn;
	private JPanel addEntryRowPanel;
	private int selectedIndex = -1;

	public DarkerPresetsDialog(Window owner, NyloerConfig config)
	{
		super(owner, "Dim Presets", ModalityType.MODELESS);
		this.config = config;
		this.buttonFont = new Font(NyloerFonts.DIALOG.toString(), Font.PLAIN, 12);
		this.tableFont = new Font(NyloerFonts.DIALOG.toString(), Font.PLAIN, 12);

		loadPresetsFromConfig();
		buildUi();

		setSize(800, 600);
		setLocationRelativeTo(owner);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	private void loadPresetsFromConfig()
	{
		presets.clear();
		presets.addAll(DarkerPreset.parseAll(config.darkerPresets()));
		refreshListModel();
	}

	private void refreshListModel()
	{
		presetListModel.clear();
		for (DarkerPreset p : presets)
		{
			presetListModel.addElement(p.name);
		}
	}

	private void buildUi()
	{
		JPanel content = new JPanel(new BorderLayout(8, 8));
		content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
		content.setBackground(ColorScheme.DARK_GRAY_COLOR);
		setContentPane(content);

		content.add(buildPresetListPanel(), BorderLayout.WEST);
		content.add(buildEntryEditorPanel(), BorderLayout.CENTER);

		// Bottom buttons
		JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
		bottomPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		JButton closeBtn = makeButton("Close");
		closeBtn.addActionListener(e -> dispose());
		bottomPanel.add(closeBtn);
		content.add(bottomPanel, BorderLayout.SOUTH);

		if (!presets.isEmpty())
		{
			presetList.setSelectedIndex(0);
			selectPreset(0);
		}
	}

	private JPanel buildPresetListPanel()
	{
		JPanel panel = new JPanel(new BorderLayout(4, 4));
		panel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		panel.setPreferredSize(new Dimension(140, 0));
		panel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(Color.BLACK),
			BorderFactory.createEmptyBorder(6, 6, 6, 6)
		));

		JLabel title = new JLabel("Presets");
		title.setFont(buttonFont);
		title.setForeground(Color.LIGHT_GRAY);
		panel.add(title, BorderLayout.NORTH);

		presetList.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		presetList.setForeground(Color.WHITE);
		presetList.setFont(tableFont);
		presetList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		presetList.addListSelectionListener(e ->
		{
			if (!e.getValueIsAdjusting())
			{
				int idx = presetList.getSelectedIndex();
				if (idx >= 0)
				{
					selectPreset(idx);
				}
			}
		});
		JScrollPane listScroll = new JScrollPane(presetList);
		styleScrollPane(listScroll, ColorScheme.DARKER_GRAY_COLOR);
		panel.add(listScroll, BorderLayout.CENTER);

		JPanel btnPanel = new JPanel(new GridLayout(2, 2, 2, 2));
		btnPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		JButton addBtn = makeButton("+ New");
		JButton dupBtn = makeButton("Duplicate");
		JButton renBtn = makeButton("Rename");
		JButton delBtn = makeButton("Delete");
		addBtn.addActionListener(e -> addNewPreset());
		dupBtn.addActionListener(e -> duplicateSelectedPreset());
		renBtn.addActionListener(e -> renameSelectedPreset());
		delBtn.addActionListener(e -> deleteSelectedPreset());
		btnPanel.add(addBtn);
		btnPanel.add(dupBtn);
		btnPanel.add(renBtn);
		btnPanel.add(delBtn);
		panel.add(btnPanel, BorderLayout.SOUTH);

		return panel;
	}

	private JPanel buildEntryEditorPanel()
	{
		JPanel panel = new JPanel(new BorderLayout(4, 4));
		panel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		panel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(Color.BLACK),
			BorderFactory.createEmptyBorder(6, 6, 6, 6)
		));

		JLabel helpLabel = new JLabel("<html>W = dim wave.<br>"
			+ "Off = dim wave offset.<br>"
			+ "Adjust = optional execution tick override.<br>"
			+ "In Adjust, first value is execution wave and second value is execution wave offset.<br>"
			+ "Smalls/Bigs = whether this dim applies to small/big nylos.</html>");
		helpLabel.setFont(buttonFont);
		helpLabel.setForeground(Color.LIGHT_GRAY);
		panel.add(helpLabel, BorderLayout.NORTH);

		JPanel header = new JPanel(new BorderLayout(2, 0));
		header.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel moveHeader = new JPanel(new BorderLayout());
		moveHeader.setBackground(ColorScheme.DARK_GRAY_COLOR);
		moveHeader.setPreferredSize(new Dimension(18, 1));
		moveHeader.setMinimumSize(new Dimension(18, 1));
		moveHeader.setMaximumSize(new Dimension(18, Integer.MAX_VALUE));
		header.add(moveHeader, BorderLayout.WEST);

		// Match row layout: WEST(move) + CENTER(3-column fields) + EAST(remove button column)
		JPanel centerHeader = new JPanel(new BorderLayout(2, 0));
		centerHeader.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel fieldsHeader = new JPanel(new GridLayout(1, 3, 2, 0));
		fieldsHeader.setBackground(ColorScheme.DARK_GRAY_COLOR);
		for (String label : new String[]{"W", "Off", "Execution Tick"})
		{
			JLabel l = new JLabel(label, JLabel.CENTER);
			l.setFont(tableFont);
			l.setForeground(Color.LIGHT_GRAY);
			fieldsHeader.add(l);
		}
		centerHeader.add(fieldsHeader, BorderLayout.CENTER);

		JPanel eastHeader = new JPanel();
		eastHeader.setLayout(new BoxLayout(eastHeader, BoxLayout.X_AXIS));
		eastHeader.setBackground(ColorScheme.DARK_GRAY_COLOR);
		for (String[] spec : new String[][]{{"Smalls", "42"}, {"Bigs", "30"}})
		{
			int w = Integer.parseInt(spec[1]);
			JLabel l = new JLabel(spec[0], JLabel.CENTER);
			l.setFont(tableFont);
			l.setForeground(Color.LIGHT_GRAY);
			l.setPreferredSize(new Dimension(w, 1));
			l.setMinimumSize(new Dimension(w, 1));
			l.setMaximumSize(new Dimension(w, Integer.MAX_VALUE));
			eastHeader.add(l);
		}
		JLabel removeHeader = new JLabel("", JLabel.CENTER);
		removeHeader.setFont(tableFont);
		removeHeader.setForeground(Color.LIGHT_GRAY);
		removeHeader.setPreferredSize(new Dimension(16, 1));
		removeHeader.setMinimumSize(new Dimension(16, 1));
		removeHeader.setMaximumSize(new Dimension(16, Integer.MAX_VALUE));
		eastHeader.add(removeHeader);
		centerHeader.add(eastHeader, BorderLayout.EAST);

		header.add(centerHeader, BorderLayout.CENTER);

		entriesPanel = new JPanel();
		entriesPanel.setLayout(new BoxLayout(entriesPanel, BoxLayout.Y_AXIS));
		entriesPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		JScrollPane scroll = new JScrollPane(
			entriesPanel,
			JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
			JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
		);
		styleScrollPane(scroll, ColorScheme.DARKER_GRAY_COLOR);
		scroll.setColumnHeaderView(header);
		panel.add(scroll, BorderLayout.CENTER);

		addEntryBtn = makeButton("+");
		addEntryBtn.addActionListener(e ->
		{
			addEntryRow(1, 0, 1, 0, false, true, true);
			addAddEntryControlToBottom();
			persistCurrentPreset();
			entriesPanel.revalidate();
			entriesPanel.repaint();
		});

		return panel;
	}

	private void selectPreset(int index)
	{
		if (selectedIndex >= 0 && selectedIndex < presets.size())
		{
			persistCurrentPreset();
		}
		selectedIndex = index;
		loadPresetEntries(presets.get(index));
	}

	private void persistCurrentPreset()
	{
		saveCurrentPresetEntries();
		persistPresets();
	}

	private void persistPresets()
	{
		StringBuilder sb = new StringBuilder();
		for (DarkerPreset p : presets)
		{
			if (sb.length() > 0)
			{
				sb.append(";");
			}
			sb.append(p.toString());
		}
		config.setDarkerPresets(sb.toString());
	}

	private void saveCurrentPresetEntries()
	{
		if (selectedIndex < 0 || selectedIndex >= presets.size())
		{
			return;
		}
		List<DarkerEntry> entries = new ArrayList<>();
		for (DarkerEntryRow row : darkerRows)
		{
			int wave = (int) row.waveSpinner.getValue();
			int offset = (int) row.offsetSpinner.getValue();
			boolean ds = row.dimSmallsCheck.isSelected();
			boolean db = row.dimBigsCheck.isSelected();
			if (row.executionOverride)
			{
				entries.add(new DarkerEntry(
					wave,
					offset,
					(int) row.triggerWaveSpinner.getValue(),
					(int) row.triggerOffsetSpinner.getValue(),
					true,
					ds,
					db
				));
			}
			else
			{
				entries.add(new DarkerEntry(wave, offset, wave, offset, false, ds, db));
			}
		}
		presets.set(selectedIndex, new DarkerPreset(presets.get(selectedIndex).name, entries));
	}

	private void loadPresetEntries(DarkerPreset preset)
	{
		entriesPanel.removeAll();
		darkerRows.clear();
		for (DarkerEntry entry : preset.entries)
		{
			addEntryRow(entry.wave, entry.offset, entry.triggerWave, entry.triggerOffset, entry.executionOverride, entry.dimSmalls, entry.dimBigs);
		}
		addAddEntryControlToBottom();
		entriesPanel.revalidate();
		entriesPanel.repaint();
	}

	private void addAddEntryControlToBottom()
	{
		if (addEntryRowPanel != null)
		{
			entriesPanel.remove(addEntryRowPanel);
		}
		addEntryRowPanel = new JPanel(new BorderLayout());
		addEntryRowPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		addEntryRowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
		addEntryRowPanel.add(addEntryBtn, BorderLayout.CENTER);
		entriesPanel.add(addEntryRowPanel);
	}

	private void addEntryRow(int wave, int offset, int triggerWave, int triggerOffset)
	{
		addEntryRow(wave, offset, triggerWave, triggerOffset, true, true, true);
	}

	private void addEntryRow(int wave, int offset, int triggerWave, int triggerOffset, boolean executionOverride)
	{
		addEntryRow(wave, offset, triggerWave, triggerOffset, executionOverride, true, true);
	}

	private void addEntryRow(int wave, int offset, int triggerWave, int triggerOffset, boolean executionOverride, boolean dimSmalls, boolean dimBigs)
	{
		JSpinner waveSpinner          = new JSpinner(new SpinnerNumberModel(wave,          1, 31,   1));
		JSpinner offsetSpinner        = new JSpinner(new SpinnerNumberModel(offset,      -100, 100,  1));
		JSpinner triggerWaveSpinner   = new JSpinner(new SpinnerNumberModel(triggerWave,   1, 31,   1));
		JSpinner triggerOffsetSpinner = new JSpinner(new SpinnerNumberModel(triggerOffset, -100, 100, 1));

		for (JSpinner s : new JSpinner[]{waveSpinner, offsetSpinner, triggerWaveSpinner, triggerOffsetSpinner})
		{
			styleSpinner(s);
			JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) s.getEditor();
			editor.getTextField().setColumns(2);
			editor.getTextField().setHorizontalAlignment(SwingConstants.CENTER);
		}
		JCheckBox dimSmallsCheck = new JCheckBox();
		dimSmallsCheck.setSelected(dimSmalls);
		dimSmallsCheck.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		dimSmallsCheck.setFocusable(false);
		dimSmallsCheck.setHorizontalAlignment(SwingConstants.CENTER);

		JCheckBox dimBigsCheck = new JCheckBox();
		dimBigsCheck.setSelected(dimBigs);
		dimBigsCheck.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		dimBigsCheck.setFocusable(false);
		dimBigsCheck.setHorizontalAlignment(SwingConstants.CENTER);

		DarkerEntryRow entryRow = new DarkerEntryRow(waveSpinner, offsetSpinner, triggerWaveSpinner, triggerOffsetSpinner, executionOverride, dimSmallsCheck, dimBigsCheck);
		darkerRows.add(entryRow);
		waveSpinner.addChangeListener(e -> persistCurrentPreset());
		offsetSpinner.addChangeListener(e -> persistCurrentPreset());
		triggerWaveSpinner.addChangeListener(e -> persistCurrentPreset());
		triggerOffsetSpinner.addChangeListener(e -> persistCurrentPreset());
		dimSmallsCheck.addActionListener(e -> persistCurrentPreset());
		dimBigsCheck.addActionListener(e -> persistCurrentPreset());

		JPanel row = new JPanel(new BorderLayout(2, 0));
		row.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
		row.setBorder(new MatteBorder(0, 0, 1, 0, ColorScheme.DARK_GRAY_COLOR));
		entryRow.rowPanel = row;
		JPanel movePanel = new JPanel(new GridLayout(2, 1, 0, 1));
		movePanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		JButton upBtn = makeButton("\u02c4");
		JButton downBtn = makeButton("\u02c5");
		Dimension moveBtnSize = new Dimension(16, 11);
		upBtn.setPreferredSize(moveBtnSize);
		upBtn.setMinimumSize(moveBtnSize);
		upBtn.setMaximumSize(moveBtnSize);
		downBtn.setPreferredSize(moveBtnSize);
		downBtn.setMinimumSize(moveBtnSize);
		downBtn.setMaximumSize(moveBtnSize);
		upBtn.addActionListener(e -> moveRowByDelta(entryRow, -1));
		downBtn.addActionListener(e -> moveRowByDelta(entryRow, 1));
		movePanel.add(upBtn);
		movePanel.add(downBtn);
		JPanel moveCell = new JPanel(new BorderLayout());
		moveCell.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		moveCell.setPreferredSize(new Dimension(18, 28));
		moveCell.setMinimumSize(new Dimension(18, 28));
		moveCell.setMaximumSize(new Dimension(18, 28));
		moveCell.add(movePanel, BorderLayout.CENTER);
		row.add(moveCell, BorderLayout.WEST);

		JPanel contentRow = new JPanel(new BorderLayout(2, 0));
		contentRow.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		JPanel fieldsPanel = new JPanel(new GridLayout(1, 3, 2, 2));
		fieldsPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		fieldsPanel.add(waveSpinner);
		fieldsPanel.add(offsetSpinner);
		fieldsPanel.add(buildExecutionCell(entryRow));
		entryRow.fieldsPanel = fieldsPanel;
		contentRow.add(fieldsPanel, BorderLayout.CENTER);

		JPanel eastContent = new JPanel();
		eastContent.setLayout(new BoxLayout(eastContent, BoxLayout.X_AXIS));
		eastContent.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		for (int[] spec : new int[][]{{42}, {30}})
		{
			JCheckBox cb = spec[0] == 42 ? dimSmallsCheck : dimBigsCheck;
			JPanel cell = new JPanel(new BorderLayout());
			cell.setBackground(ColorScheme.DARKER_GRAY_COLOR);
			cell.setPreferredSize(new Dimension(spec[0], 28));
			cell.setMinimumSize(new Dimension(spec[0], 28));
			cell.setMaximumSize(new Dimension(spec[0], 28));
			cell.add(cb, BorderLayout.CENTER);
			eastContent.add(cell);
		}
		JButton removeBtn = makeButton("X");
		Dimension removeSize = new Dimension(14, 20);
		removeBtn.setPreferredSize(removeSize);
		removeBtn.setMinimumSize(removeSize);
		removeBtn.setMaximumSize(removeSize);
		removeBtn.addActionListener(e ->
		{
			darkerRows.remove(entryRow);
			entriesPanel.remove(row);
			addAddEntryControlToBottom();
			persistCurrentPreset();
			entriesPanel.revalidate();
			entriesPanel.repaint();
		});
		JPanel removeCell = new JPanel(new BorderLayout());
		removeCell.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		removeCell.setPreferredSize(new Dimension(16, 28));
		removeCell.setMinimumSize(new Dimension(16, 28));
		removeCell.setMaximumSize(new Dimension(16, 28));
		removeCell.add(removeBtn, BorderLayout.CENTER);
		eastContent.add(removeCell);
		contentRow.add(eastContent, BorderLayout.EAST);
		row.add(contentRow, BorderLayout.CENTER);

		entriesPanel.add(row);
	}

	private void moveRowByDelta(DarkerEntryRow row, int delta)
	{
		int fromIndex = darkerRows.indexOf(row);
		if (fromIndex < 0)
		{
			return;
		}
		int toIndex = Math.max(0, Math.min(darkerRows.size() - 1, fromIndex + delta));
		if (toIndex == fromIndex)
		{
			return;
		}

		darkerRows.remove(fromIndex);
		darkerRows.add(toIndex, row);
		entriesPanel.remove(row.rowPanel);
		entriesPanel.add(row.rowPanel, toIndex);
		addAddEntryControlToBottom();
		persistCurrentPreset();
		entriesPanel.revalidate();
		entriesPanel.repaint();
	}

	private JPanel buildExecutionCell(DarkerEntryRow row)
	{
		JPanel cell = new JPanel(new BorderLayout(2, 0));
		cell.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		if (!row.executionOverride)
		{
			JButton setBtn = makeButton("Adjust");
			setBtn.addActionListener(e ->
			{
				row.executionOverride = true;
				row.triggerWaveSpinner.setValue(row.waveSpinner.getValue());
				row.triggerOffsetSpinner.setValue(row.offsetSpinner.getValue());
				replaceExecutionCell(row);
				persistCurrentPreset();
			});
			cell.add(setBtn, BorderLayout.CENTER);
			return cell;
		}

		JPanel tickPanel = new JPanel(new GridLayout(1, 2, 2, 0));
		tickPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		tickPanel.add(row.triggerWaveSpinner);
		tickPanel.add(row.triggerOffsetSpinner);
		cell.add(tickPanel, BorderLayout.CENTER);

		JButton clearBtn = makeButton("x");
		clearBtn.addActionListener(e ->
		{
			row.executionOverride = false;
			replaceExecutionCell(row);
			persistCurrentPreset();
		});
		cell.add(clearBtn, BorderLayout.EAST);
		return cell;
	}

	private void replaceExecutionCell(DarkerEntryRow row)
	{
		row.fieldsPanel.remove(2);
		row.fieldsPanel.add(buildExecutionCell(row), 2);
		row.fieldsPanel.revalidate();
		row.fieldsPanel.repaint();
	}

	private void addNewPreset()
	{
		String name = JOptionPane.showInputDialog(this, "Preset name:", "New Preset", JOptionPane.PLAIN_MESSAGE);
		if (name == null || name.isBlank())
		{
			return;
		}
		if (selectedIndex >= 0 && selectedIndex < presets.size())
		{
			persistCurrentPreset();
		}
		presets.add(new DarkerPreset(name.trim(), new ArrayList<>()));
		refreshListModel();
		persistPresets();
		int newIdx = presets.size() - 1;
		presetList.setSelectedIndex(newIdx);
		selectPreset(newIdx);
	}

	private void deleteSelectedPreset()
	{
		int idx = presetList.getSelectedIndex();
		if (idx < 0)
		{
			return;
		}
		presets.remove(idx);
		darkerRows.clear();
		entriesPanel.removeAll();
		entriesPanel.revalidate();
		entriesPanel.repaint();
		selectedIndex = -1;
		refreshListModel();
		persistPresets();
		if (!presets.isEmpty())
		{
			int newIdx = Math.min(idx, presets.size() - 1);
			presetList.setSelectedIndex(newIdx);
			selectPreset(newIdx);
		}
	}

	private void renameSelectedPreset()
	{
		int idx = presetList.getSelectedIndex();
		if (idx < 0 || idx >= presets.size())
		{
			return;
		}
		String currentName = presets.get(idx).name;
		String name = JOptionPane.showInputDialog(this, "Preset name:", currentName);
		if (name == null)
		{
			return;
		}
		name = name.trim();
		if (name.isBlank())
		{
			return;
		}
		DarkerPreset current = presets.get(idx);
		presets.set(idx, new DarkerPreset(name, new ArrayList<>(current.entries)));
		refreshListModel();
		persistPresets();
		presetList.setSelectedIndex(idx);
	}

	private void duplicateSelectedPreset()
	{
		int idx = presetList.getSelectedIndex();
		if (idx < 0 || idx >= presets.size())
		{
			return;
		}
		DarkerPreset source = presets.get(idx);
		String defaultName = source.name + " Copy";
		String name = JOptionPane.showInputDialog(this, "Duplicate preset name:", defaultName);
		if (name == null)
		{
			return;
		}
		name = name.trim();
		if (name.isBlank())
		{
			return;
		}
		List<DarkerEntry> copiedEntries = new ArrayList<>();
		for (DarkerEntry e : source.entries)
		{
			copiedEntries.add(new DarkerEntry(e.wave, e.offset, e.triggerWave, e.triggerOffset, e.executionOverride, e.dimSmalls, e.dimBigs));
		}
		presets.add(new DarkerPreset(name, copiedEntries));
		refreshListModel();
		persistPresets();
		int newIdx = presets.size() - 1;
		presetList.setSelectedIndex(newIdx);
		selectPreset(newIdx);
	}

	private JButton makeButton(String text)
	{
		JButton btn = new JButton(text);
		btn.setFont(buttonFont);
		btn.setFocusable(false);
		btn.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		btn.setForeground(Color.WHITE);
		btn.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		return btn;
	}

	private void styleSpinner(JSpinner spinner)
	{
		spinner.setFont(tableFont);
		spinner.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		spinner.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spinner.getEditor();
		editor.getTextField().setBackground(ColorScheme.DARKER_GRAY_COLOR);
		editor.getTextField().setForeground(Color.WHITE);
		editor.getTextField().setCaretColor(Color.WHITE);
		editor.getTextField().setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
	}

	private void styleScrollPane(JScrollPane pane, Color viewportColor)
	{
		pane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		pane.getViewport().setBackground(viewportColor);
		pane.getVerticalScrollBar().setUnitIncrement(14);
	}

	private static class DarkerEntryRow
	{
		private final JSpinner waveSpinner;
		private final JSpinner offsetSpinner;
		private final JSpinner triggerWaveSpinner;
		private final JSpinner triggerOffsetSpinner;
		private boolean executionOverride;
		private final JCheckBox dimSmallsCheck;
		private final JCheckBox dimBigsCheck;
		private JPanel rowPanel;
		private JPanel fieldsPanel;

		private DarkerEntryRow(JSpinner waveSpinner, JSpinner offsetSpinner, JSpinner triggerWaveSpinner, JSpinner triggerOffsetSpinner, boolean executionOverride, JCheckBox dimSmallsCheck, JCheckBox dimBigsCheck)
		{
			this.waveSpinner = waveSpinner;
			this.offsetSpinner = offsetSpinner;
			this.triggerWaveSpinner = triggerWaveSpinner;
			this.triggerOffsetSpinner = triggerOffsetSpinner;
			this.executionOverride = executionOverride;
			this.dimSmallsCheck = dimSmallsCheck;
			this.dimBigsCheck = dimBigsCheck;
		}
	}
}
