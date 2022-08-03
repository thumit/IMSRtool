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

import convenience_classes.TextAreaReadMe;
import convenience_classes.TitleScrollPane;
import root.IMSRmain;

public class Calculate_A3 {
	List<String> year = new ArrayList<String>();
	List<String> INC209R = new ArrayList<String>();
	List<String> INC = new ArrayList<String>();
	List<String> box38_12h_data = new ArrayList<String>();
	List<String> box38_24h_data = new ArrayList<String>();
	List<String> box38_48h_data = new ArrayList<String>();
	List<String> box38_72h_data = new ArrayList<String>();
	List<String> box38_GT72_data = new ArrayList<String>();
	List<Integer> box38_12h_point = new ArrayList<Integer>();
	List<Integer> box38_24h_point = new ArrayList<Integer>();
	List<Integer> box38_48h_point = new ArrayList<Integer>();
	List<Integer> box38_72h_point = new ArrayList<Integer>();
	List<Integer> box38_GT72_point = new ArrayList<Integer>();
	List<Integer> final_point = new ArrayList<Integer>();
	boolean print_message = true;
	
	public Calculate_A3() {
		// Connect to a database. Single connection can work the same as multiple connections (code for multiple connections is deleted)
		String combine_st = "";
		ResultSet resultSet = null;
		String conn_SIT2015 = "jdbc:sqlserver://localhost:1433;databaseName=SIT2015;integratedSecurity=true";
		try (Connection connection = DriverManager.getConnection(conn_SIT2015);
				Statement statement = connection.createStatement();
				) {
			// Create and execute a SELECT SQL statement.
			String selectSql = 
					"""
					SELECT 2015, [INC209R_IDENTIFIER], [INC_IDENTIFIER], [CURRENT_THREAT_12], [CURRENT_THREAT_24], [CURRENT_THREAT_48], [CURRENT_THREAT_72], [CURRENT_THREAT_GT72] FROM [SIT2015].[dbo].[SIT209_HISTORY_INCIDENT_209_REPORTS]
					UNION
					SELECT 2016, [INC209R_IDENTIFIER], [INC_IDENTIFIER], [CURRENT_THREAT_12], [CURRENT_THREAT_24], [CURRENT_THREAT_48], [CURRENT_THREAT_72], [CURRENT_THREAT_GT72] FROM [SIT2016].[dbo].[SIT209_HISTORY_INCIDENT_209_REPORTS]
					ORDER BY [INC_IDENTIFIER], [INC209R_IDENTIFIER]
					""";
			resultSet = statement.executeQuery(selectSql);
			while (resultSet.next()) {
				year.add(resultSet.getString(1));
				INC209R.add(resultSet.getString(2));
				INC.add(resultSet.getString(3));
				String st_12 = resultSet.getString(4);
				String st_24 = resultSet.getString(5);
				String st_48 = resultSet.getString(6);
				String st_72 = resultSet.getString(7);
				String st_GT72 = resultSet.getString(8);
				if (st_12 != null) combine_st = combine_st.concat(".").concat(st_12);
				if (st_24 != null) combine_st = combine_st.concat(".").concat(st_24);
				if (st_48 != null) combine_st = combine_st.concat(".").concat(st_48);
				if (st_72 != null) combine_st = combine_st.concat(".").concat(st_72);
				if (st_GT72 != null) combine_st = combine_st.concat(".").concat(st_GT72);
				box38_12h_data.add(st_12);
				box38_24h_data.add(st_24);
				box38_48h_data.add(st_48);
				box38_72h_data.add(st_72);
				box38_GT72_data.add(st_GT72);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		// Identify keywords and frequency using Apache Lucene: https://stackoverflow.com/questions/17447045/java-library-for-keywords-extraction-from-input-text
		try {
			SQL_Utilities utilities = new SQL_Utilities();
			int records_hit_count = 0;
//			String searh_word = "restrict*";
//			String searh_word = "\"road* clos*\"~0";		// Lucene proximity search: https://lucene.apache.org/core/3_6_0/queryparsersyntax.html#Range%20Searches
			// discontinued, lifted, removed, open		except for, could be closed, no, none		potential, being developed, being assessed, being signed, issued, been reduced, changed, modified, soft
			String searh_word = "(power* OR energy* OR water*) AND (line* OR corridor* OR system*) AND NOT(\"no threat*\"~4 OR \"no risk\"~4 OR \"no impact\"~4 OR \"not damage\"~4)";
			int total_rows = box38_12h_data.size();
			for (int row = 0; row < total_rows; row++) {
				String st = "";
				int end_point = 0;
				int this_caterory_point = 0;
				
				
				//---------------------------------------------------------------------------------------------------------------
				// Five points
				st = box38_12h_data.get(row);
				this_caterory_point = 0;
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
							if (print_message) System.out.println(number_of_hits + " hits.");
							for (int i = 0; i < hits.length; i++) {		// this is actually sentence hit, this will loop all the hit sentences
								int docId = hits[i].doc;
								Document d = searcher.doc(docId);
								if (print_message) System.out.println(d.get("title") + "\t" + d.get("content"));
							}
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
									boolean five_point_sentence = (utilities .find_term(new String[] { "shut*down", "damage*", }, c)) ? true : false;
									if (five_point_sentence) max_point = 5;
								}
							}
							if (print_message) System.out.println("B3 Points = " + max_point);
							this_caterory_point = max_point;
						}

						// 5. reader can only be closed when there is no need to access the documents any more.
						reader.close();
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
				if (end_point < this_caterory_point) end_point = this_caterory_point;
				box38_12h_point.add(this_caterory_point);
				
				
				
				
				//---------------------------------------------------------------------------------------------------------------
				// Four points
				st = box38_24h_data.get(row);
				this_caterory_point = 0;
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
							if (print_message) System.out.println(number_of_hits + " hits.");
							for (int i = 0; i < hits.length; i++) {		// this is actually sentence hit, this will loop all the hit sentences
								int docId = hits[i].doc;
								Document d = searcher.doc(docId);
								if (print_message) System.out.println(d.get("title") + "\t" + d.get("content"));
							}
							records_hit_count++;
						}
						
						// 5. calculate points
						if (number_of_hits > 0) {
							int max_point = 0;
							for (int i = 0; i < hits.length; i++) {		// this is actually sentence hit, this will loop all the hit sentences
								int docId = hits[i].doc;
								Document d = searcher.doc(docId);
								String c = d.get("content");
								if (max_point < 4) {
									max_point = 4;
								}
							}
							if (print_message) System.out.println("B3 Points = " + max_point);
							this_caterory_point = max_point;
						}

						// 5. reader can only be closed when there is no need to access the documents any more.
						reader.close();
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
				if (end_point < this_caterory_point) end_point = this_caterory_point;
				box38_24h_point.add(this_caterory_point);
				
				
				
				//---------------------------------------------------------------------------------------------------------------
				// Three points
				st = box38_48h_data.get(row);
				this_caterory_point = 0;
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
							if (print_message) System.out.println(number_of_hits + " hits.");
							for (int i = 0; i < hits.length; i++) {		// this is actually sentence hit, this will loop all the hit sentences
								int docId = hits[i].doc;
								Document d = searcher.doc(docId);
								if (print_message) System.out.println(d.get("title") + "\t" + d.get("content"));
							}
							records_hit_count++;
						}
						
						// 5. calculate points
						if (number_of_hits > 0) {
							int max_point = 0;
							for (int i = 0; i < hits.length; i++) {		// this is actually sentence hit, this will loop all the hit sentences
								int docId = hits[i].doc;
								Document d = searcher.doc(docId);
								String c = d.get("content");
								if (max_point < 3) {
									max_point = 3;
								}
							}
							if (print_message) System.out.println("B3 Points = " + max_point);
							this_caterory_point = max_point;
						}

						// 5. reader can only be closed when there is no need to access the documents any more.
						reader.close();
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
				if (end_point < this_caterory_point) end_point = this_caterory_point;
				box38_48h_point.add(this_caterory_point);
				
				
				
				
				//---------------------------------------------------------------------------------------------------------------
				// Two points
				st = box38_72h_data.get(row);
				this_caterory_point = 0;
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
							if (print_message) System.out.println(number_of_hits + " hits.");
							for (int i = 0; i < hits.length; i++) {		// this is actually sentence hit, this will loop all the hit sentences
								int docId = hits[i].doc;
								Document d = searcher.doc(docId);
								if (print_message) System.out.println(d.get("title") + "\t" + d.get("content"));
							}
							records_hit_count++;
						}
						
						// 5. calculate points
						if (number_of_hits > 0) {
							int max_point = 0;
							for (int i = 0; i < hits.length; i++) {		// this is actually sentence hit, this will loop all the hit sentences
								int docId = hits[i].doc;
								Document d = searcher.doc(docId);
								String c = d.get("content");
								if (max_point < 2) {
									max_point = 2;
								}
							}
							if (print_message) System.out.println("B3 Points = " + max_point);
							this_caterory_point = max_point;
						}

						// 5. reader can only be closed when there is no need to access the documents any more.
						reader.close();
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
				if (end_point < this_caterory_point) end_point = this_caterory_point;
				box38_72h_point.add(this_caterory_point);
				
				
				
				
				//---------------------------------------------------------------------------------------------------------------
				// One points
				st = box38_GT72_data.get(row);
				this_caterory_point = 0;
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
							if (print_message) System.out.println(number_of_hits + " hits.");
							for (int i = 0; i < hits.length; i++) {		// this is actually sentence hit, this will loop all the hit sentences
								int docId = hits[i].doc;
								Document d = searcher.doc(docId);
								if (print_message) System.out.println(d.get("title") + "\t" + d.get("content"));
							}
							records_hit_count++;
						}
						
						// 5. calculate points
						if (number_of_hits > 0) {
							int max_point = 0;
							for (int i = 0; i < hits.length; i++) {		// this is actually sentence hit, this will loop all the hit sentences
								int docId = hits[i].doc;
								Document d = searcher.doc(docId);
								String c = d.get("content");
								if (max_point < 1) {
									max_point = 1;
								}
							}
							if (print_message) System.out.println("B3 Points = " + max_point);
							this_caterory_point = max_point;
						}

						// 5. reader can only be closed when there is no need to access the documents any more.
						reader.close();
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
				if (end_point < this_caterory_point) end_point = this_caterory_point;
				box38_GT72_point.add(this_caterory_point);
				
				
				
				final_point.add(end_point);
			}
			System.out.println(records_hit_count + " records found by the query using '" + searh_word + "'");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void show_A3_scroll() {
		new A3_Scroll();
	}
	
	class A3_Scroll extends JScrollPane {
		public A3_Scroll() {		
			String[] header = new String[] { "RECORD", "YEAR", "INC", "INC209R", "Box38_12h", "Box38_12h_Point", "Final_Point" };
			TextAreaReadMe textarea = new TextAreaReadMe("icon_tree.png", 75, 75);	// Print to text area
			textarea.append(String.join("\t", header)  + "\n");
			int number_of_records = year.size();
			for (int i = 0; i < number_of_records; i++) {
				textarea.append(String.valueOf(i+1)
						+ "\t" + year.get(i) 
						+ "\t" + INC.get(i) 
						+ "\t" + INC209R.get(i) 
						+ "\t" + box38_12h_data.get(i) 
						+ "\t" + box38_12h_point.get(i) 
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