package root;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import convenience_classes.SubstringBetween;

public class ISMR_Process {
	String date, s_date, r_date;
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
	
	List<String> resource_summary = new ArrayList<String>();	// store all active incident resource summary information
	
	List<String> final_fires = new ArrayList<String>();	// All fires in s_fires have fire names adjusted by using r_fires
	List<String> s_fires = new ArrayList<String>();	// All fires extracted from simple2 files
	List<String> r_fires = new ArrayList<String>();	// All fires extracted from raw files, used to validate fire name from simple2 files
	List<String> AICC = new ArrayList<String>();	// Alaska
	List<String> EACC = new ArrayList<String>();	// Eastern Area
	List<String> EBCC = new ArrayList<String>();	// Eastern Great Basin (Before 2015)
	List<String> WBCC = new ArrayList<String>();	// Western Great Basin (Before 2015)
	List<String> GBCC = new ArrayList<String>();	// Great Basin (2015 and after)
	List<String> ONCC = new ArrayList<String>();	// Northern California
	List<String> NRCC = new ArrayList<String>();	// Northern Rockies
	List<String> NWCC = new ArrayList<String>();	// Northwest
	List<String> RMCC = new ArrayList<String>();	// Rocky Mountain
	List<String> SACC = new ArrayList<String>();	// Southern Area
	List<String> OSCC = new ArrayList<String>();	// Southern California
	List<String> SWCC = new ArrayList<String>();	// Southwest

	public ISMR_Process(File s_file, File r_file) {
		try {
//			List<String> lines_list = Files.readAllLines(Paths.get(file.getAbsolutePath()), StandardCharsets.UTF_8);		// Not sure why this UTF_8 fail
			List<String> s_lines_list = Files.readAllLines(Paths.get(s_file.getAbsolutePath()), Charset.defaultCharset());		// Therefore I use default
			String[] s_lines = s_lines_list.stream().toArray(String[] ::new);
			for (int i = 0; i < s_lines.length; i++) {
				s_lines[i] = s_lines[i].replaceAll("\\s{2,}", " ").trim(); // 2 or more spaces will be replaced by one space, then leading and ending spaces will be removed
			}
//			date = file.getName().substring(0, 8);
			s_date = String.join("-", s_file.getName().substring(0, 4), s_file.getName().substring(4, 6), s_file.getName().substring(6, 8));	// use this data format yyyy-mm-dd to join easily with Ross data
			
			List<String> r_lines_list = Files.readAllLines(Paths.get(r_file.getAbsolutePath()), Charset.defaultCharset());
			String[] r_lines = r_lines_list.stream().toArray(String[] ::new);
			for (int i = 0; i < r_lines.length; i++) {
				r_lines[i] = r_lines[i].replaceAll("\\s{2,}", " ").trim(); // 2 or more spaces will be replaced by one space, then leading and ending spaces will be removed
			}
			r_date = String.join("-", r_file.getName().substring(0, 4), r_file.getName().substring(4, 6), r_file.getName().substring(6, 8));	// use this data format yyyy-mm-dd to join easily with Ross data
			
			date = s_date;
			get_national_data(s_lines);
			get_area_data(s_lines);
			get_reource_summary_data(s_lines);
			get_fire_data_simple2_method(s_lines);
			get_fire_data_raw_method(r_lines);
			fire_name_validation_and_adjustment();
			s_lines_list = null; 	// free memory
			s_lines = null;			// free memory
			r_lines_list = null; 	// free memory
			r_lines = null;			// free memory
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
		}
	}
	
	private void get_national_data(String[] s_lines) {
		int mergeCount = 0;
		for (int i = 0; i < s_lines.length; i++) {
			if (s_lines[i].contains("Active Incident Resource Summary")
					|| s_lines[i].contains("GACC")		// Special case 20200110: "Active Incident Resource Summary" is not written correctly --> Use GACC
					|| s_lines[i].contains("Geographic Area daily reports")		// Special case 2012
					|| s_lines[i].contains("WFU")) {		// Special case 2008 first 4 months
				for (int j = 0; j < i; j++) {
					if (s_lines[j].contains("Type 2 IMTs")) {
						// Merge up to this mergeline. Some special cases are j+3 or j+2, usually j+1 or j in most cases)
						if (s_lines[j].substring(s_lines[j].lastIndexOf(" ") + 1).matches("-?(0|[1-9]\\d*)")) {
							mergeCount = j;			// many cases
						} else if (s_lines[j + 1].substring(s_lines[j + 1].lastIndexOf(" ") + 1).matches("-?(0|[1-9]\\d*)")) {
							mergeCount = j + 1;		// many cases
						} else if (s_lines[j + 2].substring(s_lines[j + 2].lastIndexOf(" ") + 1).matches("-?(0|[1-9]\\d*)")) {
							mergeCount = j + 2;		// 20200110
						} else if (s_lines[j + 3].substring(s_lines[j + 3].lastIndexOf(" ") + 1).matches("-?(0|[1-9]\\d*)")) {
							mergeCount = j + 3;		// 20200518
						} else {	// Type 2 IMTs may have nothing, no space as well, such as in 20070417
							mergeCount = j;
						}
					}
				}
			}
		}
		String[] merge_lines = Arrays.copyOfRange(s_lines, 0, mergeCount + 1);
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
		
		if (Integer.valueOf(date.replaceAll("-", "")) < 20070425) mstr = mstr.replaceAll("area command teams", "area command teams committed");	// Test
		if (Integer.valueOf(date.replaceAll("-", "")) == 20070423) mstr = mstr.replaceAll("national incident management 1 organization", "");	// Fix special case
		
		st = "new large incidents"; if (Integer.valueOf(date.substring(0, 4)) < 2015) st = "new large fires";
		temp = sb.substringBetween(mstr, st, get_national_next_term(mstr, st)); 
		if (temp != null) new_large_incidents = (temp.substring(temp.indexOf(" ") + 1)).replaceAll("\\(\\*\\)", "").trim();		// replace (*) to handle special cases in 2015 data i.e. 20150102
		st = "large fires contained";
		temp = sb.substringBetween(mstr, st, get_national_next_term(mstr, st)); 
		if (temp != null) large_fires_contained = (temp.substring(temp.indexOf(" ") + 1)).trim();
		st = "uncontained large fires";
		temp = sb.substringBetween(mstr, st, get_national_next_term(mstr, st));  		// fail because of null 	i.e. 20170922	(area command teams committed does not exist)
		if (temp != null) uncontained_large_fires = ((temp.substring(temp.indexOf(" ") + 1))).replaceAll("\\*", "").replaceAll(":", "").trim();		// replace *** to handle special case 20210701-31 (that actually in ISMR2020 folder), replace : to handle 2010 data in first 5 months
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
		
		// If no data then make it null
		if (date == null || date.isBlank()) date = null;
		if (national_prepareness_level == null || national_prepareness_level.isBlank()) national_prepareness_level = null;
		if (initial_attack_activity == null || initial_attack_activity.isBlank()) initial_attack_activity = null;
		if (initial_attack_new_fires == null || initial_attack_new_fires.isBlank()) initial_attack_new_fires = null;
		if (new_large_incidents == null || new_large_incidents.isBlank()) new_large_incidents = null;
		if (large_fires_contained == null || large_fires_contained.isBlank()) large_fires_contained = null;
		if (uncontained_large_fires == null || uncontained_large_fires.isBlank()) uncontained_large_fires = null;
		if (area_command_teams_committed == null || area_command_teams_committed.isBlank()) area_command_teams_committed = null;
		if (nimos_committed == null || nimos_committed.isBlank()) nimos_committed = null;
		if (type_1_imts_committed == null || type_1_imts_committed.isBlank()) type_1_imts_committed = null;
		if (type_2_imts_committed == null || type_2_imts_committed.isBlank()) type_2_imts_committed = null;
		
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
	
	private void get_area_data(String[] s_lines) {
		String current_area = "";
		int gacc_priority = 0;
		// Loop all lines, whenever found a gacc area, stop and process data
		for (int i = 0; i < s_lines.length; i++) {
			if (s_lines[i].contains("(PL")) {
				if (s_lines[i].startsWith("Alaska")) {
					current_area = "AICC";
					gacc_priority = gacc_priority + 1;
					process_area_data(s_lines, i, current_area, gacc_priority);
				} else if (s_lines[i].startsWith("Eastern Area")) {
					current_area = "EACC";
					gacc_priority = gacc_priority + 1;
					process_area_data(s_lines, i, current_area, gacc_priority);
				} else if (s_lines[i].startsWith("Eastern Great Basin")) {	// before 2015
					current_area = "EBCC";
					gacc_priority = gacc_priority + 1;
					process_area_data(s_lines, i, current_area, gacc_priority);
				}  else if (s_lines[i].startsWith("Western Great Basin")) {	// before 2015
					current_area = "WBCC";
					gacc_priority = gacc_priority + 1;
					process_area_data(s_lines, i, current_area, gacc_priority);
				} else if (s_lines[i].startsWith("Great Basin")) {	// 2015 and after
					current_area = "GBCC";
					gacc_priority = gacc_priority + 1;
					process_area_data(s_lines, i, current_area, gacc_priority);
				} else if (s_lines[i].startsWith("Northern California")) {
					current_area = "ONCC";
					gacc_priority = gacc_priority + 1;
					process_area_data(s_lines, i, current_area, gacc_priority);
				} else if (s_lines[i].startsWith("Northern Rockies")) {
					current_area = "NRCC";
					gacc_priority = gacc_priority + 1;
					process_area_data(s_lines, i, current_area, gacc_priority);
				} else if (s_lines[i].startsWith("Northwest")) {
					current_area = "NWCC";
					gacc_priority = gacc_priority + 1;
					process_area_data(s_lines, i, current_area, gacc_priority);
				} else if (s_lines[i].startsWith("Rocky Mountain")) {
					current_area = "RMCC";
					gacc_priority = gacc_priority + 1;
					process_area_data(s_lines, i, current_area, gacc_priority);
				} else if (s_lines[i].startsWith("Southern Area")) {
					current_area = "SACC";
					gacc_priority = gacc_priority + 1;
					process_area_data(s_lines, i, current_area, gacc_priority);
				} else if (s_lines[i].startsWith("Southern California")) {
					current_area = "OSCC";
					gacc_priority = gacc_priority + 1;
					process_area_data(s_lines, i, current_area, gacc_priority);
				} else if (s_lines[i].startsWith("Southwest")) {
					current_area = "SWCC";
					gacc_priority = gacc_priority + 1;
					process_area_data(s_lines, i, current_area, gacc_priority);
				}
			}
		}
	}
	
	private void process_area_data(String[] s_lines, int start_line, String current_area, int gacc_priority) {
		String gacc_prepareness_level = null;
		String gacc_new_fires = null;
		String gacc_new_large_incidents = null;
		String gacc_uncontained_large_fires = null;
		String gacc_area_command_teams_committed = null;
		String gacc_nimos_committed = null;
		String gacc_type_1_imts_committed = null;
		String gacc_type_2_imts_committed = null;
		
		if (s_lines[start_line].indexOf(")") == -1) {
			System.out.println(date + " missing gacc preparedness information");
			gacc_prepareness_level = "null";	 // Example missing information: 20211105IMSR
		} else {
			gacc_prepareness_level = s_lines[start_line].substring(s_lines[start_line].indexOf("(PL") + 3, s_lines[start_line].indexOf(")"));
		}
		
		int end_line = start_line;
		do {
			end_line = end_line + 1;
		} while (!s_lines[end_line].contains("(PL") && (end_line < s_lines.length - 1));
		String[] merge_lines = Arrays.copyOfRange(s_lines, start_line, end_line);
		String mstr = String.join(" ", merge_lines).toLowerCase();
		
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
		String[] split_value = (mstr.replaceAll("type 1", "").replaceAll("type 2", "")).split(" ");	// remove the number 1 and 2
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
	
	private void get_reource_summary_data(String[] s_lines) {
		int start_line = 0;
		int end_line = 0;
		
		boolean table_start = false;
		int l = 0;
		do {
			if (s_lines[l].contains("Active Incident Resource Summary") || s_lines[l].contains("Active Fire Resource Summary"))  {
				table_start = true;
				start_line = l;
			}
			l = l + 1;
		} while (l < s_lines.length && !table_start);
		
		if (table_start) {
			l = start_line;
			boolean table_end = false;
			do {
				if (s_lines[l].startsWith("Total") && !s_lines[l + 1].startsWith("Personnel"))  {  // Total (without Personnel in next line) is indicator of ending line. Note that There may be Total (Personnel) without space before.
					table_end = true;
					end_line = l;
				}
				l = l + 1;
			} while (l < s_lines.length && !table_end);
		}
		
		for (int i = start_line; i <= end_line; i++) {	// final the actual start line with first row (Alaska) we need to get data
			if (s_lines[i].startsWith("AK") || s_lines[i].startsWith("AICC")) {
				start_line = i;
			}
		}
		
		// Extract data
		if (table_start) {	// if summary table exists
			String join_st = "";
			for (int i = start_line; i <= end_line; i++) {
				String[] line_split = s_lines[i].split("\\s+");
				// change GACC name because it show only 2 characters such as in the first 5 months of 2015, occasionally in 2016
				if (line_split[0].equals("AK")) line_split[0] = "AICC"; 	if (line_split[0].equals("AKCC")) line_split[0] = "AICC";	// special case 20150601
				if (line_split[0].equals("NW")) line_split[0] = "NWCC";
				if (line_split[0].equals("NO")) line_split[0] = "ONCC"; 	if (line_split[0].equals("ON")) line_split[0] = "ONCC";		// special case 20150515
				if (line_split[0].equals("SO")) line_split[0] = "OSCC"; 	if (line_split[0].equals("OS")) line_split[0] = "OSCC";		// special case 20150515
				if (line_split[0].equals("NR")) line_split[0] = "NRCC";
				if (line_split[0].equals("GB")) line_split[0] = "GBCC";
				// Very important note: Up until 20150102. GB still include EB and WB. AFter this time, they are merge
				// This is to address the single special case 20150102 where GB still include EB and WB. Since all values in the two areas are zero, we keep EB and change it to GBCC, and ignore WB
				if (line_split[0].equals("EB")) line_split[0] = "GBCC";
				if (line_split[0].equals("SW")) line_split[0] = "SWCC";
				if (line_split[0].equals("RM")) line_split[0] = "RMCC";
				if (line_split[0].equals("EA")) line_split[0] = "EACC";
				if (line_split[0].equals("SA")) line_split[0] = "SACC";
				
				List<String> gacc_list = List.of("AICC", "NWCC", "ONCC", "OSCC", "NRCC", "GBCC", "SWCC", "RMCC", "EACC", "SACC", "Total");	// Note we do not add total information
				if (gacc_list.contains(line_split[0])) {
					if (!line_split[0].equals("AICC")) {
						resource_summary.add(join_st);
					}
					join_st = date + "\t" + String.join("\t", line_split);
				} else {
					if (!line_split[0].equals("WB")) join_st = join_st + "\t" + String.join("\t", line_split);		// Add while ignoring WB
				}
			}
		}
		// NOTE NOTE NOTE: 2022 have 8 columns
	}

	private List<String> area_fires(String area_name) {
		if (area_name.equals("AICC")) {
			return AICC;
		} else if (area_name.equals("EACC")) {
			return EACC;
		} else if (area_name.equals("EBCC")) {	// before 2015
			return EBCC;
		} else if (area_name.equals("WBCC")) {	// before 2015
			return WBCC;
		} else if (area_name.equals("GBCC")) {	// 2015 and after
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
	
	private void get_fire_data_simple2_method(String[] s_lines) {		// Information of a Fire is in one line		(Note: a special case: 20180803 at page 10 where the table without header if expanding 2 pages) 
		List<String[]> line_split = new ArrayList<String[]>();
		for (String st : s_lines) {
			line_split.add(st.split("\\s+"));
		} 
		
		// Loop all lines
		String current_area = "";
		int gacc_priority = 0;
		int i = 0;
		do {
			if (s_lines[i].contains("(PL")) {
				if (s_lines[i].startsWith("Alaska")) {
					current_area = "AICC";
					gacc_priority = gacc_priority + 1;
				} else if (s_lines[i].startsWith("Eastern Area")) {
					current_area = "EACC";
					gacc_priority = gacc_priority + 1;
				} else if (s_lines[i].contains("Eastern Great Basin")) {		// before 2015
					current_area = "EBCC";
					gacc_priority = gacc_priority + 1;
				} else if (s_lines[i].contains("Western Great Basin")) {		// before 2015
					current_area = "WBCC";
					gacc_priority = gacc_priority + 1;
				} else if (s_lines[i].contains("Great Basin")) {		// 2015 and after. Special case in 20170708: The gacc is "Great Basin", does not have "Area"
					current_area = "GBCC";
					gacc_priority = gacc_priority + 1;
				} else if (s_lines[i].startsWith("Northern California")) {
					current_area = "ONCC";
					gacc_priority = gacc_priority + 1;
				} else if (s_lines[i].startsWith("Northern Rockies")) {
					current_area = "NRCC";
					gacc_priority = gacc_priority + 1;
				} else if (s_lines[i].startsWith("Northwest")) {
					current_area = "NWCC";
					gacc_priority = gacc_priority + 1;
				} else if (s_lines[i].startsWith("Rocky Mountain")) {
					current_area = "RMCC";
					gacc_priority = gacc_priority + 1;
				} else if (s_lines[i].startsWith("Southern Area")) {
					current_area = "SACC";
					gacc_priority = gacc_priority + 1;
				} else if (s_lines[i].startsWith("Southern California")) {
					current_area = "OSCC";
					gacc_priority = gacc_priority + 1;
				} else if (s_lines[i].startsWith("Southwest")) {
					current_area = "SWCC";
					gacc_priority = gacc_priority + 1;
				}
			}
			
			int unit_id = 0;	// find the second column of the table
			int line_length = line_split.get(i).length;
			
			boolean year_before_2015 = Integer.valueOf(date.substring(0, 4)) < 2015;	// first 4 letters   (Note that from 2007-05-28 to the end of 2014, there are 15 columns for each fire)
			if (year_before_2015) { // before 2015 we use this to process data (Note unit is split by 2 columns and we need to merge, also we do not have the "Ctn/Comp" and we need to assign Ctn for it)
				if (line_length >= 15 
						&& line_split.get(i)[line_length - 14].length() == 2    // This is State "St" for 2014 and earlier year. It must have only 2 characters
						&& (line_split.get(i)[line_length - 12].matches("^\\d{1,3}([ ,]?\\d{3})*([.,]\\d+)?$") || line_split.get(i)[line_length - 12].equals("N/A")) 
						&& line_split.get(i)[line_length - 8].matches("^\\d{1,3}([ ,]?\\d{3})*([.,]\\d+)?$")) {		// this is likely a fire, smart check based on the "acres" and "personnel total" columns.
					unit_id = line_split.get(i).length - 14;
					String fire_priority = String.valueOf(area_fires(current_area).size() + 1);
					String this_fire = String.join("\t", date, current_area, String.valueOf(gacc_priority), fire_priority);
					
					// Since it is very rare that fire name is in 3 lines (example 20210710IMSR), we assign the default fire name (3rd line will always be ignored)
					// Check only the above line, if length <=5 then add to fire name
					// Note the special case (20140808IMSR - Devils Elbow Complex) where -simple2 command do not have expected order of fire name in 2 lines.
					String fire_name = "";
					for (int id = 0; id < unit_id; id++) {
						fire_name = String.join(" ", fire_name, line_split.get(i)[id]);	// this is the incident name (or part of the name that is in the same line with other fire information), join by space
					}
					if (line_split.get(i - 1).length <= 5 && !s_lines[i - 1].toUpperCase().endsWith("COMP LOST OWN")) {		// such as special case 20150606
						if (!s_lines[i - 1].startsWith("24 Hrs") && !s_lines[i - 1].startsWith("Hrs")) {	// this special check is needed for years < 2015. Note: (2013,2014: 24 Hrs) (2012: Hrs)
							fire_name = String.join(" ", line_split.get(i - 1)) + " " + fire_name;	// join by space
						}
					}
					
					fire_name = fire_name.replaceAll("\\*", "").replaceAll("\\s{2,}", " ").trim().toUpperCase();	// This will remove the * (if exist in the name) and change the name to capital (IMPORTANT)
					this_fire = String.join("\t", this_fire, fire_name);
					for (int id = unit_id; id < line_split.get(i).length; id++) {
						if (id == line_split.get(i).length - 9) {	// add a column that exists in 2015 and later (Ctn/Comp) but not exist in 2014 and earlier
							this_fire = this_fire + "\t" + "null";
						}
						if (id == line_split.get(i).length - 13) {	// this is "unit", we need to connect to "st" column with a "-"
							this_fire = this_fire + "-" + line_split.get(i)[id];
						} else {
							this_fire = String.join("\t", this_fire, line_split.get(i)[id]);	// this is all information in one whole line of this fire
						}
					}
					s_fires.add(this_fire);
					area_fires(current_area).add(this_fire);
				} else if (line_length >= 13 		// special case in first 5 months on 2007
						&& line_split.get(i)[line_length - 12].length() == 2    // This is State "St" for 2007 in first 5 months. It must have only 2 characters
						&& (line_split.get(i)[line_length - 10].matches("^\\d{1,3}([ ,]?\\d{3})*([.,]\\d+)?$") || line_split.get(i)[line_length - 10].equals("N/A"))
						&& line_split.get(i)[line_length - 7].matches("^\\d{1,3}([ ,]?\\d{3})*([.,]\\d+)?$")) {		// this is likely a fire, smart check based on the "acres" and "personnel total" columns.
					unit_id = line_split.get(i).length - 12;
					String fire_priority = String.valueOf(area_fires(current_area).size() + 1);
					String this_fire = String.join("\t", date, current_area, String.valueOf(gacc_priority), fire_priority);
					
					// Since it is very rare that fire name is in 3 lines, we assign the default fire name (3rd line will always be ignored)
					// Check only the above line, if length <=5 then add to fire name
					// Note the special case (20140808IMSR - Devils Elbow Complex) where -simple2 command do not have expected order of fire name in 2 lines.
					String fire_name = "";
					for (int id = 0; id < unit_id; id++) {
						fire_name = String.join(" ", fire_name, line_split.get(i)[id]);	// this is the incident name (or part of the name that is in the same line with other fire information), join by space
					}
					if (line_split.get(i - 1).length <= 5 && !s_lines[i - 1].toUpperCase().endsWith("COMP LOST OWN")) {		// such as special case 20150606
						if (!s_lines[i - 1].startsWith("24 Hrs") && !s_lines[i - 1].startsWith("Hrs")) {	// this special check is needed for years < 2015. Note: (2013,2014: 24 Hrs) (2012: Hrs)
							fire_name = String.join(" ", line_split.get(i - 1)) + " " + fire_name;	// join by space
						}
					}
					
					fire_name = fire_name.replaceAll("\\*", "").replaceAll("\\s{2,}", " ").trim().toUpperCase();	// This will remove the * (if exist in the name) and change the name to capital (IMPORTANT)
					this_fire = String.join("\t", this_fire, fire_name);
					for (int id = unit_id; id < line_split.get(i).length; id++) {
						if (id == line_split.get(i).length - 6) {	// add a column that exists in 2015 and later (Personnel Chge) but not in this case. 
							this_fire = this_fire + "\t" + "null";
						}
						if (id == line_split.get(i).length - 8) {	// add a column that exists in 2015 and later (Ctn/Comp) but not exist in 2014 and earlier
							this_fire = this_fire + "\t" + "null";
						}
						if (id == line_split.get(i).length - 9) {	// add a column that exists in 2015 and later (Size Chge) but not in this case. 
							this_fire = this_fire + "\t" + "null";
						}
						if (id == line_split.get(i).length - 11) {	// this is "unit", we need to connect to "st" column with a "-"
							this_fire = this_fire + "-" + line_split.get(i)[id];
						} else {
							this_fire = String.join("\t", this_fire, line_split.get(i)[id]);	// this is all information in one whole line of this fire
						}
					}
					s_fires.add(this_fire);
					area_fires(current_area).add(this_fire);
				} 
			} else { // from 2015 we use this to process data
				if (line_length >= 15 
						&& line_split.get(i)[line_length - 14].contains("-")    // do not use this for 2014 data, it has a different unit name, also in 20210710IMSR, one fire has wrong unit that cannot be included (Butte Creek: ID- CTS)
						&& (line_split.get(i)[line_length - 13].matches("^\\d{1,3}([ ,]?\\d{3})*([.,]\\d+)?$") || line_split.get(i)[line_length - 13].equals("N/A")) // special cases such as U.S. Virgin in 20171004
						&& line_split.get(i)[line_length - 8].matches("^\\d{1,3}([ ,]?\\d{3})*([.,]\\d+)?$")) {		// this is likely a fire, smart check based on the "acres" and "personnel total" columns.
					unit_id = line_split.get(i).length - 14;
					String fire_priority = String.valueOf(area_fires(current_area).size() + 1);
					String this_fire = String.join("\t", date, current_area, String.valueOf(gacc_priority), fire_priority);
					
					// Since it is very rare that fire name is in 3 lines (example 20210710IMSR), we assign the default fire name (3rd line will always be ignored)
					// Check only the above line, if length <=5 then add to fire name
					String fire_name = "";
					for (int id = 0; id < unit_id; id++) {
						fire_name = String.join(" ", fire_name, line_split.get(i)[id]);	// this is the incident name (or part of the name that is in the same line with other fire information), join by space
					}
					if (line_split.get(i - 1).length <= 5 && !s_lines[i - 1].toUpperCase().endsWith("COMP LOST OWN")) {		// such as special case 20150606
						fire_name = String.join(" ", line_split.get(i - 1)) + " " + fire_name;	// join by space
					}
					
					fire_name = fire_name.replaceAll("\\*", "").replaceAll("\\s{2,}", " ").trim().toUpperCase();	// This will remove the * (if exist in the name) and change the name to capital (IMPORTANT)
					this_fire = String.join("\t", this_fire, fire_name);
					for (int id = unit_id; id < line_split.get(i).length; id++) {
						this_fire = String.join("\t", this_fire, line_split.get(i)[id]);	// this is all information in one whole line of this fire
					}
					s_fires.add(this_fire);
					area_fires(current_area).add(this_fire);
				}
			}
			
			i = i + 1;
		} while (i < s_lines.length);
	}

	private void get_fire_data_raw_method(String[] r_lines) {		// Information of a Fire is in one line		(Note: a special case: 20180803 at page 10 where the table without header if expanding 2 pages) 
		// Pre-processing for some special cases
		for (int i = 0; i < r_lines.length; i++) {
			if (r_lines[i].length() == 3 && r_lines[i].endsWith("-")) {		// special case where unit is split into 2 lines such as 20150613, then we merge 2 lines and make the one line empty
				r_lines[i + 1] = r_lines[i] + r_lines[i + 1];
				r_lines[i] = "";
			}
		}
		//------------------------------------------------------------
		
		List<String[]> line_split = new ArrayList<String[]>();
		for (String st : r_lines) {
			line_split.add(st.split("\\s+"));
		} 
		
		// Loop all lines
		String current_area = "NA";
		String gacc_priority = "NA";
		String fire_priority = "NA";
		
		int i = 0;
		do {
			if (r_lines[i].contains("(PL")) {			// do not use this to compare fire name because gacc block is not consistent, but we can use this to add fire in raw but not in simple2
				if (r_lines[i].startsWith("Alaska")) {
					current_area = "AICC";
				} else if (r_lines[i].startsWith("Eastern Area")) {
					current_area = "EACC";
				} else if (r_lines[i].contains("Eastern Great Basin")) {		// before 2015
					current_area = "EBCC";
				} else if (r_lines[i].contains("Western Great Basin")) {		// before 2015
					current_area = "WBCC";
				} else if (r_lines[i].contains("Great Basin")) {		// 2015 and after. Special case in 20170708: The gacc is "Great Basin", does not have "Area"
					current_area = "GBCC";
				} else if (r_lines[i].startsWith("Northern California")) {
					current_area = "ONCC";
				} else if (r_lines[i].startsWith("Northern Rockies")) {
					current_area = "NRCC";
				} else if (r_lines[i].startsWith("Northwest")) {
					current_area = "NWCC";
				} else if (r_lines[i].startsWith("Rocky Mountain")) {
					current_area = "RMCC";
				} else if (r_lines[i].startsWith("Southern Area")) {
					current_area = "SACC";
				} else if (r_lines[i].startsWith("Southern California")) {
					current_area = "OSCC";
				} else if (r_lines[i].startsWith("Southwest")) {
					current_area = "SWCC";
				}
			}
			
			int unit_id = 0;	// find the second column of the table
			int line_length = line_split.get(i).length;
			
			boolean year_before_2015 = Integer.valueOf(r_date.substring(0, 4)) < 2015;	// first 4 letters   (Note that from 2007-05-28 to the end of 2014, there are 15 columns for each fire)
			if (year_before_2015) { // before 2015 we use this to process data (Note unit is split by 2 columns and we need to merge, also we do not have the "Ctn/Comp" and we need to assign Ctn for it)
				if (line_length >= 14 
						&& line_split.get(i)[line_length - 14].length() == 2    // This is State "St" for 2014 and earlier year. It must have only 2 characters
						&& (line_split.get(i)[line_length - 12].matches("^\\d{1,3}([ ,]?\\d{3})*([.,]\\d+)?$") || line_split.get(i)[line_length - 12].equals("N/A"))
						&& line_split.get(i)[line_length - 8].matches("^\\d{1,3}([ ,]?\\d{3})*([.,]\\d+)?$")) {		// this is likely a fire, smart check based on the "acres" and "personnel total" columns.
					unit_id = line_split.get(i).length - 14;
					String this_fire = String.join("\t", r_date, current_area, gacc_priority, fire_priority);
					
					// Check above lines, if length <=5 etc, then add to fire name
					// Note the special case (20140808IMSR - Devils Elbow Complex) where -simple2 command do not have expected order of fire name in 2 lines.
					String fire_name = "";
					for (int id = 0; id < unit_id; id++) {
						fire_name = String.join(" ", fire_name, line_split.get(i)[id]);	// this is the incident name (or part of the name that is in the same line with other fire information), join by space
					}
					// loop back previous lines to get the full fire name
					boolean continue_loop = true;
					int l = i;
					do {
						l = l - 1;
						if (line_split.get(l).length <= 5 && !r_lines[l].toUpperCase().endsWith("OWN") && !r_lines[l].toUpperCase().endsWith("HELI")) {		// HELI is special case for 20150102
							fire_name = String.join(" ", r_lines[l], fire_name);	// join by space
						} else {
							continue_loop = false;
						}
					} while (continue_loop);
					fire_name = fire_name.replaceAll("\\*", "").replaceAll("\\s{2,}", " ").trim().toUpperCase();	// This will remove the * (if exist in the name) and change the name to capital (IMPORTANT)
					
					this_fire = String.join("\t", this_fire, fire_name);
					for (int id = unit_id; id < line_split.get(i).length; id++) {
						if (id == line_split.get(i).length - 9) {	// add a column that exists in 2015 and later (Ctn/Comp) but not exist in 2014 and earlier
							this_fire = this_fire + "\t" + "null";
						}
						if (id == line_split.get(i).length - 13) {	// this is "unit", we need to connect to "st" column with a "-"
							this_fire = this_fire + "-" + line_split.get(i)[id];
						} else {
							this_fire = String.join("\t", this_fire, line_split.get(i)[id]);	// this is all information in one whole line of this fire
						}
					}
					r_fires.add(this_fire);
				} else if (line_length >= 12 
						&& line_split.get(i)[line_length - 12].length() == 2    // This is State "St" for 2014 and earlier year. It must have only 2 characters
						&& (line_split.get(i)[line_length - 10].matches("^\\d{1,3}([ ,]?\\d{3})*([.,]\\d+)?$") || line_split.get(i)[line_length - 10].equals("N/A"))
						&& line_split.get(i)[line_length - 7].matches("^\\d{1,3}([ ,]?\\d{3})*([.,]\\d+)?$")) {		// this is likely a fire, smart check based on the "acres" and "personnel total" columns.
					unit_id = line_split.get(i).length - 12;
					String this_fire = String.join("\t", r_date, current_area, gacc_priority, fire_priority);
					
					// Check above lines, if length <=5 etc, then add to fire name
					// Note the special case (20140808IMSR - Devils Elbow Complex) where -simple2 command do not have expected order of fire name in 2 lines.
					String fire_name = "";
					for (int id = 0; id < unit_id; id++) {
						fire_name = String.join(" ", fire_name, line_split.get(i)[id]);	// this is the incident name (or part of the name that is in the same line with other fire information), join by space
					}
					// loop back previous lines to get the full fire name
					boolean continue_loop = true;
					int l = i;
					do {
						l = l - 1;
						if (line_split.get(l).length <= 5 && !r_lines[l].toUpperCase().endsWith("OWN") && !r_lines[l].toUpperCase().endsWith("HELI")) {		// HELI is special case for 20150102
							fire_name = String.join(" ", r_lines[l], fire_name);	// join by space
						} else {
							continue_loop = false;
						}
					} while (continue_loop);
					fire_name = fire_name.replaceAll("\\*", "").replaceAll("\\s{2,}", " ").trim().toUpperCase();	// This will remove the * (if exist in the name) and change the name to capital (IMPORTANT)
					
					this_fire = String.join("\t", this_fire, fire_name);
					for (int id = unit_id; id < line_split.get(i).length; id++) {
						if (id == line_split.get(i).length - 6) {	// add a column that exists in 2015 and later (Personnel Chge) but not in this case. 
							this_fire = this_fire + "\t" + "null";
						}
						if (id == line_split.get(i).length - 8) {	// add a column that exists in 2015 and later (Ctn/Comp) but not exist in 2014 and earlier
							this_fire = this_fire + "\t" + "null";
						}
						if (id == line_split.get(i).length - 9) {	// add a column that exists in 2015 and later (Size Chge) but not in this case. 
							this_fire = this_fire + "\t" + "null";
						}
						if (id == line_split.get(i).length - 11) {	// this is "unit", we need to connect to "st" column with a "-"
							this_fire = this_fire + "-" + line_split.get(i)[id];
						} else {
							this_fire = String.join("\t", this_fire, line_split.get(i)[id]);	// this is all information in one whole line of this fire
						}
					}
					r_fires.add(this_fire);
				}
			} else { // from 2015 we use this to process data
				if (line_length >= 14 
						&& line_split.get(i)[line_length - 14].contains("-")    // do not use this for 2014 data, it has a different unit name, also in 20210710IMSR, one fire has wrong unit that cannot be included (Butte Creek: ID- CTS)
						&& (line_split.get(i)[line_length - 13].matches("^\\d{1,3}([ ,]?\\d{3})*([.,]\\d+)?$") || line_split.get(i)[line_length - 13].equals("N/A")) // special cases such as U.S. Virgin in 20171004
						&& line_split.get(i)[line_length - 8].matches("^\\d{1,3}([ ,]?\\d{3})*([.,]\\d+)?$")		// this is likely a fire, smart check based on the "acres" and "personnel total" columns.
				) {
					unit_id = line_split.get(i).length - 14;
					String this_fire = String.join("\t", r_date, current_area, gacc_priority, fire_priority);
					
					// Check above lines, if length <=5 etc, then add to fire name
					String fire_name = "";
					for (int id = 0; id < unit_id; id++) {
						fire_name = String.join(" ", fire_name, line_split.get(i)[id]);	// this is the incident name (or part of the name that is in the same line with other fire information), join by space
					}
					// loop back previous lines to get the full fire name
					boolean continue_loop = true;
					int l = i;
					do {
						l = l - 1;
						if (line_split.get(l).length <= 5 && !r_lines[l].toUpperCase().endsWith("OWN") && !r_lines[l].toUpperCase().endsWith("HELI")) {		// HELI is special case for 20150102
							fire_name = String.join(" ", r_lines[l], fire_name);	// join by space
						} else {
							continue_loop = false;
						}
					} while (continue_loop);
					fire_name = fire_name.replaceAll("\\*", "").replaceAll("\\s{2,}", " ").trim().toUpperCase();	// This will remove the * (if exist in the name) and change the name to capital (IMPORTANT)
					
					this_fire = String.join("\t", this_fire, fire_name);
					for (int id = unit_id; id < line_split.get(i).length; id++) {
						if (id >= 0)
						this_fire = String.join("\t", this_fire, line_split.get(i)[id]);	// this is all information in one whole line of this fire
					}
					r_fires.add(this_fire);
				} else if (line_length >= 13 
							&& (r_lines[i - 1].contains("-") || r_lines[i - 2].contains("-"))    // i-2 such as in 20150613
							&& (line_split.get(i)[line_length - 13].matches("^\\d{1,3}([ ,]?\\d{3})*([.,]\\d+)?$") || line_split.get(i)[line_length - 13].equals("N/A")) // special cases such as U.S. Virgin in 20171004
							&& line_split.get(i)[line_length - 8].matches("^\\d{1,3}([ ,]?\\d{3})*([.,]\\d+)?$")		// this is likely a fire, smart check based on the "acres" and "personnel total" columns.
				) {
					String this_fire = String.join("\t", r_date, current_area, gacc_priority, fire_priority);
					String unit_name = "";
					if (r_lines[i - 1].contains("-")) unit_name = r_lines[i - 1];
					if (r_lines[i - 2].contains("-")) unit_name = r_lines[i - 2] + r_lines[i - 1];
					
					// Check above lines, if length <=5 etc, then add to fire name
					String fire_name = "";
					// loop back previous lines to get the full fire name
					boolean continue_loop = true;
					int l = i;
					if (r_lines[i - 1].contains("-")) l = i - 1;
					if (r_lines[i - 2].contains("-")) l = i - 2;
					do {
						l = l - 1;
						if (line_split.get(l).length <= 5 && !r_lines[l].toUpperCase().endsWith("OWN") && !r_lines[l].toUpperCase().endsWith("HELI")) {		// HELI is special case for 20150102
							fire_name = String.join(" ", r_lines[l], fire_name);	// join by space
						} else {
							continue_loop = false;
						}
					} while (continue_loop);
					fire_name = fire_name.replaceAll("\\*", "").replaceAll("\\s{2,}", " ").trim().toUpperCase();	// This will remove the * (if exist in the name) and change the name to capital (IMPORTANT)
					
					this_fire = String.join("\t", this_fire, fire_name, unit_name);
					int size_id = line_split.get(i).length - 13;
					for (int id = size_id; id < line_split.get(i).length; id++) {
						if (id >= 0)
						this_fire = String.join("\t", this_fire, line_split.get(i)[id]);	// this is all information in one whole line of this fire
					}
					r_fires.add(this_fire);
					
					if (this_fire.split("\t").length < 19) System.out.println(this_fire);
				} else if (line_length >= 11 && 
							 (
								((line_split.get(i)[line_length - 1].endsWith("NR") || line_split.get(i)[line_length - 1].endsWith("K") || line_split.get(i)[line_length - 1].endsWith("M"))
										&& line_split.get(i + 1).length == 1 && line_split.get(i)[line_length - 8].matches("^\\d{1,3}([ ,]?\\d{3})*([.,]\\d+)?$"))
							 || 
							 	((line_split.get(i)[line_length - 2].endsWith("NR") || line_split.get(i)[line_length - 2].endsWith("K") || line_split.get(i)[line_length - 2].endsWith("M"))
									 	&& line_split.get(i)[line_length - 7].matches("^\\d{1,3}([ ,]?\\d{3})*([.,]\\d+)?$"))
							 )
						  ) {	// handle special cases such as in 20180726
					String this_fire = String.join("\t", r_date, current_area, gacc_priority, fire_priority);
					String combine_st = String.join("\t", line_split.get(i));
					
					// Check above lines, if length <=5 etc, then add to fire name
					if ((line_split.get(i)[line_length - 1].endsWith("NR") || line_split.get(i)[line_length - 1].endsWith("K") || line_split.get(i)[line_length - 1].endsWith("M"))
							&& line_split.get(i + 1).length == 1) {	// ctd column and next column is origin_own that contains only 1 word
						combine_st = String.join("\t", combine_st, line_split.get(i + 1)[0]);
					}
					
					String fire_name = "";
					// loop back previous lines to join
					boolean continue_loop = true;
					int l = i;
					do {
						l = l - 1;
						if ((r_fires.isEmpty() || !r_fires.get(r_fires.size() - 1).endsWith(r_lines[l])) &&	// this is to ensure we don't use the origin_own of previous fire in the name of this fire
								line_split.get(l).length <= 5 && !r_lines[l].toUpperCase().endsWith("OWN") && !r_lines[l].toUpperCase().endsWith("HELI")) {		// HELI is special case for 20150102
							if (r_lines[l].contains(" ")) {
								String previous_words = r_lines[l].substring(0, r_lines[l].lastIndexOf(" "));
								String last_word = r_lines[l].substring(r_lines[l].lastIndexOf(" ") + 1, r_lines[l].length());
								String final_word = "";
								if (last_word.equals("-")) { // such as "Michael - " in 20181022
									final_word = String.join(" ", previous_words, last_word);
								} else if (last_word.contains("-")) {
									final_word = String.join("\t", previous_words, last_word);
								} else {
									final_word = String.join(" ", previous_words, last_word);
								}
								fire_name = String.join(" ", final_word, fire_name);	// join by space
							} else {
								fire_name = String.join(" ", r_lines[l], fire_name);	// join by space
							}
						} else {
							continue_loop = false;
						}
					} while (continue_loop);
					
					fire_name = fire_name.replaceAll("---", "---" + "\t");	// special case such as 20180913
					fire_name = fire_name.replaceAll("\\*", "").replaceAll("\\s{2,}", " ").trim().toUpperCase();	// This will remove the * (if exist in the name) and change the name to capital (IMPORTANT)
					// if combine_st has length > 14 then part of fire_name is in it. We need to adjust the name
					String[] combine_st_arr = combine_st.split("\t");
					if (combine_st_arr.length > 14) {
						for (int j = 0; j < combine_st_arr.length - 14; j++) {
							fire_name = String.join(" ", fire_name, combine_st_arr[j]);	// join by space
						}
						fire_name = fire_name.replaceAll("\\*", "").replaceAll("\\s{2,}", " ").trim().toUpperCase();	// This will remove the * (if exist in the name) and change the name to capital (IMPORTANT)
						for (int j = combine_st_arr.length - 14; j < combine_st_arr.length; j++) {
							fire_name = String.join("\t", fire_name, combine_st_arr[j]);	// join by space	// this is all the other columns attached after the fire name
						}
						this_fire = String.join("\t", this_fire, fire_name);
					} else {
						this_fire = String.join("\t", this_fire, fire_name, combine_st);
					}
					if (this_fire.split("\t").length == 19) {
						r_fires.add(this_fire);
					} else {
						System.out.println(this_fire);	// problem need to fix manually: i.e. CA-KNF-006098 Complex 20170929
					}
				}
			}
			
			i = i + 1;
		} while (i < r_lines.length);
	}
	
	private void fire_name_validation_and_adjustment() {
		// hashmap to store pattern and id
		LinkedHashMap<String, Integer> map_pattern_to_r_fire_id = new LinkedHashMap<String, Integer>();
		for (int i = 0; i < r_fires.size(); i++) {
			String[] r_fire_info = r_fires.get(i).split("\t"); 
			// pattern = "date", "unit", "size_acres", "size_chge", "percentage", "ctn_comp", "est", "personnel_total", "personnel_chge", "resources_crw", "resources_eng", "resources_heli", "strc_lost", "ctd", "origin_own"
			String pattern = String.join("\t", r_fire_info[0], r_fire_info[5], r_fire_info[6], r_fire_info[7],
					r_fire_info[8], r_fire_info[9], r_fire_info[10], r_fire_info[11], r_fire_info[12], r_fire_info[13],
					r_fire_info[14], r_fire_info[15], r_fire_info[16], r_fire_info[17], r_fire_info[18]);
			map_pattern_to_r_fire_id.put(pattern, i);		// pattern = key, id = value		
		}
		if (r_fires.size() != s_fires.size()) {
			System.out.println(date + " has different results between raw and simple2: " + r_fires.size() + " " + s_fires.size());
		}
		if (r_fires.size() != map_pattern_to_r_fire_id.size()) {
			System.out.println(date + " " + r_fires.size() + " " + map_pattern_to_r_fire_id.size() + ": same pattern is identified but added once in hashmap");
		}
		
		// use hashmap to find matching pattern, then if names overlap between raw and simple we can adjust.
		int rename_count = 0;
		for (int i = 0; i < s_fires.size(); i++) {
			String[] s_fire_info = s_fires.get(i).split("\t"); 
			// pattern = "date", "unit", "size_acres", "size_chge", "percentage", "ctn_comp", "est", "personnel_total", "personnel_chge", "resources_crw", "resources_eng", "resources_heli", "strc_lost", "ctd", "origin_own"
			String pattern = String.join("\t", s_fire_info[0], s_fire_info[5], s_fire_info[6], s_fire_info[7],
					s_fire_info[8], s_fire_info[9], s_fire_info[10], s_fire_info[11], s_fire_info[12], s_fire_info[13],
					s_fire_info[14], s_fire_info[15], s_fire_info[16], s_fire_info[17], s_fire_info[18]);
			if (map_pattern_to_r_fire_id.get(pattern) != null) {
				int r_id = map_pattern_to_r_fire_id.get(pattern);
				String r_fire_name = r_fires.get(r_id).split("\t")[4];
				String s_fire_name = s_fire_info[4];
				String s_last_name = s_fire_name.substring(s_fire_name.lastIndexOf(" ") + 1);
				if (r_fire_name.contains(s_last_name)) {
					s_fire_info[4] = r_fire_name;	// adjust fire name
					rename_count++;
				}
			}
			final_fires.add(String.join("\t", s_fire_info));	// adjust fire name
		}
		int not_rename_count = s_fires.size() - rename_count;
		if (not_rename_count > 0) System.out.println(date + " " + not_rename_count + " has not been renamed");
	}
}
