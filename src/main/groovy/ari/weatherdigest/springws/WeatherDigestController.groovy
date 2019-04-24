package ari.weatherdigest.springws

import ari.weatherdigest.DigestService
import ari.weatherdigest.OWMWeatherService
import ari.weatherdigest.WeatherService
import ari.weatherdigest.WeatherService.WeatherDigest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody

import java.util.function.Predicate

/** HTTP GET and JSON based web service interface towards DigestService. **/
@Controller
class WeatherDigestController {
	DigestService digestService

	@Autowired
	WeatherDigestController(WeatherDigestConfiguration config) {
		WeatherService weatherService = new OWMWeatherService(config.weatherServiceURL, config.appid)
		digestService = new DigestService(weatherService, config.refreshThreadCount, config.refreshRetryIntervalSec)
		digestService.setRefreshIntervalSec(config.refreshIntervalSec)
		config.getLocations().split(';').each { digestService.addLocation(it) }
		digestService.filter = new GroovyShell().evaluate(config.tickFilter) as Predicate<WeatherService.WeatherTick>
		// digestService.filter = { WeatherService.WeatherTick tick -> Eval.x(tick, config.tickFilter) } as Predicate<WeatherService.WeatherTick>
	}

	@GetMapping("/locations") @ResponseBody
	String[] getLocations() {
		return digestService.getLocations()
	}

	@GetMapping("/add-location") @ResponseBody
	String getLocations(@RequestParam(name="location", required=true) String location) {
		digestService.addLocation(location)
		return '{status:"OK"}'
	}

	@GetMapping("/digest") @ResponseBody
	WeatherDigest[] getDigests() {
		return digestService.getDigests()
	}

	@GetMapping(value="/digest", params="location") @ResponseBody
	WeatherDigest getDigest(@RequestParam(name="location", required=true) String location) {
		return digestService.getDigest(location)
	}

}
