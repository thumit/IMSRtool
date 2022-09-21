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

public class ISMR_Process {
	String date;
	String national_prepareness_level;
	String initial_attack_activity;
	String initial_attack_new_fires;
	String new_large_incidents;
	String large_fires_contained;
	String uncontained_large_fires;
	String area_command_teams_committed;
	String nimos_committed;
	String type_1_imts_committed;
	String type_2_imts_committed;
	List<String> national_fire_activity = new ArrayList<String>();	// store all the above information
	
//	String gacc_prepareness_level;
//	String gacc_new_fires;
//	String gacc_new_large_incidents;
//	String gacc_uncontained_large_fires;
//	String gacc_area_command_teams_committed;
//	String gacc_NIMOs_committed;
//	String gacc_type_1_IMTs_committed;
//	String gacc_type_2_IMTs_committed;
	List<String> gacc_fire_activity = new ArrayList<String>();	// almost the same information structure as national activity, but the level is gacc area
	
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
//			List<String> lines_list = Files.readAllLines(Paths.get(file.getAbsolutePath()), StandardCharsets.UTF_8);		// Not sure why this UTF_8 fail
			List<String> lines_list = Files.readAllLines(Paths.get(file.getAbsolutePath()), Charset.defaultCharset());		// Therefore I use default
			String[] lines = lines_list.stream().toArray(String[] ::new);
//			date = file.getName().substring(0, 8);
			date = String.join("-", file.getName().substring(0, 4), file.getName().substring(4, 6), file.getName().substring(6, 8));	// use this data format yyyy-mm-dd to join easily with Ross data
			get_national_data(lines);
			get_area_data(lines);
			get_fire_data(lines);
			lines_list = null; 	// free memory
			lines = null;		// free memory
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
		}
	}
	
	private void get_national_data(String[] lines) {
		int mergeCount = 0;
		for (int i = 0; i < lines.length; i++) {
			lines[i] = lines[i].replaceAll("\\s{2,}", " ").trim(); // 2 or more spaces will be replaced by one space, then leading and ending spaces will be removed
			if (lines[i].contains("Active Incident Resource Summary") || lines[i].contains("GACC")) {		// Special case 20200110: "Active Incident Resource Summary" is not written correctly --> Use GACC
				for (int j = 0; j < i; j++) {
					if (lines[j].contains("Type 2 IMTs")) {
						// Merge up to this mergeline. Some special cases are j+3 or j+2, usually j+1 or j in most cases)
						if (lines[j].substring(lines[j].lastIndexOf(" ") + 1).matches("-?(0|[1-9]\\d*)")) {
							mergeCount = j;			// many cases
						} else if (lines[j + 1].substring(lines[j + 1].lastIndexOf(" ") + 1).matches("-?(0|[1-9]\\d*)")) {
							mergeCount = j + 1;		// many cases
						} else if (lines[j + 2].substring(lines[j + 2].lastIndexOf(" ") + 1).matches("-?(0|[1-9]\\d*)")) {
							mergeCount = j + 2;		// 20200110
						} else if (lines[j + 3].substring(lines[j + 3].lastIndexOf(" ") + 1).matches("-?(0|[1-9]\\d*)")) {
							mergeCount = j + 3;		// 20200518
						}
					}
				}
			}
		}
		String[] merge_lines = Arrays.copyOfRange(lines, 0, mergeCount + 1);
		String mstr = String.join(" ", merge_lines).toLowerCase().trim();
		SubstringBetween sb = new SubstringBetween();
		
		String st = "national preparedness level";
		String temp = sb.substringBetween(mstr, st, get_national_next_term(mstr, st)); 
		if (temp != null) national_prepareness_level = (temp.substring(temp.indexOf(" ") + 1)).trim();	// remove all characters (such as :) before the first space and then trim
		if (temp != null) national_prepareness_level = national_prepareness_level.split(" ")[0];	// Fix the case in 2016 data i.e. 20160429,20160506, etc
		st = "initial attack activity";
		temp = sb.substringBetween(mstr, st, get_national_next_term(mstr, st));
		if (temp == null) {
			st = "initial activity";	// i.e. 20170712 to 18
			temp = sb.substringBetween(mstr, st, get_national_next_term(mstr, st));
		}
		if (temp != null) {
			temp = (temp.substring(temp.indexOf(" ") + 1)).trim();	// remove all characters (such as :) before the first space and then trim
			if (temp.matches("-?(0|[1-9]\\d*)")) {		// this special case happens in several instance	i.e. 20180813, 20170331			use Regex to check if this is just a number
				initial_attack_new_fires = temp;
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
				if (initial_attack_activity.contains("(")) initial_attack_activity = initial_attack_activity.substring(0, initial_attack_activity.indexOf("(")); // special case: 20170620
				if (temp.split(" ").length > 1) {
					initial_attack_new_fires = (temp.substring(temp.indexOf("(") + 1, temp.indexOf(")")).replaceAll("new", "").replaceAll("fire", "").replaceAll("s", "")).trim();		// i.e. 20180915 is a special case, 20160626 is also special there are 2))
				}
			}
			if (initial_attack_activity.isBlank()) initial_attack_activity = null;	// Fix when it print out just empty value
		}
		
		// Fix a special case 20170629: Light (169) --> stupid reversed information that needs to be switch
		if (initial_attack_activity != null && initial_attack_new_fires != null) {
			if (initial_attack_activity.matches("-?(0|[1-9]\\d*)") && !initial_attack_new_fires.matches("-?(0|[1-9]\\d*)")) {
				temp = initial_attack_activity;
				initial_attack_activity = initial_attack_new_fires;
				initial_attack_new_fires = temp;
				if (initial_attack_activity != null && initial_attack_activity.length() > 1) {
					initial_attack_activity = initial_attack_activity.split(" ")[0].toUpperCase();
					initial_attack_activity = initial_attack_activity.substring(0, 1) + initial_attack_activity.substring(1).toLowerCase();
				}
			}
		}
		
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
		
		st = "new large incidents"; if (date.startsWith("2014")) st = "new large fires";
		temp = sb.substringBetween(mstr, st, get_national_next_term(mstr, st)); 
		if (temp != null) new_large_incidents = (temp.substring(temp.indexOf(" ") + 1)).replaceAll("\\(\\*\\)", "").trim();		// replace (*) to handle special cases in 2015 data i.e. 20150102
		st = "large fires contained";
		temp = sb.substringBetween(mstr, st, get_national_next_term(mstr, st)); 
		if (temp != null) large_fires_contained = (temp.substring(temp.indexOf(" ") + 1)).trim();
		st = "uncontained large fires";
		temp = sb.substringBetween(mstr, st, get_national_next_term(mstr, st));  		// fail because of null 	i.e. 20170922	(area command teams committed does not exist)
		if (temp != null) uncontained_large_fires = ((temp.substring(temp.indexOf(" ") + 1))).replaceAll("\\*", "").trim();		// replace *** to handle special case 20210701-31 (that actually in ISMR2020 folder)
		st = "area command teams committed";
		temp = sb.substringBetween(mstr, st, get_national_next_term(mstr, st)); 
		if (temp != null) area_command_teams_committed = (temp.substring(temp.indexOf(" ") + 1)).trim();
		st = "nimos committed";
		temp = sb.substringBetween(mstr, st, get_national_next_term(mstr, st)); 
		if (temp != null) nimos_committed = (temp.substring(temp.indexOf(" ") + 1)).trim();
		st = "type 1 imts committed";
		temp = sb.substringBetween(mstr, st, get_national_next_term(mstr, st)); 
		if (temp != null) type_1_imts_committed = (temp.substring(temp.indexOf(" ") + 1)).trim();
		st = "type 2 imts committed";
		temp = (mstr.substring(mstr.indexOf(st) + 22)).trim();		// Note: different from above, not use sub string between
		if (temp != null) type_2_imts_committed = temp.split(" ")[0];
		
		national_fire_activity.add(date);
		national_fire_activity.add(national_prepareness_level);
		national_fire_activity.add(initial_attack_activity);
		national_fire_activity.add(initial_attack_new_fires);
		national_fire_activity.add(new_large_incidents);
		national_fire_activity.add(large_fires_contained);
		national_fire_activity.add(uncontained_large_fires);
		national_fire_activity.add(area_command_teams_committed);
		national_fire_activity.add(nimos_committed);
		national_fire_activity.add(type_1_imts_committed);
		national_fire_activity.add(type_2_imts_committed);
	}
	
	private String get_national_next_term(String mstr, String st) {
		String temp = (mstr.substring(mstr.indexOf(st) + 1)).trim(); // remove 1 leading character then use it to find next term
		String[] term = new String[] { "national prepareness level", "national fire activity",
				"initial attack activity", "new large incidents", "new large fires", "large fires contained", "uncontained large fires",
				"area command teams committed", "nimos committed", "type 1 imts committed", "type 2 imts committed" };		// "new large fires" is a special case used for 2014 data instead of "new large incidents"
		int start_id = 0;
		for (int i = 0; i < term.length; i++) {
			if (term[i].contains(st)) {
				start_id = i + 1;
			}
		}
		for (int i = start_id; i < term.length; i++) {
			if (temp.indexOf(term[i]) > -1) return term[i];
		}
		return null; // should never happen
	}
	
	private void get_area_data(String[] lines) {
		String current_area = "";
		int gacc_priority = 0;
		// Loop all lines, whenever found a gacc area, stop and process data
		for (int i = 0; i < lines.length; i++) {
			if (lines[i].contains("(PL")) {
				if (lines[i].startsWith("Alaska Area")) {
					current_area = "AICC";
					gacc_priority = gacc_priority + 1;
					process_area_data(lines, i, current_area, gacc_priority);
				} else if (lines[i].startsWith("Eastern")) {
					current_area = "EACC";
					gacc_priority = gacc_priority + 1;
					process_area_data(lines, i, current_area, gacc_priority);
				} else if (lines[i].startsWith("Great Basin Area")) {
					current_area = "GBCC";
					gacc_priority = gacc_priority + 1;
					process_area_data(lines, i, current_area, gacc_priority);
				} else if (lines[i].startsWith("Northern California Area")) {
					current_area = "ONCC";
					gacc_priority = gacc_priority + 1;
					process_area_data(lines, i, current_area, gacc_priority);
				} else if (lines[i].startsWith("Northern Rockies Area")) {
					current_area = "NRCC";
					gacc_priority = gacc_priority + 1;
					process_area_data(lines, i, current_area, gacc_priority);
				} else if (lines[i].startsWith("Northwest Area")) {
					current_area = "NWCC";
					gacc_priority = gacc_priority + 1;
					process_area_data(lines, i, current_area, gacc_priority);
				} else if (lines[i].startsWith("Rocky Mountain Area")) {
					current_area = "RMCC";
					gacc_priority = gacc_priority + 1;
					process_area_data(lines, i, current_area, gacc_priority);
				} else if (lines[i].startsWith("Southern Area")) {
					current_area = "SACC";
					gacc_priority = gacc_priority + 1;
					process_area_data(lines, i, current_area, gacc_priority);
				} else if (lines[i].startsWith("Southern California Area")) {
					current_area = "OSCC";
					gacc_priority = gacc_priority + 1;
					process_area_data(lines, i, current_area, gacc_priority);
				} else if (lines[i].startsWith("Southwest Area")) {
					current_area = "SWCC";
					gacc_priority = gacc_priority + 1;
					process_area_data(lines, i, current_area, gacc_priority);
				}
			}
		}
	}
	
	private void process_area_data(String[] lines, int start_line, String current_area, int gacc_priority) {
		String gacc_prepareness_level = null;
		String gacc_new_fires = null;
		String gacc_new_large_incidents = null;
		String gacc_uncontained_large_fires = null;
		String gacc_area_command_teams_committed = null;
		String gacc_nimos_committed = null;
		String gacc_type_1_imts_committed = null;
		String gacc_type_2_imts_committed = null;
		try {
			gacc_prepareness_level = lines[start_line].substring(lines[start_line].indexOf("(PL") + 3, lines[start_line].indexOf(")"));
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("missing information, national level will be used to replace gacc level"); // Example fail: 20111105IMSR
			gacc_prepareness_level = national_prepareness_level;
		}
		
		int end_line = start_line;
		do {
			end_line = end_line + 1;
		} while (!lines[end_line].contains("(PL") && (end_line < lines.length - 1));
		String[] merge_lines = Arrays.copyOfRange(lines, start_line, end_line);
		String mstr = String.join(" ", merge_lines).toLowerCase().replaceAll("\\s{2,}", " ").trim();	// 2 or more spaces will be replaced by one space, then leading and ending spaces will be removed
		
//		int i = mstr.indexOf("new fires") + 9;
//		if (i > -1) gacc_new_fires = (mstr.substring(i + 1)).trim().split(" ")[0];
//		if (!gacc_new_fires.matches("-?(0|[1-9]\\d*)"))	gacc_new_fires = null;
//		i = mstr.indexOf("new large incidents") + 19;
//		if (i > -1) gacc_new_large_incidents = (mstr.substring(i + 1)).trim().split(" ")[0];
//		if (!gacc_new_large_incidents.matches("-?(0|[1-9]\\d*)")) gacc_new_large_incidents = null;
//		i = mstr.indexOf("uncontained large fires") + 23;
//		if (i > -1) gacc_uncontained_large_fires = (mstr.substring(i + 1)).trim().split(" ")[0];
//		if (!gacc_uncontained_large_fires.matches("-?(0|[1-9]\\d*)")) gacc_uncontained_large_fires = null;
//		i = mstr.indexOf("area command teams committed") + 28;
//		if (i > -1) gacc_area_command_teams_committed = (mstr.substring(i + 1)).trim().split(" ")[0];
//		if (!gacc_area_command_teams_committed.matches("-?(0|[1-9]\\d*)")) gacc_area_command_teams_committed = null;
//		i = mstr.indexOf("nimos committed") + 15;
//		if (i > -1) gacc_nimos_committed = (mstr.substring(i + 1)).trim().split(" ")[0];
//		if (!gacc_nimos_committed.matches("-?(0|[1-9]\\d*)")) gacc_nimos_committed = null;
//		i = mstr.indexOf("type 1 imts committed") + 21;
//		if (i > -1) gacc_type_1_imts_committed = (mstr.substring(i + 1)).trim().split(" ")[0];
//		if (!gacc_type_1_imts_committed.matches("-?(0|[1-9]\\d*)"))	gacc_type_1_imts_committed = null;
//		i = mstr.indexOf("type 2 imts committed") + 21;
//		if (i > -1) gacc_type_2_imts_committed = (mstr.substring(i + 1)).trim().split(" ")[0];
//		if (!gacc_type_2_imts_committed.matches("-?(0|[1-9]\\d*)"))	gacc_type_2_imts_committed = null;
		
		String info = null;
		// Match term and value (a very smart matching) that works for both normal cases and special cases (i.e. 20180620, 20180622, 20180804, ...)
		// Example of the special case 20180804 text file produced by adobe: 2 types of writing gacc activity: single line (Northern Rockies Area) or multiple lines (Southwest Area)
		List<String> term = new ArrayList<String>();
		if (mstr.contains("new fires")) term.add("new fires");
		if (mstr.contains("new large incidents") || mstr.contains("new large fires")) term.add("new large incidents");	// i.e. 2014 data uses "new large fires"
		if (mstr.contains("uncontained large fires")) term.add("uncontained large fires");
		if (mstr.contains("area command teams committed") || mstr.contains("area command")) term.add("area command teams committed");	// i.e. 20201021 uses Area Command
		if (mstr.contains("nimos committed")) term.add("nimos committed");
		if (mstr.contains("type 1 imts committed") || mstr.contains("type 1 teams committed")) term.add("type 1 imts committed");		// i.e. 20180704 uses "type 1 teams committed"
		if (mstr.contains("type 2 imts committed") || mstr.contains("type 2 teams committed")) term.add("type 2 imts committed");		// i.e. 20180704 uses "type 2 teams committed"
		List<String> value = new ArrayList<String>();
		String[] split_value = (mstr.replaceAll("type 1", "").replaceAll("type 2", "").replaceAll("\\s{2,}", " ")).split(" ");	// remove the number 1 and 2
		for (String st : split_value) {
			if (st.matches("-?(0|[1-9]\\d*)")) {	// if this is numeric
				value.add(st);
			}
		}
		// get the value associated with the term
		for (int j = 0; j < term.size(); j++) {
			if (term.get(j).equals("new fires")) gacc_new_fires = value.get(j);
			if (term.get(j).equals("new large incidents")) gacc_new_large_incidents = value.get(j);
			if (term.get(j).equals("uncontained large fires")) gacc_uncontained_large_fires = value.get(j);
			if (term.get(j).equals("area command teams committed")) gacc_area_command_teams_committed = value.get(j);
			if (term.get(j).equals("nimos committed")) gacc_nimos_committed = value.get(j);
			if (term.get(j).equals("type 1 imts committed")) gacc_type_1_imts_committed = value.get(j);	
			if (term.get(j).equals("type 2 imts committed")) gacc_type_2_imts_committed = value.get(j);
		}
		
		info = String.join("\t", date, current_area, String.valueOf(gacc_priority), 
				gacc_prepareness_level, gacc_new_fires, gacc_new_large_incidents, gacc_uncontained_large_fires,
				gacc_area_command_teams_committed, gacc_nimos_committed, gacc_type_1_imts_committed,
				gacc_type_2_imts_committed);
		gacc_fire_activity.add(info);
		// IMPORTANT NOTE NOTE NOTE: 20180619: Rocky Mountain Area has uncontained large files but do not printed in pdf file (Erin's excel file got the right number of 4 uncontained)
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
	
	private void get_fire_data(String[] lines) {		// Information of a Fire is in one line		(Note: a special case: 20180803 at page 10 where the table without header if expanding 2 pages) 
		List<String[]> line_split = new ArrayList<String[]>();
		for (String st : lines) {
			line_split.add(st.replaceAll("\\s{2,}", " ").trim().split("\\s+"));	// 2 or more spaces will be replaced by one space, then leading and ending spaces will be removed
		} 
		
		// Loop all lines
		String current_area = "";
		int gacc_priority = 0;
		int i = 0;
		do {
			if (lines[i].contains("(PL")) {
				if (lines[i].startsWith("Alaska")) {
					current_area = "AICC";
					gacc_priority = gacc_priority + 1;
				} else if (lines[i].startsWith("Eastern")) {
					current_area = "EACC";
					gacc_priority = gacc_priority + 1;
				} else if (lines[i].startsWith("Great Basin")) {		// Special case in 20170708. The gacc is Great Basin (does not have "Area")
					current_area = "GBCC";
					gacc_priority = gacc_priority + 1;
				} else if (lines[i].startsWith("Northern California")) {
					current_area = "ONCC";
					gacc_priority = gacc_priority + 1;
				} else if (lines[i].startsWith("Northern Rockies")) {
					current_area = "NRCC";
					gacc_priority = gacc_priority + 1;
				} else if (lines[i].startsWith("Northwest")) {
					current_area = "NWCC";
					gacc_priority = gacc_priority + 1;
				} else if (lines[i].startsWith("Rocky Mountain")) {
					current_area = "RMCC";
					gacc_priority = gacc_priority + 1;
				} else if (lines[i].startsWith("Southern Area")) {
					current_area = "SACC";
					gacc_priority = gacc_priority + 1;
				} else if (lines[i].startsWith("Southern California")) {
					current_area = "OSCC";
					gacc_priority = gacc_priority + 1;
				} else if (lines[i].startsWith("Southwest")) {
					current_area = "SWCC";
					gacc_priority = gacc_priority + 1;
				}
			}
			
			int unit_id = 0;	// find the second column of the table
			int line_length = line_split.get(i).length;
			
			boolean year_before_2015 = Integer.valueOf(date.substring(0, 4)) < 2015;	// first 4 letters
			if (year_before_2015) { // before 2015 we use this to process data (Note unit is split by 2 columns and we need to merge, also we do not have the "Ctn/Comp" and we need to assign Ctn for it)
				if (line_length >= 15 
						&& line_split.get(i)[line_length - 14].length() == 2    // This is State "St" for 2014 and earlier year. It must have only 2 characters
						&& line_split.get(i)[line_length - 12].matches("-?\\d+(\\,\\d+)?")
						&& line_split.get(i)[line_length - 8].matches("-?\\d+(\\,\\d+)?")) {		// this is likely a fire, smart check based on the "acres" and "personnel total" columns.
					unit_id = line_split.get(i).length - 14;
					String fire_priority = String.valueOf(area_fires(current_area).size() + 1);
					String this_fire = String.join("\t", date, current_area, String.valueOf(gacc_priority), fire_priority);
					
					// Since it is very rare that fire name is in 3 lines (example 20210710IMSR), we disable the options for users to choose names, but assign the default fire name (3rd line will always be ignored)
					// Check only the above line, if length <=3 then add to fire name
					// Note the special case (20140808IMSR - Devils Elbow Complex) where -simple2 command do not have expected order of fire name in 2 lines.
					String fire_name = "";
					for (int id = 0; id < unit_id; id++) {
						fire_name = String.join(" ", fire_name, line_split.get(i)[id]);	// this is the incident name (or part of the name that is in the same line with other fire information), join by space
					}
					if (line_split.get(i - 1).length <= 2 || (line_split.get(i - 1).length <= 3 && line_split.get(i - 1)[0].contains("\\*"))) {
						if (!lines[i - 1].startsWith("24 Hrs")) {	// this special check is needed for years < 2015
							fire_name = String.join(" ", line_split.get(i - 1)) + " " + fire_name;	// join by space
						}
					}
					
					fire_name = fire_name.replaceAll("\\*", "").trim().toUpperCase();	// This will remove the * (if exist in the name) and change the name to capital (IMPORTANT)
					this_fire = String.join("\t", this_fire, fire_name);
					for (int id = unit_id; id < line_split.get(i).length; id++) {
						if (id == line_split.get(i).length - 9) {	// add a column that exists in 2015 and later (Ctn/Comp) but not exist in 2014 and earlier
							this_fire = this_fire + "\t" + "Ctn";
						}
						if (id == line_split.get(i).length - 13) {	// this is "unit", we need to connect to "st" column with a "-"
							this_fire = this_fire + "-" + line_split.get(i)[id];
						} else {
							this_fire = String.join("\t", this_fire, line_split.get(i)[id]);	// this is all information in one whole line of this fire
						}
					}
					all_fires.add(this_fire);
					area_fires(current_area).add(this_fire);
				}
			} else { // from 2015 we use this to process data
				if (line_length >= 15 
						&& line_split.get(i)[line_length - 14].contains("-")    // do not use this for 2014 data, it has a different unit name, also in 20210710IMSR, one fire has wrong unit that cannot be included (Butte Creek: ID- CTS)
						&& line_split.get(i)[line_length - 13].matches("-?\\d+(\\,\\d+)?")
						&& line_split.get(i)[line_length - 8].matches("-?\\d+(\\,\\d+)?")) {		// this is likely a fire, smart check based on the "acres" and "personnel total" columns.
					unit_id = line_split.get(i).length - 14;
					String fire_priority = String.valueOf(area_fires(current_area).size() + 1);
					String this_fire = String.join("\t", date, current_area, String.valueOf(gacc_priority), fire_priority);
					
					// Since it is very rare that fire name is in 3 lines (example 20210710IMSR), we disable the options for users to choose names, but assign the default fire name (3rd line will always be ignored)
					// Check only the above line, if length <=3 then add to fire name
					String fire_name = "";
					for (int id = 0; id < unit_id; id++) {
						fire_name = String.join(" ", fire_name, line_split.get(i)[id]);	// this is the incident name (or part of the name that is in the same line with other fire information), join by space
					}
					if (line_split.get(i - 1).length <= 2 || (line_split.get(i - 1).length <= 3 && line_split.get(i - 1)[0].contains("\\*"))) {
						fire_name = String.join(" ", line_split.get(i - 1)) + " " + fire_name;	// join by space
					}
//					-------------------------------------------------------------------------------------------------------------------------------------		
//					String fire_name = "";
//					String fire_name_1 = "";
//					for (int id = 0; id < unit_id; id++) {
//						fire_name_1 = String.join(" ", fire_name_1, line_split.get(i)[id]);	// this is the incident name (or part of the name that is in the same line with other fire information), join by space
//					}
//					
//					// This is the list of optional names of the fire, users need to select the correct fire name (the request only show up when a fire name associated with 3 lines)
//					List<String> optional_fire_names = new ArrayList<String>();
//					
//					// xpdf (using -simple2 command) may generate fire name in 2 or 3 lines, we need to ask users to correct fire name for these special cases.
//					// An example special case: 20210710IMSR where we have 2021 SUF (1st line) West Zone (2nd line) Complex (3rd line)
//					// Note that fire name in 2 lines often has the last line (second line) associated with other fire infor, while 3 lines would have the middle line (also second line) associated with the fire infor.
//					// If the line above has <= 3 terms then it is very likely that it belongs to the fire name
//					// Only if the line above has <= 3 terms, we will need to check the below line as well
//					if (line_split.get(i - 1).length <= 3) {
//						String fire_name_2 = String.join(" ", line_split.get(i - 1)) + " " + fire_name_1;	// join by space
//						if (line_split.get(i - 3).length <= 3) {	// check previous fire
//							optional_fire_names.add(fire_name_1);
//							optional_fire_names.add(fire_name_2);
//							// In case we spot 2 lines (line above has <=3 words), we need to check it and also the below line
//							if (line_split.get(i + 1).length <= 3) {
//								String fire_name_3 = fire_name_1 + " " + String.join(" ", line_split.get(i + 1)); // join by space
//								String fire_name_4 = fire_name_2 + " " + String.join(" ", line_split.get(i + 1)); // join by space
//								optional_fire_names.add(fire_name_3);
//								optional_fire_names.add(fire_name_4);
//							}
//							OptionPane_ConfirmFireName op_name = new OptionPane_ConfirmFireName(date, current_area, optional_fire_names.stream().toArray(String[]::new));
//							fire_name = optional_fire_names.get(op_name.response);
//						} else { // if previous fire does not have qualified above line with <=3 words, then this current file name definitely has 2 terms in 2 lines, need to check the line below as well
//							optional_fire_names.add(fire_name_2);
//							// In case we spot 2 lines (line above has <=3 words), we need to check it and also the below line
//							if (line_split.get(i + 1).length <= 3) {
//								String fire_name_4 = fire_name_2 + " " + String.join(" ", line_split.get(i + 1)); // join by space
//								optional_fire_names.add(fire_name_4);
//								OptionPane_ConfirmFireName op_name = new OptionPane_ConfirmFireName(date, current_area, optional_fire_names.stream().toArray(String[]::new));
//								fire_name = optional_fire_names.get(op_name.response);
//							} else { // the line below is not qualified, so stop checking, we know that the fire name has 2 term from 2 lines.
//								fire_name = fire_name_2;
//							}
//						}
//					} else { // If there is no qualified above line then fire name is in a single line 
//						fire_name = fire_name_1;
//					}
//					-------------------------------------------------------------------------------------------------------------------------------------
					
					fire_name = fire_name.replaceAll("\\*", "").trim().toUpperCase();	// This will remove the * (if exist in the name) and change the name to capital (IMPORTANT)
					this_fire = String.join("\t", this_fire, fire_name);
					for (int id = unit_id; id < line_split.get(i).length; id++) {
						this_fire = String.join("\t", this_fire, line_split.get(i)[id]);	// this is all information in one whole line of this fire
					}
					all_fires.add(this_fire);
					area_fires(current_area).add(this_fire);
				}
			}
			
			i = i + 1;
		} while (i < lines.length);
	}
	
}
