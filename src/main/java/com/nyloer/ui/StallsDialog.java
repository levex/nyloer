package com.nyloer.ui;

import com.nyloer.fonts.NyloerFonts;
import com.nyloer.stats.Stall;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import net.runelite.client.ui.ColorScheme;

public class StallsDialog extends JDialog
{
	public StallsDialog(Window owner, String roomLabel, List<Stall> stalls)
	{
		super(owner, "Stalls" + (roomLabel != null && !roomLabel.isEmpty() ? " - " + roomLabel : ""), ModalityType.MODELESS);

		Font tableFont = new Font(NyloerFonts.DIALOG.toString(), Font.PLAIN, 12);

		setLayout(new BorderLayout());
		getContentPane().setBackground(ColorScheme.DARK_GRAY_COLOR);

		if (stalls == null || stalls.isEmpty())
		{
			JLabel empty = new JLabel("No stalls recorded for this room.", SwingConstants.CENTER);
			empty.setFont(tableFont);
			empty.setForeground(Color.LIGHT_GRAY);
			empty.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
			add(empty, BorderLayout.CENTER);
		}
		else
		{
			String[] columnNames = {"Wave", "Alive", "Total"};
			DefaultTableModel model = new DefaultTableModel(columnNames, 0);
			for (Stall stall : stalls)
			{
				String aliveDisplay = stall.getAliveCount() + "/" + stall.getCapSize();
				model.addRow(new Object[]{stall.getWave(), aliveDisplay, stall.getTotalStalls()});
			}

			JTable table = new JTable(model);
			table.getTableHeader().setReorderingAllowed(false);
			table.setDefaultEditor(Object.class, null);
			table.setRowHeight(25);
			table.setBackground(ColorScheme.DARKER_GRAY_COLOR);
			table.setFont(tableFont);
			table.getTableHeader().setFont(tableFont);
			table.setRowSelectionAllowed(false);
			table.setCellSelectionEnabled(false);
			table.setShowGrid(false);
			table.setFillsViewportHeight(true);

			DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer();
			cellRenderer.setVerticalAlignment(JLabel.CENTER);
			cellRenderer.setHorizontalAlignment(JLabel.CENTER);
			for (int columnIndex = 0; columnIndex < model.getColumnCount(); columnIndex++)
			{
				table.getColumnModel().getColumn(columnIndex).setCellRenderer(cellRenderer);
			}

			JScrollPane scrollPane = new JScrollPane(table);
			scrollPane.setPreferredSize(new Dimension(240, 325));
			scrollPane.setBackground(ColorScheme.DARK_GRAY_COLOR);
			add(scrollPane, BorderLayout.CENTER);
		}

		pack();
		setLocationRelativeTo(owner);
	}
}
