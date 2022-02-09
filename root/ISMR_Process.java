package root;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import convenience_classes.SubstringBetween;
import convenience_classes.TextAreaReadMe;

public class ISMR_Process {
	String date;
	String national_prepareness_level;
	String initial_attack_activity;
	String initial_attack_activity_number;
	String new_large_incidents;
	String large_fires_contained;
	String uncontained_large_fires;
	String area_command_teams_committed;
	String NIMOs_committed;
	String type_1_IMTs_committed;
	String type_2_IMTs_committed;
	List<String> national_fire_activity = new ArrayList<String>();	// store all the above information
	
	List<String> all_fires = new ArrayList<String>();	// All fires with priority order as in the ISMR file
	List<String> AICC = new ArrayList<String>();	// Alaska
	List<String> EACC = new ArrayList<String>();	// Eastern
	List<String> GBCC = new ArrayList<String>();	// Great Basin
	List<String> ONCC = new ArrayList<String>();	// Northern California
	List<String> NRCC = new ArrayList<String>();	// Northern Rockies
	List<String> NWCC = new ArrayList<String>();	// Northwest
	List<String> RMCC = new ArrayList<String>();	// Rocky Mountain
	List<String> SACC = new ArrayList<String>();	// Southern Area
	List<String> OSCC = new ArrayList<String>();	// Southern California
	List<String> SWCC = new ArrayList<String>();	// Southwest
	
	public ISMR_Process(File file) {
		try {
			// Read all lines to list and array
//			List<String> lines_list = Files.readAllLines(Paths.get(file.getAbsolutePath()), StandardCharsets.UTF_8);		// Not sure why this UTF_8 fail
			List<String> lines_list = Files.readAllLines(Paths.get(file.getAbsolutePath()), Charset.defaultCharset());		// Therefore I use default
			String[] lines = lines_list.stream().toArray(String[] ::new);
			date = file.getName().substring(0, 8);
			process_data_method_1(lines);
//			process_data_method_2(lines, textarea);
			lines_list = null; 	// free memory
			lines = null;		// free memory
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
		}
	}
	
	
	private List<String> area_fires(String area_name) {
		if (area_name.equals("AICC")) {
			return AICC;
		} else if (area_name.equals("EACC")) {
			return EACC;
		} else if (area_name.equals("GBCC")) {
			return GBCC;
		} else if (area_name.equals("ONCC")) {
			return ONCC;
		} else if (area_name.equals("NRCC")) {
			return NRCC;
		} else if (area_name.equals("NWCC")) {
			return NWCC;
		} else if (area_name.equals("RMCC")) {
			return RMCC;
		} else if (area_name.equals("SACC")) {
			return SACC;
		} else if (area_name.equals("OSCC")) {
			return OSCC;
		} else if (area_name.equals("SWCC")) {
			return SWCC;
		} else {
			return null;
		}
	}
	
	private void process_data_method_1(String[] lines) {
		int mergeCount = 0;
		for (int i = 0; i < lines.length; i++) {
			lines[i] = lines[i].replaceAll("\\s{2,}", " ").trim(); // 2 or more spaces will be replaced by one space, then leading and ending spaces will be removed
			if (lines[i].contains("Active Incident Resource Summary")) {
				for (int j = 0; j < i; j++) {
					if (lines[j].contains("Type 2 IMTs")) {
						mergeCount = j + 1;		// Merge up to after this line
					}
				}
			}
		}
		String[] merge_lines = Arrays.copyOfRange(lines, 0, mergeCount + 1);
		String mstr = String.join(" ", merge_lines).toLowerCase().trim();
		
		SubstringBetween sb = new SubstringBetween();
		String temp = sb.substringBetween(mstr, "national preparedness level", "national fire activity"); 
		if (temp != null) national_prepareness_level = (temp.substring(temp.indexOf(" ") + 1)).trim();	// remove all characters (such as :) before the first space and then trim
		temp = sb.substringBetween(mstr, "initial attack activity", "new large incidents"); 
		if (temp == null) temp = sb.substringBetween(mstr, "initial activity", "new large incidents"); 
		temp = (temp.substring(temp.indexOf(" ") + 1)).trim();	// remove all characters (such as :) before the first space and then trim
		if (temp.matches("-?(0|[1-9]\\d*)")) {		// this special case happens in several instance	i.e. 20180813, 20170331			use Regex to check if this is just a number
			initial_attack_activity_number = temp;
			int num = Integer.valueOf(temp);
			if (num <= 199) {
				initial_attack_activity = "Light";
			} else if (num >= 200 && num <= 299) {
				initial_attack_activity = "Moderate";
			} else if (num >= 300) {
				initial_attack_activity = "Heavy";
			}
		} else {
			initial_attack_activity = temp.split(" ")[0].toUpperCase();
			if (initial_attack_activity.length() > 1) initial_attack_activity = initial_attack_activity.substring(0, 1) + initial_attack_activity.substring(1).toLowerCase();
			if (temp.split(" ").length > 1) {
				initial_attack_activity_number = (temp.substring(temp.lastIndexOf("(") + 1, temp.lastIndexOf(")")).replaceAll("new", "").replaceAll("fire", "").replaceAll("s", "")).trim();		// i.e. 20180915 is a special case
			} else {
				initial_attack_activity_number = "";
			}
		}
		
//		initial_attack_activity = initial_attack_activity.substring(0, 1).toUpperCase() + initial_attack_activity.substring(1);
//		temp = (mstr.substring(mstr.indexOf("new large incidents") + 20)).trim();
//		if (temp != null) new_large_incidents = temp.split(" ")[0];
//		temp = (mstr.substring(mstr.indexOf("large fires contained") + 22)).trim();
//		if (temp != null) large_fires_contained = temp.split(" ")[0];
//		temp = (mstr.substring(mstr.indexOf("uncontained large fires") + 24)).trim();
//		if (temp != null) uncontained_large_fires = temp.split(" ")[0];
//		temp = (mstr.substring(mstr.indexOf("area command teams committed") + 29)).trim();		// special case with null value i.e. 20170922  (area command teams committed does not exist)
//		if (temp != null) area_command_teams_committed = temp.split(" ")[0];
//		temp = (mstr.substring(mstr.indexOf("nimos committed") + 24)).trim();
//		if (temp != null) NIMOs_committed = temp.split(" ")[0];
//		temp = (mstr.substring(mstr.indexOf("type 1 imts committed") + 24)).trim();
//		if (temp != null) type_1_IMTs_committed = temp.split(" ")[0];
//		temp = (mstr.substring(mstr.indexOf("type 2 imts committed") + 22)).trim();
//		if (temp != null) type_2_IMTs_committed = temp.split(" ")[0];
		
		temp = sb.substringBetween(mstr, "new large incidents", "large fires contained"); 
		if (temp != null) new_large_incidents = (temp.substring(temp.indexOf(" ") + 1)).trim();
		temp = sb.substringBetween(mstr, "large fires contained", "uncontained large fires");
		if (temp != null) large_fires_contained = (temp.substring(temp.indexOf(" ") + 1)).trim();
		temp = sb.substringBetween(mstr, "uncontained large fires", "area command teams committed");		// fail because of null 	i.e. 20170922	(area command teams committed does not exist)
		if (temp != null) uncontained_large_fires = (temp.substring(temp.indexOf(" ") + 1)).trim();
		temp = sb.substringBetween(mstr, "area command teams committed", "nimos committed");
		if (temp != null) area_command_teams_committed = (temp.substring(temp.indexOf(" ") + 1)).trim();
		temp = sb.substringBetween(mstr, "nimos committed", "type 1 imts committed");
		if (temp != null) NIMOs_committed = (temp.substring(temp.indexOf(" ") + 1)).trim();
		temp = sb.substringBetween(mstr, "type 1 imts committed", "type 2 imts committed");
		if (temp != null) type_1_IMTs_committed = (temp.substring(temp.indexOf(" ") + 1)).trim();
		temp = (mstr.substring(mstr.indexOf("type 2 imts committed") + 22)).trim();
		if (temp != null) type_2_IMTs_committed = temp.split(" ")[0];
		
		// Check either of the 2 lines right after "Active Incident Resource Summary" to see if information of each fire will be in a single line (i.e. 20180803) or multiple lines
		int lineCount = 0;
		for (int i = 0; i < lines.length; i++) {
			if (lines[i].contains("Active Incident Resource Summary")) {
				lineCount = i + 1;
			}
		}
		if (lines[lineCount].split(" ").length > 1 || lines[lineCount + 1].split(" ").length > 1) {	// either line will have at least 2 words
			get_data_type_single_line(lines);
		} else {	// both lines have 1 or 0 word
			get_data_type_multiple_lines(lines);
		}

		// IMPORTANT NOTE NOTE NOTE: 20190902-03-04 ... adobe acrobat failed to convert tables (tables are not recognized and not included in text files)
	}
	
	private void process_data_method_2(String[] lines, TextAreaReadMe textarea) {	// unused method because method 1 is better
		for (int i = 0; i < lines.length; i++) {
			lines[i] = lines[i].replaceAll("\\s{2,}", " ").trim(); // 2 or more spaces will be replaced by one space, then leading and ending spaces will be removed
		}
		
		// The Format 1 of result when using adobe acrobat to convert from "pdf" to "text"		most data in the first 6 months		i.e. 20170106		(Note special case 20170203, I delete a row manually)
		if (lines[4].contains("National Preparedness Level ")) {
			textarea.append("Format 1. 20170106" + "\n");
			national_prepareness_level = lines[4].substring(lines[4].lastIndexOf("National Preparedness Level ") + 28);
			if (lines[7].matches("-?(0|[1-9]\\d*)")) {		// this special case happens in several instance	i.e. 20180813, 20170331			use regex to check if this is just a number
				initial_attack_activity_number = lines[7];
				int num = Integer.valueOf(lines[7]);
				if (num <= 199) {
					initial_attack_activity = "Light";
				} else if (num >= 200 && num <= 299) {
					initial_attack_activity = "Moderate";
				} else if (num >= 300) {
					initial_attack_activity = "Heavy";
				}  
			} else {
				initial_attack_activity = lines[7].split(" ")[0];
				if (lines[7].split(" ").length > 1) {
					initial_attack_activity_number = (lines[7].substring(lines[7].lastIndexOf("(") + 1, lines[7].lastIndexOf(")")).replace("new", "").replace("fire", "").replace("s", "")).trim();		// i.e. 20180915 is a special case
				} else {
					initial_attack_activity_number = "";
				}
			}
			new_large_incidents = lines[10];
			large_fires_contained = lines[13];
			uncontained_large_fires = lines[16];
			area_command_teams_committed = lines[19];
			NIMOs_committed = lines[22];
			type_1_IMTs_committed = lines[25];
			type_2_IMTs_committed = lines[28];
			get_data_type_multiple_lines(lines);
		}
		
		// The Format 2 type of result when using adobe acrobat to convert from "pdf" to "text"		i.e. 20190329
		if (lines[0].contains("National Preparedness Level ") && lines[5].contains("Initial Attack Activity")) {
			textarea.append("Format 2. 20190329" + "\n");
			national_prepareness_level = lines[0].substring(lines[0].lastIndexOf("National Preparedness Level ") + 28);
			if (lines[6].matches("-?(0|[1-9]\\d*)")) {		// this special case happens in several instance	i.e. 20180813, 20170331			use regex to check if this is just a number
				initial_attack_activity_number = lines[6];
				int num = Integer.valueOf(lines[6]);
				if (num <= 199) {
					initial_attack_activity = "Light";
				} else if (num >= 200 && num <= 299) {
					initial_attack_activity = "Moderate";
				} else if (num >= 300) {
					initial_attack_activity = "Heavy";
				}  
			} else {
				initial_attack_activity = lines[6].split(" ")[0];
				if (lines[6].split(" ").length > 1) {
					initial_attack_activity_number = (lines[6].substring(lines[6].lastIndexOf("(") + 1, lines[6].lastIndexOf(")")).replace("new", "").replace("fire", "").replace("s", "")).trim();		// i.e. 20180915 is a special case
				} else {
					initial_attack_activity_number = "";
				}
			}
			new_large_incidents = lines[9];
			large_fires_contained = lines[12];
			uncontained_large_fires = lines[15];
			area_command_teams_committed = lines[18];
			NIMOs_committed = lines[21];
			type_1_IMTs_committed = lines[24];
			type_2_IMTs_committed = lines[27];
			get_data_type_multiple_lines(lines);
		}
					
		// The Format 3 of result when using adobe acrobat to convert from "pdf" to "text"
		if (lines[0].contains("National Preparedness Level ") && lines[3].contains("Initial Attack Activity")) {
			textarea.append("Format 3. 20180813" + "\n");
			national_prepareness_level = lines[0].substring(lines[0].lastIndexOf("National Preparedness Level ") + 28);
			if (lines[4].matches("-?(0|[1-9]\\d*)")) {		// this special case happens in several instance	i.e. 20180813, 20170331			use regex to check if this is just a number
				initial_attack_activity_number = lines[4];
				int num = Integer.valueOf(lines[4]);
				if (num <= 199) {
					initial_attack_activity = "Light";
				} else if (num >= 200 && num <= 299) {
					initial_attack_activity = "Moderate";
				} else if (num >= 300) {
					initial_attack_activity = "Heavy";
				}  
			} else {
				initial_attack_activity = lines[4].split(" ")[0];
				if (lines[4].split(" ").length > 1) {
					initial_attack_activity_number = (lines[4].substring(lines[4].lastIndexOf("(") + 1, lines[4].lastIndexOf(")")).replace("new", "").replace("fire", "").replace("s", "")).trim();		// i.e. 20180915 is a special case
				} else {
					initial_attack_activity_number = "";
				}
			}
			new_large_incidents = lines[7];
			large_fires_contained = lines[10];
			uncontained_large_fires = lines[13];
			area_command_teams_committed = lines[16];
			NIMOs_committed = lines[19];
			type_1_IMTs_committed = lines[22];
			type_2_IMTs_committed = lines[25];
			get_data_type_multiple_lines(lines);
		}
		
		
		// The Format 4 of result when using adobe acrobat to convert from "pdf" to "text"
		if (lines[3].contains("National Preparedness Level ") && lines[8].startsWith("New large incidents") && lines[11].startsWith("Large fires contained")) {
			textarea.append("Format 4. 20170524" + "\n");
			national_prepareness_level = lines[3].substring(lines[3].lastIndexOf("National Preparedness Level ") + 28);
			if (lines[6].matches("-?(0|[1-9]\\d*)")) {		// this special case happens in several instance	i.e. 20180813, 20170331			use regex to check if this is just a number
				initial_attack_activity_number = lines[6];
				int num = Integer.valueOf(lines[6]);
				if (num <= 199) {
					initial_attack_activity = "Light";
				} else if (num >= 200 && num <= 299) {
					initial_attack_activity = "Moderate";
				} else if (num >= 300) {
					initial_attack_activity = "Heavy";
				}  
			} else {
				initial_attack_activity = lines[6].split(" ")[0];
				if (lines[6].split(" ").length > 1) {
					initial_attack_activity_number = (lines[6].substring(lines[6].lastIndexOf("(") + 1, lines[6].lastIndexOf(")")).replace("new", "").replace("fire", "").replace("s", "")).trim();		// i.e. 20180915 is a special case
				} else {
					initial_attack_activity_number = "";
				}
			}
			new_large_incidents = lines[9];
			large_fires_contained = lines[12];
			uncontained_large_fires = lines[15];
			area_command_teams_committed = lines[18];
			NIMOs_committed = lines[21];
			type_1_IMTs_committed = lines[24];
			type_2_IMTs_committed = lines[27];
			get_data_type_multiple_lines(lines);
		}
		
		// The Format 5 of result when using adobe acrobat to convert from "pdf" to "text"		i.e. 20190517
		if (lines[3].contains("National Preparedness Level ") && lines[4].isEmpty() && lines[5].isEmpty() && lines[6].isEmpty() && lines[9].startsWith("New large incidents")) {
			textarea.append("Format 5. 20190517" + "\n");
			national_prepareness_level = lines[3].substring(lines[3].lastIndexOf("National Preparedness Level ") + 28);
			String st = (lines[8].substring(lines[8].indexOf(" ") + 1)).trim();		// Note this should be location of first space + 1
			if (st.matches("-?(0|[1-9]\\d*)")) {		// this special case happens in several instance	i.e. 20180813, 20170331			use regex to check if this is just a number
				initial_attack_activity_number = st;
				int num = Integer.valueOf(st);
				if (num <= 199) {
					initial_attack_activity = "Light";
				} else if (num >= 200 && num <= 299) {
					initial_attack_activity = "Moderate";
				} else if (num >= 300) {
					initial_attack_activity = "Heavy";
				}  
			} else {
				initial_attack_activity = st.split(" ")[0];
				if (st.split(" ").length > 1) {
					initial_attack_activity_number = (st.substring(st.lastIndexOf("(") + 1, st.lastIndexOf(")")).replace("new", "").replace("fire", "").replace("s", "")).trim();
				} else {
					initial_attack_activity_number = "";
				}
			}
			new_large_incidents = (lines[9].substring(lines[9].lastIndexOf(" ") + 1)).trim();
			large_fires_contained = (lines[10].substring(lines[10].lastIndexOf(" ") + 1)).trim();
			uncontained_large_fires = (lines[11].substring(lines[11].lastIndexOf(" ") + 1)).trim();;
			area_command_teams_committed = (lines[12].substring(lines[12].lastIndexOf(" ") + 1)).trim();
			NIMOs_committed = (lines[13].substring(lines[13].lastIndexOf(" ") + 1)).trim();
			type_1_IMTs_committed = (lines[14].substring(lines[14].lastIndexOf(" ") + 1)).trim();
			type_2_IMTs_committed = (lines[15].substring(lines[15].lastIndexOf(" ") + 1)).trim();
			get_data_type_multiple_lines(lines);
		}
		
		// The Format 6 of result when using adobe acrobat to convert from "pdf" to "text"		i.e. 20190921
		if (lines[3].contains("National Preparedness Level ") && lines[4].isEmpty() && lines[5].isEmpty() && lines[6].isEmpty() && lines[11].startsWith("New large incidents")) {
			textarea.append("Format 6. 20190921" + "\n");
			national_prepareness_level = lines[3].substring(lines[3].lastIndexOf("National Preparedness Level ") + 28);
			if (lines[9].matches("-?(0|[1-9]\\d*)")) {		// this special case happens in several instance	i.e. 20180813, 20170331			use regex to check if this is just a number
				initial_attack_activity_number = lines[9];
				int num = Integer.valueOf(lines[9]);
				if (num <= 199) {
					initial_attack_activity = "Light";
				} else if (num >= 200 && num <= 299) {
					initial_attack_activity = "Moderate";
				} else if (num >= 300) {
					initial_attack_activity = "Heavy";
				}  
			} else {
				initial_attack_activity = lines[9].split(" ")[0];
				if (lines[9].split(" ").length > 1) {
					initial_attack_activity_number = (lines[9].substring(lines[9].lastIndexOf("(") + 1, lines[9].lastIndexOf(")")).replace("new", "").replace("fire", "").replace("s", "")).trim();
				} else {
					initial_attack_activity_number = "";
				}
			}
			new_large_incidents = lines[12];
			large_fires_contained = lines[15];
			uncontained_large_fires = lines[18];
			area_command_teams_committed = lines[21];
			NIMOs_committed = lines[24];
			type_1_IMTs_committed = lines[27];
			type_2_IMTs_committed = lines[30];
			get_data_type_multiple_lines(lines);
		}
		
		// The Format 7 of result when using adobe acrobat to convert from "pdf" to "text"		i.e. 20170914			(currently wrong in 20170922 because area command team does not exist in this file)
		if (lines[3].contains("National Preparedness Level ") && lines[8].isEmpty()) {
			textarea.append("Format 7. 20170914" + "\n");
			national_prepareness_level = lines[3].substring(lines[3].lastIndexOf("National Preparedness Level ") + 28);
			if (lines[7].matches("-?(0|[1-9]\\d*)")) {		// this special case happens in several instance	i.e. 20180813, 20170331			use regex to check if this is just a number
				initial_attack_activity_number = lines[7];
				int num = Integer.valueOf(lines[7]);
				if (num <= 199) {
					initial_attack_activity = "Light";
				} else if (num >= 200 && num <= 299) {
					initial_attack_activity = "Moderate";
				} else if (num >= 300) {
					initial_attack_activity = "Heavy";
				}  
			} else {
				initial_attack_activity = lines[7].split(" ")[0];
				if (lines[7].split(" ").length > 1) {
					initial_attack_activity_number = (lines[7].substring(lines[7].lastIndexOf("(") + 1, lines[7].lastIndexOf(")")).replace("new", "").replace("fire", "").replace("s", "")).trim();
				} else {
					initial_attack_activity_number = "";
				}
			}
			new_large_incidents = lines[10];
			large_fires_contained = lines[13];
			uncontained_large_fires = lines[16];
			area_command_teams_committed = lines[19];
			NIMOs_committed = lines[22];
			type_1_IMTs_committed = lines[25];
			type_2_IMTs_committed = lines[28];
			get_data_type_multiple_lines(lines);
		}
		
		// The Format 8 of result when using adobe acrobat to convert from "pdf" to "text"		i.e. 20180510	20180511	20180512
		if (lines[3].contains("National Preparedness Level ") && lines[9].isEmpty()) {
			textarea.append("Format 8. 20180510, 20180511, 20180512" + "\n");
			national_prepareness_level = lines[3].substring(lines[3].lastIndexOf("National Preparedness Level ") + 28);
			if (lines[8].matches("-?(0|[1-9]\\d*)")) {		// this special case happens in several instance	i.e. 20180813, 20170331			use regex to check if this is just a number
				initial_attack_activity_number = lines[8];
				int num = Integer.valueOf(lines[8]);
				if (num <= 199) {
					initial_attack_activity = "Light";
				} else if (num >= 200 && num <= 299) {
					initial_attack_activity = "Moderate";
				} else if (num >= 300) {
					initial_attack_activity = "Heavy";
				}  
			} else {
				initial_attack_activity = lines[8].split(" ")[0];
				if (lines[8].split(" ").length > 1) {
					initial_attack_activity_number = (lines[8].substring(lines[8].lastIndexOf("(") + 1, lines[8].lastIndexOf(")")).replace("new", "").replace("fire", "").replace("s", "")).trim();
				} else {
					initial_attack_activity_number = "";
				}
			}
			new_large_incidents = lines[11];
			large_fires_contained = lines[14];
			uncontained_large_fires = lines[17];
			area_command_teams_committed = lines[20];
			NIMOs_committed = lines[23];
			type_1_IMTs_committed = lines[26];
			type_2_IMTs_committed = lines[29];
			get_data_type_multiple_lines(lines);
		}
					
		// The Format 9 of result when using adobe acrobat to convert from "pdf" to "text"			i.e. 20180813
		if (lines[3].contains("National Preparedness Level ") && lines[8].startsWith("Large fires contained")) {
			textarea.append("Format 9. " + "\n");
			national_prepareness_level = lines[3].substring(lines[3].lastIndexOf("National Preparedness Level ") + 28);
			if (lines[6].matches("-?(0|[1-9]\\d*)")) {		// this special case happens in several instance	i.e. 20180813, 20170331			use regex to check if this is just a number
				initial_attack_activity_number = lines[6];
				int num = Integer.valueOf(lines[6]);
				if (num <= 199) {
					initial_attack_activity = "Light";
				} else if (num >= 200 && num <= 299) {
					initial_attack_activity = "Moderate";
				} else if (num >= 300) {
					initial_attack_activity = "Heavy";
				}  
			} else {
				initial_attack_activity = lines[6].split(" ")[3];
				if (lines[6].split(" ").length > 1) {
					initial_attack_activity_number = (lines[6].substring(lines[6].lastIndexOf("(") + 1, lines[6].lastIndexOf(")")).replace("new", "").replace("fire", "").replace("s", "")).trim();
				} else {
					initial_attack_activity_number = "";
				}
			}
			new_large_incidents = lines[7].substring(lines[7].lastIndexOf(" ") + 1);
			large_fires_contained = lines[8].substring(lines[8].lastIndexOf(" ") + 1);
			uncontained_large_fires = lines[9].substring(lines[9].lastIndexOf(" ") + 1);
			area_command_teams_committed = lines[10].substring(lines[10].lastIndexOf(" ") + 1);
			NIMOs_committed = lines[11].substring(lines[11].lastIndexOf(" ") + 1);
			type_1_IMTs_committed = lines[12].substring(lines[12].lastIndexOf(" ") + 1);
			type_2_IMTs_committed = lines[13].substring(lines[13].lastIndexOf(" ") + 1);
			get_data_type_single_line(lines);
		}
	}
	

	private void get_data_type_multiple_lines(String[] lines) {		// Information of a Fire is in 15 lines
		// Loop all lines
		String current_area = "";
		int count = 0;
		for (int i = 0; i < lines.length; i++) {
			if (lines[i].contains("(PL")) {
				if (lines[i].startsWith("Alaska")) {
					current_area = "AICC";
				} else if (lines[i].startsWith("Eastern")) {
					current_area = "EACC";
				} else if (lines[i].startsWith("Great Basin")) {
					current_area = "GBCC";
				} else if (lines[i].startsWith("Northern California")) {
					current_area = "ONCC";
				} else if (lines[i].startsWith("Northern Rockies")) {
					current_area = "NRCC";
				} else if (lines[i].startsWith("Northwest")) {
					current_area = "NWCC";
				} else if (lines[i].startsWith("Rocky Mountain")) {
					current_area = "RMCC";
				} else if (lines[i].startsWith("Southern Area")) {
					current_area = "SACC";
				} else if (lines[i].startsWith("Southern California")) {
					current_area = "OSCC";
				} else if (lines[i].startsWith("Southwest")) {
					current_area = "SWCC";
				}
			}
			
			if (lines[i].isEmpty() && count == 15 && lines[i - 14].toUpperCase().equals(lines[i - 14]) && lines[i - 14].contains("-")) {		// this is likely a fire, smart check based on the "unit" column
				String priority = String.valueOf(area_fires(current_area).size() + 1);
				String this_fire = String.join("\t", date, current_area, priority, lines[i - 15], lines[i - 14], lines[i - 13], lines[i - 12], lines[i - 11],
														lines[i - 10], lines[i - 9], lines[i - 8], lines[i - 7], lines[i - 6],
														lines[i - 5], lines[i - 4], lines[i - 3], lines[i - 2], lines[i - 1]);
				all_fires.add(this_fire);
				area_fires(current_area).add(this_fire);
			}
			count = (lines[i].isEmpty()) ? 0 : (count + 1);	// increase count by 1 if not empty line
		}
	}
	
	private void get_data_type_single_line(String[] lines) {		// Information of a Fire is in one line		(Note: a special case: 20180803 at page 10 where the table without header if expanding 2 pages) 
		// Loop all lines
		String current_area = "";
		int count = 0;
		do {
			if (lines[count].contains("(PL")) {
				if (lines[count].startsWith("Alaska")) {
					current_area = "AICC";
				} else if (lines[count].startsWith("Eastern")) {
					current_area = "EACC";
				} else if (lines[count].startsWith("Great Basin")) {
					current_area = "GBCC";
				} else if (lines[count].startsWith("Northern California")) {
					current_area = "ONCC";
				} else if (lines[count].startsWith("Northern Rockies")) {
					current_area = "NRCC";
				} else if (lines[count].startsWith("Northwest")) {
					current_area = "NWCC";
				} else if (lines[count].startsWith("Rocky Mountain")) {
					current_area = "RMCC";
				} else if (lines[count].startsWith("Southern Area")) {
					current_area = "SACC";
				} else if (lines[count].startsWith("Southern California")) {
					current_area = "OSCC";
				} else if (lines[count].startsWith("Southwest")) {
					current_area = "SWCC";
				}
			}
			
			String[] line_split = lines[count].split(" ");
			int unit_id = 0;	// find the second column of the table
			int line_length = line_split.length;
			if (line_length >= 15 && line_split[line_length - 14].toUpperCase().equals(line_split[line_length - 14]) && line_split[line_length - 14].contains("-")) {		// this is likely a fire, smart check based on the "unit" column
				unit_id = line_split.length - 14;
				
				String priority = String.valueOf(area_fires(current_area).size() + 1);
				String this_fire = String.join("\t", date, current_area, priority);
				// this is the incident name, join by space
				for (int id = 0; id < unit_id; id++) {
					this_fire = String.join(" ", this_fire, line_split[id]);
				}
				// this is information in the whole line of this fire
				for (int id = unit_id; id < line_split.length; id++) {
					this_fire = String.join("\t", this_fire, line_split[id]);
				}
				all_fires.add(this_fire);
				area_fires(current_area).add(this_fire);
			}
			count = count + 1;
		} while (count < lines.length);
	}
	
}
