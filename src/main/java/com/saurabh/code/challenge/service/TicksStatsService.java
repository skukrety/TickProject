package com.saurabh.code.challenge.service;

import org.springframework.stereotype.Service;

import com.saurabh.code.challenge.exception.BadTickParameterException;
import com.saurabh.code.challenge.model.Statistics;
import com.saurabh.code.challenge.model.Tick;

@Service
public interface TicksStatsService {

	void addTick(Tick tick) throws BadTickParameterException;
	Statistics computeStatisticsForAllInstruments();
	Statistics computeStatisticsForInstrument(String instrument) throws BadTickParameterException;
	int tickPerTimestampSize();
}

