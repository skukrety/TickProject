package com.saurabh.code.challenge.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.saurabh.code.challenge.exception.BadTickParameterException;
import com.saurabh.code.challenge.model.Statistics;
import com.saurabh.code.challenge.model.Tick;
import com.saurabh.code.challenge.service.TicksStatsService;
import com.saurabh.code.challenge.util.IUtility;

@RestController
public class TicksStatsController {

	@Autowired
	private TicksStatsService ticksStatsService;

	/**
	 * This method adds a Tick for an instrument in-Memory.
	 * 
	 * @param tick Tick object to be added.
	 * @return This method returns empty body with Response as 201 in case tick is
	 *         added successfully (timestamp of tick within last 60sec) else 204.
	 * @throws BadTickParameterException Exception is thrown if Tick is NULL or
	 *                                   values are blank
	 */
	@PostMapping("/ticks")
	public ResponseEntity<String> collectTicks(@RequestBody Tick tick) throws BadTickParameterException {

		HttpStatus statusCode = HttpStatus.CREATED;

		if ((System.currentTimeMillis() - tick.getTimestamp()) > IUtility.TIME_SLICE_LIMIT) {
			statusCode = HttpStatus.NO_CONTENT;
		} else {
			ticksStatsService.addTick(tick);
		}

		return ResponseEntity.status(statusCode).build();
	}

	/**
	 * This method returns aggregated statistics of last 60 sec for all instruments.
	 * 
	 * @return Returns Statistics object.
	 */
	@GetMapping("/statistics")
	public Statistics getAllStatistics() {
		return ticksStatsService.computeStatisticsForAllInstruments();
	}

	/**
	 * This method returns aggregated statistics of last 60 sec for the specified
	 * instrument.
	 * 
	 * @param instrument Instrument identifier for which statistics is requested.
	 * @return Returns Statistics object.
	 * @throws BadTickParameterException Exception is thrown if instruments is
	 *                                   passed as NULL or blank
	 */
	@GetMapping("/statistics/{instrument_identifier}")
	public Statistics statsForInstrument(@PathVariable("instrument_identifier") String instrument)
			throws BadTickParameterException {

		return ticksStatsService.computeStatisticsForInstrument(instrument);
	}
}
