package weather_monitor;

// Swing utilities
import javax.swing.SwingUtilities;

// Jsoup
import org.jsoup.Jsoup;  
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

// Logger
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// Utilities and IOs
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.InputStream;

// Executor
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

// Exceptions
import org.jsoup.HttpStatusException;
import org.jsoup.UnsupportedMimeTypeException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.concurrent.RejectedExecutionException;
import java.lang.NullPointerException;
import java.lang.IllegalArgumentException;
import java.lang.SecurityException;
import java.io.FileNotFoundException;
import java.io.IOException;


public class WeatherMonitorMain {
	
	// GiOS URLs for each city
	public static String krakowURL, warsawURL, wroclawURL, poznanURL, gdanskURL;
	// Accuweather URLs for each city
	public static String krakowWeatherURL, warsawWeatherURL, wroclawWeatherURL, poznanWeatherURL, gdanskWeatherURL;
	// Arraylists to store table with air quality parameters for each city
	public static ArrayList <ArrayList<String>> krakowArray, warsawArray, wroclawArray, poznanArray, gdanskArray;
	// Strings to store current temperature in each city
	public static String krakowTemp, warsawTemp, wroclawTemp, poznanTemp, gdanskTemp;
	// Boolean indicators if data is already downloaded
	public static boolean krakowReady = false;
	public static boolean warsawReady = false;
	public static boolean wroclawReady = false;
	public static boolean poznanReady = false;
	public static boolean gdanskReady = false;
	// Calendar declaration and indicator to current hour of day
	public static Calendar cal;
	public static int currentHour;
	// Properties
	public static Properties myProps;
	// Logger
	public static final Logger logger = LogManager.getLogger(WeatherMonitorMain.class);
	// Executor
	public static ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);

	//method used to laod properties to a program from properties file
	public static void loadProps(Properties myProps) {
		try {
			krakowURL = new String(myProps.getProperty("gios_krakow"));
			logger.info("URL for data from Krakow city is: " + krakowURL);
			warsawURL = new String(myProps.getProperty("gios_warsaw"));
			logger.info("URL for data from Warsaw city is: " + warsawURL);
			wroclawURL = new String(myProps.getProperty("gios_wroclaw"));
			logger.info("URL for data from Wroclaw city is: " + wroclawURL);
			poznanURL = new String(myProps.getProperty("gios_poznan"));
			logger.info("URL for data from Poznan city is: " + poznanURL);
			gdanskURL = new String(myProps.getProperty("gios_gdansk"));
			logger.info("URL for data from Gdansk city is: " + gdanskURL);
			
			krakowWeatherURL = new String(myProps.getProperty("accuweather_krakow"));
			logger.info("URL for weather in Krakow city is " + krakowWeatherURL);
			warsawWeatherURL = new String(myProps.getProperty("accuweather_warsaw"));
			logger.info("URL for weather in Warsaw city is " + warsawWeatherURL);
			wroclawWeatherURL = new String(myProps.getProperty("accuweather_wroclaw"));
			logger.info("URL for weather in Wroclaw city is " + wroclawWeatherURL);
			poznanWeatherURL = new String(myProps.getProperty("accuweather_poznan"));
			logger.info("URL for weather in Poznan city is " + poznanWeatherURL);
			gdanskWeatherURL = new String(myProps.getProperty("accuweather_gdansk"));
			logger.info("URL for weather in Gdansk city is " + gdanskWeatherURL);
		}
		
		catch(IllegalArgumentException e) {
			logger.error("Key-value in properties file provides invalid argument, URI is not absolute.");
			logger.warn("Please correct data in properties file.");
			logger.info("Program will now exit.");
			System.exit(0);
		}	
	}
	
	public static  ArrayList <ArrayList <String>> parseAirQualityDataFromDoc(String inputURL) {
		
		ArrayList <ArrayList <String>> myTable = new ArrayList<>(8); // column count s 8
		for (int i = 0; i < 8; i++) {
			myTable.add(new ArrayList<String>());
		}
			
		try {
	        // Parse html to Document
	        Document doc = Jsoup.connect(inputURL).get();

	        // There is only one data table in HTML
	        Element table = doc.select("table").first();

	        // Parse table headers
	        Elements headers = table.select("thead tr th");
	        int i = 0;
	        for (Element header : headers) {
	            if (!header.hasAttr("scope")) {
	            	myTable.get(i).add(header.text());
	            	i++;
	            	if (i == 8) {i = 0;}
	            }
	        }
	        // Parse table rows
	        Elements rows = table.select("tbody tr");
	        for (Element row : rows) {
	            int k = 0;
	            Elements cells = row.select("td");
	            for (Element cell : cells) {
	                myTable.get(k).add(cell.text());
	                k++;
	            }
	        }
	        // Replacing "," in data Arrays to "."
	        for (int k = 0; k < 7; k++) {
	            for	(int j = 2; j < 28; j++) {
	            	myTable.get(k).set(j, myTable.get(k).get(j).replaceAll(",", "."));
	            }
	        }
	    }
		catch (HttpStatusException e) {
			logger.error("HTTP request resulted in NOK HTTP response. " + e.getStatusCode() + " - " + e.getMessage());
		} 
		catch (UnsupportedMimeTypeException e) {
			logger.error("Unsupported MIME type " + e.getMimeType() + " - " + e.getMessage());
		} 
		catch (MalformedURLException e) {
			logger.error("Invalid URL: " + e.getMessage());
			logger.info("Try correcting URL data in properties file.");
		} 
		catch (SocketTimeoutException e) {
			logger.warn("Connection timeout: " + e.getMessage());
			logger.info("Try downloading data again by clicking button.");
		} 
		catch (IOException e) {
			logger.error("There was an IOException during Parsing table " + e.getMessage());
	        logger.info("Try downloading data again by clicking button.");
		} 
		catch (Exception e) {
			logger.error("Unexpected exception " + e.getMessage());
	        logger.info("Try downloading data again by clicking button.");
		}
		logger.info("Succesfully parsed data from " + inputURL);
		return myTable;
	}
	
	public static String parseWeatherData(String inputURL) {
		
		String temperature = null;
		try {
			// parse HTML into Document
			Document doc = Jsoup.connect(inputURL).get();

        	// Select temperature data
        	Element tempElement = doc.selectFirst(".display-temp");
        	temperature = tempElement != null ? tempElement.text() : "N/A";
		}
		catch (HttpStatusException e) {
			logger.error("HTTP request resulted in NOK HTTP response. " + e.getStatusCode() + " - " + e.getMessage());
		} 
		catch (UnsupportedMimeTypeException e) {
			logger.error("Unsupported MIME type " + e.getMimeType() + " - " + e.getMessage());
		} 
		catch (MalformedURLException e) {
			logger.error("Invalid URL: " + e.getMessage());
			logger.info("Try correcting URL data in properties file.");
		} 
		catch (SocketTimeoutException e) {
			logger.warn("Connection timeout: " + e.getMessage());
			logger.info("Try downloading data again by clicking button.");
		} 
		catch (IOException e) {
			logger.error("There was an IOException during Parsing table " + e.getMessage());
        	logger.info("Try downloading data again by clicking button.");
		} 
		catch (Exception e) {
			logger.error("Unexpected exception " + e.getMessage());
        	logger.info("Try downloading data again by clicking button.");
		}
		logger.info("Succesfully parsed data from " + inputURL);
		return temperature;
    }
	
	public static ArrayList<String[]> getCurrentAirQualityParameters(String city, ArrayList <ArrayList <String>> inputArray) {
		
		ArrayList <String[]> returnedMeasurements = new ArrayList<String[]>();
		String tempVarT[] = {" ", " "};
		tempVarT[0] = "T";
		switch(city) {
		case "Krakow":
			tempVarT[1] = krakowTemp;
			returnedMeasurements.add(0, tempVarT);
			break;
		case "Warsaw":
			tempVarT[1] = warsawTemp;
			returnedMeasurements.add(0, tempVarT);
			break;
		case "Wroclaw":
			tempVarT[1] = wroclawTemp;
			returnedMeasurements.add(0, tempVarT);
			break;
		case "Poznan":
			tempVarT[1] = poznanTemp;
			returnedMeasurements.add(0, tempVarT);
			break;
		case "Gdansk":
			tempVarT[1] = gdanskTemp;
			returnedMeasurements.add(0, tempVarT);
			break;
		default:
			tempVarT[1] = "N/A";
			returnedMeasurements.add(0, tempVarT);
			break;
		}
		
		for (int i = 0; i < inputArray.size() -1; i++)
		{
			String[] tempVar = {" ", " "};
			if (inputArray.get(i).get(currentHour + 1).equals("") && inputArray.get(i).get(currentHour).equals("")) {
				tempVar[0] = inputArray.get(i).get(0); 
				tempVar[1] = "N/A";
				for (int j = currentHour-1; j > 1; j--) {
					if (!inputArray.get(i).get(j).equals("")) {
						tempVar[1] = inputArray.get(i).get(j);
						logger.info("For " + city + " city displaying value of parameter " + tempVar[0] + " from " + j + ":00 because futher data is not available yet.");
						break;
					}
				}
				if (tempVar[1].equals("N/A")) {
					logger.info("Value of parameter " + tempVar[0] + " for " + city + " city is not available on the website.");
				}
			}
			else if (inputArray.get(i).get(currentHour + 1).equals("") && !inputArray.get(i).get(currentHour).equals("")) {
				tempVar[0] = inputArray.get(i).get(0); 
				tempVar[1] = inputArray.get(i).get(currentHour) + " " +  inputArray.get(i).get(1);
				logger.info("For " + city + " city displaying value of parameter " + tempVar[0]  + " from hour "  + (currentHour-1) + ":00 because data from "
							+  currentHour +":00 is not available on the website yet.");
			}
			else {
				tempVar[0] = inputArray.get(i).get(0); 
				tempVar[1] = inputArray.get(i).get(currentHour + 1) + " " +  inputArray.get(i).get(1);
				logger.info("For " + city + " city displaying value of parameter " + tempVar[0] + " from hour "  + (currentHour) + ":00.");
			}
			returnedMeasurements.add(tempVar);
		}
		return returnedMeasurements;
	}

	public static void main (String args[]) {
		
		//initiate Calendar to get hour now
		cal = Calendar.getInstance();
		currentHour = cal.get(Calendar.HOUR_OF_DAY);
		
		myProps = new Properties();
	
		try (InputStream propFile = new FileInputStream("resources\\WM.properties")) {
			myProps.load(propFile);
			loadProps(myProps);
		}
		catch (FileNotFoundException e) {
			logger.error("Properites file not found.");	
		}
		catch (SecurityException e) {
			logger.error("Cannot access to file, application has no permissions.");
		}
		catch (NullPointerException e) {
			logger.error("Input stream for Properties File is null.");
		}
		catch (IOException e) {
			logger.error("IOException.");
		}
		// Launch GUI
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					Frame window = new Frame();
					window.create_GUI();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		// Warsaw is first selected tab, so download data immediately after launching program
		try {
			logger.info("Downloading data for Warsaw city.");
			executor.submit(RunWeatherThreads.loadWarsaw);
		}
		catch (RejectedExecutionException e) {
			logger.error("Task cannot be accepted for execution.");
		}
		catch (NullPointerException e) {
			logger.error("Submitted task is null.");
		} 	
	}
}
