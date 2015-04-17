package fr.dilink.calendar;

import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MINUTE;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import fr.dilink.notificationAPI.Notification;

public class ThreadBackground extends Thread implements Runnable
{
	private java.util.Calendar calendar;
	ArrayList<Date> dates;
	ArrayList<String[]> dates_desc;
	//private File datesFile;

	public ThreadBackground(File datesFileI) {
		this.calendar = java.util.Calendar.getInstance();
		//this.datesFile = datesFileI;
		dates = new ArrayList<Date>();
		dates_desc = new ArrayList<String[]>();
		//this.addDate(16, 43, 19, 01, 2015, "Description 19");
	}
	@SuppressWarnings("deprecation")
	public void addDate(int hour, int minutes, int day, int month, int year,  String desc) {
		Date te = new Date(year-1900, month-1, day, hour, minutes);
		if(!dates.contains(te) && (!dates_desc.contains(new String[] { "Calendar", desc }))) {
			dates.add(te);
			dates_desc.add(new String[] { "Calendar", desc });
		}
	}
	@SuppressWarnings("deprecation")
	@Override
	public void run() {
		//System.out.println(dates.get(0).compareTo(date)); //Check if != 0 (0 = True)
		while (true) {
			calendar.setTime(new Date());
			for(int i = 0; i < dates.size(); i++) {
				//System.out.println(dates.get(i));
				if((calendar.get(YEAR) == (dates.get(i).getYear()+1900)) && (calendar.get(MONTH) + 1 == (dates.get(i).getMonth()+1)) && (calendar.get(DAY_OF_MONTH) == dates.get(i).getDate()) && (calendar.get(HOUR_OF_DAY)) == dates.get(i).getHours() && (calendar.get(MINUTE) == dates.get(i).getMinutes())) {					
					System.out.println(dates.get(i) + " - It's TIME !");
					Notification notif = new Notification(dates_desc.get(i)[0], dates_desc.get(i)[1]);
					notif.setSize(300, 100);
					notif.setIcon(Calendar.class, "icon.png");
					notif.setSoundAlert(Calendar.class, "bell.wav");
					notif.draw();
				}
				/*if(dates.get(i).before(calendar.getTime())) {
					dates.remove(i);
				}*/
			}
			try {Thread.sleep(60000);} catch (InterruptedException e) {}
		}
	}
}