/* Note: 
 * https://blog.sqlauthority.com/2019/03/01/sql-server-sql-server-configuration-manager-missing-from-start-menu/
 * https://docs.microsoft.com/en-us/answers/questions/499956/jdbc-connection-issue.html
 * https://docs.microsoft.com/en-us/sql/database-engine/configure-windows/configure-a-windows-firewall-for-database-engine-access?view=sql-server-ver16
 * https://stackoverflow.com/questions/18841744/jdbc-connection-failed-error-tcp-ip-connection-to-host-failed
 * Important: use JDBC driver 9.4 JRE 11 because it is compatible with JDK15 
 * Important: add sqljdbc_xa.dll to the Native Location of the jar in the Build Path
 */
package sql;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.complexPhrase.ComplexPhraseQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;

import convenience_classes.ColorTextArea;
import convenience_classes.TitleScrollPane;
import root.IMSRmain;

public class Calculate_A1 {
	List<String> year = new ArrayList<String>();
	List<String> INC209R = new ArrayList<String>();
	List<String> INC = new ArrayList<String>();
	List<String> box33_data = new ArrayList<String>();
	List<String> box34_data = new ArrayList<String>();
	List<Integer> box33_point = new ArrayList<Integer>();
	List<Integer> box34_point = new ArrayList<Integer>();
	List<Integer> final_point = new ArrayList<Integer>();
	boolean print_message = true;
	
	public Calculate_A1(List<String> selected_years) {
		// Connect to a database. Single connection can work the same as multiple connections (code for multiple connections is deleted)
		String combine_st = "";
		ResultSet resultSet = null;
		String conn_SIT2015 = "jdbc:sqlserver://localhost:1433;databaseName=SIT2015;integratedSecurity=true";
		try (Connection connection = DriverManager.getConnection(conn_SIT2015);
				Statement statement = connection.createStatement();
				) {
			// Create and execute a SELECT SQL statement.
			String sql_2015 = 
					"""
					-- List of Box34 items:
					--No Likely Threat
					--Potential Future Threat
					--Mass Notifications in Progress
					--Mass Notifications Completed
					--No Evacuation(s) Imminent
					--Planning for Evacuation
					--Planning for Shelter-in-Place
					--Evacuation(s) in Progress
					--Shelter-in-Place in Progress
					--Repopulation in Progress
					--Mass Immunization in Progress
					--Mass Immunization Complete
					--Quarantine in Progress
					--Area Restriction in Effect
					
					SELECT 
					[YEAR], table1.[INC209R_IDENTIFIER], [INC_IDENTIFIER], [LIFE_SAFETY_HEALTH_STATUS_NARR], CODE_NAME_AGGR, ABBREVIATION_AGGR,
					CASE WHEN CHARINDEX('Evacuation(s) in Progress', CODE_NAME_AGGR)>0 THEN 5		--order must be 5, 3, 1. This is very important otherwise results will be wrong
					WHEN CHARINDEX('Planning for Evacuation', CODE_NAME_AGGR)>0 THEN 3
					WHEN CHARINDEX('No Evacuation(s) Imminent', CODE_NAME_AGGR)>0 THEN 1
					ELSE 0 END AS A1_Box34_Points
					FROM
					
					(SELECT 2015 AS [YEAR], [INC209R_IDENTIFIER], [INC_IDENTIFIER],[LIFE_SAFETY_HEALTH_STATUS_NARR] FROM [SIT2015].[dbo].[SIT209_HISTORY_INCIDENT_209_REPORTS]) table1
					LEFT JOIN
					(SELECT INC209R_IDENTIFIER,
					STRING_AGG(STR(INC209RLSM_IDENTIFIER, 7, 0),',') INC209RLSM_IDENTIFIER_AGGR,
					STRING_AGG(STR(LSTT_IDENTIFIER, 7, 0),',') LSTT_IDENTIFIER_AGGR,
					STRING_AGG(ACTIVE_INACTIVE_FLAG,',') ACTIVE_INACTIVE_FLAG_AGGR,
					STRING_AGG(CODE_NAME,',') CODE_NAME_AGGR,
					STRING_AGG(ABBREVIATION,',') ABBREVIATION_AGGR
					FROM [SIT2015].[dbo].[SIT209_HISTORY_INCIDENT_209_LIFE_SAFETY_MGMTS] LEFT JOIN [SIT2015].[dbo].[SIT209_HISTORY_SIT209_LOOKUP_CODES] ON LSTT_IDENTIFIER = LUCODES_IDENTIFIER
					GROUP BY INC209R_IDENTIFIER) table2
					ON table1.INC209R_IDENTIFIER = table2.INC209R_IDENTIFIER
					""";
			String[] sql = new String[selected_years.size()];
			for (int i = 0; i < selected_years.size(); i++) {
				sql[i] = sql_2015.replaceAll("2015", selected_years.get(i));
			}
			String final_sql = String.join(" UNION ", sql) + " ORDER BY INC_IDENTIFIER, INC209R_IDENTIFIER";
			resultSet = statement.executeQuery(final_sql);
			while (resultSet.next()) {
				year.add(resultSet.getString(1));
				INC209R.add(resultSet.getString(2));
				INC.add(resultSet.getString(3));
				box34_point.add(resultSet.getInt(7));
				String st = resultSet.getString(4);
				if (st != null) combine_st = combine_st.concat(".").concat(st);		// https://stackoverflow.com/questions/5076740/whats-the-fastest-way-to-concatenate-two-strings-in-java
				box33_data.add(st);
				box34_data.add(resultSet.getString(5));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		// Identify keywords and frequency using Apache Lucene: https://stackoverflow.com/questions/17447045/java-library-for-keywords-extraction-from-input-text
		try {
			SQL_Utilities utilities = new SQL_Utilities();
			int records_hit_count = 0;
			// Note: downgraded to a warning, level i, evacuation center remains open, evacuations to the town of hyampom (300) expected within the next 24 hours, evacuations are expected
			// Note: trigger points for evacuation have been identified
			// management action points have been established for evacuations of resid
			// evacuation trigger point
			String searh_word = "evac* AND NOT(\"no evac*\"~2 OR \"evac* center*\"~0)";
			int total_rows = box33_data.size();
			for (int row = 0; row < total_rows; row++) {
				String st = box33_data.get(row);
				int this_caterory_point = 0;
				if (st != null) {
					try {
						// Reference:
						// https://stackoverflow.com/questions/49066168/search-a-text-string-with-a-lucene-query-in-java
						// https://www.baeldung.com/lucene-analyzers
						// https://www.baeldung.com/lucene

						// 0. Specify the analyzer for tokenizing text. The same analyzer should be used for indexing and searching
						Analyzer analyzer = new WhitespaceAnalyzer();
						TokenStream tokenStream = utilities.customizeTokenStream(analyzer, st);
						CharTermAttribute token = tokenStream.getAttribute(CharTermAttribute.class);
						tokenStream.reset();
						st = "";
						while (tokenStream.incrementToken()) {
							String token_st = token.toString();
							if (token_st != null) st = st.concat(" ").concat(token.toString());		// https://stackoverflow.com/questions/5076740/whats-the-fastest-way-to-concatenate-two-strings-in-java
						}
						if (tokenStream != null) {
							tokenStream.close();
						}

						// 1. create the index
						Directory index = new ByteBuffersDirectory();
						IndexWriterConfig config = new IndexWriterConfig(analyzer);
						IndexWriter w = new IndexWriter(index, config);
						String[] line = st.split("\\.");
						for (int l = 0; l < line.length; l++) {
							Document doc = new Document();
							String content = line[l];
							String title = String.valueOf(row + 1) + "." + year.get(row) + "." + INC209R.get(row) + ".Line " + String.valueOf(l + 1);
							doc.add(new StringField("title", title, Field.Store.YES)); // adding title field
							doc.add(new TextField("content", content, Field.Store.YES)); // adding content field
							w.addDocument(doc);
						}
						w.close();

						// 2. query
//						Query query = new QueryParser("content", analyzer).parse(searh_word);
						ComplexPhraseQueryParser queryParser = new ComplexPhraseQueryParser("content", analyzer);
						queryParser.setAllowLeadingWildcard(true);
						Query query = queryParser.parse(searh_word);

						// 3. search
						int hitsPerPage = 10;
						IndexReader reader = DirectoryReader.open(index);
						IndexSearcher searcher = new IndexSearcher(reader);
						TopDocs docs = searcher.search(query, hitsPerPage);
						ScoreDoc[] hits = docs.scoreDocs;

						// 4. display results
						int number_of_hits = hits.length;
						if (number_of_hits > 0) {
//							if (print_message) System.out.println(number_of_hits + " hits.");
//							for (int i = 0; i < hits.length; i++) {		// this is actually sentence hit, this will loop all the hit sentences
//								int docId = hits[i].doc;
//								Document d = searcher.doc(docId);
//								if (print_message) System.out.println(d.get("title") + "\t" + d.get("content"));
//							}
							records_hit_count++;
						}
						
						// 5. calculate points
						if (number_of_hits > 0) {
							int max_point = 0;
							for (int i = 0; i < hits.length; i++) {		// this is actually sentence hit, this will loop all the hit sentences
								int docId = hits[i].doc;
								Document d = searcher.doc(docId);
								String c = d.get("content");
								if (max_point < 5) {
									boolean one_point_sentence = (utilities.find_term(new String[] { "evac*lifted" }, c)) ? true : false;
									boolean two_point_sentence = (utilities.find_term(new String[] { "potential*evac", "evac*expected" }, c)) ? true : false;
									boolean three_point_sentence = (utilities.find_term(new String[] {  "advisor*evac",  "evac*advisor", "voluntary*evac", "evac*notice", "evacuation not" }, c)) ? true : false;
									boolean four_point_sentence = (utilities.find_term(new String[] { "evac*warning" }, c)) ? true : false;
									max_point = 5;	// all the others: mandatory, level 1, , level 2, level 3, level i, level ii, level iii, evac (in general)
									if (one_point_sentence) max_point = 1;
									if (two_point_sentence) max_point = 2;
									if (three_point_sentence) max_point = 3;
									if (four_point_sentence) max_point = 4;
								}
							}
							this_caterory_point = max_point;
						}

						// 5. reader can only be closed when there is no need to access the documents any more.
						reader.close();
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
				box33_point.add(this_caterory_point);
			}
			for (int i = 0; i < box33_point.size(); i++) {
				final_point.add(Math.max(box33_point.get(i), box34_point.get(i)));		// final point will the the maximum points between 2 categories
			}
			System.out.println(records_hit_count + " records found by the query using '" + searh_word + "'");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void show_A1_scroll() {
		new A1_Scroll();
	}
	
	class A1_Scroll extends JScrollPane {
		public A1_Scroll() {		
			String[] header = new String[] { "RECORD", "YEAR", "INC", "INC209R", "Box33", "Box34", "Box33_Point", "Box34_Point", "Final_Point" };
			ColorTextArea textarea = new ColorTextArea("icon_tree.png", 75, 75);	// Print to text area
			textarea.append(String.join("\t", header)  + "\n");
			int number_of_records = year.size();
			for (int i = 0; i < number_of_records; i++) {
				textarea.append(String.valueOf(i+1)
						+ "\t" + year.get(i) 
						+ "\t" + INC.get(i) 
						+ "\t" + INC209R.get(i) 
						+ "\t" + box33_data.get(i) 
						+ "\t" + box34_data.get(i) 
						+ "\t" + box33_point.get(i) 
						+ "\t" + box34_point.get(i) 
						+ "\t" + final_point.get(i) 
						+ "\n");
			}
			textarea.setSelectionStart(0);	// scroll to top
			textarea.setSelectionEnd(0);
			textarea.setEditable(false);
			
			TitleScrollPane explore_scrollpane = new TitleScrollPane("", "CENTER", textarea);
			addHierarchyListener(new HierarchyListener() {	//	These codes make the license_scrollpane resizable --> the Big ScrollPane resizable --> JOptionPane resizable
			    public void hierarchyChanged(HierarchyEvent e) {
			        Window window = SwingUtilities.getWindowAncestor(explore_scrollpane);
			        if (window instanceof Dialog) {
			            Dialog dialog = (Dialog)window;
			            if (!dialog.isResizable()) {
			                dialog.setResizable(true);
			                dialog.setPreferredSize(new Dimension((int) (IMSRmain.get_main().getWidth() / 1.1), (int) (IMSRmain.get_main().getHeight() / 1.21)));
			            }
			        }
			    }
			});
			
			// Add the Panel to this Big ScrollPane
			setBorder(BorderFactory.createEmptyBorder());
			setViewportView(explore_scrollpane);
			
			// Add everything to a popup panel
			String ExitOption[] = {"EXIT" };
			int response = JOptionPane.showOptionDialog(IMSRmain.get_DesktopPane(), this, "California Priority Points",
					JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, ExitOption, ExitOption[0]);
		}
	}
}
