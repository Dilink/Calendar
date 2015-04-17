package fr.dilink.calendar;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class Calendar
{
	static TrayIcon trayIcon;
	static SystemTray tray;
	static ThreadBackground backgroundThread;
	static File datesFile;

	public static void main(String[] args) {
		datesFile = new File(System.getProperty("user.home") + "/dates.txt");
		if(!datesFile.exists()) {
			try {
				datesFile.createNewFile();
				System.out.println("Create file to : "+System.getProperty("user.home"));
			} catch (IOException e) {
				System.err.println("Cannot create dates file !");
				System.exit(-1);
			}
		}
		backgroundThread = new ThreadBackground(datesFile);
		backgroundThread.start();
		refreshDates();
		
		if(SystemTray.isSupported()) {
			tray = SystemTray.getSystemTray();
			Image image = Toolkit.getDefaultToolkit().getImage(Calendar.class.getResource("icon.png"));

			ActionListener exitListener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					System.exit(0);
				}
			};
			ActionListener addDateListener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//Calendar now = Calendar.getInstance();
					//String result = JOptionPane.showInputDialog(null, "Exemple:\n\r"+now.get(Calendar.HOUR_OF_DAY)+":"+now.get(Calendar.MINUTE)+"/"+now.get(Calendar.DAY_OF_MONTH)+":"+(now.get(Calendar.MONTH)+1)+":"+now.get(Calendar.YEAR)+"/Description\n\r", "Ajouter une date", JOptionPane.PLAIN_MESSAGE);
					String result = JOptionPane.showInputDialog(null, "Exemple:\n\rHour:Minutes/Day:Month:Year/Description\n\r", "Ajouter une date", JOptionPane.PLAIN_MESSAGE);
					//String result = JOptionPane.showInputDialog(null, "Exemple:\n\r17:02/19:01:2015/Description\n\r", "Ajouter une date", JOptionPane.PLAIN_MESSAGE);
					if(result != null && !result.equals("")) {
						if(result.contains(":") && result.contains("/")) {
							writeInFile(datesFile, result);
							refreshDates();
						}
					}
				}
			};
			ActionListener refreshListener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					refreshDates();
				}
			};
			ActionListener openDatesListener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(Desktop.isDesktopSupported()) {
						try {
							Desktop desktop = Desktop.getDesktop();
							desktop.open(datesFile);
						} catch (IOException e1) {
							JOptionPane.showMessageDialog(null, "Impossible d'ouvrir le fichier contenant les dates !", "Error !", JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			};
			ActionListener drawDatesListener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					final JFrame frame = new JFrame("Dates :");
					frame.addWindowListener(new WindowAdapter() {
			            @Override
			            public void windowClosing(WindowEvent e) {
			            	frame.setVisible(false);
			            }
			        });
					frame.setSize(400, 400);
					frame.setLocationRelativeTo(null);
					
					JTextArea textArea = new JTextArea();
					
					for (int i = 0; i < backgroundThread.dates.size(); i++) {
						textArea.append(backgroundThread.dates.get(i) + "  -  " + backgroundThread.dates_desc.get(i)[1] + "\n\r");
					}
					/*try {
						FileInputStream fis = new FileInputStream(datesFile);
						@SuppressWarnings("resource")
						BufferedReader br = new BufferedReader(new InputStreamReader(fis));
						String line = null;
						while ((line = br.readLine()) != null) {
							textArea.append(line + "\n\r");
						}
					} catch (IOException e1) {
						e1.printStackTrace();
					}*/
					textArea.setEditable(false);
					
					JScrollPane scrollPane = new JScrollPane(textArea);
					scrollPane.setLocation(0, 0);
					scrollPane.setPreferredSize(new Dimension(frame.getWidth() - 30, frame.getHeight() - 79));// buttons  - 79
					scrollPane.setBorder(null);
					scrollPane.setVisible(true);

					final JPanel panel = new JPanel();
					panel.setLocation(0, 0);
					panel.setBackground(new Color(0xa8a8a8));
					
					JButton refresh_button = new JButton("Refresh");
					refresh_button.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							refreshDates();
							textArea.setText("");
							for (int i = 0; i < backgroundThread.dates.size(); i++) {
								textArea.append(backgroundThread.dates.get(i) + "  -  " + backgroundThread.dates_desc.get(i)[1] + "\n\r");
							}
							textArea.repaint();
						}
					});
					
					panel.add(scrollPane);
					panel.add(refresh_button);
					
					frame.add(panel);
					frame.setVisible(true);
				}
			};

			PopupMenu popup = new PopupMenu();
			MenuItem defaultItem;

			defaultItem = new MenuItem("Ajouter une date");
			defaultItem.addActionListener(addDateListener);
			popup.add(defaultItem);

			defaultItem = new MenuItem("Rafraichir les dates");
			defaultItem.addActionListener(refreshListener);
			popup.add(defaultItem);

			defaultItem = new MenuItem("Afficher les dates");
			defaultItem.addActionListener(drawDatesListener);
			popup.add(defaultItem);

			defaultItem = new MenuItem("Ouvrir le fichier des dates");
			defaultItem.addActionListener(openDatesListener);
			popup.add(defaultItem);

			defaultItem = new MenuItem("Quitter");
			defaultItem.addActionListener(exitListener);
			popup.add(defaultItem);

			trayIcon = new TrayIcon(image, "Calendar", popup);
			trayIcon.setImageAutoSize(true);

			try {
				tray.add(trayIcon);
			} catch (AWTException ex) {
				System.out.println("unable to add to tray");
			}
		}
	}
	public static void refreshDates() {
		try {
			FileInputStream fis = new FileInputStream(datesFile);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			String line = null;
			backgroundThread.dates.clear();
			backgroundThread.dates_desc.clear();
			while ((line = br.readLine()) != null) {
				if(line.contains("/") && line.contains(":")) {
					String[] differents_part = line.split("/");
					String[] differents_time = differents_part[0].split(":");
					String[] differents_date = differents_part[1].split(":");
					int hour = Integer.valueOf(differents_time[0]);
					int minutes = Integer.valueOf(differents_time[1]);
					int day = Integer.valueOf(differents_date[0]);
					int month = Integer.valueOf(differents_date[1]);
					int year = Integer.valueOf(differents_date[2]);
					String desc = differents_part[2];
					backgroundThread.addDate(hour, minutes, day, month, year, desc);
				}
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@SuppressWarnings("deprecation")
	public static void deleteOldDates() {
		Date currentDate = new Date();
		currentDate.setSeconds(0);
		for (int i = 0; i < backgroundThread.dates.size(); i++) {
			if(currentDate.after(backgroundThread.dates.get(i))) {
				backgroundThread.dates.remove(i);

			}
		}
	}
	public static void writeInFile(File inFile, String text) {
		try {
		    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(datesFile, true)));
		    out.println(text);
		    out.close();
			refreshDates();
		} catch (IOException e1) {
			System.out.println("Le fichier des dates n'existe pas !");
			try {
				inFile.createNewFile();
				writeInFile(inFile, text);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public void removeLineFromFile(File inFile, String lineToRemove) {
		try {
			if (!inFile.isFile()) {
				System.out.println("Parameter is not an existing file");
				return;
			}
			//Construct the new file that will later be renamed to the original filename.
			File tempFile = new File(inFile.getAbsolutePath() + ".tmp");
			BufferedReader br = new BufferedReader(new FileReader(inFile));
			PrintWriter pw = new PrintWriter(new FileWriter(tempFile));
			String line = null;
			//Read from the original file and write to the new
			//unless content matches data to be removed.
			while ((line = br.readLine()) != null) {
				if (!line.trim().equals(lineToRemove)) {
					pw.println(line);
					pw.flush();
				}
			}
			pw.close();
			br.close();
			//Delete the original file
			if (!inFile.delete()) {
				System.out.println("Could not delete file");
				return;
			}
			//Rename the new file to the filename the original file had.
			if (!tempFile.renameTo(inFile))
				System.out.println("Could not rename file");
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}