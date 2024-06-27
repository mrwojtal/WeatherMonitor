package weather_monitor;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

public class Frame {
	
	public static String selectedCity = " ";
	
	//Creating list of list with array of objects to store data for each city (measurment and type of measurement)
	public static List <List<Object[]>> dataTextFieldsList = new ArrayList<>();
	
    public void create_GUI() {
    	//Creating new JFrame and TabbelPannel for app
        JFrame frame = new JFrame("Weather Data");
        JTabbedPane tabbedPane = new JTabbedPane();
        
        //Creating string arrays with const data for GUI
        String[] cities = {"Warsaw", "Krakow", "Wroclaw", "Poznan", "Gdansk"};
        String[] measurement = {"T", "PM2,5", "PM10", "CO", "C6H6", "SO2", "O3", "NO2"};

        //Loop which creates tabs for each city with data
        for (String city : cities) {
        	//Defining main pannel for each city
            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());
            //Panel organizing set of data
            JPanel dataPanel = new JPanel();
            dataPanel.setLayout(new GridLayout(10, 1));

            //Main name of the city button to update data manually on each tab with checkbox deciding wether or not to display more data
            JButton updateButton = new JButton (city + " city data update");
            JCheckBox checkBox = new JCheckBox("Print data from whole day");
            //Combobox for choosing witch data to show on the 2dchart
            JComboBox<String> measurementComboBox = new JComboBox<>();
            
            for (int i = 1; i < 8; i++) {
                measurementComboBox.addItem(measurement[i]);
            }

            dataPanel.add(updateButton);
            dataPanel.add(checkBox);
            dataPanel.add(new JLabel("Choose measurement:"));
            dataPanel.add(measurementComboBox);

            //Creating List of object array for each city with measurement
            List<Object[]> cityDataTextFieldsList = new ArrayList<>(); 

            //Filling the data to the list
            for (int i = 0; i < measurement.length; i++) {
                JTextField dataTextField = new JTextField(measurement[i] + ": " + "...");
                dataTextField.setFont(new Font("Arial", Font.BOLD, 12));
                dataTextField.setEditable(false); 
                dataPanel.add(dataTextField);
                cityDataTextFieldsList.add(new Object[]{measurement[i], dataTextField}); 
            }

            //Adding data for main list with all the cities
            dataTextFieldsList.add(cityDataTextFieldsList);

            panel.add(dataPanel, BorderLayout.NORTH);
            tabbedPane.addTab(city, panel);

            //New action listener for checkbox
            ActionListener updateChartAction = new ActionListener() {
                private JFrame chartFrame;

                //method called when checkbox is checked and which data is selected in combobox
                public void actionPerformed(ActionEvent e) {
                    if (checkBox.isSelected()) {
                    	//creating chart
                        String selectedMeasurement = (String) measurementComboBox.getSelectedItem();
                        JFreeChart chart = createChart(city, selectedMeasurement);

                        if (chartFrame == null) {
                            chartFrame = new JFrame("Chart for " + city);
                            chartFrame.setSize(1200, 800);
                            chartFrame.setLocationRelativeTo(null);
                            chartFrame.addWindowListener(new WindowAdapter() {
                            	//windowlistener for windows closing
                                public void windowClosing(WindowEvent we) {
                                    checkBox.setSelected(false);
                                    chartFrame.dispose();
                                    chartFrame = null;
                                }
                            });
                        }

                        ChartPanel chartPanelComponent = new ChartPanel(chart);
                        chartFrame.setContentPane(chartPanelComponent);
                        chartFrame.setVisible(true);
                    } else {
                    	//if checkbox is not selected hide and set to null chartframe
                        if (chartFrame != null) {
                            chartFrame.setVisible(false);
                            chartFrame.dispose();
                            chartFrame = null;
                        }
                    }
                }
            };

            //adding actionlistener
            checkBox.addActionListener(updateChartAction);
            measurementComboBox.addActionListener(updateChartAction);
            
            // action listener for update button
            updateButton.addActionListener(new ActionListener() {
            	public void actionPerformed(ActionEvent e) {
            		switch(selectedCity) {
            		case "Krakow":
            			WeatherMonitorMain.logger.info("Updating data for " + selectedCity + " city.");
            			WeatherMonitorMain.executor.submit(RunWeatherThreads.loadKrakow);
            			break;
            		case "Warsaw":
            			WeatherMonitorMain.logger.info("Updating data for " + selectedCity + " city.");
            			WeatherMonitorMain.executor.submit(RunWeatherThreads.loadWarsaw);
            			break;
            		case "Wroclaw":
            			WeatherMonitorMain.logger.info("Updating data for " + selectedCity + " city.");
            			WeatherMonitorMain.executor.submit(RunWeatherThreads.loadWroclaw);
            			break;
            		case "Poznan":
            			WeatherMonitorMain.logger.info("Updating data for " + selectedCity + " city.");
            			WeatherMonitorMain.executor.submit(RunWeatherThreads.loadPoznan);
            			break;
            		case "Gdansk":
            			WeatherMonitorMain.logger.info("Updating data for " + selectedCity + " city.");
            			WeatherMonitorMain.executor.submit(RunWeatherThreads.loadGdansk);
            			break;
            		default:
            			break;
            		} 			
            	}	
            });
        }

        //Adding changelistener for changing the tabs
        //and updating the data only for opened tabs
        tabbedPane.addChangeListener(new ChangeListener() {
        	//method called after changing tab
            public void stateChanged(ChangeEvent e) {
            	//getting the object that changed
                JTabbedPane sourceTabbedPane = (JTabbedPane) e.getSource();
                //get city name from tab
                String selectedTab = sourceTabbedPane.getTitleAt(sourceTabbedPane.getSelectedIndex());
                //Downloading current time to check wether the 
                //data is up to date or needs to be updated
                WeatherMonitorMain.cal = Calendar.getInstance();
                WeatherMonitorMain.currentHour = WeatherMonitorMain.cal.get(Calendar.HOUR_OF_DAY);
                // Downloading data only after switching tabs if data is updated (an hour since latest download has passed)
                try {
                	//switch to choose correct data for specific city
                	switch (selectedTab) {
                    case "Krakow":
                    	selectedCity = "Krakow";
                    	//Downloading data for city first time after running app
                    	if (!WeatherMonitorMain.krakowReady) {
                    		WeatherMonitorMain.logger.info("Downloading data for " + selectedTab + " city.");
                    		WeatherMonitorMain.executor.submit(RunWeatherThreads.loadKrakow);
                    		break;
                    	}
                    	//Updating data if 1hous since last update passed
                    	else if (WeatherMonitorMain.krakowReady && WeatherMonitorMain.krakowArray.get(0).get(WeatherMonitorMain.currentHour+1).equals("")) {
                    		WeatherMonitorMain.logger.info("Updating data for " + selectedTab + " city.");
                    		WeatherMonitorMain.krakowReady = false;
                    		WeatherMonitorMain.executor.submit(RunWeatherThreads.loadKrakow);
                    		break;
                    	}
                    	//Current data is OK
                    	else {
                    		WeatherMonitorMain.logger.info("Data for " + selectedTab + " city is up to date.");
                    		break;
                    	}
                    //reapeats for each city switch case and if statement
                	case "Warsaw":
                		selectedCity = "Warsaw";
                		if (!WeatherMonitorMain.warsawReady) {
                    		WeatherMonitorMain.logger.info("Downloading data for " + selectedTab + " city.");
                    		WeatherMonitorMain.executor.submit(RunWeatherThreads.loadWarsaw);
                    		break;
                    	}
                    	else if (WeatherMonitorMain.warsawReady && WeatherMonitorMain.warsawArray.get(0).get(WeatherMonitorMain.currentHour+1).equals("")) {
                    		WeatherMonitorMain.logger.info("Updating data for " + selectedTab + " city.");
                    		WeatherMonitorMain.warsawReady = false;
                    		WeatherMonitorMain.executor.submit(RunWeatherThreads.loadWarsaw);
                    		break;
                    	}
                    	else {
                    		WeatherMonitorMain.logger.info("Data for " + selectedTab + " city is up to date.");
                    		break;
                    	}
                	case "Wroclaw":
                		selectedCity = "Wroclaw";
                		if (!WeatherMonitorMain.wroclawReady) {
                    		WeatherMonitorMain.logger.info("Downloading data for " + selectedTab + " city.");
                    		WeatherMonitorMain.executor.submit(RunWeatherThreads.loadWroclaw);
                    		break;
                    	}
                    	else if (WeatherMonitorMain.wroclawReady && WeatherMonitorMain.wroclawArray.get(0).get(WeatherMonitorMain.currentHour+1).equals("")) {
                    		WeatherMonitorMain.logger.info("Updating data for " + selectedTab + " city.");
                    		WeatherMonitorMain.wroclawReady = false;
                    		WeatherMonitorMain.executor.submit(RunWeatherThreads.loadWroclaw);
                    		break;
                    	}
                    	else {
                    		WeatherMonitorMain.logger.info("Data for " + selectedTab + " city is up to date.");
                    		break;
                    	}
                	case "Poznan":
                		selectedCity = "Poznan";
                		if (!WeatherMonitorMain.poznanReady) {
                    		WeatherMonitorMain.logger.info("Downloading data for " + selectedTab + " city.");
                    		WeatherMonitorMain.executor.submit(RunWeatherThreads.loadPoznan);
                    		break;
                    	}
                    	else if (WeatherMonitorMain.poznanReady && WeatherMonitorMain.poznanArray.get(0).get(WeatherMonitorMain.currentHour+1).equals("")) {
                    		WeatherMonitorMain.logger.info("Updating data for " + selectedTab + " city.");
                    		WeatherMonitorMain.poznanReady = false;
                    		WeatherMonitorMain.executor.submit(RunWeatherThreads.loadPoznan);
                    		break;
                    	}
                    	else {
                    		WeatherMonitorMain.logger.info("Data for " + selectedTab + " city is up to date.");
                    		break;
                    	}
                	case "Gdansk":
                		selectedCity = "Gdansk";
                		if (!WeatherMonitorMain.gdanskReady) {
                    		WeatherMonitorMain.logger.info("Downloading data for " + selectedTab + " city.");
                    		WeatherMonitorMain.executor.submit(RunWeatherThreads.loadGdansk);
                    		break;
                    	}
                    	else if (WeatherMonitorMain.gdanskReady && WeatherMonitorMain.gdanskArray.get(0).get(WeatherMonitorMain.currentHour+1).equals("")) {
                    		WeatherMonitorMain.logger.info("Updating data for " + selectedTab + " city.");
                    		WeatherMonitorMain.gdanskReady = false;
                    		WeatherMonitorMain.executor.submit(RunWeatherThreads.loadGdansk);
                    		break;
                    	}
                    	else {
                    		WeatherMonitorMain.logger.info("Data for " + selectedTab + " city is up to date.");
                    		break;
                    	}
                	}
                }
                //catching exceptions
        		catch (RejectedExecutionException ex) {
        			WeatherMonitorMain.logger.error("Task cannot be accepted for execution.");
        		}
        		catch (NullPointerException ex) {
        			WeatherMonitorMain.logger.error("Submitted task is null.");
        		} 	   
            }
        });
        
        //Displaying data on the tab
        frame.add(tabbedPane);
        frame.setSize(600, 220);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        
        
        
    }

    //method for displaying a chart
    private static JFreeChart createChart(String city, String measurement) {
    	//create variables to store the data
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String yAxisDescription = measurement;
        //switch between the cities
        switch (city) {
        case "Krakow":
        	//check current data
        	if (!WeatherMonitorMain.krakowReady) {
        		WeatherMonitorMain.logger.warn("Data for " + city + " is not downloaded yet, thus cannot be graphed.");
        		break;
        	}
        	//switch to show data selected in combobox for specific city
        	switch (measurement) {
        	case "PM10":
        		//iterate throught hours to update data till available hour
        		for (int hour = 2; hour <= WeatherMonitorMain.currentHour+1; hour++) {
        			//if statements checks which data are available for creating a 2d chart
        			if (WeatherMonitorMain.krakowArray.get(0).get(hour).equals("")) {
        				if (!WeatherMonitorMain.krakowArray.get(0).get(hour+1).equals("")) {
        					WeatherMonitorMain.logger.info("Data for hour " + (hour-1) + ":00 is not available, but futther data is available");
        					continue;
        				}
        				WeatherMonitorMain.logger.info("Data for hour " + WeatherMonitorMain.currentHour + ":00" +" is not available on the website yet");
        				break;
        			}
        			//else add data to dataset
                	dataset.addValue(Double.parseDouble(WeatherMonitorMain.krakowArray.get(0).get(hour)), measurement, hour-1 + ":00");
            	}
        		//description for 2d chart
        		yAxisDescription = measurement + " " + "[" + WeatherMonitorMain.krakowArray.get(0).get(1) + "]";
        		WeatherMonitorMain.logger.info("Printing data for city " + city + " measurement: " + measurement + ": ");
        		break;
        		//repeats for each type of data and city
        	case "PM2,5":
        		for (int hour = 2; hour <= WeatherMonitorMain.currentHour+1; hour++) {
        			if (WeatherMonitorMain.krakowArray.get(1).get(hour).equals("")) {
        				if (!WeatherMonitorMain.krakowArray.get(1).get(hour+1).equals("")) {
        					WeatherMonitorMain.logger.info("Data for hour " + (hour-1) + ":00 is not available, but futther data is available");
        					continue;
        				}
        				WeatherMonitorMain.logger.info("Data for hour " + WeatherMonitorMain.currentHour + ":00" +" is not available on the website yet");
        				break;
        			}
                	dataset.addValue(Double.parseDouble(WeatherMonitorMain.krakowArray.get(1).get(hour)), measurement, hour-1 + ":00");
            	}
        		yAxisDescription = measurement + " " + "[" + WeatherMonitorMain.krakowArray.get(1).get(1) + "]";
        		WeatherMonitorMain.logger.info("Printing data for city " + city + " measurement: " + measurement + ": ");
        		break;
        	case "NO2":
        		for (int hour = 2; hour <= WeatherMonitorMain.currentHour+1; hour++) {
        			if (WeatherMonitorMain.krakowArray.get(3).get(hour).equals("")) {
        				if (!WeatherMonitorMain.krakowArray.get(3).get(hour+1).equals("")) {
        					WeatherMonitorMain.logger.info("Data for hour " + (hour-1) + ":00 is not available, but futther data is available");
        					continue;
        				}
        				WeatherMonitorMain.logger.info("Data for hour " + WeatherMonitorMain.currentHour + ":00" +" is not available on the website yet");
        				break;
        			}
                	dataset.addValue(Double.parseDouble(WeatherMonitorMain.krakowArray.get(3).get(hour)), measurement, hour-1 + ":00");
            	}
        		yAxisDescription = measurement + " " + "[" + WeatherMonitorMain.krakowArray.get(3).get(1) + "]";
        		WeatherMonitorMain.logger.info("Printing data for city " + city + " measurement: " + measurement + ": ");
        		break;
        	case "SO2":
        		for (int hour = 2; hour <= WeatherMonitorMain.currentHour+1; hour++) {
        			if (WeatherMonitorMain.krakowArray.get(4).get(hour).equals("")) {
        				if (!WeatherMonitorMain.krakowArray.get(4).get(hour+1).equals("")) {
        					WeatherMonitorMain.logger.info("Data for hour " + (hour-1) + ":00 is not available, but futther data is available");
        					continue;
        				}
        				WeatherMonitorMain.logger.info("Data for hour " + WeatherMonitorMain.currentHour + ":00" +" is not available on the website yet");
        				break;
        			}
                	dataset.addValue(Double.parseDouble(WeatherMonitorMain.krakowArray.get(4).get(hour)), measurement, hour-1 + ":00");
            	}
        		yAxisDescription = measurement + " " + "[" + WeatherMonitorMain.krakowArray.get(4).get(1) + "]";
        		WeatherMonitorMain.logger.info("Printing data for city " + city + " measurement: " + measurement + ": ");
        		break;
        	case "C6H6":
        		for (int hour = 2; hour <= WeatherMonitorMain.currentHour+1; hour++) {
        			if (WeatherMonitorMain.krakowArray.get(5).get(hour).equals("")) {
        				if (!WeatherMonitorMain.krakowArray.get(5).get(hour+1).equals("")) {
        					WeatherMonitorMain.logger.info("Data for hour " + (hour-1) + ":00 is not available, but futther data is available");
        					continue;
        				}
        				WeatherMonitorMain.logger.info("Data for hour " + WeatherMonitorMain.currentHour + ":00" +" is not available on the website yet");
        				break;
        			}
                	dataset.addValue(Double.parseDouble(WeatherMonitorMain.krakowArray.get(5).get(hour)), measurement, hour-1 + ":00");
            	}
        		yAxisDescription = measurement + " " + "[" + WeatherMonitorMain.krakowArray.get(5).get(1) + "]";
        		WeatherMonitorMain.logger.info("Printing data for city " + city + " measurement: " + measurement + ": ");
        		break;
        	case "CO":
        		for (int hour = 2; hour <= WeatherMonitorMain.currentHour+1; hour++) {
        			if (WeatherMonitorMain.krakowArray.get(6).get(hour).equals("")) {
        				if (!WeatherMonitorMain.krakowArray.get(6).get(hour+1).equals("")) {
        					WeatherMonitorMain.logger.info("Data for hour " + (hour-1) + ":00 is not available, but futther data is available");
        					continue;
        				}
        				WeatherMonitorMain.logger.info("Data for hour " + WeatherMonitorMain.currentHour + ":00" +" is not available on the website yet");
        				break;
        			}
                	dataset.addValue(Double.parseDouble(WeatherMonitorMain.krakowArray.get(6).get(hour)), measurement, hour-1 + ":00");
            	}
        		yAxisDescription = measurement + " " + "[" + WeatherMonitorMain.krakowArray.get(6).get(1) + "]";
        		WeatherMonitorMain.logger.info("Printing data for city " + city + " measurement: " + measurement + ": ");
        		break;
        	default:
        		WeatherMonitorMain.logger.warn("Selected measurement " + measurement + " is not available for city: " + city + ".");
        		break;	
        	}
        	break;
        	
        case "Warsaw":
        	if (!WeatherMonitorMain.warsawReady) {
        		WeatherMonitorMain.logger.warn("Data for " + city + " is not downloaded yet, thus cannot be graphed.");
        		break;
        	}
        	switch (measurement) {
        	case "PM10":
        		for (int hour = 2; hour <= WeatherMonitorMain.currentHour+1; hour++) {
        			if (WeatherMonitorMain.warsawArray.get(0).get(hour).equals("")) {
        				if (!WeatherMonitorMain.warsawArray.get(0).get(hour+1).equals("")) {
        					WeatherMonitorMain.logger.info("Data for hour " + (hour-1) + ":00 is not available, but futther data is available");
        					continue;
        				}
        				WeatherMonitorMain.logger.info("Data for hour " + WeatherMonitorMain.currentHour + ":00" +" is not available on the website yet");
        				break;
        			}
                	dataset.addValue(Double.parseDouble(WeatherMonitorMain.warsawArray.get(0).get(hour)), measurement, hour-1 + ":00");
            	}
        		yAxisDescription = measurement + " " + "[" + WeatherMonitorMain.warsawArray.get(0).get(1) + "]";
        		WeatherMonitorMain.logger.info("Printing data for city " + city + " measurement: " + measurement + ": ");
        		break;
        	case "PM2,5":
        		for (int hour = 2; hour <= WeatherMonitorMain.currentHour+1; hour++) {
        			if (WeatherMonitorMain.warsawArray.get(1).get(hour).equals("")) {
        				if (!WeatherMonitorMain.warsawArray.get(1).get(hour+1).equals("")) {
        					WeatherMonitorMain.logger.info("Data for hour " + (hour-1) + ":00 is not available, but futther data is available");
        					continue;
        				}
        				WeatherMonitorMain.logger.info("Data for hour " + WeatherMonitorMain.currentHour + ":00" +" is not available on the website yet");
        				break;
        			}
                	dataset.addValue(Double.parseDouble(WeatherMonitorMain.warsawArray.get(1).get(hour)), measurement, hour-1 + ":00");
            	}
        		yAxisDescription = measurement + " " + "[" + WeatherMonitorMain.warsawArray.get(1).get(1) + "]";
        		WeatherMonitorMain.logger.info("Printing data for city " + city + " measurement: " + measurement + ": ");
        		break;
        	case "O3":
        		for (int hour = 2; hour <= WeatherMonitorMain.currentHour+1; hour++) {
        			if (WeatherMonitorMain.warsawArray.get(2).get(hour).equals("")) {
        				if (!WeatherMonitorMain.warsawArray.get(2).get(hour+1).equals("")) {
        					WeatherMonitorMain.logger.info("Data for hour " + (hour-1) + ":00 is not available, but futther data is available");
        					continue;
        				}
        				WeatherMonitorMain.logger.info("Data for hour " + WeatherMonitorMain.currentHour + ":00" +" is not available on the website yet");
        				break;
        			}
                	dataset.addValue(Double.parseDouble(WeatherMonitorMain.warsawArray.get(2).get(hour)), measurement, hour-1 + ":00");
            	}
        		yAxisDescription = measurement + " " + "[" + WeatherMonitorMain.warsawArray.get(2).get(1) + "]";
        		WeatherMonitorMain.logger.info("Printing data for city " + city + " measurement: " + measurement + ": ");
        		break;
        	case "NO2":
        		for (int hour = 2; hour <= WeatherMonitorMain.currentHour+1; hour++) {
        			if (WeatherMonitorMain.warsawArray.get(3).get(hour).equals("")) {
        				if (!WeatherMonitorMain.warsawArray.get(3).get(hour+1).equals("")) {
        					WeatherMonitorMain.logger.info("Data for hour " + (hour-1) + ":00 is not available, but futther data is available");
        					continue;
        				}
        				WeatherMonitorMain.logger.info("Data for hour " + WeatherMonitorMain.currentHour + ":00" +" is not available on the website yet");
        				break;
        			}
                	dataset.addValue(Double.parseDouble(WeatherMonitorMain.warsawArray.get(3).get(hour)), measurement, hour-1 + ":00");
            	}
        		yAxisDescription = measurement + " " + "[" + WeatherMonitorMain.warsawArray.get(3).get(1) + "]";
        		WeatherMonitorMain.logger.info("Printing data for city " + city + " measurement: " + measurement + ": ");
        		break;
        	default:
        		WeatherMonitorMain.logger.warn("Selected measurement " + measurement + " is not available for city: " + city + ".");
        		break;	
        	}
        	break;
        	
        case "Wroclaw":
        	if (!WeatherMonitorMain.wroclawReady) {
        		WeatherMonitorMain.logger.warn("Data for " + city + " is not downloaded yet, thus cannot be graphed.");
        		break;
        	}
        	switch (measurement) {
        	case "PM10":
        		for (int hour = 2; hour <= WeatherMonitorMain.currentHour+1; hour++) {
        			if (WeatherMonitorMain.wroclawArray.get(0).get(hour).equals("")) {
        				if (!WeatherMonitorMain.wroclawArray.get(0).get(hour+1).equals("")) {
        					WeatherMonitorMain.logger.info("Data for hour " + (hour-1) + ":00 is not available, but futther data is available");
        					continue;
        				}
        				WeatherMonitorMain.logger.info("Data for hour " + WeatherMonitorMain.currentHour + ":00" +" is not available on the website yet");
        				break;
        			}
                	dataset.addValue(Double.parseDouble(WeatherMonitorMain.wroclawArray.get(0).get(hour)), measurement, hour-1 + ":00");
            	}
        		yAxisDescription = measurement + " " + "[" + WeatherMonitorMain.wroclawArray.get(0).get(1) +"]";
        		WeatherMonitorMain.logger.info("Printing data for city " + city + " measurement: " + measurement + ": ");
        		
        		break;
        	case "PM2,5":
        		for (int hour = 2; hour <= WeatherMonitorMain.currentHour+1; hour++) {
        			if (WeatherMonitorMain.wroclawArray.get(1).get(hour).equals("")) {
        				if (!WeatherMonitorMain.wroclawArray.get(1).get(hour+1).equals("")) {
        					WeatherMonitorMain.logger.info("Data for hour " + (hour-1) + ":00 is not available, but futther data is available");
        					continue;
        				}
        				WeatherMonitorMain.logger.info("Data for hour " + WeatherMonitorMain.currentHour + ":00" +" is not available on the website yet");
        				break;
        			}
                	dataset.addValue(Double.parseDouble(WeatherMonitorMain.wroclawArray.get(1).get(hour)), measurement, hour-1 + ":00");
            	}
        		yAxisDescription = measurement + " " + "[" + WeatherMonitorMain.wroclawArray.get(1).get(1) +"]";
        		WeatherMonitorMain.logger.info("Printing data for city " + city + " measurement: " + measurement + ": ");
        		break;
        	case "O3":
        		for (int hour = 2; hour <= WeatherMonitorMain.currentHour+1; hour++) {
        			if (WeatherMonitorMain.wroclawArray.get(2).get(hour).equals("")) {
        				if (!WeatherMonitorMain.wroclawArray.get(2).get(hour+1).equals("")) {
        					WeatherMonitorMain.logger.info("Data for hour " + (hour-1) + ":00 is not available, but futther data is available");
        					continue;
        				}
        				WeatherMonitorMain.logger.info("Data for hour " + WeatherMonitorMain.currentHour + ":00" +" is not available on the website yet");
        				break;
        			}
                	dataset.addValue(Double.parseDouble(WeatherMonitorMain.wroclawArray.get(2).get(hour)), measurement, hour-1 + ":00");
            	}
        		yAxisDescription = measurement + " " + "[" + WeatherMonitorMain.wroclawArray.get(2).get(1) +"]";
        		WeatherMonitorMain.logger.info("Printing data for city " + city + " measurement: " + measurement + ": ");
        		break;
        	case "NO2":
        		for (int hour = 2; hour <= WeatherMonitorMain.currentHour+1; hour++) {
        			if (WeatherMonitorMain.wroclawArray.get(3).get(hour).equals("")) {
        				if (!WeatherMonitorMain.wroclawArray.get(3).get(hour+1).equals("")) {
        					WeatherMonitorMain.logger.info("Data for hour " + (hour-1) + ":00 is not available, but futther data is available");
        					continue;
        				}
        				WeatherMonitorMain.logger.info("Data for hour " + WeatherMonitorMain.currentHour + ":00" +" is not available on the website yet");
        				break;
        			}
                	dataset.addValue(Double.parseDouble(WeatherMonitorMain.wroclawArray.get(3).get(hour)), measurement, hour-1 + ":00");
            	}
        		yAxisDescription = measurement + " " + "[" + WeatherMonitorMain.wroclawArray.get(3).get(1) +"]";
        		WeatherMonitorMain.logger.info("Printing data for city " + city + " measurement: " + measurement + ": ");
        		break;
        	case "SO2":
        		for (int hour = 2; hour <= WeatherMonitorMain.currentHour+1; hour++) {
        			if (WeatherMonitorMain.wroclawArray.get(4).get(hour).equals("")) {
        				if (!WeatherMonitorMain.wroclawArray.get(4).get(hour+1).equals("")) {
        					WeatherMonitorMain.logger.info("Data for hour " + (hour-1) + ":00 is not available, but futther data is available");
        					continue;
        				}
        				WeatherMonitorMain.logger.info("Data for hour " + WeatherMonitorMain.currentHour + ":00" +" is not available on the website yet");
        				break;
        			}
                	dataset.addValue(Double.parseDouble(WeatherMonitorMain.wroclawArray.get(4).get(hour)), measurement, hour-1 + ":00");
            	}
        		yAxisDescription = measurement + " " + "[" + WeatherMonitorMain.wroclawArray.get(4).get(1) +"]";
        		WeatherMonitorMain.logger.info("Printing data for city " + city + " measurement: " + measurement + ": ");
        		break;
        	case "C6H6":
        		for (int hour = 2; hour <= WeatherMonitorMain.currentHour+1; hour++) {
        			if (WeatherMonitorMain.wroclawArray.get(5).get(hour).equals("")) {
        				if (!WeatherMonitorMain.wroclawArray.get(5).get(hour+1).equals("")) {
        					WeatherMonitorMain.logger.info("Data for hour " + (hour-1) + ":00 is not available, but futther data is available");
        					continue;
        				}
        				WeatherMonitorMain.logger.info("Data for hour " + WeatherMonitorMain.currentHour + ":00" +" is not available on the website yet");
        				break;
        			}
                	dataset.addValue(Double.parseDouble(WeatherMonitorMain.wroclawArray.get(5).get(hour)), measurement, hour-1 + ":00");
            	}
        		yAxisDescription = measurement + " " + "[" + WeatherMonitorMain.wroclawArray.get(5).get(1) +"]";
        		WeatherMonitorMain.logger.info("Printing data for city " + city + " measurement: " + measurement + ": ");
        		break;
        	case "CO":
        		for (int hour = 2; hour <= WeatherMonitorMain.currentHour+1; hour++) {
        			if (WeatherMonitorMain.wroclawArray.get(6).get(hour).equals("")) {
        				if (!WeatherMonitorMain.wroclawArray.get(6).get(hour+1).equals("")) {
        					WeatherMonitorMain.logger.info("Data for hour " + (hour-1) + ":00 is not available, but futther data is available");
        					continue;
        				}
        				WeatherMonitorMain.logger.info("Data for hour " + WeatherMonitorMain.currentHour + ":00" +" is not available on the website yet");
        				break;
        			}
                	dataset.addValue(Double.parseDouble(WeatherMonitorMain.wroclawArray.get(6).get(hour)), measurement, hour-1 + ":00");
            	}
        		yAxisDescription = measurement + " " + "[" + WeatherMonitorMain.wroclawArray.get(6).get(1) +"]";
        		WeatherMonitorMain.logger.info("Printing data for city " + city + " measurement: " + measurement + ": ");
        		break;
        	default:
        		WeatherMonitorMain.logger.warn("Selected measurement " + measurement + " is not available for city: " + city + ".");
        		break;	
        	}
        	break;
        	
        case "Poznan":
        	if (!WeatherMonitorMain.poznanReady) {
        		WeatherMonitorMain.logger.warn("Data for " + city + " is not downloaded yet, thus cannot be graphed.");
        		break;
        	}
        	switch (measurement) {
        	case "PM10":
        		for (int hour = 2; hour <= WeatherMonitorMain.currentHour+1; hour++) {
        			if (WeatherMonitorMain.poznanArray.get(0).get(hour).equals("")) {
        				if (!WeatherMonitorMain.poznanArray.get(0).get(hour+1).equals("")) {
        					WeatherMonitorMain.logger.info("Data for hour " + (hour-1) + ":00 is not available, but futther data is available");
        					continue;
        				}
        				WeatherMonitorMain.logger.info("Data for hour " + WeatherMonitorMain.currentHour + ":00" +" is not available on the website yet");
        				break;
        			}
                	dataset.addValue(Double.parseDouble(WeatherMonitorMain.poznanArray.get(0).get(hour)), measurement, hour-1 + ":00");
            	}
        		yAxisDescription = measurement + " " + "[" + WeatherMonitorMain.poznanArray.get(0).get(1) + "]";
        		WeatherMonitorMain.logger.info("Printing data for city " + city + " measurement: " + measurement + ": ");
        		break;
        	case "PM2,5":
        		for (int hour = 2; hour <= WeatherMonitorMain.currentHour+1; hour++) {
        			if (WeatherMonitorMain.poznanArray.get(1).get(hour).equals("")) {
        				if (!WeatherMonitorMain.poznanArray.get(1).get(hour+1).equals("")) {
        					WeatherMonitorMain.logger.info("Data for hour " + (hour-1) + ":00 is not available, but futther data is available");
        					continue;
        				}
        				WeatherMonitorMain.logger.info("Data for hour " + WeatherMonitorMain.currentHour + ":00" +" is not available on the website yet");
        				break;
        			}
                	dataset.addValue(Double.parseDouble(WeatherMonitorMain.poznanArray.get(1).get(hour)), measurement, hour-1 + ":00");
            	}
        		yAxisDescription = measurement + " " + "[" + WeatherMonitorMain.poznanArray.get(1).get(1) + "]";
        		WeatherMonitorMain.logger.info("Printing data for city " + city + " measurement: " + measurement + ": ");
        		break;
        	case "O3":
        		for (int hour = 2; hour <= WeatherMonitorMain.currentHour+1; hour++) {
        			if (WeatherMonitorMain.poznanArray.get(2).get(hour).equals("")) {
        				if (!WeatherMonitorMain.poznanArray.get(2).get(hour+1).equals("")) {
        					WeatherMonitorMain.logger.info("Data for hour " + (hour-1) + ":00 is not available, but futther data is available");
        					continue;
        				}
        				WeatherMonitorMain.logger.info("Data for hour " + WeatherMonitorMain.currentHour + ":00" +" is not available on the website yet");
        				break;
        			}
                	dataset.addValue(Double.parseDouble(WeatherMonitorMain.poznanArray.get(2).get(hour)), measurement, hour-1 + ":00");
            	}
        		yAxisDescription = measurement + " " + "[" + WeatherMonitorMain.poznanArray.get(2).get(1) + "]";
        		WeatherMonitorMain.logger.info("Printing data for city " + city + " measurement: " + measurement + ": ");
        		break;
        	case "NO2":
        		for (int hour = 2; hour <= WeatherMonitorMain.currentHour+1; hour++) {
        			if (WeatherMonitorMain.poznanArray.get(3).get(hour).equals("")) {
        				if (!WeatherMonitorMain.poznanArray.get(3).get(hour+1).equals("")) {
        					WeatherMonitorMain.logger.info("Data for hour " + (hour-1) + ":00 is not available, but futther data is available");
        					continue;
        				}
        				WeatherMonitorMain.logger.info("Data for hour " + WeatherMonitorMain.currentHour + ":00" +" is not available on the website yet");
        				break;
        			}
                	dataset.addValue(Double.parseDouble(WeatherMonitorMain.poznanArray.get(3).get(hour)), measurement, hour-1 + ":00");
            	}
        		yAxisDescription = measurement + " " + "[" + WeatherMonitorMain.poznanArray.get(3).get(1) + "]";
        		WeatherMonitorMain.logger.info("Printing data for city " + city + " measurement: " + measurement + ": ");
        		break;
        	default:
        		WeatherMonitorMain.logger.warn("Selected measurement " + measurement + " is not available for city: " + city + ".");
        		break;	
        	}
        	break;
        	
        case "Gdansk":
        	if (!WeatherMonitorMain.gdanskReady) {
        		WeatherMonitorMain.logger.warn("Data for " + city + " is not downloaded yet, thus cannot be graphed.");
        		break;
        	}
        	switch (measurement) {
        	case "PM10":
        		for (int hour = 2; hour <= WeatherMonitorMain.currentHour+1; hour++) {
        			if (WeatherMonitorMain.gdanskArray.get(0).get(hour).equals("")) {
        				if (!WeatherMonitorMain.gdanskArray.get(0).get(hour+1).equals("")) {
        					WeatherMonitorMain.logger.info("Data for hour " + (hour-1) + ":00 is not available, but futther data is available");
        					continue;
        				}
        				WeatherMonitorMain.logger.info("Data for hour " + WeatherMonitorMain.currentHour + ":00" +" is not available on the website yet");
        				break;
        			}
                	dataset.addValue(Double.parseDouble(WeatherMonitorMain.gdanskArray.get(0).get(hour)), measurement, hour-1 + ":00");
            	}
        		yAxisDescription = measurement + " " + "[" + WeatherMonitorMain.gdanskArray.get(0).get(1) + "]";
        		WeatherMonitorMain.logger.info("Printing data for city " + city + " measurement: " + measurement + ": ");
        		break;
        	case "PM2,5":
        		for (int hour = 2; hour <= WeatherMonitorMain.currentHour+1; hour++) {
        			if (WeatherMonitorMain.gdanskArray.get(1).get(hour).equals("")) {
        				if (!WeatherMonitorMain.gdanskArray.get(1).get(hour+1).equals("")) {
        					WeatherMonitorMain.logger.info("Data for hour " + (hour-1) + ":00 is not available, but futther data is available");
        					continue;
        				}
        				WeatherMonitorMain.logger.info("Data for hour " + WeatherMonitorMain.currentHour + ":00" +" is not available on the website yet");
        				break;
        			}
                	dataset.addValue(Double.parseDouble(WeatherMonitorMain.gdanskArray.get(1).get(hour)), measurement, hour-1 + ":00");
            	}
        		yAxisDescription = measurement + " " + "[" + WeatherMonitorMain.gdanskArray.get(1).get(1) + "]";
        		WeatherMonitorMain.logger.info("Printing data for city " + city + " measurement: " + measurement + ": ");
        		break;
        	case "O3":
        		for (int hour = 2; hour <= WeatherMonitorMain.currentHour+1; hour++) {
        			if (WeatherMonitorMain.gdanskArray.get(2).get(hour).equals("")) {
        				if (!WeatherMonitorMain.gdanskArray.get(2).get(hour+1).equals("")) {
        					WeatherMonitorMain.logger.info("Data for hour " + (hour-1) + ":00 is not available, but futther data is available");
        					continue;
        				}
        				WeatherMonitorMain.logger.info("Data for hour " + WeatherMonitorMain.currentHour + ":00" +" is not available on the website yet");
        				break;
        			}
                	dataset.addValue(Double.parseDouble(WeatherMonitorMain.gdanskArray.get(2).get(hour)), measurement, hour-1 + ":00");
            	}
        		yAxisDescription = measurement + " " + "[" + WeatherMonitorMain.gdanskArray.get(2).get(1) + "]";
        		WeatherMonitorMain.logger.info("Printing data for city " + city + " measurement: " + measurement + ": ");
        		break;
        	case "NO2":
        		for (int hour = 2; hour <= WeatherMonitorMain.currentHour+1; hour++) {
        			if (WeatherMonitorMain.gdanskArray.get(3).get(hour).equals("")) {
        				if (!WeatherMonitorMain.gdanskArray.get(3).get(hour+1).equals("")) {
        					WeatherMonitorMain.logger.info("Data for hour " + (hour-1) + ":00 is not available, but futther data is available");
        					continue;
        				}
        				WeatherMonitorMain.logger.info("Data for hour " + WeatherMonitorMain.currentHour + ":00" +" is not available on the website yet");
        				break;
        			}
                	dataset.addValue(Double.parseDouble(WeatherMonitorMain.gdanskArray.get(3).get(hour)), measurement, hour-1 + ":00");
            	}
        		yAxisDescription = measurement + " " + "[" + WeatherMonitorMain.gdanskArray.get(3).get(1) + "]";
        		WeatherMonitorMain.logger.info("Printing data for city " + city + " measurement: " + measurement + ": ");
        		break;
        	case "SO2":
        		for (int hour = 2; hour <= WeatherMonitorMain.currentHour+1; hour++) {
        			if (WeatherMonitorMain.gdanskArray.get(4).get(hour).equals("")) {
        				if (!WeatherMonitorMain.gdanskArray.get(4).get(hour+1).equals("")) {
        					WeatherMonitorMain.logger.info("Data for hour " + (hour-1) + ":00 is not available, but futther data is available");
        					continue;
        				}
        				WeatherMonitorMain.logger.info("Data for hour " + WeatherMonitorMain.currentHour + ":00" +" is not available on the website yet");
        				break;
        			}
                	dataset.addValue(Double.parseDouble(WeatherMonitorMain.gdanskArray.get(4).get(hour)), measurement, hour-1 + ":00");
            	}
        		yAxisDescription = measurement + " " + "[" + WeatherMonitorMain.gdanskArray.get(4).get(1) + "]";
        		WeatherMonitorMain.logger.info("Printing data for city " + city + " measurement: " + measurement + ": ");
        		break;
        	case "C6H6":
        		for (int hour = 2; hour <= WeatherMonitorMain.currentHour+1; hour++) {
        			if (WeatherMonitorMain.gdanskArray.get(5).get(hour).equals("")) {
        				if (!WeatherMonitorMain.gdanskArray.get(5).get(hour+1).equals("")) {
        					WeatherMonitorMain.logger.info("Data for hour " + (hour-1) + ":00 is not available, but futther data is available");
        					continue;
        				}
        				WeatherMonitorMain.logger.info("Data for hour " + WeatherMonitorMain.currentHour + ":00" +" is not available on the website yet");
        				break;
        			}
                	dataset.addValue(Double.parseDouble(WeatherMonitorMain.gdanskArray.get(5).get(hour)), measurement, hour-1 + ":00");
            	}
        		yAxisDescription = measurement + " " + "[" + WeatherMonitorMain.gdanskArray.get(5).get(1) + "]";
        		WeatherMonitorMain.logger.info("Printing data for city " + city + " measurement: " + measurement + ": ");
        		break;
        	case "CO":
        		for (int hour = 2; hour <= WeatherMonitorMain.currentHour+1; hour++) {
        			if (WeatherMonitorMain.gdanskArray.get(6).get(hour).equals("")) {
        				if (!WeatherMonitorMain.gdanskArray.get(6).get(hour+1).equals("")) {
        					WeatherMonitorMain.logger.info("Data for hour " + (hour-1) + ":00 is not available, but futther data is available");
        					continue;
        				}
        				WeatherMonitorMain.logger.info("Data for hour " + WeatherMonitorMain.currentHour + ":00" +" is not available on the website yet");
        				break;
        			}
                	dataset.addValue(Double.parseDouble(WeatherMonitorMain.gdanskArray.get(6).get(hour)), measurement, hour-1 + ":00");
            	}
        		yAxisDescription = measurement + " " + "[" + WeatherMonitorMain.gdanskArray.get(6).get(1) + "]";
        		WeatherMonitorMain.logger.info("Printing data for city " + city + " measurement: " + measurement + ": ");
        		break;
        	default:
        		WeatherMonitorMain.logger.warn("Selected measurement " + measurement + " is not available for city: " + city + ".");
        		break;	
        	}
        	break;
        	
        	default:
        		break;
        }
        //after all return 2dchart and display it
        return ChartFactory.createLineChart(
                "Data for " + city,
                "Time",
                yAxisDescription,
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
    }
    
    //method to update the data in the list and store it 
    public static void updateMeasurements(String city, ArrayList<String[]> measurements) {
    	//find specific city and save it index from list
    	int cityIndex = 0;
    	switch(city)
    	{
    		case "Warsaw":
    			cityIndex = 0;
    			break;
    		case "Krakow":
        		cityIndex = 1;
        		break;
    		case "Wroclaw":
        		cityIndex = 2;
        		break;
    		case "Poznan":
        		cityIndex = 3;
        		break;
    		case "Gdansk":
        		cityIndex = 4;
        		break;
        	default:
        		cityIndex = -1;
        		break;
    	}
    	
        if (cityIndex != -1) {
            List<Object[]> fieldsList = dataTextFieldsList.get(cityIndex);
            //Iterate through all the list and change the values for specific city
            for (String[] measurement : measurements) {
            	//get the measurment type
                String measurementType = measurement[0];
                String measurementValue = measurement[1];
                //change values in the object list and set new text field with specific downloaded value
                for (Object[] field : fieldsList) {
                    if (field[0].equals(measurementType)) {
                        JTextField textField = (JTextField) field[1];
                        textField.setText(measurementType + ": " + measurementValue);
                        break;
                    }
                }
            }
        }
    }
}
