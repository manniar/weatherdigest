package ari.weatherdigest.springws

import groovy.transform.Canonical
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "weatherdigest")
@Canonical
class WeatherDigestConfiguration {
	String weatherServiceURL, appid, locations, tickFilter
	int refreshThreadCount = 4
	long refreshRetryIntervalSec = 15, refreshIntervalSec = 900
}
