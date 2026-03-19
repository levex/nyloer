package com.nyloer.ui;

import java.util.ArrayList;
import java.util.List;

/**
 * A named collection of {@link DarkerEntry} rules that can be saved and loaded as a preset.
 *
 * <p>Config format: {@code name|w:o:tw:to,w:o:tw:to} per preset, semicolon-separated between presets.
 */
public class DarkerPreset
{
	public final String name;
	public final List<DarkerEntry> entries;

	public DarkerPreset(String name, List<DarkerEntry> entries)
	{
		this.name = name;
		this.entries = entries;
	}

	/** Parses a single {@code "name|entries"} token. Returns null if malformed. */
	public static DarkerPreset parse(String s)
	{
		int pipe = s.indexOf('|');
		if (pipe < 0)
		{
			return null;
		}
		String name = s.substring(0, pipe).trim();
		String entriesStr = s.substring(pipe + 1);
		return new DarkerPreset(name, DarkerEntry.parseAll(entriesStr));
	}

	/** Parses a semicolon-separated list of presets. */
	public static List<DarkerPreset> parseAll(String s)
	{
		List<DarkerPreset> result = new ArrayList<>();
		if (s == null || s.isBlank())
		{
			return result;
		}
		for (String part : s.split(";"))
		{
			part = part.trim();
			if (!part.isEmpty())
			{
				DarkerPreset p = parse(part);
				if (p != null)
				{
					result.add(p);
				}
			}
		}
		return result;
	}

	public String entriesString()
	{
		StringBuilder sb = new StringBuilder();
		for (DarkerEntry e : entries)
		{
			if (sb.length() > 0)
			{
				sb.append(",");
			}
			sb.append(e.toString());
		}
		return sb.toString();
	}

	@Override
	public String toString()
	{
		return name + "|" + entriesString();
	}
}
