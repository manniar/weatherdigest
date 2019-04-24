package ari.weatherdigest

import groovy.transform.CompileStatic
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.function.Predicate

import ari.weatherdigest.WeatherService.WeatherDigest
import ari.weatherdigest.WeatherService.WeatherTick

/** DigestService is a facade over WeatherService, providing caching of @{link WeatherDigest}s,
 * asynchronous multithreaded cache refresh and user defined filtering.
 */
@CompileStatic
class DigestService {
	static final Logger log = LoggerFactory.getLogger(DigestService.class)

	// Only set per constructor
	final int refreshThreadCount
	final long refreshRetryIntervalSec

	// Available to be set externally
	WeatherService weatherService
	long refreshIntervalSec = 300
	Predicate<WeatherTick> filter = { true } as Predicate<WeatherTick>

	// Internal fields
	protected Map<String, WeatherDigest> digestMap = new Hashtable<String, WeatherService.WeatherDigest>()
	protected Set<String> locations = new ConcurrentHashMap<String,?>().newKeySet() // Concurrent set
	protected LinkedBlockingQueue<String> refreshQueue = new LinkedBlockingQueue<String>()
	protected ScheduledExecutorService scheduledExecutor
	protected long lastRefilledMs = 0

	Runnable refreshTask = {
		log.debug "RefreshTask started"
		refillRefreshQueue()
		String location = refreshQueue.poll()
		while (location != null) {
			long currentMs = System.currentTimeMillis()
			long refreshThresholdMs = currentMs - (refreshIntervalSec * 990) // 990 to allow 1% advance
			WeatherDigest digest = digestMap.get(location)
			if ( digest == null || digest.updatedTs < refreshThresholdMs ) {
				try {
					log.trace "Load location '$location' start"
					digest = weatherService.getDigest(location)
					digest.ticks.retainAll { WeatherTick tick -> filter.test(tick) }
					digestMap.put(location, digest)
					log.trace "Load location '$location' done"
				} catch(IOException e) {
					log.warn("Load location '$location' failed", e)
					digest = digest ?: new WeatherDigest(location: location)
					digest.error = e.toString()
					if (e instanceof FileNotFoundException) digest.updatedTs = currentMs // No point retrying if location is not found
					digestMap.put(location, digest)
				}
			}
			location = refreshQueue.poll()
		}
		log.debug "RefreshTask Done"
	}

	DigestService(WeatherService weatherService, int refreshThreadCount, long refreshRetryIntervalSec) {
		this.weatherService = weatherService
		this.refreshThreadCount = refreshThreadCount
		this.refreshRetryIntervalSec = refreshRetryIntervalSec
		log.info "DigestService start $refreshThreadCount threads with $refreshRetryIntervalSec s retry interval"
		scheduledExecutor = Executors.newScheduledThreadPool(refreshThreadCount)
		refreshThreadCount.times {
			scheduledExecutor.scheduleAtFixedRate(refreshTask, 1, refreshRetryIntervalSec, TimeUnit.SECONDS);
		}
	}

	private DigestService() {}

	void stop() {
		scheduledExecutor.shutdownNow()
	}

	void addLocation(String location) {
		locations << location.toLowerCase().trim()
	}

	String[] getLocations() {
		return locations as String[]
	}

	Collection<WeatherDigest> getDigests() {
		return digestMap.values()
	}

	WeatherDigest getDigest(String location) {
		return digestMap.get(location.toLowerCase().trim())
	}

	/** Copy all strings from locations to refreshQueue, ensuring no duplicates.
	 * This is thread safe and ensures it won't refill if already recently refilled. */
	synchronized void refillRefreshQueue() {
		// Avoid refilling if less than 90% of refreshRetryIntervalSec passed since last refresh
		long currentMs = System.currentTimeMillis()
		if ( currentMs < lastRefilledMs + (refreshRetryIntervalSec * 900) ) return
		lastRefilledMs = currentMs

		locations.each { location ->
			location = location.toLowerCase().trim()
			if (!refreshQueue.contains(location))
				refreshQueue.put(location)
		}
	}
}
