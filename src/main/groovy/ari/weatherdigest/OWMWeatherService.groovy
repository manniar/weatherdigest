package ari.weatherdigest

import groovy.json.JsonParserType
import groovy.json.JsonSlurper
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor

import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime

@CompileStatic
@TupleConstructor
class OWMWeatherService implements WeatherService {
	String openWeatherURL = 'http://api.openweathermap.org/data/2.5/forecast'
	String appid = 'c9b981740ba065151625da7b60097710'

	OWMWeatherService(String openWeatherURL, String appid) {
		this.openWeatherURL = openWeatherURL
		this.appid = appid
	}

	WeatherDigest getDigest(String location ) throws IOException {
		String json = "${openWeatherURL}?q=${location}&appid=${appid}".toURL().getText( readTimeout: 20000 )
		def resp = new JsonSlurper(type: JsonParserType.INDEX_OVERLAY).parseText(json)
		return jsonObj2Digest(resp)
	}

	@CompileDynamic
	WeatherDigest jsonObj2Digest(def obj) {
		WeatherDigest digest = new WeatherDigest(
			location: obj.city.name,
			population: obj.city.population,
			updatedTs: System.currentTimeMillis()
		)
		obj.list.forEach() {
			digest.ticks << new WeatherTick(
				instant: ZonedDateTime.ofInstant(Instant.ofEpochSecond(it.dt), ZoneOffset.UTC),
				humidity: it.main.humidity,
				temp: it.main.temp
			)
		}
		return digest
	}

}
