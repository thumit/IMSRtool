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
//			List<String> lines_list = Files.readAllLines(Paths.get(file.getAbsolutePath()), StandardCharsets.UTF_8);		// Not sure why this UTF_8 fail
			List<String> lines_list = Files.readAllLines(Paths.get(file.getAbsolutePath()), Charset.defaultCharset());		// Therefore I use default
			String[] lines = lines_list.stream().toArray(String[] ::new);
			date = file.getName().substring(0, 8);
			process_data_method_1(lines);
			national_fire_activity.add(date);
			national_fire_activity.add(national_prepareness_level);
			national_fire_activity.add(initial_attack_activity);
			national_fire_activity.add(initial_attack_activity_number);
			national_fire_activity.add(new_large_incidents);
			national_fire_activity.add(large_fires_contained);
			national_fire_activity.add(uncontained_large_fires);
			national_fire_activity.add(area_command_teams_committed);
			national_fire_activity.add(NIMOs_committed);
			national_fire_activity.add(type_1_IMTs_committed);
			national_fire_activity.add(type_2_IMTs_committed);
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
		
		String st = "national preparedness level";
		String temp = sb.substringBetween(mstr, st, get_next_term(mstr, st)); 
		if (temp != null) national_prepareness_level = (temp.substring(temp.indexOf(" ") + 1)).trim();	// remove all characters (such as :) before the first space and then trim
		st = "initial attack activity";				// Note a special case 20170629:   Light (169) --> stupid reversed information
		temp = sb.substringBetween(mstr, st, get_next_term(mstr, st));
		if (temp == null) {
			st = "initial activity";	// i.e. 20170712 to 18
			temp = sb.substringBetween(mstr, st, get_next_term(mstr, st));
		}
		if (temp != null) {
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
				if (initial_attack_activity.contains("(")) initial_attack_activity = initial_attack_activity.substring(0, initial_attack_activity.indexOf("(")); // special case: 20170620
				if (temp.split(" ").length > 1) {
					initial_attack_activity_number = (temp.substring(temp.lastIndexOf("(") + 1, temp.lastIndexOf(")")).replaceAll("new", "").replaceAll("fire", "").replaceAll("s", "")).trim();		// i.e. 20180915 is a special case
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
		
		st = "new large incidents";
		temp = sb.substringBetween(mstr, st, get_next_term(mstr, st)); 
		if (temp != null) new_large_incidents = (temp.substring(temp.indexOf(" ") + 1)).trim();
		st = "large fires contained";
		temp = sb.substringBetween(mstr, st, get_next_term(mstr, st)); 
		if (temp != null) large_fires_contained = (temp.substring(temp.indexOf(" ") + 1)).trim();
		st = "uncontained large fires";
		temp = sb.substringBetween(mstr, st, get_next_term(mstr, st));  		// fail because of null 	i.e. 20170922	(area command teams committed does not exist)
		if (temp != null) uncontained_large_fires = (temp.substring(temp.indexOf(" ") + 1)).trim();
		st = "area command teams committed";
		temp = sb.substringBetween(mstr, st, get_next_term(mstr, st)); 
		if (temp != null) area_command_teams_committed = (temp.substring(temp.indexOf(" ") + 1)).trim();
		st = "nimos committed";
		temp = sb.substringBetween(mstr, st, get_next_term(mstr, st)); 
		if (temp != null) NIMOs_committed = (temp.substring(temp.indexOf(" ") + 1)).trim();
		st = "type 1 imts committed";
		temp = sb.substringBetween(mstr, st, get_next_term(mstr, st)); 
		if (temp != null) type_1_IMTs_committed = (temp.substring(temp.indexOf(" ") + 1)).trim();
		st = "type 2 imts committed";
		temp = (mstr.substring(mstr.indexOf(st) + 22)).trim();
		if (temp != null) type_2_IMTs_committed = temp.split(" ")[0];
		
		
		// Check either of the 2 lines right after "Active Incident Resource Summary" to see if information of each fire will be in a single line (i.e. 20180803) or multiple lines
		int lineCount = 0;
		for (int i = 0; i < lines.length; i++) {
			if (lines[i].contains("Active Incident Resource Summary")) {
				lineCount = i + 1;
			}
		}
		if (lines[lineCount].split(" ").length > 1 || lines[lineCount + 1].split(" ").length > 1) {	// either line will have at least 2 words
			get_fire_infor_from_single_line(lines);
		} else {	// both lines have 1 or 0 word
			get_fire_infor_from_multiple_lines(lines);
		}

		// IMPORTANT NOTE NOTE NOTE: 20190902-03-04 ... adobe acrobat failed to convert tables (tables are not recognized and not included in text files)
	}
	
	private String get_next_term(String mstr, String st) {
		String temp = (mstr.substring(mstr.indexOf(st) + 1)).trim(); // remove 1 leading character then use it to find next term
		String[] term = new String[] { "national prepareness level", "national fire activity",
				"initial attack activity", "new large incidents", "large fires contained", "uncontained large fires",
				"area command teams committed", "nimos committed", "type 1 imts committed", "type 2 imts committed" };
		for (int i = 0; i < term.length; i++) {
			if (temp.indexOf(term[i]) > -1)
				return term[i];
		}
		return null; // should never happen
	}

	private void get_fire_infor_from_multiple_lines(String[] lines) {		// Information of a Fire is in 15 lines
		// Loop all lines
		String current_area = "";
		int gacc_priority = 0;
		int count = 0;
		for (int i = 0; i < lines.length; i++) {
			if (lines[i].contains("(PL")) {
				if (lines[i].startsWith("Alaska")) {
					current_area = "AICC";
					gacc_priority = gacc_priority + 1;
				} else if (lines[i].startsWith("Eastern")) {
					current_area = "EACC";
					gacc_priority = gacc_priority + 1;
				} else if (lines[i].startsWith("Great Basin")) {
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
			
			if (lines[i].isEmpty() && count == 15 && lines[i - 14].toUpperCase().equals(lines[i - 14]) && lines[i - 14].contains("-")) {		// this is likely a fire, smart check based on the "unit" column
				String fire_priority = String.valueOf(area_fires(current_area).size() + 1);
				String this_fire = String.join("\t", date, current_area, String.valueOf(gacc_priority), fire_priority, lines[i - 15], lines[i - 14], lines[i - 13], lines[i - 12], lines[i - 11],
														lines[i - 10], lines[i - 9], lines[i - 8], lines[i - 7], lines[i - 6],
														lines[i - 5], lines[i - 4], lines[i - 3], lines[i - 2], lines[i - 1]);
				all_fires.add(this_fire);
				area_fires(current_area).add(this_fire);
			}
			count = (lines[i].isEmpty()) ? 0 : (count + 1);	// increase count by 1 if not empty line
		}
	}
	
	private void get_fire_infor_from_single_line(String[] lines) {		// Information of a Fire is in one line		(Note: a special case: 20180803 at page 10 where the table without header if expanding 2 pages) 
		// Loop all lines
		String current_area = "";
		int gacc_priority = 0;
		int count = 0;
		do {
			if (lines[count].contains("(PL")) {
				if (lines[count].startsWith("Alaska")) {
					current_area = "AICC";
					gacc_priority = gacc_priority + 1;
				} else if (lines[count].startsWith("Eastern")) {
					current_area = "EACC";
					gacc_priority = gacc_priority + 1;
				} else if (lines[count].startsWith("Great Basin")) {
					current_area = "GBCC";
					gacc_priority = gacc_priority + 1;
				} else if (lines[count].startsWith("Northern California")) {
					current_area = "ONCC";
					gacc_priority = gacc_priority + 1;
				} else if (lines[count].startsWith("Northern Rockies")) {
					current_area = "NRCC";
					gacc_priority = gacc_priority + 1;
				} else if (lines[count].startsWith("Northwest")) {
					current_area = "NWCC";
					gacc_priority = gacc_priority + 1;
				} else if (lines[count].startsWith("Rocky Mountain")) {
					current_area = "RMCC";
					gacc_priority = gacc_priority + 1;
				} else if (lines[count].startsWith("Southern Area")) {
					current_area = "SACC";
					gacc_priority = gacc_priority + 1;
				} else if (lines[count].startsWith("Southern California")) {
					current_area = "OSCC";
					gacc_priority = gacc_priority + 1;
				} else if (lines[count].startsWith("Southwest")) {
					current_area = "SWCC";
					gacc_priority = gacc_priority + 1;
				}
			}
			
			String[] line_split = lines[count].split(" ");
			int unit_id = 0;	// find the second column of the table
			int line_length = line_split.length;
			if (line_length >= 15 && line_split[line_length - 14].toUpperCase().equals(line_split[line_length - 14]) && line_split[line_length - 14].contains("-")) {		// this is likely a fire, smart check based on the "unit" column
				unit_id = line_split.length - 14;
				
				String fire_priority = String.valueOf(area_fires(current_area).size() + 1);
				String this_fire = String.join("\t", date, current_area, String.valueOf(gacc_priority), fire_priority);
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
