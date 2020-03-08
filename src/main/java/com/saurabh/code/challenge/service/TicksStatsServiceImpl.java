package com.saurabh.code.challenge.service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.saurabh.code.challenge.exception.BadTickParameterException;
import com.saurabh.code.challenge.model.Statistics;
import com.saurabh.code.challenge.model.Tick;
import com.saurabh.code.challenge.util.IUtility;

@Service
public class TicksStatsServiceImpl implements TicksStatsService {

	private static final Logger LOG = LoggerFactory.getLogger(TicksStatsServiceImpl.class);

	private static final ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();

	// This map stores all ticks for available timestamps as key,
	// as value it stores map with key as instrument and value as tick.
	private static ConcurrentHashMap<Long, ConcurrentHashMap<String, Tick>> ticksPerTimestamp = new ConcurrentHashMap<>();

	// This map has instruments as key and their aggregated statistics as value.
	private static ConcurrentHashMap<String, Statistics> statsPerInstrument = new ConcurrentHashMap<>();

	// This stores aggregated statistics for all instruments.
	private static Statistics timeSliceStatisticsForAllInstruments;

	/**
	 * This method adds a Tick for an instrument in-Memory. Post addition it updates
	 * statistics for specified instrument and aggregated statistics for all
	 * instruments.
	 * 
	 * @param tick Tick object to be added.
	 */
	@Async(value = "taskExecutor")
	@Override
	public void addTick(Tick tick) throws BadTickParameterException {
		if (tick == null || !validateTick(tick)) {
			throw new BadTickParameterException("Tick is NULL or values are blank");
		}

		this.tickUpdate(tick);
		TicksStatsServiceImpl.statsPerInstrument.putIfAbsent(tick.getInstrument(), new Statistics());
		this.tickStatsUpdateScheduler();
	}

	/**
	 * This method returns aggregated statistics of last 60 sec for all instruments.
	 * 
	 * @return Returns Statistics object, it returns empty (avg, max, min & count as 0)
	 *         statistics object in case no statistics available.
	 */
	@Override
	public Statistics computeStatisticsForAllInstruments() {
		reentrantReadWriteLock.readLock().lock();
		try {
			return (timeSliceStatisticsForAllInstruments == null ? new Statistics()
					: timeSliceStatisticsForAllInstruments);
		} finally {
			reentrantReadWriteLock.readLock().unlock();
		}
	}

	/**
	 * This method returns aggregated statistics of last 60 sec for the specified
	 * instrument.
	 * 
	 * @param instrument Instrument identifier for which statistics is requested.
	 * @return Returns Statistics object, it returns empty (avg, max, min & count as 0)
	 *         statistics object in case no statistics available.
	 * @throws BadTickParameterException Exception is thrown if instruments is
	 *                                   passed as NULL or blank
	 */
	@Override
	public Statistics computeStatisticsForInstrument(String instrument) throws BadTickParameterException {
		LOG.info("Fetching statistics for instrument - " + instrument);
		if (instrument == null || instrument.trim().equals("")) {
			throw new BadTickParameterException("Instrument identifier is NULL or blank");
		}

		return (statsPerInstrument.get(instrument) == null) ? new Statistics() : statsPerInstrument.get(instrument);
	}

	/**
	 * This method has a scheduler to run every second and do below: 
	 *     1. Clean ticks with timestamp lower than last 60 sec. 
	 *     2. Update statistics for each instrument available in last 60 sec. 
	 *     3. Update aggregated statistics for all instruments available in last 60 sec.
	 */
	@Scheduled(fixedRate = 1000)
	public void tickStatsUpdateScheduler() {
		long currentTimestamp = System.currentTimeMillis();

		this.cleanTicksForTimeSlice(currentTimestamp);

		Set<String> instruments = statsPerInstrument.keySet();
		LOG.info("Updating statistics for #instruments - " + instruments.size());

		instruments.parallelStream().forEach(instrument -> statisticsCalculatorForInstrument(instrument));

		this.statisticsCalculatorForAll();
	}

	/**
	 * This method adds a Tick for an instrument in-Memory.
	 * 
	 * @param tick Tick object to be added.
	 */
	private void tickUpdate(Tick tick) {
		LOG.info("Updating tick - " + tick);
		ConcurrentHashMap<String, Tick> tickForInstrument = ticksPerTimestamp.get(tick.getTimestamp());

		if (tickForInstrument == null) {
			tickForInstrument = new ConcurrentHashMap<>();
			tickForInstrument.put(tick.getInstrument(), tick);
			ticksPerTimestamp.put(tick.getTimestamp(), tickForInstrument);
		} else {
			tickForInstrument.put(tick.getInstrument(), tick);
		}

		LOG.info("Total unique timestamps stored after tick addition - " + tickPerTimestampSize());
	}

	/**
	 * This method cleans ticks from in-memory with timestamp lower than last 60
	 * sec.
	 * 
	 * @param timestamp Timestamp in milliseconds for considering last 60 sec.
	 */
	private void cleanTicksForTimeSlice(long timestamp) {
		Set<Long> keys = ticksPerTimestamp.keySet();

		for (Long key : keys) {
			if (key < (timestamp - IUtility.TIME_SLICE_LIMIT)) {
				ticksPerTimestamp.remove(key);
			}
		}
		LOG.info("Total unique timestamps stored after cleanup - " + tickPerTimestampSize());
	}

	/**
	 * This method calculates statistics for specified instrument and updates
	 * in-memory.
	 * 
	 * @param instrument Instrument for which statistics needs to be calculated.
	 */
	private void statisticsCalculatorForInstrument(String instrument) {
		Set<Long> keys = ticksPerTimestamp.keySet();
		Statistics stats = new Statistics();

		LOG.info("Calulating statistics for instrument, unique timestamps - " + keys.size());

		for (Long key : keys) {
			ConcurrentHashMap<String, Tick> instrTicks = ticksPerTimestamp.get(key);
			Tick tick = instrTicks.get(instrument);
			if (tick != null) {
				if (stats.getCount() == 0) {
					stats.setAvg(tick.getPrice());
					stats.setMax(tick.getPrice());
					stats.setMin(tick.getPrice());
					stats.setCount(1);
				} else {
					stats.setAvg(((stats.getAvg() * stats.getCount()) + tick.getPrice()) / (stats.getCount() + 1));
					stats.setCount(stats.getCount() + 1);
					stats.setMax(Math.max(stats.getMax(), tick.getPrice()));
					stats.setMin(Math.min(stats.getMin(), tick.getPrice()));
				}
			}
		}
		statsPerInstrument.put(instrument, stats);
	}

	/**
	 * TThis method calculates aggregated statistics for all instruments and updates
	 * in-memory.
	 */
	private void statisticsCalculatorForAll() {
		Set<String> instruments = statsPerInstrument.keySet();
		LOG.info("Calulating statistics for all, got unique instruments - " + instruments.size());

		Statistics stats = new Statistics();

		if (instruments.size() > 0) {
			for (String instrument : instruments) {
				Statistics instruStats = statsPerInstrument.get(instrument);
				if (stats.getCount() == 0) {
					stats.setAvg(instruStats.getAvg());
					stats.setCount(instruStats.getCount());
					stats.setMax(instruStats.getMax());
					stats.setMin(instruStats.getMin());
				} else {
					stats.setMax(Math.max(stats.getMax(), instruStats.getMax()));
					stats.setMin(Math.min(stats.getMin(), instruStats.getMin()));
					stats.setAvg(((stats.getAvg() * stats.getCount()) + (instruStats.getAvg() * instruStats.getCount()))
							/ (stats.getCount() + instruStats.getCount()));
					stats.setCount(stats.getCount() + instruStats.getCount());
				}
			}
		}

		reentrantReadWriteLock.readLock().lock();
		try {
			timeSliceStatisticsForAllInstruments = stats;
		} finally {
			reentrantReadWriteLock.readLock().unlock();
		}
	}

	/**
	 * This method returns number of unique ticks available in-memory (required for
	 * test case)
	 */
	public int tickPerTimestampSize() {
		return ticksPerTimestamp.size();
	}

	/**
	 * This method returns true if the tick is valid else false.
	 * 
	 * @param tick Tick object to be validated.
	 * @return Returns true/false depending on condition.
	 */
	private boolean validateTick(Tick tick) {
		boolean tickIsGood = true;

		if (tick.getInstrument() == null || tick.getInstrument().trim().equals("")) {
			tickIsGood = false;
		} else if (tick.getPrice() < 0.0) {
			tickIsGood = false;
		} else if (tick.getTimestamp() < 1) {
			tickIsGood = false;
		}

		return tickIsGood;
	}
}
