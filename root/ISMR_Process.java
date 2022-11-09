package root;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
	String fire_use_teams_committed;
	List<String> national_activity = new ArrayList<String>();	// store all the above information
	
//	String gacc_prepareness_level;
//	String gacc_new_fires;
//	String gacc_new_large_incidents;
//	String gacc_uncontained_large_fires;
//	String gacc_area_command_teams_committed;
//	String gacc_NIMOs_committed;
//	String gacc_type_1_IMTs_committed;
//	String gacc_type_2_IMTs_committed;
//	String gacc_fire_use_teams_committed;
	List<String> gacc_activity = new ArrayList<String>();	// almost the same information structure as national activity, but the level is gacc area
	
	List<String> resource_summary = new ArrayList<String>();	// store all active incident resource summary information
	
	LinkedHashMap<String, String> manual_fire_adjustment_list = new LinkedHashMap<String, String>();
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
			generate_manual_fire_adjustment_list();
			
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
			data_cleaning();
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
		boolean continue_loop = true;
		int mergeCount = 0; 
		int l = -1;
		do {
			l = l + 1;
			if (s_lines[l].contains("Type 2 IMTs")) {
				mergeCount = l;
				continue_loop = false;
			} else {
			}
		} while (continue_loop && l < s_lines.length - 1);
		
		continue_loop = true;
		l = mergeCount;
		do {
			l = l + 1;
			if (s_lines[l].contains("Fire Use Teams")) {
				mergeCount = l;
				continue_loop = false;
			} else {
			}
		} while (continue_loop && l < s_lines.length - 1);
		
		String[] merge_lines = Arrays.copyOfRange(s_lines, 0, mergeCount + 1);
		String mstr = String.join(" ", merge_lines).toLowerCase().trim(); 
		mstr = mstr.replaceAll("fire use teams:", "fire use teams committed:");
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
				initial_attack_activity = null;
			} else {
				initial_attack_activity = temp.split(" ")[0].toUpperCase();
				if (initial_attack_activity.contains("(")) initial_attack_activity = initial_attack_activity.substring(0, initial_attack_activity.indexOf("(")); // special case: 20170620
				if (temp.split(" ").length > 1) {
					initial_attack_new_fires = (temp.substring(temp.indexOf("(") + 1, temp.indexOf(")")).replaceAll("new", "").replaceAll("fire", "").replaceAll("s", "")).trim();		// i.e. 20180915 is a special case, 20160626 is also special there are 2))
				}
			}
		}
		// Fix a special case 20170629: Light (169) --> stupid reversed information that needs to be switch
		if (initial_attack_activity != null && initial_attack_new_fires != null) {
			if (initial_attack_activity.matches("-?(0|[1-9]\\d*)") && !initial_attack_new_fires.matches("-?(0|[1-9]\\d*)")) {
				String swtich_st = initial_attack_activity;
				initial_attack_activity = initial_attack_new_fires;
				initial_attack_new_fires = swtich_st;
				if (initial_attack_activity != null && initial_attack_activity.length() > 1) {
					initial_attack_activity = initial_attack_activity.split(" ")[0].toUpperCase();
				}
			}
		}
		initial_attack_new_fires = temp.replaceAll("[^0-9.]", "");	// final adjustment for correct number of new fires
		
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
		if (mstr.indexOf(st) != - 1) {	// type 2 teams always exist
			temp = (mstr.substring(mstr.indexOf(st) + 22)).trim();		// Note: different from above, not use sub string between
			if (temp != null) type_2_imts_committed = temp.split(" ")[0];
		}
		if (l < s_lines.length - 1) {	// if fire use teams committed exists, we also need to read type 2 team in a different way
			st = "type 2 imts committed";
			temp = sb.substringBetween(mstr, st, get_national_next_term(mstr, st)); 
			if (temp != null) type_2_imts_committed = (temp.substring(temp.indexOf(" ") + 1)).trim();
			st = "fire use teams committed";
			if (mstr.indexOf(st) != - 1) {
				temp = (mstr.substring(mstr.indexOf(st) + 25)).trim();		// Note: different from above, not use sub string between
				if (temp != null) fire_use_teams_committed = temp.split(" ")[0];
			}
		}
		
		// If no data then make it null
		if (date == null || date.isBlank()) date = "null";
		if (national_prepareness_level == null || national_prepareness_level.isBlank()) national_prepareness_level = "null";
		if (initial_attack_activity == null || initial_attack_activity.isBlank()) initial_attack_activity = "null";
		if (initial_attack_new_fires == null || initial_attack_new_fires.isBlank()) initial_attack_new_fires = "null";
		if (new_large_incidents == null || new_large_incidents.isBlank()) new_large_incidents = "null";
		if (large_fires_contained == null || large_fires_contained.isBlank()) large_fires_contained = "null";
		if (uncontained_large_fires == null || uncontained_large_fires.isBlank()) uncontained_large_fires = "null";
		if (area_command_teams_committed == null || area_command_teams_committed.isBlank()) area_command_teams_committed = "null";
		if (nimos_committed == null || nimos_committed.isBlank()) nimos_committed = "null";
		if (type_1_imts_committed == null || type_1_imts_committed.isBlank()) type_1_imts_committed = "null";
		if (type_2_imts_committed == null || type_2_imts_committed.isBlank()) type_2_imts_committed = "null";
		if (fire_use_teams_committed == null || fire_use_teams_committed.isBlank()) fire_use_teams_committed = "null";
		
		national_activity.add(date);
		national_activity.add(national_prepareness_level);
		national_activity.add(initial_attack_activity);
		national_activity.add(initial_attack_new_fires);
		national_activity.add(new_large_incidents);
		national_activity.add(large_fires_contained);
		national_activity.add(uncontained_large_fires);
		national_activity.add(area_command_teams_committed);
		national_activity.add(nimos_committed);
		national_activity.add(type_1_imts_committed);
		national_activity.add(type_2_imts_committed);
		national_activity.add(fire_use_teams_committed);
	}
	
	private String get_national_next_term(String mstr, String st) {
		String temp = (mstr.substring(mstr.indexOf(st) + 1)).trim(); // remove 1 leading character then use it to find next term
		String[] term = new String[] { "national prepareness level", "national fire activity",
				"initial attack activity", "new large incidents", "new large fires", "large fires contained", "uncontained large fires",			// "new large fires" is a special case used for 2014 data instead of "new large incidents"
				"area command teams committed", "nimos committed", "type 1 imts committed", "type 2 imts committed", "fire use teams committed" };
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
		LinkedHashMap<String, Integer> map_gacc_to_priority = new LinkedHashMap<String, Integer>();
		String current_area = "";
		int gacc_priority = 0;
		// Loop all lines, whenever found a gacc area, stop and process data
		for (int i = 0; i < s_lines.length; i++) {
			if (s_lines[i].contains("(PL")) {
				if (s_lines[i].startsWith("Alaska")) {
					current_area = "AICC";
					gacc_priority = gacc_priority + 1;
					if (map_gacc_to_priority.get(current_area) == null) map_gacc_to_priority.put(current_area, gacc_priority);
					if (gacc_priority == map_gacc_to_priority.get(current_area)) process_area_data(s_lines, i, current_area, gacc_priority);
				} else if (s_lines[i].startsWith("Eastern Area")) {
					current_area = "EACC";
					gacc_priority = gacc_priority + 1;
					if (map_gacc_to_priority.get(current_area) == null) map_gacc_to_priority.put(current_area, gacc_priority);
					if (gacc_priority == map_gacc_to_priority.get(current_area)) process_area_data(s_lines, i, current_area, gacc_priority);
				} else if (s_lines[i].startsWith("Eastern Great Basin")) {	// before 2015
					current_area = "EBCC";
					gacc_priority = gacc_priority + 1;
					if (map_gacc_to_priority.get(current_area) == null) map_gacc_to_priority.put(current_area, gacc_priority);
					if (gacc_priority == map_gacc_to_priority.get(current_area)) process_area_data(s_lines, i, current_area, gacc_priority);
				}  else if (s_lines[i].startsWith("Western Great Basin")) {	// before 2015
					current_area = "WBCC";
					gacc_priority = gacc_priority + 1;
					if (map_gacc_to_priority.get(current_area) == null) map_gacc_to_priority.put(current_area, gacc_priority);
					if (gacc_priority == map_gacc_to_priority.get(current_area)) process_area_data(s_lines, i, current_area, gacc_priority);
				} else if (s_lines[i].startsWith("Great Basin")) {	// 2015 and after
					current_area = "GBCC";
					gacc_priority = gacc_priority + 1;
					if (map_gacc_to_priority.get(current_area) == null) map_gacc_to_priority.put(current_area, gacc_priority);
					if (gacc_priority == map_gacc_to_priority.get(current_area)) process_area_data(s_lines, i, current_area, gacc_priority);
				} else if (s_lines[i].startsWith("Northern California")) {
					current_area = "ONCC";
					gacc_priority = gacc_priority + 1;
					if (map_gacc_to_priority.get(current_area) == null) map_gacc_to_priority.put(current_area, gacc_priority);
					if (gacc_priority == map_gacc_to_priority.get(current_area)) process_area_data(s_lines, i, current_area, gacc_priority);
				} else if (s_lines[i].startsWith("Northern Rockies")) {
					current_area = "NRCC";
					gacc_priority = gacc_priority + 1;
					if (map_gacc_to_priority.get(current_area) == null) map_gacc_to_priority.put(current_area, gacc_priority);
					if (gacc_priority == map_gacc_to_priority.get(current_area)) process_area_data(s_lines, i, current_area, gacc_priority);
				} else if (s_lines[i].startsWith("Northwest")) {
					current_area = "NWCC";
					gacc_priority = gacc_priority + 1;
					if (map_gacc_to_priority.get(current_area) == null) map_gacc_to_priority.put(current_area, gacc_priority);
					if (gacc_priority == map_gacc_to_priority.get(current_area)) process_area_data(s_lines, i, current_area, gacc_priority);
				} else if (s_lines[i].startsWith("Rocky Mountain")) {
					current_area = "RMCC";
					gacc_priority = gacc_priority + 1;
					if (map_gacc_to_priority.get(current_area) == null) map_gacc_to_priority.put(current_area, gacc_priority);
					if (gacc_priority == map_gacc_to_priority.get(current_area)) process_area_data(s_lines, i, current_area, gacc_priority);
				} else if (s_lines[i].startsWith("Southern Area")) {
					current_area = "SACC";
					gacc_priority = gacc_priority + 1;
					if (map_gacc_to_priority.get(current_area) == null) map_gacc_to_priority.put(current_area, gacc_priority);
					if (gacc_priority == map_gacc_to_priority.get(current_area)) process_area_data(s_lines, i, current_area, gacc_priority);
				} else if (s_lines[i].startsWith("Southern California")) {
					current_area = "OSCC";
					gacc_priority = gacc_priority + 1;
					if (map_gacc_to_priority.get(current_area) == null) map_gacc_to_priority.put(current_area, gacc_priority);
					if (gacc_priority == map_gacc_to_priority.get(current_area)) process_area_data(s_lines, i, current_area, gacc_priority);
				} else if (s_lines[i].startsWith("Southwest")) {
					current_area = "SWCC";
					gacc_priority = gacc_priority + 1;
					if (map_gacc_to_priority.get(current_area) == null) map_gacc_to_priority.put(current_area, gacc_priority);
					if (gacc_priority == map_gacc_to_priority.get(current_area)) process_area_data(s_lines, i, current_area, gacc_priority);
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
		String gacc_fire_use_teams_committed = null;
		
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
		if (mstr.contains("fire use teams committed") || mstr.contains("fire use teams")) term.add("fire use teams committed");	
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
			if (term.get(j).equals("fire use teams committed")) gacc_fire_use_teams_committed = value.get(j);
		}
		
		info = String.join("\t", date, current_area, String.valueOf(gacc_priority), 
				gacc_prepareness_level, gacc_new_fires, gacc_new_large_incidents, gacc_uncontained_large_fires,
				gacc_area_command_teams_committed, gacc_nimos_committed, gacc_type_1_imts_committed,
				gacc_type_2_imts_committed, gacc_fire_use_teams_committed);
		gacc_activity.add(info);
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
			if (year_before_2015) s_lines[i] = s_lines[i].replaceAll(" - ", " -").replaceAll(" M ", "M ");
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
						&& (line_split.get(i)[line_length - 12].matches("^-?\\d{1,3}([ ,]?\\d{3})*([.,]\\d+)?$") || line_split.get(i)[line_length - 12].equals("N/A") || line_split.get(i)[line_length - 12].equals("NR") || line_split.get(i)[line_length - 12].equals("---")) 
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
					
					fire_name = fire_name.replaceAll("\\s{2,}", " ").trim().toUpperCase();	// This will remove the * (if exist in the name) and change the name to capital (IMPORTANT)
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
						&& (line_split.get(i)[line_length - 10].matches("^-?\\d{1,3}([ ,]?\\d{3})*([.,]\\d+)?$") || line_split.get(i)[line_length - 10].equals("N/A") || line_split.get(i)[line_length - 10].equals("NR") || line_split.get(i)[line_length - 10].equals("---"))
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
					
					fire_name = fire_name.replaceAll("\\s{2,}", " ").trim().toUpperCase();	// This will remove the * (if exist in the name) and change the name to capital (IMPORTANT)
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
						&& (line_split.get(i)[line_length - 13].matches("^-?\\d{1,3}([ ,]?\\d{3})*([.,]\\d+)?$") || line_split.get(i)[line_length - 13].equals("N/A") || line_split.get(i)[line_length - 13].equals("NR") || line_split.get(i)[line_length - 13].equals("---")) // special cases N/A such as U.S. Virgin in 20171004, --- such as in 20210822
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
					
					fire_name = fire_name.replaceAll("\\s{2,}", " ").trim().toUpperCase();	// This will remove the * (if exist in the name) and change the name to capital (IMPORTANT)
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
			if (year_before_2015) r_lines[i] = r_lines[i].replaceAll(" - ", " -").replaceAll(" M ", "M ");
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
						&& (line_split.get(i)[line_length - 12].matches("^-?\\d{1,3}([ ,]?\\d{3})*([.,]\\d+)?$") || line_split.get(i)[line_length - 12].equals("N/A") || line_split.get(i)[line_length - 12].equals("NR") || line_split.get(i)[line_length - 12].equals("---"))
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
						if (date.equals("2008-01-11") && r_lines[l].startsWith("Uncontained large fires")) {	// This is only one special case in 20080111
							continue_loop = false;
						} else {
							if (line_split.get(l).length <= 5 && !r_lines[l].substring(r_lines[l].lastIndexOf(" ") + 1).toUpperCase().equals("OWN") && !r_lines[l].toUpperCase().endsWith("HELI")) {		// HELI is special case for 20150102
								fire_name = String.join(" ", r_lines[l], fire_name);	// join by space
							} else {
								continue_loop = false;
							}
						}
					} while (continue_loop);
					fire_name = fire_name.replaceAll("\\s{2,}", " ").trim().toUpperCase();	// This will remove the * (if exist in the name) and change the name to capital (IMPORTANT)
					
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
						&& (line_split.get(i)[line_length - 10].matches("^-?\\d{1,3}([ ,]?\\d{3})*([.,]\\d+)?$") || line_split.get(i)[line_length - 10].equals("N/A") || line_split.get(i)[line_length - 10].equals("NR") || line_split.get(i)[line_length - 10].equals("---"))
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
					fire_name = fire_name.replaceAll("\\s{2,}", " ").trim().toUpperCase();	// This will remove the * (if exist in the name) and change the name to capital (IMPORTANT)
					
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
					String this_fire = String.join("\t", r_date, current_area, gacc_priority, fire_priority, r_lines[i]);
					if (manual_fire_adjustment_list.get(this_fire) != null) {
						this_fire = manual_fire_adjustment_list.get(this_fire);
						r_fires.add(this_fire);
					} else {
						System.out.println(date + ": " + "Is this a fire we did not add when processing raw?   " + this_fire);
					}
				}
			} else { // from 2015 we use this to process data
				if (line_length >= 14 
						&& line_split.get(i)[line_length - 14].contains("-")    // do not use this for 2014 data, it has a different unit name, also in 20210710IMSR, one fire has wrong unit that cannot be included (Butte Creek: ID- CTS)
						&& (line_split.get(i)[line_length - 13].matches("^-?\\d{1,3}([ ,]?\\d{3})*([.,]\\d+)?$") || line_split.get(i)[line_length - 13].equals("N/A") || line_split.get(i)[line_length - 13].equals("NR") || line_split.get(i)[line_length - 13].equals("---")) // special cases such as U.S. Virgin in 20171004
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
					fire_name = fire_name.replaceAll("\\s{2,}", " ").trim().toUpperCase();	// This will remove the * (if exist in the name) and change the name to capital (IMPORTANT)
					
					this_fire = String.join("\t", this_fire, fire_name);
					for (int id = unit_id; id < line_split.get(i).length; id++) {
						if (id >= 0)
						this_fire = String.join("\t", this_fire, line_split.get(i)[id]);	// this is all information in one whole line of this fire
					}
					r_fires.add(this_fire);
				} else if (line_length >= 13 
							&& ((r_lines[i - 1].contains("-") && r_lines[i - 1].length() <= 8) || (r_lines[i - 2].endsWith("-") && r_lines[i - 2].length() <= 8))    // i-2 such as in 20150613
							&& (line_split.get(i)[line_length - 13].matches("^-?\\d{1,3}([ ,]?\\d{3})*([.,]\\d+)?$") || line_split.get(i)[line_length - 13].equals("N/A") || line_split.get(i)[line_length - 13].equals("NR") || line_split.get(i)[line_length - 13].equals("---")) // special cases such as U.S. Virgin in 20171004
							&& (line_split.get(i)[line_length - 8].matches("^-?\\d{1,3}([ ,]?\\d{3})*([.,]\\d+)?$") || line_split.get(i)[line_length - 8].equals("UNK") || line_split.get(i)[line_length - 8].equals("NR") || line_split.get(i)[line_length - 8].equals("---"))
									// this is likely a fire, smart check based on the "acres" and "personnel total" columns.
				) {
					String this_fire = String.join("\t", r_date, current_area, gacc_priority, fire_priority);
					String unit_name = "";
					if (r_lines[i - 1].contains("-")) {
						unit_name = r_lines[i - 1];
					} else {
						if (r_lines[i - 2].endsWith("-")) unit_name = r_lines[i - 2] + r_lines[i - 1];
					}
					
					// Check above lines, if length <=5 etc, then add to fire name
					String fire_name = "";
					// fix the special case: Trestle 20180727 where unit still cotain part of fire name
					String[] unit_contain_fire_name = unit_name.trim().split(" ");	// replace * and trim to handle special case such as Cranston in 20180726
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
					fire_name = fire_name.replaceAll("\\s{2,}", " ").trim().toUpperCase();	// This will remove the * (if exist in the name) and change the name to capital (IMPORTANT)
					
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
						combine_st = combine_st.replaceAll("---0", "---" + "\t" + "0");	// this is because line i-1 shown as --- but it is actually not after reading
						combine_st = String.join("\t", r_lines[i - 2].replaceAll(" ", "\t"), combine_st);
						fire_name = r_lines[i - 3];
						l = i - 2;
					} else 
					if (combine_st.startsWith("Ctn") && r_lines[i - 1].matches("^-?\\d{1,3}([ ,]?\\d{3})*([.,]\\d+)?$") && r_lines[i - 2].contains("-")) {	// special case such as Spokane Complex 20180824
						combine_st = String.join("\t", r_lines[i - 2].replaceAll(" ", "\t"), r_lines[i - 1], combine_st);
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
									final_word = String.join(" - ", previous_words, last_word);
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
					
					fire_name = fire_name.replaceAll("\\s{2,}", " ").trim().toUpperCase();	// This will remove the * (if exist in the name) and change the name to capital (IMPORTANT)
					// if combine_st has length > 14 then part of fire_name is in it. We need to adjust the name
					String[] combine_st_arr = combine_st.split("\t");
					if (combine_st_arr.length > 14) {
						for (int j = 0; j < combine_st_arr.length - 14; j++) {
							fire_name = String.join(" ", fire_name, combine_st_arr[j]);	// join by space
						}
						fire_name = fire_name.replaceAll("\\s{2,}", " ").trim().toUpperCase();	// This will remove the * (if exist in the name) and change the name to capital (IMPORTANT)
						// Fix special cases such as 20170629 where part of unit is in fire name (Battle CA- MDF)
						String st_15 = fire_name.substring(fire_name.lastIndexOf(" ") + 1); // check if fire name contain part of unit
						if (st_15.length() == 3 && st_15.endsWith("-")) {	// such as 20170629: Battle CA- MDF
							fire_name = fire_name.substring(0, fire_name.lastIndexOf(" "));
							combine_st_arr[combine_st_arr.length - 14] = st_15 + combine_st_arr[combine_st_arr.length - 14];	// such as merging "CA-" + "MDF"
						}
						
						for (int j = combine_st_arr.length - 14; j < combine_st_arr.length; j++) {
							fire_name = String.join("\t", fire_name, combine_st_arr[j]);	// join by space	// this is all the other columns attached after the fire name
						}
						this_fire = String.join("\t", this_fire, fire_name);
					} else {
						this_fire = String.join("\t", this_fire, fire_name, combine_st);
					}
					if (this_fire.split("\t").length == 19 && this_fire.split("\t")[5].contains("-")) {
						r_fires.add(this_fire);
					} else if (manual_fire_adjustment_list.get(this_fire) != null) {
						this_fire = manual_fire_adjustment_list.get(this_fire);
						r_fires.add(this_fire);
					} else {
//						// problem need to fix manually: i.e. CA-KNF-006098 Complex 20170929
//						if (this_fire.equals("2017-09-29	ONCC	NA	NA	CA-KNF- 006098 COMPLEX CA-KNF 78,698 .4	0	51	Comp	10/10	318	1	5	11	2	0	44.5M	FS")) {
//							System.out.println("this fire is fixed and added manually");
//							System.out.println("old info:     " + this_fire);
//							this_fire =  "2017-09-29	ONCC	NA	NA	CA-KNF-006098 COMPLEX	CA-KNF	78,698	0	51	Comp	10/10	318	1	5	11	2	0	44.5M	FS";
//							System.out.println("new info:     " + this_fire);
//							r_fires.add(this_fire);
//						} else if (this_fire.equals("2015-06-01	SACC	NA	NA	MUD LAKE COMPLEX	FL-BCP	35,321	0	70	Comp	260	-32	4	7	6	0	7.6M	NPS")) {
//							System.out.println("this fire is fixed and added manually");
//							System.out.println("old info:     " + this_fire);
//							this_fire =  "2015-06-01	SACC	NA	NA	MUD LAKE COMPLEX	FL-BCP	35,321	0	70	Comp	---	260	-32	4	7	6	0	7.6M	NPS";
//							System.out.println("new info:     " + this_fire);
//							r_fires.add(this_fire);
//						}  else if (this_fire.equals("2015-06-01	SACC	NA	NA	BOLIN SLIME PIT	FL-FLS	300	---	95	Ctn	2	---	0	0	0	0	1K	ST")) {
//							System.out.println("this fire is fixed and added manually");
//							System.out.println("old info:     " + this_fire);
//							this_fire =  "2015-06-01	SACC	NA	NA	BOLIN SLIME PIT	FL-FLS	300	---	95	Ctn	---	2	---	0	0	0	0	1K	ST";
//							System.out.println("new info:     " + this_fire);
//							r_fires.add(this_fire);
//						} else {
//							System.out.println( date + ": " +"Is this a fire we did not add (length not 19) when processing raw?   " + this_fire);
//						} 
						
						String adjust_fire = "";
						String[] this_fire_split = this_fire.split("\t");
						for (int k = 0; k < this_fire_split.length; k++) {
							if (k < 7) adjust_fire = String.join("\t", adjust_fire, this_fire_split[k]);
							if (k >= 7) {	// started from fire size column (7-1=6)
								if (this_fire_split[k - 1].equals("-")) {		// 2020-09-14	AUGUST COMPLEX	- 140,218 should be -140,218 (need to join instead of having space in between)
									adjust_fire = adjust_fire + this_fire_split[k];
								} else {
									String[] st_split = this_fire_split[k - 1].split(",");
									if (st_split.length == 2 && st_split[1].length() < 3) {	// cases where a number with comma is split into 2 terms, such as in 20210802 BOOTLEG, 413,76 2 should be 413,762
										adjust_fire = adjust_fire + this_fire_split[k];
									} else {
										adjust_fire = String.join("\t", adjust_fire, this_fire_split[k]);
									}
								}
							}
						}
						this_fire = adjust_fire.substring(adjust_fire.indexOf("\t") + 1);	// we need to remove the first tab due to the String Join with the beginning ""
						if (this_fire.split("\t").length == 19 && this_fire.split("\t")[5].contains("-")) {
							r_fires.add(this_fire);
						} else if (manual_fire_adjustment_list.get(this_fire) != null) {
							this_fire = manual_fire_adjustment_list.get(this_fire);
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
					String this_fire = String.join("\t", r_date, current_area, gacc_priority, fire_priority, current_merge_info);	// fire joined but not correct yet.
					// Process when enough information to correct the join by tab
					String[] info_split = current_merge_info.split(" ");
					if (info_split.length > 14) {
						this_fire = String.join("\t", r_date, current_area, gacc_priority, fire_priority);
						String fire_name = "";
						for (int k = 0; k < info_split.length - 14; k++) {
							fire_name = String.join(" ", fire_name, info_split[k]);	// This is part of fire name
						}
						fire_name = fire_name.replaceAll("\\s{2,}", " ").trim().toUpperCase();	// This will remove the * (if exist in the name) and change the name to capital (IMPORTANT)
						this_fire = String.join("\t", this_fire, fire_name);
						for (int k = info_split.length - 14; k < info_split.length; k++) {
							this_fire = String.join("\t", this_fire, info_split[k]);
						}
					}
					
					if (this_fire.split("\t").length == 19 && this_fire.split("\t")[5].contains("-")) {
						r_fires.add(this_fire);
					} else if (manual_fire_adjustment_list.get(this_fire) != null) {
						this_fire = manual_fire_adjustment_list.get(this_fire);
						r_fires.add(this_fire);
					} else {
						System.out.println(date + ": " +"Is this a fire we did not add (length >=6) when processing raw?   " + this_fire);
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
		List<Integer> s_id_to_r_id = new ArrayList<Integer>();
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
				s_id_to_r_id.add(-9999);
			} else {
				s_id_to_r_id.add(r_id - 1);
//				if (!s_fire_info[1].equals(r_fire_list.get(r_id - 1)[1])) {
//					System.out.println(date + " Matching fire does not have GACC matched: " + s_fire_info[4] + " " + s_fire_info[1] + " " + r_fire_list.get(r_id - 1)[1]);
//				}
			}
		}
		
		//-------------------------------NOW GENERATE FINAL FIRE RESULTS--------------------------------------------
		//-------------------------------NOW GENERATE FINAL FIRE RESULTS--------------------------------------------
		//-------------------------------NOW GENERATE FINAL FIRE RESULTS--------------------------------------------
		// Because GACC positions are messed up in raw, we will substitute by GACC positions from raw when possible
		for (int r_id = 0; r_id < r_fires.size(); r_id++) {
			int s_id = s_id_to_r_id.indexOf(r_id);
			String[] r_fire_info = r_fire_list.get(r_id);
			if (s_id > -1) {
				String[] s_fire_info = s_fire_list.get(s_id);
				r_fire_info[1] = s_fire_info[1];	// Gacc
			} else if ((r_id > 0) && (r_id < r_fires.size() - 1) && r_fire_list.get(r_id - 1)[1].equals(r_fire_list.get(r_id + 1)[1])) {	// because above and below raw IDs have the same GACC, we apply that GACC to the middle
				r_fire_info[1] = r_fire_list.get(r_id - 1)[1];
			} else {
				r_fire_info[1] = r_fire_info[1];	// Gacc: missing GACC and is replaced by raw GACC, 100% correct across 2016-2021
			}
			final_fires.add(String.join("\t", r_fire_info));	
		}
		int original_fire_size = final_fires.size();	// duplicated records may exist
		
		// use set to remove duplicated records (such as triple duplication in 20200719) and convert back to list
		Set<String> unique_fires_set = new LinkedHashSet<String>();
		unique_fires_set.addAll(final_fires);
		final_fires = new ArrayList<>(unique_fires_set);
		
		// recalculate priority for gacc and fire
		LinkedHashMap<String, Integer> map_gacc_to_priority = new LinkedHashMap<String, Integer>();
		int gacc_priority = 0;
		int fire_priority = 0;
		for (int i = 0; i < final_fires.size(); i++) {
			String[] final_fire_info = final_fires.get(i).split("\t");
			String current_gacc = final_fire_info[1];
			if (map_gacc_to_priority.get(current_gacc) == null) {	// new gacc
				gacc_priority = gacc_priority + 1;
				map_gacc_to_priority.put(current_gacc, gacc_priority);
				fire_priority = 1;	// reset fire priority
			} else {
				fire_priority = fire_priority + 1;
			}
			final_fire_info[2] = String.valueOf(gacc_priority);
			final_fire_info[3] = String.valueOf(fire_priority);
			final_fires.set(i, String.join("\t", final_fire_info));
		}
		
		int final_fire_size = final_fires.size();	// no duplication, only unique records
//		if (final_fire_size != original_fire_size) {
//			System.out.println(date + ": number of duplication records removed: " + String.valueOf(original_fire_size - final_fire_size));
//		}
		
		// Only 1 adjustment needed for 15 years when comparing between simple2 and raw
		if (fire_in_s_not_in_r.size() > 0) {
			for (String st : fire_in_s_not_in_r) {
				if (st.equals("2010-03-12	EACC	2	5	* EXCELSIOR SCHOOL	MO-MOS	126	---	100	null	---	8	---	0	0	0	0	NR	PRI")) {
					// Exist in raw but with different information, we need to replace this fire
					final_fires.set(final_fires.indexOf("2010-03-12	EACC	2	5	* EXCELSIOR SCHOOL	MO-MOS	126	null	---	null	100	---	null	8	---	0	0	0	0"),
							"2010-03-12	EACC	2	5	* EXCELSIOR SCHOOL	MO-MOS	126	---	100	null	---	8	---	0	0	0	0	NR	PRI");						
				} else if (st.equals("2015-08-21	SACC	7	2	LANE FIRE	GA-BLR	337	---	85	Comp	8/25	4	---	0	1	0	0	13K	FWS")) {
					// no need to do anything. This fire is in raw with correct information
				} else if (st.equals("2020-09-14	ONCC	1	3	- AUGUST COMPLEX	CA-MNF	706,594	140,218	30	Ctn	11/15	1,436	-473	21	105	8	35	30.4M	FS")) {
					// no need to do anything. This fire is in raw with correct information
				} else {
					System.out.println("fires in simple2 but may not be in raw  or may be in raw with different info:");
					System.out.println(st);
				}
			}
		}
	}
	
	private void generate_manual_fire_adjustment_list() {
		manual_fire_adjustment_list.put("2007-03-02	SACC	NA	NA	* Coal Bank Mtn. KY KYS 527 100 -- 0 0 0 0 NR ST",
				"2007-03-02	SACC	NA	NA	* COAL BANK MTN.	KY-KYS	527	null	100	null	---	---	null	0	0	0	0	NR	ST");
		
		manual_fire_adjustment_list.put("2007-10-04	NA	NA	NA	* Phillips Branch KY KYS 125 --- 50 UNK 20 --- 2 1 -0 NR PRI",
				"2007-10-04	SACC	NA	NA	* PHILLIPS BRANCH	KY-KYS	125	---	50	null	UNK	20	---	2	1	---	0	NR	PRI");
		
		manual_fire_adjustment_list.put("2007-10-22	OSCC	NA	NA	* Witch CA MVU 10,000 --- 0 UNK 369 --- 22 33 -0 NR ST",
				"2007-10-22	OSCC	NA	NA	* WITCH	CA-MVU	10,000	---	0	null	UNK	369	---	22	33	---	0	NR	ST");
		
		manual_fire_adjustment_list.put("2008-04-21	SWCC	NA	NA	Burn Out OK OMA 519 -90 UNK 2 -12 0 1 0 0 0.8K BIA",
				"2008-04-21	SWCC	NA	NA	BURN OUT	OK-OMA	519	---	90	null	UNK	2	-12	0	1	0	0	0.8K	BIA");
		
		manual_fire_adjustment_list.put("2008-05-20	SACC	NA	NA	* Juno TX TXS 600 --- 0 UNK 25 --- 4 1 0 NR ST",
				"2008-05-20	SACC	NA	NA	* JUNO	TX-TXS	600	---	0	null	UNK	25	---	---	4	1	0	NR	ST");
		
		manual_fire_adjustment_list.put("2008-06-21	SWCC	NA	NA	* Stone Coal -VA VAF 650 --- N/A N/A 45 --- 1 1 1 0 28K FS",	// Note next fire name is incorrect, need to remove WFU; Also CLOVER -WFU is a problem
				"2008-06-21	SWCC	NA	NA	* STONE COAL - WFU	VA-VAF	650	---	N/A	null	N/A	45	---	1	1	1	0	28K	FS");
		
		manual_fire_adjustment_list.put("2009-04-23	SACC	NA	NA	* Highway 31 SC SCS 15,000 --- 10 4/25 130 --- 10 0 40 NR ST",
				"2009-04-23	SACC	NA	NA	* HIGHWAY 31	SC-SCS	15,000	---	10	null	4/25	130	---	---	10	0	40	NR	ST");

		manual_fire_adjustment_list.put("2009-07-04	NWCC	NA	NA	* Panther Creek WA NCP 123 --- NR 9/30 15 --- 0 0 0 NR NPS",
				"2009-07-04	NWCC	NA	NA	* PANTHER CREEK	WA-NCP	123	---	NR	null	9/30	15	---	---	0	0	0	NR	NPS");
		
		manual_fire_adjustment_list.put("2009-08-15	AICC	NA	NA	Rock Slough AK UYD 61,467 --- N/A N/A 0 --- 0 0 0 0 5.1M",
				"2009-08-15	AICC	NA	NA	ROCK SLOUGH	AK-UYD	61,467	---	N/A	null	N/A	0	---	0	0	0	0	5.1M	FWS");
		
		manual_fire_adjustment_list.put("2009-09-19	NWCC	NA	NA	Lobert OR WNF 115 2 100 --- 67 -72 6 0 0 64K FS",
				"2009-09-19	NWCC	NA	NA	LOBERT	OR-WNF	115	2	100	null	---	67	-72	---	6	0	0	64K	FS");
		
		manual_fire_adjustment_list.put("2010-04-16	SACC	NA	NA	Taylor OK OKS 260 0 100 --- 6 -0 2 0 0 NR ST",
				"2010-04-16	SACC	NA	NA	TAYLOR	OK-OKS	260	0	100	null	---	6	---	0	2	0	0	NR	ST");
		
		manual_fire_adjustment_list.put("2010-05-29	AICC	NA	NA	--- N/A N/A 0 --- 0 0 0 0 NR FWS",
				"2010-05-29	AICC	NA	NA	* SHEENJEK 2	AK-UYD	200	---	N/A	null	N/A	0	---	0	0	0	0	NR	FWS");
		
		manual_fire_adjustment_list.put("2010-08-25	RMCC	NA	NA	North Fork 330 SHF 330 0 98 8/25 151 -45 5 0 2 0 850K FS",
				"2010-08-25	RMCC	NA	NA	NORTH FORK	WY-SHF	330	0	98	null	8/25	151	-45	5	0	2	0	850K	FS");
		
		manual_fire_adjustment_list.put("2010-08-26	RMCC	NA	NA	North Fork 330 SHF 330 --- 98 UNK 151 --- 5 0 2 0 850K FS",
				"2010-08-26	RMCC	NA	NA	NORTH FORK	WY-SHF	330	---	98	null	UNK	151	---	5	0	2	0	850K	FS");
		
		manual_fire_adjustment_list.put("2010-08-27	WBCC	NA	NA	Wolf Creek 1,000 HTF 1,000 --- N/A N/A 15 --- 0 0 1 0 1.6M FS",
				"2010-08-27	WBCC	NA	NA	WOLF CREEK	NV-HTF	1,000	---	N/A	null	N/A	15	---	0	0	1	0	1.6M	FS");
		
		manual_fire_adjustment_list.put("2012-06-17	OSCC	NA	NA	Ophir Creek UT NWS 1,574 374 100 --- 3 -57 0 0 0 40K ST",
				"2012-06-17	OSCC	NA	NA	OPHIR CREEK	UT-NWS	1,574	374	100	null	---	3	-57	0	0	---	0	40K	ST");

		manual_fire_adjustment_list.put("2012-11-08	RMCC	NA	NA	* Coal Creek SJF 127 127 --- N/A N/A 9 --- 0 0 1 0 24K FS",
				"2012-11-08	RMCC	NA	NA	* COAL CREEK	CO-SJF	127	---	N/A	null	N/A	9	---	0	0	1	0	24K	FS");
		
		manual_fire_adjustment_list.put("2013-03-08	SACC	NA	NA	* Middle Mountain OK OKS 600 --- 65 3/10 9 0 5 0 0 3K ST",
				"2013-03-08	SACC	NA	NA	* MIDDLE MOUNTAIN	OK-OKS	600	---	65	null	3/10	9	---	0	5	0	0	3K	ST");
		
		manual_fire_adjustment_list.put("2013-03-08	SACC	NA	NA	* Volunteer AL ALS 300 --- 100 --- 0 -0 0 0 0 3K ST",
				"2013-03-08	SACC	NA	NA	* VOLUNTEER	AL-ALS	300	---	100	null	---	0	---	0	0	0	0	3K	ST");
		
		manual_fire_adjustment_list.put("2013-04-12	SACC	NA	NA	KY KYS 125 --- 100 --- 11 0 2 0 0 1K PRI",
				"2013-04-12	SACC	NA	NA	* LEFT FORK ABBOTT CREEK	KY-KYS	125	---	100	null	---	11	---	0	2	0	0	1K	PRI");

		manual_fire_adjustment_list.put("2014-05-02	EACC	NA	NA	* Goose Pond PA PAS 350 --- 100 --- 8 --- 0 1 0 0 9K",
				"2014-05-02	EACC	NA	NA	* GOOSE POND	PA-PAS	350	---	100	null	---	8	---	0	1	0	0	9K	PRI");
		
		manual_fire_adjustment_list.put("2015-06-01	SACC	NA	NA	MUD LAKE COMPLEX	FL-BCP	35,321	0	70	Comp	260	-32	4	7	6	0	7.6M	NPS",
				"2015-06-01	SACC	NA	NA	MUD LAKE COMPLEX	FL-BCP	35,321	0	70	Comp	---	260	-32	4	7	6	0	7.6M	NPS");
		
		manual_fire_adjustment_list.put("2015-06-01	SACC	NA	NA	* BOLIN SLIME PIT	FL-FLS	300	---	95	Ctn	2	---	0	0	0	0	1K	ST",
				"2015-06-01	SACC	NA	NA	* BOLIN SLIME PIT	FL-FLS	300	---	95	Ctn	---	2	---	0	0	0	0	1K	ST");
		
		manual_fire_adjustment_list.put("2015-07-05	AICC	NA	NA		Munsatli	AK-TAD	18,847	1,963	Comp	NR	0	-1	0	0	0	0	1K	ST",
				"2015-07-05	AICC	NA	NA	MUNSATLI	AK-TAD	18,847	1,963	---	Comp	NR	0	-1	0	0	0	0	1K	ST");
		
		manual_fire_adjustment_list.put("2015-08-02	GBCC	NA	NA		Solitude	UT-FIF	1,000	622	25	Comp	133	23	4	4	1	0	324K	FS",
				"2015-08-02	GBCC	NA	NA	SOLITUDE	UT-FIF	1,000	622	25	Comp	null	133	23	4	4	1	0	324K	FS");
		
		manual_fire_adjustment_list.put("2016-08-03	SWCC	NA	NA	* PEARSON	#2	NM-CAD	900	---	90	Ctn	UNK	23	0	0	9	0	15K	BLM",
				"2016-08-03	SWCC	NA	NA	* PEARSON #2	NM-CAD	900	---	90	Ctn	UNK	23	0	0	9	0	null	15K	BLM");
		
		manual_fire_adjustment_list.put("2017-09-29	ONCC	NA	NA	CA-KNF- 006098 COMPLEX CA-KNF 78,698 .4	0	51	Comp	10/10	318	1	5	11	2	0	44.5M	FS",
				"2017-09-29	ONCC	NA	NA	CA-KNF-006098 COMPLEX	CA-KNF	78,698	0	51	Comp	10/10	318	1	5	11	2	0	44.5M	FS");

		manual_fire_adjustment_list.put("2017-10-31	ONCC	NA	NA	CENTRAL LNU COMPLEX CA-LNU	110,720	0	99	Ctn	10/31	150	-88	4	5	0	7,010	101	M	ST",
				"2017-10-31	ONCC	NA	NA	CENTRAL LNU COMPLEX	CA-LNU	110,720	0	99	Ctn	10/31	150	-88	4	5	0	7,010	101M	ST");
		
		manual_fire_adjustment_list.put("2018-07-18	NWCC	NA	NA	* GARNER COMPLEX	OR-712S	539	---	2	7/31	542	---	18	9	8	0	750K	ST",
				"2018-07-18	NWCC	NA	NA	* GARNER COMPLEX	OR-712S	539	---	2	---	7/31	542	---	18	9	8	0	750K	ST");

		manual_fire_adjustment_list.put("2018-09-01	NRCC	NA	NA	GOLD HILL MT-KNF	3,512	0	1	Ctn	10/1	5	316	-21	8	10	0	0	7M	FS",
				"2018-09-01	NRCC	NA	NA	GOLD HILL	MT-KNF	3,512	0	1	Ctn	10/15	316	-21	8	10	0	0	7M	FS");
		
		manual_fire_adjustment_list.put("2018-09-01	NRCC	NA	NA	BEAVER CREEK MT-BDF	2,074	0	35	Ctn	10/1	5	16	16	0	0	0	0	467K	FS",
				"2018-09-01	NRCC	NA	NA	BEAVER CREEK	MT-BDF	2,074	0	35	Ctn	10/15	16	16	0	0	0	0	467K	FS");
		
		manual_fire_adjustment_list.put("2018-09-01	NRCC	NA	NA	COPPER MOUNTAIN ID-IPF	480	---	24	Comp	10/3	1	2	---	0	0	0	0	1.9M	FS",
				"2018-09-01	NRCC	NA	NA	COPPER MOUNTAIN	ID-IPF	480	---	24	Comp	10/31	2	---	0	0	0	0	1.9M	FS");
		
		manual_fire_adjustment_list.put("2018-09-01	NRCC	NA	NA	RAMPIKE ID-IPF	2,810	---	0	Comp	11/3	0	34	---	0	1	0	0	900K	FS",
				"2018-09-01	NRCC	NA	NA	RAMPIKE	ID-IPF	2,810	---	0	Comp	11/30	34	---	0	1	0	0	900K	FS");
		
		manual_fire_adjustment_list.put("2018-09-01	NRCC	NA	NA	MOOSE CREEK MT-HLF	268	---	0	Comp	10/3	1	11	---	0	0	1	0	10K	FS",
				"2018-09-01	NRCC	NA	NA	MOOSE CREEK	MT-HLF	268	---	0	Comp	10/31	11	---	0	0	1	0	10K	FS");
		
		manual_fire_adjustment_list.put("2018-09-01	NRCC	NA	NA	SMITH CREEK ID-IPF	971	---	0	Comp	10/3	1	7	---	0	1	0	0	193K	FS",
				"2018-09-01	NRCC	NA	NA	SMITH CREEK	ID-IPF	971	---	0	Comp	10/31	7	---	0	1	0	0	193K	FS");
		
		manual_fire_adjustment_list.put("2019-09-08	ONCC	NA	NA	* LIME	CA.KNF	200	---	0	Ctn	10/18	168	---	6	0	2	0	2.1M	FS",
				"2019-09-08	ONCC	NA	NA	* LIME	CA-KNF	200	---	0	Ctn	10/18	168	---	6	0	2	0	2.1M	FS");
		
		manual_fire_adjustment_list.put("2019-09-12	ONCC	NA	NA	WALKER CA-PNF	48,	507	1,167	28	Ctn	9/28	1,891	607	39	150	6	0	12.1M	FS",
				"2019-09-12	ONCC	NA	NA	WALKER	CA-PNF	48,507	1,167	28	Ctn	9/28	1,891	607	39	150	6	0	12.1M	FS");
		
		manual_fire_adjustment_list.put("2019-09-13	NRCC	NA	NA	SNOW	Peak	ID-IPF	100	---	0	Comp	10/31	0	0	0	0	0	5K	FS",
				"2019-09-13	NRCC	NA	NA	SNOW PEAK	ID-IPF	100	---	0	Comp	10/31	0	---	0	0	0	0	5K	FS");
		
		manual_fire_adjustment_list.put("2019-10-19	GBCC	NA	NA	SKULL FLAT 2	0710	450	---	0	Comp	11/7	5	---	0	0	0	0	3K	FS",
				"2019-10-19	GBCC	NA	NA	SKULL FLAT 2	UT-FIF	450	---	0	Comp	11/7	5	---	0	0	0	0	3K	FS");	// unit is 0710 and should be UT-FIF as in other dates.
		
		manual_fire_adjustment_list.put("2020-07-30	RMCC	NA	NA	* BUFFALO	Creek	WY-SHX	326	---	100	Ctn	---	17	---	0	5	0	15K	CNTY",
				"2020-07-30	RMCC	NA	NA	* BUFFALO CREEK	WY-SHX	326	---	100	Ctn	---	17	---	0	5	0	null	15K	CNTY");
		
		manual_fire_adjustment_list.put("2020-09-14	ONCC	NA	NA	AUGUST COMPLEX CA-MNF 706,594 - 140,218	30	Ctn	11/15	1,436	-473	21	105	8	35	30.4M	FS",
				"2020-09-14	ONCC	NA	NA	AUGUST COMPLEX	CA-MNF	706,594	-140,218	30	Ctn	11/15	1,436	-473	21	105	8	35	30.4M	FS");

		manual_fire_adjustment_list.put("2020-09-25	RMCC	NA	NA	CAMERON PEAK	CO-ARF 104,79 1	0	27	Ctn	10/31	819	-12	9	52	10	57	49.6M	FS",
				"2020-09-25	RMCC	NA	NA	CAMERON PEAK	CO-ARF	104,791	0	27	Ctn	10/31	819	-12	9	52	10	57	49.6M	FS");

		manual_fire_adjustment_list.put("2021-04-07	SACC	NA	NA	BLACK	Calf	OK-OMA	1,600	0	80	Ctn	4/15	22	1	6	0	0	40K	BIA",
				"2021-04-07	SACC	NA	NA	BLACK CALF	OK-OMA	1,600	0	80	Ctn	4/15	22	---	1	6	0	0	40K	BIA");

		manual_fire_adjustment_list.put("2021-07-17	NWCC	NA	NA	BOOTLEG OR-FWF 273,582 46,34 8	22	Ctn	10/1	1,921	185	44	195	14	75	15.8M	FS",
				"2021-07-17	NWCC	NA	NA	BOOTLEG	OR-FWF	273,582	46,348	22	Ctn	10/1	1,921	185	44	195	14	75	15.8M	FS");
		
		manual_fire_adjustment_list.put("2021-07-17	SWCC	NA	NA	Ocotillo AZ-TNF 980 35 Comp 7/30 25 0 1 1 0 50K FS",
				"2021-07-17	SWCC	NA	NA	OCOTILLO	AZ-TNF	980	---	35	Comp	7/30	25	---	0	1	1	0	50K	FS");
		
		manual_fire_adjustment_list.put("2021-07-17	SWCC	NA	NA	Bear AZ-TNF 1,948 50 Comp 7/20 25 0 1 1 0 50K FS",
				"2021-07-17	SWCC	NA	NA	BEAR	AZ-TNF	1,948	---	50	Comp	7/20	25	---	0	1	1	0	50K	FS");
		
		manual_fire_adjustment_list.put("2021-07-17	SWCC	NA	NA	Johnson NM-GNF 88,918 95 Comp 7/23 1 0 0 0 0 10.5M FS",
				"2021-07-17	SWCC	NA	NA	JOHNSON	NM-GNF	88,918	---	95	Comp	7/23	1	---	0	0	0	0	10.5M	FS");
		
		manual_fire_adjustment_list.put("2021-07-17	SWCC	NA	NA	Firebox AZ-ASF 1,065 0 Comp 8/9 49 1 2 1 0 80K FS",
				"2021-07-17	SWCC	NA	NA	FIREBOX	AZ-ASF	1,065	---	0	Comp	8/9	49	---	1	2	1	0	80K	FS");

		manual_fire_adjustment_list.put("2021-08-02	NWCC	NA	NA	BOOTLEG	OR-FWF 413,76 2	28	84	Ctn	10/1	1,780	-98	44	83	10	408	75.6M	FS",
				"2021-08-02	NWCC	NA	NA	BOOTLEG	OR-FWF	413,762	28	84	Ctn	10/1	1,780	-98	44	83	10	408	75.6M	FS");
		
		manual_fire_adjustment_list.put("2021-08-03	NWCC	NA	NA	BOOTLEG	OR-FWF 413,76 2	0	84	Ctn	10/1	1,617	-163	42	66	11	408	78.4M	FS",
				"2021-08-03	NWCC	NA	NA	BOOTLEG	OR-FWF	413,762	0	84	Ctn	10/1	1,617	-163	42	66	11	408	78.4M	FS");

		manual_fire_adjustment_list.put("2021-08-04	NWCC	NA	NA	BOOTLEG	OR-FWF 413,76 5	3	84	Ctn	10/1	1,493	-124	37	65	11	408	81.5M	FS",
				"2021-08-04	NWCC	NA	NA	BOOTLEG	OR-FWF	413,765	3	84	Ctn	10/1	1,493	-124	37	65	11	408	81.5M	FS");
		
		manual_fire_adjustment_list.put("2021-08-12	NWCC	NA	NA	Bear Creek WA-NCP 998 5 Comp 10/1 12 1 0 1 0 30K NPS",
				"2021-08-12	NWCC	NA	NA	BEAR CREEK	WA-NCP	998	---	5	Comp	10/1	12	---	1	0	1	0	30K	NPS");

		manual_fire_adjustment_list.put("2021-08-12	NRCC	NA	NA	Robertson Draw MT-CGF 29,885 90 Ctn 10/1 13 0 2 0 30 13.2M FS",
				"2021-08-12	NRCC	NA	NA	ROBERTSON DRAW	MT-CGF	29,885	---	90	Ctn	10/1	13	---	0	2	0	30	13.2M	FS");

		manual_fire_adjustment_list.put("2021-09-05	NRCC	NA	NA	STORM THEATRE COMPLEX ID-NCF	22,320	---	10	8	Comp	10/15	29	---	0	3	0	0	4.8M	FS",
				"2021-09-05	NRCC	NA	NA	STORM THEATRE COMPLEX	ID-NCF	22,320	---	10	Comp	10/15	29	---	0	3	0	0	4.8M	FS");

		manual_fire_adjustment_list.put("2021-09-11	GBCC	NA	NA	MUD LICK ID-S, USFSCF	20,856	---	70	Comp	9/19	1	---	0	0	0	0	25M	FS",
				"2021-09-11	GBCC	NA	NA	MUD LICK	ID-SCF	20,856	---	70	Comp	9/19	1	---	0	0	0	0	25M	FS");	// unit is S,USFSCF but should be ID-SCF as in other dates
		
		manual_fire_adjustment_list.put("2021-09-29	EACC	NA	NA		Greenwood	MN-SUF	26,797	80	Ctn	10/14	85	-20	0	4	0	71	21.8M	FS",
				  "2021-09-29	EACC	NA	NA	GREENWOOD	MN-SUF	26,797	null	80	Ctn	10/14	85	-20	0	4	0	71	21.8M	FS");
		
		manual_fire_adjustment_list.put("2021-10-08	NRCC	NA	NA	STATELINE	Complex	ID-IPF	13,199	0	50	Ctn	11/1	46	0	0	0	0	22.5M	FS",
				  "2021-10-08	NRCC	NA	NA	STATELINE COMPLEX	ID-IPF	13,199	0	50	Ctn	11/1	46	---	0	0	0	0	22.5M	FS");
		
		// WFU AGUA SI is incorrect and should be changes to AGUA SI. This is not spotted by this program.
	}
	
	private void data_cleaning() {
		// clean national activity
		List<String> cleaned_national_activity = new ArrayList<String>();
		for (String st : national_activity) {
			st = st.toUpperCase().replaceAll(",", "").replaceAll("NULL", "").replaceAll("\\`", "")	// 20070809 has the 3`
					.replaceAll("\\*", "").replaceAll("\\)", "").replaceAll("\\(", "").replaceAll("\\s+", "");	// some records such as 20190721 still have the (* )
			cleaned_national_activity.add(st);
		}
		national_activity = cleaned_national_activity;
		
		// clean gacc activity
		List<String> cleaned_gacc_activity = new ArrayList<String>();
		for (String st : gacc_activity) {
			st = st.toUpperCase().replaceAll(",", "").replaceAll("NULL", "");
			cleaned_gacc_activity.add(st);
		}
		gacc_activity = cleaned_gacc_activity;
		
		// clean resource summary
		List<String> cleaned_resource_summary = new ArrayList<String>();
		for (String st : resource_summary) {
			st = st.toUpperCase().replaceAll(",", "").replaceAll("NULL", "");
			cleaned_resource_summary.add(st);
		}
		resource_summary = cleaned_resource_summary;
		
		// clean fires
		List<String[]> fs_list = new ArrayList<String[]>();
		for (int i = 0; i < final_fires.size(); i++) {
			String st = final_fires.get(i).toUpperCase().replaceAll(",", "").replaceAll("NULL", "").replaceAll("N/A", "NA").replaceAll("N/R", "NR")
														.replaceAll("\\$", "").replaceAll("\\`", "").replaceAll("\\=", "")
														.replaceAll("\\-\\-\\-\\-", "---").replaceAll("\\.\\.\\.", "---").replaceAll("\\_\\_\\_", "---").replaceAll("\\_\\_", "---")
														.replaceAll("\\-\\-\\-", "--").replaceAll("\\-\\-", "---")	// a work around to replace double hyphen --
														.replaceAll(" /", "/").replaceAll("/ ", "/").replaceAll(" -", "-").replaceAll("- ", "-");
			String[] fs = st.split("\t");
			fs_list.add(fs);
		}
		
		// check if fire_name starts with or contains "origin_ownership" of previous fire and fix it
		for (int i = 0; i < final_fires.size(); i++) {
			String[] previous_fs = null;
			if (i > 0) previous_fs = fs_list.get(i - 1);
			String[] fs = fs_list.get(i);
			if (i > 0 && !fs[4].startsWith("*")) {	// check from the second fire only, if fire name not start with *
				String[] this_fire_name = fs[4].split(" ");
				if (this_fire_name[0].equals(previous_fs[18]) && !previous_fs[18].equals("ST")) {		// Avoid Problem: 2010509 ST JOHNS
					this_fire_name[0] = "";
					fs[4] = String.join(" ", this_fire_name).replaceAll("\\s+", " ").trim();
					System.out.println(String.join("\t", fs[0], fs[1], fs[4], "fire name contains origin_ownership: fixed"));
				} else if (this_fire_name.length >=2 && this_fire_name[1].equals(previous_fs[18]) && !previous_fs[18].equals("ST")) {
					this_fire_name[0] = "";
					this_fire_name[1] = "";
					fs[4] = String.join(" ", this_fire_name).replaceAll("\\s+", " ").trim();
					System.out.println(String.join("\t", fs[0], fs[1], fs[4], "fire name contains origin_ownership: fixed"));
				} else if (fs[4].startsWith("HEL I ")) {	// only 3 cases in 2018 such as 2018-05-13 to 15
					fs[4] = fs[4].substring(5);
					System.out.println(String.join("\t", fs[0], fs[1], fs[4], "fire name contains table header Hel i: fixed"));
				}
			}
		}
		
		// clean fires 
		for (int i = 0; i < final_fires.size(); i++) {
			String[] fs = fs_list.get(i);
			// move * i "fire_name" to another column "new_large_fire_mark"
			if (fs[4].contains("*")) {
				fs[4] = "*" + "\t" + fs[4].replaceAll("\\*", "").replaceAll("\\s+", " ").trim(); 
			} else {
				fs[4] = "" + "\t" + fs[4].replaceAll("\\*", "").replaceAll("\\s+", " ").trim(); 
			}
			
			// fix "contained_completed"
			fs[9] = fs[9].replaceAll("CNT", "CTN").replaceAll("\\.", "");
			// fix "estimated_containment_date" typos
			fs[10] = fs[10].replaceAll("ÚNK", "UNK").replaceAll("UKN", "UNK");
			// fix "estimated_containment_date" non-date format
			if (fs[10].equals("0")) { fs[10] = ""; }
			else if (fs[10].equals("100")) { fs[10] = ""; System.out.println(String.join("\t", fs[0], fs[1], fs[4], "estimated_containment_date: removed")); }
			else if (fs[10].equals("1017")) { fs[10] = "10/17"; System.out.println(String.join("\t", fs[0], fs[1], fs[4], "estimated_containment_date: replaced by 10/17")); }
			else if (fs[10].equals("40")) { fs[10] = ""; System.out.println(String.join("\t", fs[0], fs[1], fs[4], "estimated_containment_date: removed"));  }
			else if (fs[10].equals("6")) { fs[10] = ""; System.out.println(String.join("\t", fs[0], fs[1], fs[4], "estimated_containment_date: removed"));  }
			else if (fs[10].equals("8.21")) { fs[10] = "8/21"; System.out.println(String.join("\t", fs[0], fs[1], fs[4], "estimated_containment_date: replaced by 8/21")); }
			else if (fs[10].equals("915")) { fs[10] = "9/15"; System.out.println(String.join("\t", fs[0], fs[1], fs[4], "estimated_containment_date: replaced by 9/15")); }
			// fix "origin_ownership" wrong numeric representing name
			if (fs[18].equals("48")) { fs[18] = "ST"; System.out.println(String.join("\t", fs[0], fs[1], fs[4], "origin_ownership: replaced by ST")); }
			else if (fs[18].equals("157")) { fs[18] = "FS"; System.out.println(String.join("\t", fs[0], fs[1], fs[4], "origin_ownership: replaced by FS")); }
		
			// fix non-integer in several fields: “fire_size” (30), “fire_size_change” (2), “percent_containment” (2), “personnel” (4), “personnel_change” (1), and “structures_lost” (11)
			if (fs[0].equals("2012-06-07") && fs[6].equals("1.350")) { fs[6] = "1350"; System.out.println(String.join("\t", fs[0], fs[1], fs[4], "fire_size: replaced by 1350")); }
			else if (fs[0].equals("2008-06-12") && fs[6].equals("10.800")) { fs[6] = "10800"; System.out.println(String.join("\t", fs[0], fs[1], fs[4], "fire_size: replaced by 10800")); }
			else if (fs[0].equals("2015-07-03") && fs[6].equals("11278.1")) { fs[6] = "11278"; System.out.println(String.join("\t", fs[0], fs[1], fs[4], "fire_size: replaced by 11278")); }
			else if (fs[0].equals("2015-09-30") && fs[6].equals("121.6")) { fs[6] = "122"; System.out.println(String.join("\t", fs[0], fs[1], fs[4], "fire_size: replaced by 122")); }
			else if (fs[0].equals("2015-07-02") && fs[6].equals("13650.4")) { fs[6] = "13650"; System.out.println(String.join("\t", fs[0], fs[1], fs[4], "fire_size: replaced by 13650")); }
			else if (fs[0].equals("2015-07-02") && fs[6].equals("14855.8")) { fs[6] = "14856"; System.out.println(String.join("\t", fs[0], fs[1], fs[4], "fire_size: replaced by 14856")); }
			else if (fs[0].equals("2010-06-11") && fs[6].equals("18.952")) { fs[6] = "18952"; System.out.println(String.join("\t", fs[0], fs[1], fs[4], "fire_size: replaced by 18952")); }
			else if (fs[0].equals("2015-07-03") && fs[6].equals("2893.2")) { fs[6] = "2893"; System.out.println(String.join("\t", fs[0], fs[1], fs[4], "fire_size: replaced by 2893")); }
			else if (fs[0].equals("2012-05-24") && fs[6].equals("2.200")) { fs[6] = "2200"; System.out.println(String.join("\t", fs[0], fs[1], fs[4], "fire_size: replaced by 2200")); }
			else if (fs[0].equals("2007-05-10") && fs[6].equals("2.400")) { fs[6] = "2400"; System.out.println(String.join("\t", fs[0], fs[1], fs[4], "fire_size: replaced by 2400")); }
			else if (fs[0].equals("2007-05-11") && fs[6].equals("2.400")) { fs[6] = "2400"; System.out.println(String.join("\t", fs[0], fs[1], fs[4], "fire_size: replaced by 2400")); }
			else if (fs[0].equals("2009-04-13") && fs[6].equals("2.600")) { fs[6] = "2600"; System.out.println(String.join("\t", fs[0], fs[1], fs[4], "fire_size: replaced by 2600")); }
			else if (fs[0].equals("2015-07-03") && fs[6].equals("3036.6")) { fs[6] = "3037"; System.out.println(String.join("\t", fs[0], fs[1], fs[4], "fire_size: replaced by 3037")); }
			else if (fs[0].equals("2018-08-21") && fs[6].equals("34.518")) { fs[6] = "34518"; System.out.println(String.join("\t", fs[0], fs[1], fs[4], "fire_size: replaced by 34518")); }
			else if (fs[0].equals("2015-08-16") && fs[6].equals("361.7")) { fs[6] = "362"; System.out.println(String.join("\t", fs[0], fs[1], fs[4], "fire_size: replaced by 362")); }
			else if (fs[0].equals("2015-08-18") && fs[6].equals("432.8")) { fs[6] = "432"; System.out.println(String.join("\t", fs[0], fs[1], fs[4], "fire_size: replaced by 432")); }
			else if (fs[0].equals("2015-08-20") && fs[6].equals("432.8")) { fs[6] = "432"; System.out.println(String.join("\t", fs[0], fs[1], fs[4], "fire_size: replaced by 432")); }
			else if (fs[0].equals("2015-08-21") && fs[6].equals("432.8")) { fs[6] = "432"; System.out.println(String.join("\t", fs[0], fs[1], fs[4], "fire_size: replaced by 432")); }
			else if (fs[0].equals("2015-07-02") && fs[6].equals("442.5")) { fs[6] = "442"; System.out.println(String.join("\t", fs[0], fs[1], fs[4], "fire_size: replaced by 442")); }
			else if (fs[0].equals("2017-08-11") && fs[6].equals("5264.4")) { fs[6] = "5264"; System.out.println(String.join("\t", fs[0], fs[1], fs[4], "fire_size: replaced by 5264")); }
			else if (fs[0].equals("2015-06-29") && fs[6].equals("5493.1")) { fs[6] = "5493"; System.out.println(String.join("\t", fs[0], fs[1], fs[4], "fire_size: replaced by 5493")); }
			else if (fs[0].equals("2011-08-23") && fs[6].equals("5.623")) { fs[6] = "5623"; System.out.println(String.join("\t", fs[0], fs[1], fs[4], "fire_size: replaced by 5623")); }
			else if (fs[0].equals("2015-07-03") && fs[6].equals("7569.3")) { fs[6] = "7569"; System.out.println(String.join("\t", fs[0], fs[1], fs[4], "fire_size: replaced by 7569")); }
			else if (fs[0].equals("2011-03-13") && fs[6].equals("7.555")) { fs[6] = "7555"; System.out.println(String.join("\t", fs[0], fs[1], fs[4], "fire_size: replaced by 7555")); }
			else if (fs[0].equals("2011-03-14") && fs[6].equals("7.555")) { fs[6] = "7555"; System.out.println(String.join("\t", fs[0], fs[1], fs[4], "fire_size: replaced by 7555")); }
			else if (fs[0].equals("2011-03-15") && fs[6].equals("7.555")) { fs[6] = "7555"; System.out.println(String.join("\t", fs[0], fs[1], fs[4], "fire_size: replaced by 7555")); }
			else if (fs[0].equals("2011-03-16") && fs[6].equals("7.555")) { fs[6] = "7555"; System.out.println(String.join("\t", fs[0], fs[1], fs[4], "fire_size: replaced by 7555")); }
			else if (fs[0].equals("2011-03-17") && fs[6].equals("7.555")) { fs[6] = "7555"; System.out.println(String.join("\t", fs[0], fs[1], fs[4], "fire_size: replaced by 7555")); }
			else if (fs[0].equals("2008-10-06") && fs[6].equals("72.344")) { fs[6] = "72344"; System.out.println(String.join("\t", fs[0], fs[1], fs[4], "fire_size: replaced by 72344")); }
			else if (fs[0].equals("2007-07-23") && fs[6].equals("8.200")) { fs[6] = "8200"; fs[7] = "3700"; System.out.println(String.join("\t", fs[0], fs[1], fs[4], "fire_size and fire_size_change: replaced by 8200 and 3700")); }
			if (fs[0].equals("2011-06-03") && fs[7].equals("2.700")) { fs[7] = "2700"; System.out.println(String.join("\t", fs[0], fs[1], fs[4], "fire_size_change: replaced by 2700")); }
			else if (fs[0].equals("2007-08-21") && fs[7].equals("3.525")) { fs[7] = "3525"; System.out.println(String.join("\t", fs[0], fs[1], fs[4], "fire_size_change: replaced by 2700")); }
			if (fs[0].equals("2010-08-30") && fs[8].equals("10-")) { fs[8] = "100"; System.out.println(String.join("\t", fs[0], fs[1], fs[4], "percent_containment: replaced by 100")); }
			else if (fs[0].equals("2017-05-21") && fs[8].equals("CTN")) { fs[8] = "---"; System.out.println(String.join("\t", fs[0], fs[1], fs[4], "percent_containment: replaced by ---")); }
			if (fs[0].equals("2016-08-06") && fs[11].equals("1.506")) { fs[11] = "1506"; System.out.println(String.join("\t", fs[0], fs[1], fs[4], "personnel: replaced by 1506")); }
			else if (fs[0].equals("2008-07-11") && fs[11].equals("2.092")) { fs[11] = "2092"; System.out.println(String.join("\t", fs[0], fs[1], fs[4], "personnel: replaced by 2092")); }
			else if (fs[0].equals("2008-07-12") && fs[11].equals("2.092")) { fs[11] = "2092"; System.out.println(String.join("\t", fs[0], fs[1], fs[4], "personnel: replaced by 2092")); }
			else if (fs[0].equals("2018-08-16") && fs[11].equals("3.831")) { fs[11] = "3831"; System.out.println(String.join("\t", fs[0], fs[1], fs[4], "personnel: replaced by 3831")); }
			if (fs[0].equals("2012-05-30") && fs[12].equals("---3")) { fs[12] = "-3"; System.out.println(String.join("\t", fs[0], fs[1], fs[4], "personnel_change: replaced by -3")); }
			if (fs[0].equals("2010-07-01") && fs[16].equals("1.9M")) { fs[16] = "0"; fs[17] = "1.9M"; System.out.println(String.join("\t", fs[0], fs[1], fs[4], "structures_lost and cost_to_date: replaced by 0 and 1.9M")); }
			else if (fs[0].equals("2012-06-29") && fs[16].equals("110K")) { fs[16] = "0"; fs[17] = "110K"; System.out.println(String.join("\t", fs[0], fs[1], fs[4], "structures_lost: replaced by 0 and 110K")); }
			else if (fs[0].equals("2008-08-08") && fs[16].equals("162K")) { fs[16] = "0"; System.out.println(String.join("\t", fs[0], fs[1], fs[4], "structures_lost: replaced by 0")); }
			else if (fs[0].equals("2013-07-01") && fs[16].equals("650K")) { fs[16] = "0"; fs[17] = "650K"; System.out.println(String.join("\t", fs[0], fs[1], fs[4], "structures_lost and cost_to_date: replaced by 0 and 650K")); }
			else if (fs[0].equals("2008-01-18") && fs[16].equals("6K")) { fs[16] = "0"; System.out.println(String.join("\t", fs[0], fs[1], fs[4], "structures_lost: replaced by 0")); }
			else if (fs[0].equals("2010-05-11") && fs[16].equals("80K")) { fs[16] = "0"; fs[17] = "80K"; System.out.println(String.join("\t", fs[0], fs[1], fs[4], "structures_lost and cost_to_date: replaced by 0 and 80K")); }
			else if (fs[0].equals("2020-05-15") && fs[16].equals("O")) { fs[16] = "0"; System.out.println(String.join("\t", fs[0], fs[1], fs[4], "structures_lost: replaced by 0")); }
			else if (fs[0].equals("2011-05-29") && fs[16].equals("O")) { fs[16] = "0"; System.out.println(String.join("\t", fs[0], fs[1], fs[4], "structures_lost: replaced by 0")); }
			else if (fs[0].equals("2011-05-30") && fs[16].equals("O")) { fs[16] = "0"; System.out.println(String.join("\t", fs[0], fs[1], fs[4], "structures_lost: replaced by 0")); }
			else if (fs[0].equals("2008-03-07") && fs[16].equals("O")) { fs[16] = "0"; System.out.println(String.join("\t", fs[0], fs[1], fs[4], "structures_lost: replaced by 0")); }
			else if (fs[0].equals("2021-11-26") && fs[16].equals("ST")) { fs[16] = ""; System.out.println(String.join("\t", fs[0], fs[1], fs[4], "structures_lost: replaced by 0")); }
			
			// fix "cost_to_date"
			if (!(fs[17].equals("NA") || fs[17].equals("NR") || fs[17].equals("---") || fs[17].endsWith("K") || fs[17].endsWith("M") || fs[17].length() <= 1)) {
				if (fs[17].equals("0")) {	// several records (4) have this, we need to replace it to NR
					fs[17] = "0K";
					System.out.println(String.join("\t", fs[0], fs[1], fs[4], "cost _to_date: 0 replaced by 0K"));
				} else if (fs[17].equals("NF")) {	// several records (4) have this, we need to replace it to NR
					fs[17] = "NR";
					System.out.println(String.join("\t", fs[0], fs[1], fs[4], "cost _to_date: NF replaced by NR"));
				} else if (fs[17].startsWith(".")) {	// such as 2011-03-17 HIGHLINE
					fs[17] = "0" + fs[17];
					System.out.println(String.join("\t", fs[0], fs[1], fs[4], "cost _to_date: number starts with . replaced by 0."));
				} else if (fs[17].endsWith("J") || fs[17].endsWith("L")) {	// such as 2018-08-27 AIRPORT	135J
					fs[17] = fs[17].substring(0, fs[17].length() - 1);
					System.out.println(String.join("\t", fs[0], fs[1], fs[4], "cost _to_date: J or L removed"));
				} else {	
					// ctd that does not end with K or M can be fixed only if we can check the same fire in most recent previous date or next date.
					// this is not possible here, but it will be done when aggregating results. See Option_Pane_Explore class
				}
			}
			
			// apply all changes
			String adjusted_fire = String.join("\t", fs);
			final_fires.set(i, adjusted_fire);
		}
		
	}
}
