package ari.weatherdigest

import com.fasterxml.jackson.annotation.JsonInclude
import groovy.transform.Canonical

import java.time.ZonedDateTime

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL

/** Interface for getting weather digestMap. **/
interface WeatherService {

	@Canonical
	static class WeatherDigest {
		/** Location name as interpreted by WeatherService implementation **/
		String location
		@JsonInclude(NON_NULL)
		String error
		int population
		/** Time (in epoch millis) when this digest was last updated **/
		long updatedTs
		/** List of future ticks **/
		List<WeatherTick> ticks = []
	}

	@Canonical
	/** WeatherTick describes the weather in some specific instant of date time. **/
	static class WeatherTick {
		/** Date and time for which this tick applies **/
		ZonedDateTime instant
		/** Humidity as percentage **/
		int humidity
		/** Temperature as Kelvin **/
		int temp
	}

	/**
	 * Get weather digest of given location.
	 * @param location  Name of location, for example a city name.
	 * 		May be suffixed with comma and country code. Examples "Helsinki,FI" or "Oslo".
	 * 		Character case is insignificant.
	 * @return  The digest for given location
	 * @throws IOException  In case location is not recognized or in case of problem in implementing service.
	 */
	WeatherDigest getDigest( String location ) throws IOException ;

}
