package com.saurabh.code.challenge.service;

import org.junit.Assert;
import org.junit.Test;

import com.saurabh.code.challenge.exception.BadTickParameterException;
import com.saurabh.code.challenge.model.Tick;

public class TicksStatsServiceImplTest {

	private TicksStatsService ticksStatsService = new TicksStatsServiceImpl();

	@Test
	public void tickAddition() throws InterruptedException, BadTickParameterException {
		ticksStatsService.addTick(new Tick("IBM", 100.0, System.currentTimeMillis()));
		Thread.sleep(5);
		ticksStatsService.addTick(new Tick("TCS", 150.0, System.currentTimeMillis()));
		Assert.assertEquals(2, ticksStatsService.tickPerTimestampSize());
	}

	@Test
	public void testInstrumentStatistics() throws BadTickParameterException {
		Assert.assertEquals(100.0, ticksStatsService.computeStatisticsForInstrument("IBM").getAvg(), 0.0);
	}

	@Test
	public void testAllStatistics() {
		Assert.assertEquals(125.0, ticksStatsService.computeStatisticsForAllInstruments().getAvg(), 0.0);
	}

	@Test
	public void testInstrumentIfNoStatisticsAvailable() throws BadTickParameterException {
		Assert.assertEquals(0.0, ticksStatsService.computeStatisticsForInstrument("INTEL").getAvg(), 0.0);
	}
	
	@Test (expected=BadTickParameterException.class)
	public void testNullInstrumentStatistics() throws BadTickParameterException {
		Assert.assertEquals(0.0, ticksStatsService.computeStatisticsForInstrument(null).getAvg(), 0.0);
	}
	
	@Test (expected=BadTickParameterException.class)
	public void addNullIdentifier() throws BadTickParameterException {
		ticksStatsService.addTick(new Tick(null, 100.0, System.currentTimeMillis()));
	}
	
	@Test (expected=BadTickParameterException.class)
	public void addBlankIdentifier() throws BadTickParameterException {
		ticksStatsService.addTick(new Tick("  ", 100.0, System.currentTimeMillis()));
	}
	
	@Test (expected=BadTickParameterException.class)
	public void addNegativePrice() throws BadTickParameterException {
		ticksStatsService.addTick(new Tick("APPLE", -1.0, System.currentTimeMillis()));
	}
	
	@Test (expected=BadTickParameterException.class)
	public void addZeroTimestamp() throws BadTickParameterException {
		ticksStatsService.addTick(new Tick("APPLE", 10.0, 0));
	}
}
