package weather_monitor;


public class RunWeatherThreads {
	

	//public static Runnable loadPropsThread = () -> {WeatherMonitorMain.loadProps(WeatherMonitorMain.myProps);};
	public static Runnable loadKrakow = () -> {
		WeatherMonitorMain.krakowArray = WeatherMonitorMain.parseAirQualityDataFromDoc(WeatherMonitorMain.krakowURL);
		WeatherMonitorMain.krakowTemp = WeatherMonitorMain.parseWeatherData(WeatherMonitorMain.krakowWeatherURL);
		WeatherMonitorMain.krakowReady = true;
		Frame.updateMeasurements("Krakow", WeatherMonitorMain.getCurrentAirQualityParameters("Krakow", WeatherMonitorMain.krakowArray));
	};
	
	public static Runnable loadWarsaw = () -> {
		WeatherMonitorMain.warsawArray = WeatherMonitorMain.parseAirQualityDataFromDoc(WeatherMonitorMain.warsawURL);
		WeatherMonitorMain.warsawTemp = WeatherMonitorMain.parseWeatherData(WeatherMonitorMain.warsawWeatherURL);
		WeatherMonitorMain.warsawReady = true;
		Frame.updateMeasurements("Warsaw", WeatherMonitorMain.getCurrentAirQualityParameters("Warsaw", WeatherMonitorMain.warsawArray));
	};
	public static Runnable loadWroclaw = () -> {
		WeatherMonitorMain.wroclawArray = WeatherMonitorMain.parseAirQualityDataFromDoc(WeatherMonitorMain.wroclawURL);
		WeatherMonitorMain.wroclawTemp = WeatherMonitorMain.parseWeatherData(WeatherMonitorMain.wroclawWeatherURL);
		WeatherMonitorMain.wroclawReady = true;
		Frame.updateMeasurements("Wroclaw", WeatherMonitorMain.getCurrentAirQualityParameters("Wroclaw", WeatherMonitorMain.wroclawArray));
		
	};
	public static Runnable loadPoznan = () -> {
		WeatherMonitorMain.poznanArray = WeatherMonitorMain.parseAirQualityDataFromDoc(WeatherMonitorMain.poznanURL);
		WeatherMonitorMain.poznanTemp = WeatherMonitorMain.parseWeatherData(WeatherMonitorMain.poznanWeatherURL);
		WeatherMonitorMain.poznanReady = true;
		Frame.updateMeasurements("Poznan", WeatherMonitorMain.getCurrentAirQualityParameters("Poznan", WeatherMonitorMain.poznanArray));
		
	};
	public static Runnable loadGdansk = () -> {
		WeatherMonitorMain.gdanskArray = WeatherMonitorMain.parseAirQualityDataFromDoc(WeatherMonitorMain.gdanskURL);
		WeatherMonitorMain.gdanskTemp = WeatherMonitorMain.parseWeatherData(WeatherMonitorMain.gdanskWeatherURL);
		WeatherMonitorMain.gdanskReady = true;
		Frame.updateMeasurements("Gdansk", WeatherMonitorMain.getCurrentAirQualityParameters("Gdansk", WeatherMonitorMain.gdanskArray));
	};
}
