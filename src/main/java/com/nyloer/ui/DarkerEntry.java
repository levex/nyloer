package com.nyloer.ui;

import java.util.ArrayList;
import java.util.List;

/**
 * A single "make darker" rule.
 *
 * <p>First two values ({@code wave}, {@code offset}) define which nylos are dimmed by
 * setting {@code makeDarkerT = waveSpawnTick(wave) + offset}.
 *
 * <p>Third and fourth values ({@code triggerWave}, {@code triggerOffset}) define when
 * the dim action executes: {@code fireAtTick = waveSpawnTick(triggerWave) + triggerOffset}.
 *
 * <p>Config format: {@code wave:offset} or {@code wave:offset:triggerWave:triggerOffset}
 */
public class DarkerEntry
{
	public final int wave;
	public final int offset;
	public final int triggerWave;
	public final int triggerOffset;
	public final boolean executionOverride;

	public DarkerEntry(int wave, int offset, int triggerWave, int triggerOffset)
	{
		this(wave, offset, triggerWave, triggerOffset, true);
	}

	public DarkerEntry(int wave, int offset, int triggerWave, int triggerOffset, boolean executionOverride)
	{
		this.wave = wave;
		this.offset = offset;
		this.triggerWave = triggerWave;
		this.triggerOffset = triggerOffset;
		this.executionOverride = executionOverride;
	}

	/** Parses a comma-separated list of {@code wave:offset} or {@code wave:offset:triggerWave:triggerOffset} tokens. */
	public static List<DarkerEntry> parseAll(String config)
	{
		List<DarkerEntry> entries = new ArrayList<>();
		if (config == null || config.isBlank())
		{
			return entries;
		}
		for (String part : config.split(","))
		{
			part = part.trim();
			if (part.isEmpty())
			{
				continue;
			}
			try
			{
				String[] t = part.split(":");
				int wave   = Integer.parseInt(t[0].trim());
				int offset = Integer.parseInt(t[1].trim());
				boolean hasExecutionOverride = t.length >= 4;
				int triggerWave   = hasExecutionOverride ? Integer.parseInt(t[2].trim()) : wave;
				int triggerOffset = hasExecutionOverride ? Integer.parseInt(t[3].trim()) : offset;
				entries.add(new DarkerEntry(wave, offset, triggerWave, triggerOffset, hasExecutionOverride));
			}
			catch (Exception e)
			{
				// skip malformed entries
			}
		}
		return entries;
	}

	@Override
	public String toString()
	{
		if (!executionOverride)
		{
			return wave + ":" + offset;
		}
		return wave + ":" + offset + ":" + triggerWave + ":" + triggerOffset;
	}
}
