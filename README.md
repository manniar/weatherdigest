# WeatherDigest
Simple Spring boot based web service, that digests weather information from 3rd party service

## Building the project
To build the project
* Clone the repository on your local computer, e.g. `git clone https://github.com/manniar/weatherdigest.git`
* Make sure JAVA_HOME is properly defined (preferable to point JDK 8 instance)
* On project root, run `gradlew build`

To open the projet in IntelliJ IDEA, choose **File - Open** and open the file `./build.gradle`.

## Running and using
To launch the application, execute the `build\libs\weather-groovy-n.n.n.jar`. For example (windows) `"%JAVA_HOME%\bin\java" -jar build\libs\weather-groovy-0.9.0.jar`.

To use the application, you can try following URL's on your browser:
* `http://localhost:7000/locations` \
This lists current locations (locations currently being monitored from WeatherService)
* `http://localhost:7000/digest` \
This lists weather digests for all current locations
* `http://localhost:7000/digest?location=paris` \
This returns weather digest for given location
* `http://localhost:7000/add-location?location=dallas` \
This adds a new location to be monitored
* `http://localhost:7000/digest?location=dallas` \
You can verify that the location was actually added
* `http://localhost:7001/actuator/health` \
There is also [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready.html) interface available.

## Configuring
Application can be configured using `src/main/resources/application.properties` file.
By default, the build bundles this file within the `weather-groovy-nnn.jar` file.
However, you can override the settings in several ways, for example by placing another `application.properties` file in runtime classpath.
See [Spring Boot Externalized Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html) for more information.

### Notable sections in properties
Configuring HTTP ports. Application and Actuator can have different port.

    server.port: 7000
    management.server.port: 7001

Configuring the initial list of locations to be monitored.

    weatherdigest.locations = Helsinki;Kerava;Oslo;Vienna;Paris;Cairo

Configuring the number of threads used for async refresh of digests from WeatherService.

    weatherdigest.refreshThreadCount = 4

Configuring the interval (in seconds) of refreshing digests or retrying refresh in case of failure.

    weatherdigest.refreshIntervalSec = 60
    weatherdigest.refreshRetryIntervalSec = 10

Configuring the URL and appid to connect `openweathermap.org` API.

    weatherdigest.weatherServiceURL = http://api.openweathermap.org/data/2.5/forecast
    weatherdigest.appid = c9b981740ba065151625da7b60097710

Configuring the filter to define which weather ticks should be included in digests. 
The syntax is of groovy closure.
Example filter below reports cold (under 6 deg Celcius), hot (over 26 deg Celcius) and sweaty periods (humidity > 50%).

    weatherdigest.tickFilter = { tick -> tick.temp < 280 || tick.temp > 300 || tick.humidity > 50 }

