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
	public final boolean dimSmalls;
	public final boolean dimBigs;

	public DarkerEntry(int wave, int offset, int triggerWave, int triggerOffset)
	{
		this(wave, offset, triggerWave, triggerOffset, true, true, true);
	}

	public DarkerEntry(int wave, int offset, int triggerWave, int triggerOffset, boolean executionOverride)
	{
		this(wave, offset, triggerWave, triggerOffset, executionOverride, true, true);
	}

	public DarkerEntry(int wave, int offset, int triggerWave, int triggerOffset, boolean executionOverride, boolean dimSmalls, boolean dimBigs)
	{
		this.wave = wave;
		this.offset = offset;
		this.triggerWave = triggerWave;
		this.triggerOffset = triggerOffset;
		this.executionOverride = executionOverride;
		this.dimSmalls = dimSmalls;
		this.dimBigs = dimBigs;
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
				boolean dimSmalls = t.length < 5 || !t[4].trim().equals("0");
				boolean dimBigs   = t.length < 6 || !t[5].trim().equals("0");
				entries.add(new DarkerEntry(wave, offset, triggerWave, triggerOffset, hasExecutionOverride, dimSmalls, dimBigs));
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
		if (dimSmalls && dimBigs)
		{
			if (!executionOverride)
			{
				return wave + ":" + offset;
			}
			return wave + ":" + offset + ":" + triggerWave + ":" + triggerOffset;
		}
		// Include trigger fields (indices 2-3) to reach dim fields at indices 4-5
		int tw = executionOverride ? triggerWave : wave;
		int to = executionOverride ? triggerOffset : offset;
		return wave + ":" + offset + ":" + tw + ":" + to
			+ ":" + (dimSmalls ? "1" : "0")
			+ ":" + (dimBigs ? "1" : "0");
	}
}
