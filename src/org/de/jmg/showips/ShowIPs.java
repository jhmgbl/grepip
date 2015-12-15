package org.de.jmg.showips;
	import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

	public class ShowIPs implements ClipboardOwner, ActionListener
	{
		public static Frame frame;
		public static Button button;
		public static JFileChooser fc = new JFileChooser();
		public static JTable listview;
		public static DefaultTableModel model = new DefaultTableModel(new Object[]{"IP","Host","Type","Log"}, 0);
		public static ActionListener ActionL = new ActionListener() 
		{
			class foundIP
			{
				public foundIP(String ip, String line)
				{
					this.ip = ip;
					this.line = line;
				}
				public String ip;
				public String line;
				@Override
				public String toString()
				{
					return ip;
					
				}
			}
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				  int returnVal = fc.showOpenDialog(button);
				  if (returnVal == JFileChooser.APPROVE_OPTION) {
					  String IPADDRESS_PATTERN = 
			                    "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";
			            String IP6PatternStd = "^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$";
			            String IP6PatternCompr = "^((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)::((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)$";
			            String IP6PatternAlt = "(?<![[:alnum:]]|[[:alnum:]]:)(?:(?:[a-f0-9]{1,4}:){7}[a-f0-9]{1,4}|(?:[a-f0-9]{1,4}:){1,6}:(?:[a-f0-9]{1,4}:){0,5}[a-f0-9]{1,4})(?![[:alnum:]]:?)";
			            Pattern patternip4 = Pattern.compile(IPADDRESS_PATTERN);
			            Pattern patternip6std = Pattern.compile(IP6PatternStd);
			            Pattern patternip6compr = Pattern.compile(IP6PatternCompr);
			            Pattern patternip6alt = Pattern.compile(IP6PatternAlt);
			            File file = fc.getSelectedFile();
			            LinkedHashMap<String,foundIP> ips = new LinkedHashMap<>();
			            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
							    String line;
							    while ((line = br.readLine()) != null) {
							       // process the line.
							    	Matcher matcher = patternip4.matcher(line);
				                    while (matcher.find()) {
				                    	if (!ips.containsKey(matcher.group()))
				                    	{
				                    		ips.put(matcher.group(),new foundIP(matcher.group(),line));
				                    	}
				                    }
				                    matcher = patternip6std.matcher(line);
				                    while (matcher.find()) {
				                    	if (!ips.containsKey(matcher.group()))
				                    	{
				                    		ips.put(matcher.group(),new foundIP(matcher.group(),line));
				                    	}
				                    }
				                    matcher = patternip6compr.matcher(line);
				                    while (matcher.find()) {
				                    	if (!ips.containsKey(matcher.group()))
				                    	{
				                    		ips.put(matcher.group(),new foundIP(matcher.group(),line));
				                    	}
				                    }
				                    matcher = patternip6alt.matcher(line);
				                    while (matcher.find()) {
				                    	if (!ips.containsKey(matcher.group()))
				                    	{
				                    		ips.put(matcher.group(),new foundIP(matcher.group(),line));
				                    	}
				                    }
							    }
						}
						//This is where a real application would open the file.
			            catch (FileNotFoundException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
			            
			            ArrayList<Entry<String, foundIP>> iplist = new ArrayList<>(ips.entrySet());        
			            Collections.sort(iplist, new Comparator<Entry<String, foundIP>>() 
			            {


									@Override
									public int compare(
											Entry<String, foundIP> o1,
											Entry<String, foundIP> o2) {
										// TODO Auto-generated method stub
										return o1.getKey().compareToIgnoreCase(o2.getKey());
									}
						});        
			                    String output = join(iplist, "\n");
			                    StringSelection stringSelection = new StringSelection(output);
			                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			                    clipboard.setContents(stringSelection, new ClipboardOwner() {
									
									@Override
									public void lostOwnership(Clipboard clipboard, Transferable contents) {
										// TODO Auto-generated method stub
										
									}
								});
			                    } 
				  else 
			        {
			        }
			}
			String join(List<Entry<String, foundIP>> list, String conjunction)
			{
			   StringBuilder sb = new StringBuilder();
			   boolean first = true;
			   model.setRowCount(0);
			   for (Entry<String, foundIP>item : list)
			   {
			      if (first)
			         first = false;
			      else
			         sb.append(conjunction);
			      InetAddress addr = null;
			      String line = null;
			      try 
			      {
						addr = InetAddress.getByName(item.getKey());
						frame.setTitle(item.getKey());
						String host = addr.getHostName();
						String type = "extern";
						if (addr.isAnyLocalAddress() || addr.isLinkLocalAddress() || addr.isLoopbackAddress() || addr.isMCLinkLocal() || addr.isMCNodeLocal() || addr.isMCOrgLocal() || addr.isMCSiteLocal() || addr.isSiteLocalAddress())
						{
							type = "local";
						}
						else if(addr.isMulticastAddress())
						{
							type = "multi";
						}
						line = item.getKey() + " " + host + " " + item.getValue().line;
						if (item.getValue().line.contains("DROP"))
						{
							type += " drop";
						}
						
						model.addRow(new Object[]{item.getKey(),host,type,item.getValue().line});	
			      }
			      catch (UnknownHostException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						line = item.getKey() + " " +  " " + item.getValue().line;
						model.addRow(new Object[]{item.getKey(),"invalid","",item.getValue().line});
						
					}
			      if (line != null) sb.append(line);
			   }
			   listview.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
			   listview.doLayout();
			   return sb.toString();
			}

		};
		public static WindowListener WinLi = new WindowListener() {
			
			@Override
			public void windowOpened(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowIconified(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowDeiconified(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowDeactivated(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowClosing(WindowEvent e) {
				// TODO Auto-generated method stub
				System.exit(0);
			}
			
			@Override
			public void windowClosed(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowActivated(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
		};

 
	  public static void main(String[] args) 
	  {
		//Create a file chooser
		  // Create frame with specific title
		  frame = new Frame("ShowIPs");
		  frame.setLayout(new BorderLayout());
		  // Create a component to add to the frame; in this case a text area with sample text
		  button = new Button("Click Me!!");
		  button.addActionListener(ActionL);
		  listview = new JTable(model);
		  listview.getColumnModel().getColumn(0).setCellRenderer(new StatusColumnCellRenderer());
		  JScrollPane pane = new JScrollPane(listview);
		  frame.add(pane, BorderLayout.CENTER);
		  frame.add(button, BorderLayout.SOUTH);
		  
		  int width = 300;
		  int height = 300;
		  frame.setSize(width, height);
		  frame.addWindowListener(WinLi);
		  frame.setVisible(true);
		  listview.addMouseListener(new java.awt.event.MouseAdapter() {
			    @Override
			    public void mouseClicked(java.awt.event.MouseEvent evt) {
			        int row = listview.rowAtPoint(evt.getPoint());
			        int col = listview.columnAtPoint(evt.getPoint());
			        DefaultTableModel tableModel = (DefaultTableModel) listview.getModel();
				    
			        String ip = (String) tableModel.getValueAt(row,0);
			        
			        if (ip != null) 
			        {
			        	try 
			        	{
			        		JTextArea textArea = new JTextArea(30, 75);
			        	    textArea.setText(shell("whois " + ip));
			        	    textArea.setEditable(false);
			        	       
			        	      // wrap a scrollpane around it
			        	    JScrollPane scrollPane = new JScrollPane(textArea);
			        		JOptionPane.showMessageDialog(listview, scrollPane);
						} catch (HeadlessException | IOException
								| InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
			        }
			    }
			});
	  }

	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	
	public static String shell(String cmd) throws IOException, InterruptedException
	{
		Process p = Runtime.getRuntime().exec(cmd);
	    p.waitFor();

	    BufferedReader reader = 
	         new BufferedReader(new InputStreamReader(p.getInputStream()));

	    String line = "";
	    StringBuilder sb = new StringBuilder();
	    while ((line = reader.readLine())!= null) {
		sb.append(line + "\n");
	    }
	    return sb.toString();

	}
	}