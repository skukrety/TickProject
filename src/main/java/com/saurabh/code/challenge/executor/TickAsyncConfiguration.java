package com.saurabh.code.challenge.executor;

import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;

import com.saurabh.code.challenge.util.IUtility;

@Configuration
public class TickAsyncConfiguration {
	
	@Bean (name = "taskExecutor")
	public TaskExecutor taskExecutor() {
		return new ConcurrentTaskExecutor(Executors.newFixedThreadPool(IUtility.POOL_SIZE));
	}
}
