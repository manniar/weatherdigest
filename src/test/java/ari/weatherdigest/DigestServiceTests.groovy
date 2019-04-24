package ari.weatherdigest

import org.junit.Assert
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll

class DigestServiceTests {
	static DigestService digestService

	static final String weatherServiceURL = 'http://api.openweathermap.org/data/2.5/forecast'
	static final String appid = 'c9b981740ba065151625da7b60097710'


	@BeforeAll
	static void setup() {
		WeatherService weatherService = new OWMWeatherService(weatherServiceURL, appid)
		digestService = new DigestService(weatherService, 1, 3)
		digestService.setRefreshIntervalSec(100)
		digestService.addLocation('Helsinki')
	}

	@Test
	void end2end() {
		WeatherService.WeatherDigest digest = digestService.getDigest('Helsinki')
		int times = 10
		10.times {
			while (digest == null) {
				Thread.sleep(500)
				digest = digestService.getDigest('Helsinki')
			}
		}
		Assert.assertNotNull(digest)
		Assert.assertNull(digest.error)
		Assert.assertTrue(digest.ticks.size() > 5)
		Assert.assertEquals('Helsinki', digest.location)
	}

	@AfterAll
	static void end() {
		digestService.stop()
	}

}
