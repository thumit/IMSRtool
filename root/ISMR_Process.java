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
		boolean year_before_2015 = Integer.valueOf(r_date.substring(0, 4)) < 2015;	// first 4 letters   
		// Pre-processing for some special cases
		for (int i = 0; i < s_lines.length; i++) {
			if (s_lines[i].length() == 3 && s_lines[i].endsWith("-")) {		// special case where unit is split into 2 lines such as 20150613, then we merge 2 lines and make the one line empty
				s_lines[i + 1] = s_lines[i] + s_lines[i + 1];
				s_lines[i] = "";
			}
			// in many cases such as 20080201, negative number is represented by - and a space then number. This is to merger the sign and the number.
			// Also in some cases ctd contains a space between the number and M, we need to join as well
			if (year_before_2015) s_lines[i] = s_lines[i].replaceAll(" - ", " -").replaceAll(" M ", "- ");
		}
		//------------------------------------------------------------
		
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
			
			// (Note that from 2007-05-28 to the end of 2014, there are 15 columns for each fire)
			if (year_before_2015) { // before 2015 we use this to process data (Note unit is split by 2 columns and we need to merge, also we do not have the "Ctn/Comp" and we need to assign Ctn for it)
				if (line_length >= 15 
						&& line_split.get(i)[line_length - 14].length() == 2    // This is State "St" for 2014 and earlier year. It must have only 2 characters
						&& (line_split.get(i)[line_length - 12].matches("^-?\\d{1,3}([ ,]?\\d{3})*([.,]\\d+)?$") || line_split.get(i)[line_length - 12].equals("N/A") || line_split.get(i)[line_length - 12].equals("---")) 
						&& (line_split.get(i)[line_length - 8].matches("^-?\\d{1,3}([ ,]?\\d{3})*([.,]\\d+)?$") || line_split.get(i)[line_length - 8].equals("UNK") || line_split.get(i)[line_length - 8].equals("NR") || line_split.get(i)[line_length - 8].equals("---"))
						) {		// this is likely a fire, smart check based on the "acres" and "personnel total" columns.
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
						&& (line_split.get(i)[line_length - 10].matches("^-?\\d{1,3}([ ,]?\\d{3})*([.,]\\d+)?$") || line_split.get(i)[line_length - 10].equals("N/A") || line_split.get(i)[line_length - 10].equals("---"))
						&& (line_split.get(i)[line_length - 7].matches("^-?\\d{1,3}([ ,]?\\d{3})*([.,]\\d+)?$") || line_split.get(i)[line_length - 7].equals("UNK") || line_split.get(i)[line_length - 7].equals("NR") || line_split.get(i)[line_length - 7].equals("---"))
						) {		// this is likely a fire, smart check based on the "acres" and "personnel total" columns.
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
						&& (line_split.get(i)[line_length - 13].matches("^-?\\d{1,3}([ ,]?\\d{3})*([.,]\\d+)?$") || line_split.get(i)[line_length - 13].equals("N/A") || line_split.get(i)[line_length - 13].equals("---")) // special cases N/A such as U.S. Virgin in 20171004, --- such as in 20210822
						&& (line_split.get(i)[line_length - 8].matches("^-?\\d{1,3}([ ,]?\\d{3})*([.,]\\d+)?$") || line_split.get(i)[line_length - 8].equals("UNK") || line_split.get(i)[line_length - 8].equals("NR") || line_split.get(i)[line_length - 8].equals("---"))
					) {		// this is likely a fire, smart check based on the "acres" and "personnel total" columns.
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
		boolean year_before_2015 = Integer.valueOf(r_date.substring(0, 4)) < 2015;	// first 4 letters   
		// Pre-processing for some special cases
		for (int i = 0; i < r_lines.length; i++) {
			if (r_lines[i].length() == 3 && r_lines[i].endsWith("-")) {		// special case where unit is split into 2 lines such as 20150613, then we merge 2 lines and make the one line empty
				r_lines[i + 1] = r_lines[i] + r_lines[i + 1];
				r_lines[i] = "";
			}
			// Also in some cases ctd contains a space between the number and M, we need to join as well
			if (year_before_2015) r_lines[i] = r_lines[i].replaceAll(" - ", " -").replaceAll(" M ", "- ");
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
			
			// (Note that from 2007-05-28 to the end of 2014, there are 15 columns for each fire)
			if (year_before_2015) { // before 2015 we use this to process data (Note unit is split by 2 columns and we need to merge, also we do not have the "Ctn/Comp" and we need to assign Ctn for it)
				if (line_length >= 14 
						&& line_split.get(i)[line_length - 14].length() == 2    // This is State "St" for 2014 and earlier year. It must have only 2 characters
						&& (line_split.get(i)[line_length - 12].matches("^-?\\d{1,3}([ ,]?\\d{3})*([.,]\\d+)?$") || line_split.get(i)[line_length - 12].equals("N/A") || line_split.get(i)[line_length - 12].equals("---"))
						&& (line_split.get(i)[line_length - 8].matches("^-?\\d{1,3}([ ,]?\\d{3})*([.,]\\d+)?$") || line_split.get(i)[line_length - 8].equals("UNK") || line_split.get(i)[line_length - 8].equals("NR") || line_split.get(i)[line_length - 8].equals("---"))
					) {		// this is likely a fire, smart check based on the "acres" and "personnel total" columns.
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
						if (line_split.get(l).length <= 5 && !r_lines[l].substring(r_lines[l].lastIndexOf(" ") + 1).toUpperCase().equals("OWN") && !r_lines[l].toUpperCase().endsWith("HELI")) {		// HELI is special case for 20150102
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
						&& (line_split.get(i)[line_length - 10].matches("^-?\\d{1,3}([ ,]?\\d{3})*([.,]\\d+)?$") || line_split.get(i)[line_length - 10].equals("N/A") || line_split.get(i)[line_length - 10].equals("---"))
						&& (line_split.get(i)[line_length - 7].matches("^-?\\d{1,3}([ ,]?\\d{3})*([.,]\\d+)?$") || line_split.get(i)[line_length - 7].equals("UNK") || line_split.get(i)[line_length - 7].equals("NR") || line_split.get(i)[line_length - 7].equals("---"))
						) {		// this is likely a fire, smart check based on the "acres" and "personnel total" columns.
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
						if (line_split.get(l).length <= 5 && !r_lines[l].substring(r_lines[l].lastIndexOf(" ") + 1).toUpperCase().equals("OWN") && !r_lines[l].toUpperCase().endsWith("HELI")) {		// HELI is special case for 20150102
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
				} else if (line_length >= 6 && 
						 (
							((line_split.get(i)[line_length - 1].endsWith("NR") || line_split.get(i)[line_length - 1].endsWith("K") || line_split.get(i)[line_length - 1].endsWith("M"))
									&& line_split.get(i + 1).length == 1 
									&& line_split.get(i)[line_length - 2].matches("^-?\\d{1,3}([ ,]?\\d{3})*([.,]\\d+)?$")
									&& line_split.get(i)[line_length - 3].matches("^-?\\d{1,3}([ ,]?\\d{3})*([.,]\\d+)?$")
							)
						 || 
						 	((line_split.get(i)[line_length - 2].endsWith("NR") || line_split.get(i)[line_length - 2].endsWith("K") || line_split.get(i)[line_length - 2].endsWith("M"))
									&& line_split.get(i)[line_length - 3].matches("^-?\\d{1,3}([ ,]?\\d{3})*([.,]\\d+)?$")
									&& line_split.get(i)[line_length - 4].matches("^-?\\d{1,3}([ ,]?\\d{3})*([.,]\\d+)?$")
							)		
						 )
					  ) {	// is there any fire else?
					System.out.println( date + ": " + "Is this a fire we forgot to add when processing raw?   " + r_lines[i]);
				}
			} else { // from 2015 we use this to process data
				if (line_length >= 14 
						&& line_split.get(i)[line_length - 14].contains("-")    // do not use this for 2014 data, it has a different unit name, also in 20210710IMSR, one fire has wrong unit that cannot be included (Butte Creek: ID- CTS)
						&& (line_split.get(i)[line_length - 13].matches("^-?\\d{1,3}([ ,]?\\d{3})*([.,]\\d+)?$") || line_split.get(i)[line_length - 13].equals("N/A") || line_split.get(i)[line_length - 13].equals("---")) // special cases such as U.S. Virgin in 20171004
						&& (line_split.get(i)[line_length - 8].matches("^-?\\d{1,3}([ ,]?\\d{3})*([.,]\\d+)?$") || line_split.get(i)[line_length - 8].equals("UNK") || line_split.get(i)[line_length - 8].equals("NR") || line_split.get(i)[line_length - 8].equals("---"))
								// this is likely a fire, smart check based on the "acres" and "personnel total" columns.
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
						if (line_split.get(l).length <= 5 && !r_lines[l].substring(r_lines[l].lastIndexOf(" ") + 1).toUpperCase().equals("OWN") && !r_lines[l].toUpperCase().endsWith("HELI")) {		// HELI is special case for 20150102
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
							&& (line_split.get(i)[line_length - 13].matches("^-?\\d{1,3}([ ,]?\\d{3})*([.,]\\d+)?$") || line_split.get(i)[line_length - 13].equals("N/A") || line_split.get(i)[line_length - 13].equals("---")) // special cases such as U.S. Virgin in 20171004
							&& (line_split.get(i)[line_length - 8].matches("^-?\\d{1,3}([ ,]?\\d{3})*([.,]\\d+)?$") || line_split.get(i)[line_length - 8].equals("UNK") || line_split.get(i)[line_length - 8].equals("NR") || line_split.get(i)[line_length - 8].equals("---"))
									// this is likely a fire, smart check based on the "acres" and "personnel total" columns.
				) {
					String this_fire = String.join("\t", r_date, current_area, gacc_priority, fire_priority);
					String unit_name = "";
					if (r_lines[i - 1].contains("-")) {
						unit_name = r_lines[i - 1];
					} else {
						if (r_lines[i - 2].contains("-")) unit_name = r_lines[i - 2] + r_lines[i - 1];
					}
					
					// Check above lines, if length <=5 etc, then add to fire name
					String fire_name = "";
					// fix the special case: Trestle 20180727 where unit still cotain part of fire name
					String[] unit_contain_fire_name = unit_name.replaceAll("\\*", "").trim().split(" ");	// replace * and trim to handle special case such as Cranston in 20180726
					if (unit_contain_fire_name.length == 2) {
						fire_name = unit_contain_fire_name[0];
						unit_name = unit_contain_fire_name[1];
					}
					
					// loop back previous lines to get the full fire name
					boolean continue_loop = true;
					int l = i;
					if (r_lines[i - 1].contains("-")) {
						l = i - 1;
					} else {
						if (r_lines[i - 2].contains("-")) l = i - 2;
					}
					do {
						l = l - 1;
						if (line_split.get(l).length <= 5 && !r_lines[l].substring(r_lines[l].lastIndexOf(" ") + 1).toUpperCase().equals("OWN") && !r_lines[l].toUpperCase().endsWith("HELI")) {		// HELI is special case for 20150102
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
				} else if (line_length >= 10 && 
							 (
								((line_split.get(i)[line_length - 1].endsWith("NR") || line_split.get(i)[line_length - 1].endsWith("K") || line_split.get(i)[line_length - 1].endsWith("M"))
										&& line_split.get(i + 1).length == 1 
										&& (line_split.get(i)[line_length - 7].matches("^-?\\d{1,3}([ ,]?\\d{3})*([.,]\\d+)?$") || line_split.get(i)[line_length - 7].equals("UNK") || line_split.get(i)[line_length - 7].equals("NR") || line_split.get(i)[line_length - 7].equals("---")))
							 || 
							 	((line_split.get(i)[line_length - 2].endsWith("NR") || line_split.get(i)[line_length - 2].endsWith("K") || line_split.get(i)[line_length - 2].endsWith("M"))
									 	&& (line_split.get(i)[line_length - 8].matches("^-?\\d{1,3}([ ,]?\\d{3})*([.,]\\d+)?$") || line_split.get(i)[line_length - 8].equals("UNK") || line_split.get(i)[line_length - 8].equals("NR") || line_split.get(i)[line_length - 8].equals("---")))
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
					if (combine_st.startsWith("---0")) {	// special case such as 20180913 in AICC ---0 is shown in the raw txt as 2 lines but actually they are in a single line after reading.
						combine_st = combine_st.replace("---0", "---" + "\t" + "0");	// this is because line i-1 shown as --- but it is actually not after reading
						combine_st = String.join("\t", r_lines[i - 2].replace(" ", "\t"), combine_st);
						fire_name = r_lines[i - 3];
						l = i - 2;
					} else 
					if (combine_st.startsWith("Ctn") && r_lines[i - 1].matches("^-?\\d{1,3}([ ,]?\\d{3})*([.,]\\d+)?$") && r_lines[i - 2].contains("-")) {	// special case such as Spokane Complex 20180824
						combine_st = String.join("\t", r_lines[i - 2].replace(" ", "\t"), r_lines[i - 1], combine_st);
						fire_name = r_lines[i - 3];
						l = i - 2;
					}
					do {
						l = l - 1;
						if ((r_fires.isEmpty() || !r_fires.get(r_fires.size() - 1).endsWith(r_lines[l])) &&	// this is to ensure we don't use the origin_own of previous fire in the name of this fire
								line_split.get(l).length <= 5 && !r_lines[l].substring(r_lines[l].lastIndexOf(" ") + 1).toUpperCase().equals("OWN") && !r_lines[l].toUpperCase().endsWith("HELI")) {		// HELI is special case for 20150102
							if (r_lines[l].contains(" ")) {
								String previous_words = r_lines[l].substring(0, r_lines[l].lastIndexOf(" "));
								String last_word = r_lines[l].substring(r_lines[l].lastIndexOf(" ") + 1, r_lines[l].length());
								String final_word = "";
								if (last_word.equals("-")) { // such as "Michael - " in 20181022
									final_word = String.join(" ", previous_words, last_word);
								} else if (last_word.contains("-")) {	// this is unit
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
						// problem need to fix manually: i.e. CA-KNF-006098 Complex 20170929
						if (this_fire.equals("2017-09-29	ONCC	NA	NA	CA-KNF- 006098 COMPLEX CA-KNF 78,698 .4	0	51	Comp	10/10	318	1	5	11	2	0	44.5M	FS")) {
							System.out.println("this fire is fixed and added manually");
							System.out.println("old info:     " + this_fire);
							this_fire =  "2017-09-29	ONCC	NA	NA	CA-KNF-006098 COMPLEX	CA-KNF	78,698	0	51	Comp	10/10	318	1	5	11	2	0	44.5M	FS";
							System.out.println("new info:     " + this_fire);
							r_fires.add(this_fire);
						} else if (this_fire.equals("2015-06-01	SACC	NA	NA	MUD LAKE COMPLEX	FL-BCP	35,321	0	70	Comp	260	-32	4	7	6	0	7.6M	NPS")) {
							System.out.println("this fire is fixed and added manually");
							System.out.println("old info:     " + this_fire);
							this_fire =  "2015-06-01	SACC	NA	NA	MUD LAKE COMPLEX	FL-BCP	35,321	0	70	Comp	---	260	-32	4	7	6	0	7.6M	NPS";
							System.out.println("new info:     " + this_fire);
							r_fires.add(this_fire);
						}  else if (this_fire.equals("2015-06-01	SACC	NA	NA	BOLIN SLIME PIT	FL-FLS	300	---	95	Ctn	2	---	0	0	0	0	1K	ST")) {
							System.out.println("this fire is fixed and added manually");
							System.out.println("old info:     " + this_fire);
							this_fire =  "2015-06-01	SACC	NA	NA	BOLIN SLIME PIT	FL-FLS	300	---	95	Ctn	---	2	---	0	0	0	0	1K	ST";
							System.out.println("new info:     " + this_fire);
							r_fires.add(this_fire);
						} else {
							System.out.println( date + ": " +"Is this a fire we did not add (length not 19) when processing raw?   " + this_fire);
						}
					}
				} else if (line_length >= 6 && 
						 (
							((line_split.get(i)[line_length - 1].endsWith("NR") || line_split.get(i)[line_length - 1].endsWith("K") || line_split.get(i)[line_length - 1].endsWith("M"))
									&& line_split.get(i + 1).length == 1 
									&& line_split.get(i)[line_length - 2].matches("^-?\\d{1,3}([ ,]?\\d{3})*([.,]\\d+)?$")
									&& line_split.get(i)[line_length - 3].matches("^-?\\d{1,3}([ ,]?\\d{3})*([.,]\\d+)?$")
							)
						 || 
						 	((line_split.get(i)[line_length - 2].endsWith("NR") || line_split.get(i)[line_length - 2].endsWith("K") || line_split.get(i)[line_length - 2].endsWith("M"))
									&& line_split.get(i)[line_length - 3].matches("^-?\\d{1,3}([ ,]?\\d{3})*([.,]\\d+)?$")
									&& line_split.get(i)[line_length - 4].matches("^-?\\d{1,3}([ ,]?\\d{3})*([.,]\\d+)?$")
							)		
						 )
					  ) {	// is there any fire else?
//					if (r_lines[i].equals("")) {
//						System.out.println("this fire is fixed and added manually");
//						System.out.println("old info:     " + r_lines[i]);
//						String this_fire =  "";
//						System.out.println("new info:     " + this_fire);
//						r_fires.add(this_fire);
//					}
					
					String current_merge_info = r_lines[i].replaceAll("---", "---" + " ").replaceAll("\\s{2,}", " ");			// special case such as "---0" in 20180930
					// loop back previous lines to join, loop 6 previous lines at max
					boolean continue_loop = true;
					int l = i;
					do {
						l = l - 1;
						if (r_lines[l].isBlank()) l = l - 1;	// this is very special case where the line is empty
						if ((r_fires.isEmpty() || !r_fires.get(r_fires.size() - 1).endsWith(r_lines[l])) &&	// this is to ensure we don't use the origin_own of previous fire in the name of this fire
								(r_lines[l].toUpperCase().endsWith("CTN") || r_lines[l].toUpperCase().endsWith("COMP") || line_split.get(l).length <= 5) && !r_lines[l].substring(r_lines[l].lastIndexOf(" ") + 1).toUpperCase().equals("OWN") && !r_lines[l].toUpperCase().endsWith("HELI")) {		// HELI is special case for 20150102
							current_merge_info = String.join(" ", r_lines[l].replaceAll("---", "---" + " ").replaceAll("\\s{2,}", " "), current_merge_info);	// join by space
						} else {
							continue_loop = false;
						}
					} while (continue_loop);
					String[] info_split = current_merge_info.split(" ");
					String this_fire = String.join("\t", r_date, current_area, gacc_priority, fire_priority);
					String fire_name = "";
					if (info_split.length > 14) {
						for (int k = 0; k < info_split.length - 14; k++) {
							fire_name = String.join(" ", fire_name, info_split[k]);	// This is part of fire name
						}
						fire_name = fire_name.replaceAll("\\*", "").replaceAll("\\s{2,}", " ").trim().toUpperCase();	// This will remove the * (if exist in the name) and change the name to capital (IMPORTANT)
						this_fire = String.join("\t", this_fire, fire_name);
						for (int k = info_split.length - 14; k < info_split.length; k++) {
							this_fire = String.join("\t", this_fire, info_split[k]);
						}
					}
					
					if (this_fire.split("\t").length == 19) {
						r_fires.add(this_fire);
					} else {
						System.out.println(date + ": " +"Is this a fire we did not add (length >=6) when processing raw?   " + r_lines[i]);
						System.out.println(current_merge_info);
					}
				}
			}
			
			i = i + 1;
		} while (i < r_lines.length);
	}
	
	private void fire_name_validation_and_adjustment() {
		// list to store pattern and id
		List<String[]> s_fire_list = new ArrayList<String[]>();
		for (int i = 0; i < s_fires.size(); i++) {
			String[] s_fire_info = s_fires.get(i).split("\t"); 
			s_fire_list.add(s_fire_info);
		}
		List<String[]> r_fire_list = new ArrayList<String[]>();
		for (int i = 0; i < r_fires.size(); i++) {
			String[] r_fire_info = r_fires.get(i).split("\t"); 
			r_fire_list.add(r_fire_info);
		}
		
		List<String> r_pattern_1 = new ArrayList<String>();		// check full info in some columns
		List<String> r_pattern_2 = new ArrayList<String>();		// check the initial character in some columns
		for (int i = 0; i < r_fires.size(); i++) {
			String[] r_fire_info = r_fires.get(i).split("\t"); 
			// pattern = "date", "unit", "size_acres", "size_chge", "percentage", "ctn_comp", "est", "personnel_total", "personnel_chge", "resources_crw", "resources_eng", "resources_heli", "strc_lost", "ctd", "origin_own"
			String pattern_1 = String.join("\t", r_fire_info[0], r_fire_info[5], r_fire_info[6], r_fire_info[7],
					r_fire_info[8], r_fire_info[9], r_fire_info[10], r_fire_info[11], r_fire_info[12], r_fire_info[13],
					r_fire_info[14], r_fire_info[15], r_fire_info[16], r_fire_info[17], r_fire_info[18]);
			String pattern_2 = String.join("\t", r_fire_info[0], r_fire_info[5].substring(0, 1), r_fire_info[6].substring(0, 1), r_fire_info[7].substring(0, 1),
					r_fire_info[8].substring(0, 1), r_fire_info[9].substring(0, 1), r_fire_info[10].substring(0, 1), r_fire_info[11].substring(0, 1), r_fire_info[12].substring(0, 1), r_fire_info[13].substring(0, 1),
					r_fire_info[14].substring(0, 1), r_fire_info[15].substring(0, 1), r_fire_info[16].substring(0, 1), r_fire_info[17].substring(0, 1), r_fire_info[18].substring(0, 1));
			r_pattern_1.add(pattern_1);
			r_pattern_2.add(pattern_2);
		}
				
		// find matching pattern, then if names overlap between raw and simple we can adjust.
		List<String> fire_in_s_not_in_r = new ArrayList<String>();
		int in_s_but_not_in_r_count = 0;
		for (int s_id = 0; s_id < s_fires.size(); s_id++) {
			String[] s_fire_info = s_fires.get(s_id).split("\t"); 
			// pattern = "date", "unit", "size_acres", "size_chge", "percentage", "ctn_comp", "est", "personnel_total", "personnel_chge", "resources_crw", "resources_eng", "resources_heli", "strc_lost", "ctd", "origin_own"
			String pattern_1 = String.join("\t", s_fire_info[0], s_fire_info[5], s_fire_info[6], s_fire_info[7],
					s_fire_info[8], s_fire_info[9], s_fire_info[10], s_fire_info[11], s_fire_info[12], s_fire_info[13],
					s_fire_info[14], s_fire_info[15], s_fire_info[16], s_fire_info[17], s_fire_info[18]);
			String pattern_2 = String.join("\t", s_fire_info[0], s_fire_info[5].substring(0, 1), s_fire_info[6].substring(0, 1), s_fire_info[7].substring(0, 1),
					s_fire_info[8].substring(0, 1), s_fire_info[9].substring(0, 1), s_fire_info[10].substring(0, 1), s_fire_info[11].substring(0, 1), s_fire_info[12].substring(0, 1), s_fire_info[13].substring(0, 1),
					s_fire_info[14].substring(0, 1), s_fire_info[15].substring(0, 1), s_fire_info[16].substring(0, 1), s_fire_info[17].substring(0, 1), s_fire_info[18].substring(0, 1));
			
			String s_fire_name = s_fire_info[4];
			String s_last_name = s_fire_name.substring(s_fire_name.lastIndexOf(" ") + 1);
			
			boolean overlap_found = false;
			int r_id = 0;
			do {
				String r_fire_name = r_fire_list.get(r_id)[4];
				if (r_fire_name.contains(s_last_name) && (r_pattern_1.get(r_id).equals(pattern_1) || r_pattern_2.get(r_id).equals(pattern_2))) {
					overlap_found = true;	// overlapped fire
				}
				r_id = r_id + 1;
			} while (r_id < r_fires.size() && !overlap_found);
			
			if (!overlap_found) {	// not overlapped fire
				in_s_but_not_in_r_count = in_s_but_not_in_r_count + 1;
				fire_in_s_not_in_r.add(s_fires.get(s_id));
			}
		}
		
		for (int i = 0; i < r_fires.size(); i++) {
			final_fires.add(r_fires.get(i));	
		}
		
//		if (r_fires.size() != s_fires.size()) {
//			System.out.println(date + ": different number of fires in raw vs simple2: " + r_fires.size() + " " + s_fires.size());
//		}
		
		if (fire_in_s_not_in_r.size() > 0) {
			System.out.println(date + " --------------------------------------- fires in simple2 list but not in raw list: " + fire_in_s_not_in_r.size());
			for (String st : fire_in_s_not_in_r) {
				System.out.println("simple2:     " + st);
				if (st.equals("2015-08-21	SACC	7	2	LANE FIRE	GA-BLR	337	---	85	Comp	8/25	4	---	0	1	0	0	13K	FWS")) {
					System.out.println("incorrect simple2 but correct raw:     " + st);
				}
			}
		}
	}
	
	private void fire_name_validation_and_adjustmen_old_not_use() {
		// list to store pattern and id
		List<String> r_pattern_list = new ArrayList<String>();
		for (int i = 0; i < r_fires.size(); i++) {
			String[] r_fire_info = r_fires.get(i).split("\t"); 
			// pattern = "date", "unit", "size_acres", "size_chge", "percentage", "ctn_comp", "est", "personnel_total", "personnel_chge", "resources_crw", "resources_eng", "resources_heli", "strc_lost", "ctd", "origin_own"
			String pattern = String.join("\t", r_fire_info[0], r_fire_info[5], r_fire_info[6], r_fire_info[7],
					r_fire_info[8], r_fire_info[9], r_fire_info[10], r_fire_info[11], r_fire_info[12], r_fire_info[13],
					r_fire_info[14], r_fire_info[15], r_fire_info[16], r_fire_info[17], r_fire_info[18]);
			r_pattern_list.add(pattern);
		}
		if (r_fires.size() != s_fires.size()) {
			System.out.println(date + " has different number of fires between raw and simple2: " + r_fires.size() + " " + s_fires.size());
		}
		
		// find matching pattern, then if names overlap between raw and simple we can adjust.
		int rename_count = 0;
		for (int i = 0; i < s_fires.size(); i++) {
			String[] s_fire_info = s_fires.get(i).split("\t"); 
			// pattern = "date", "unit", "size_acres", "size_chge", "percentage", "ctn_comp", "est", "personnel_total", "personnel_chge", "resources_crw", "resources_eng", "resources_heli", "strc_lost", "ctd", "origin_own"
			String pattern = String.join("\t", s_fire_info[0], s_fire_info[5], s_fire_info[6], s_fire_info[7],
					s_fire_info[8], s_fire_info[9], s_fire_info[10], s_fire_info[11], s_fire_info[12], s_fire_info[13],
					s_fire_info[14], s_fire_info[15], s_fire_info[16], s_fire_info[17], s_fire_info[18]);
			
			String s_fire_name = s_fire_info[4];
			String s_last_name = s_fire_name.substring(s_fire_name.lastIndexOf(" ") + 1);
			int r_id = r_pattern_list.indexOf(pattern);
			String r_fire_name = (r_id > -1)? r_fires.get(r_id).split("\t")[4] : "-9999";
			if (r_id > -1 && r_fire_name.contains(s_last_name)) {
				s_fire_info[4] = r_fire_name;	// adjust fire name
				rename_count = rename_count + 1;
			} else {
				System.out.println(date + " " + s_fire_info[4] + " has not been renamed");
			}
			final_fires.add(String.join("\t", s_fire_info));	// adjust fire name
		}
//		int not_rename_count = s_fires.size() - rename_count;
//		if (not_rename_count > 0) System.out.println(date + " " + not_rename_count + " has not been renamed");
	}
}
