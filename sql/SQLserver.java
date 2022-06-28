/* Note: 
 * https://blog.sqlauthority.com/2019/03/01/sql-server-sql-server-configuration-manager-missing-from-start-menu/
 * https://docs.microsoft.com/en-us/answers/questions/499956/jdbc-connection-issue.html
 * https://docs.microsoft.com/en-us/sql/database-engine/configure-windows/configure-a-windows-firewall-for-database-engine-access?view=sql-server-ver16
 * https://stackoverflow.com/questions/18841744/jdbc-connection-failed-error-tcp-ip-connection-to-host-failed
 * Important: use JDBC driver 9.4 JRE 11 because it is compatible with JDK15 
 * Important: add sqljdbc_xa.dll to the Native Location of the jar in the Build Path
 */
package sql;

import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
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

public class SQLserver {

	public SQLserver() {
		A2_Calculation();
	}
	
	public void A2_Calculation() {
		List<String> year = new ArrayList<String>();
		List<String> INC209R = new ArrayList<String>();
		List<String> row_data = new ArrayList<String>();
		String combine_st = "";
		
		// Connect to your database.
		// Single connection can work the same as the below multiple connections 
		ResultSet resultSet = null;
		String conn_SIT2015 = "jdbc:sqlserver://localhost:1433;databaseName=SIT2015;integratedSecurity=true";
		try (Connection connection = DriverManager.getConnection(conn_SIT2015);
				Statement statement = connection.createStatement();
				) {
			// Create and execute a SELECT SQL statement.
			String selectSql = 
					"""
					SELECT 2015, [INC209R_IDENTIFIER], [LIFE_SAFETY_HEALTH_STATUS_NARR] FROM [SIT2015].[dbo].[SIT209_HISTORY_INCIDENT_209_REPORTS]
					UNION
					SELECT 2016, [INC209R_IDENTIFIER], [LIFE_SAFETY_HEALTH_STATUS_NARR] FROM [SIT2016].[dbo].[SIT209_HISTORY_INCIDENT_209_REPORTS]
					""";
			resultSet = statement.executeQuery(selectSql);
			while (resultSet.next()) {
				year.add(resultSet.getString(1));
				INC209R.add(resultSet.getString(2));
				String st = resultSet.getString(3);
				combine_st = combine_st + " " + st;
				row_data.add(st);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
//		// Multiple connection		
//		List<String> databases = Arrays.asList("SIT2015", "SIT2016");
//		for (String database : databases) {
//			String conn_SIT2015 = "jdbc:sqlserver://localhost:1433;databaseName=" + database + ";integratedSecurity=true";
//			ResultSet resultSet = null;
//			try (Connection connection = DriverManager.getConnection(conn_SIT2015);
//					Statement statement = connection.createStatement();
//					) {
//				// Create and execute a SELECT SQL statement.
//				String selectSql = "SELECT [INC209R_IDENTIFIER], [LIFE_SAFETY_HEALTH_STATUS_NARR] FROM [SIT209_HISTORY_INCIDENT_209_REPORTS]";
////				String selectSql = "SELECT [CURRENT_THREAT_12]FROM [SIT209_HISTORY_INCIDENT_209_REPORTS]";
//
//				resultSet = statement.executeQuery(selectSql);
//				while (resultSet.next()) {
//					INC209R.add(database + "." + resultSet.getString(1));
//					String st = resultSet.getString(2);
//					combine_st = combine_st + " " + st;
//					row_data.add(st);
//				}
//			} catch (SQLException e) {
//				e.printStackTrace();
//			}
//		}
		
		// Apache Lucene: https://stackoverflow.com/questions/17447045/java-library-for-keywords-extraction-from-input-text
		try {	// Identify Keywords and frequency
			int allword_frequency = 0;
			int keyword_frequency = 0;
			List<Keyword> kw = guessFromString(combine_st);
			for (Keyword i : kw) {
				int freq = i.getFrequency();
				if (freq >= 10) {
					System.out.println(i.getStem() + "\t" + freq);
					keyword_frequency = keyword_frequency + freq;
				}
				allword_frequency = allword_frequency + freq;
			}
			System.out.println(keyword_frequency);
			System.out.println(allword_frequency);
			double ratio = (double) keyword_frequency / (double) allword_frequency * 100;
			System.out.println("selection vs all ratio = " + ratio + " %");

			// Search using keyword
			int records_hit_count = 0;
//			String searh_word = "restrict*";
//			String searh_word = "\"road* clos*\"~0";		// Lucene proximity search: https://lucene.apache.org/core/3_6_0/queryparsersyntax.html#Range%20Searches
//			String searh_word = "\"no clos*\"~4 OR \"clos* none\"~4";
			// discontinued, lifted, removed, open		except for, could be closed, no, none		potential, being assessed, being signed, been reduced, being developed
			String searh_word = "(area* OR highway* OR hwy* OR motorway* OR road* OR route* OR trail*) AND clos* AND NOT(discontinu* OR lift* OR remove* OR *open*) AND NOT(\"no clos*\"~4 OR \"clos* none\"~4)";
			int total_rows = row_data.size();
			for (int row = 0; row < total_rows; row++) {
				String st = row_data.get(row);
				if (st != null) {
					try {
						// Reference:
						// https://stackoverflow.com/questions/49066168/search-a-text-string-with-a-lucene-query-in-java
						// https://www.baeldung.com/lucene-analyzers
						// https://www.baeldung.com/lucene

						// 0. Specify the analyzer for tokenizing text. The same analyzer should be used
						// for indexing and searching
						Analyzer analyzer = new SimpleAnalyzer();
						TokenStream tokenStream = analyzer.tokenStream(null, new StringReader(st));
						tokenStream = new LowerCaseFilter(tokenStream); // to lower-case
						tokenStream = new ASCIIFoldingFilter(tokenStream); // convert any char to ASCII
//						tokenStream = new ClassicFilter(tokenStream); // remove dots from acronyms (and "'s" but already done manually above
//						tokenStream = new StopFilter(tokenStream, EnglishAnalyzer.getDefaultStopSet());		// remove English stop words
//						tokenStream = new PorterStemFilter(tokenStream);
//						tokenStream = new SnowballFilter(tokenStream, "English");
						
//						List<String> filter_words = Arrays.asList("road", "close");
//						CharArraySet chars = new CharArraySet(filter_words, false);
//						tokenStream = new KeepWordFilter(tokenStream, chars);
						
//						ShingleFilter sf = new ShingleFilter(tokenStream, 2, 2);
//						// sf.setOutputUnigrams(false);
//						tokenStream = sf;
						
						
						
						

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
							System.out.println(number_of_hits + " hits.");
							for (int i = 0; i < hits.length; ++i) {
								int docId = hits[i].doc;
								Document d = searcher.doc(docId);
								System.out.println(d.get("title") + "\t" + d.get("content"));
							}
							records_hit_count++;
						}
						
						// 5. calculate points
						if (number_of_hits > 0) {
							int max_point = 0;
							for (int i = 0; i < hits.length; ++i) {
								int docId = hits[i].doc;
								Document d = searcher.doc(docId);
								List<Keyword> kwords = guessFromString(d.get("content"));
								if (max_point < 5) {
//									if (find(kwords, new Keyword("area")).getFrequency() > 0) max_point = 5;
//									else if (find(kwords, new Keyword("highway")).getFrequency() > 0) max_point = 5;
//									else if (find(kwords, new Keyword("hwy")).getFrequency() > 0) max_point = 5;
									if (Pattern.compile(get_Regex("area*")).matcher(d.get("content").toLowerCase()).find()) max_point = 5;	// Regex guide: https://dev.to/kooin/wildcard-type-search-in-java-pattern-3h54
									else if (Pattern.compile(get_Regex("highway*")).matcher(d.get("content").toLowerCase()).find()) max_point = 5;
									else if (Pattern.compile(get_Regex("hwy*")).matcher(d.get("content").toLowerCase()).find()) max_point = 5;
									if (max_point < 3) {
										if (Pattern.compile(get_Regex("motorway*")).matcher(d.get("content").toLowerCase()).find()) max_point = 3;
										else if (Pattern.compile(get_Regex("road*")).matcher(d.get("content").toLowerCase()).find()) max_point = 3;
										else if (Pattern.compile(get_Regex("route*")).matcher(d.get("content").toLowerCase()).find()) max_point = 3;
										else if (Pattern.compile(get_Regex("trail*")).matcher(d.get("content").toLowerCase()).find()) max_point = 3;
									}
								}
							}
							System.out.println("A2 Points = " + max_point);
						}

						// 5. reader can only be closed when there is no need to access the documents
						// any more.
						reader.close();
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
			}
			System.out.println("Number of records that contain the word '" + searh_word + "' = " + records_hit_count);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public static String get_Regex (String input) {
		return ("\\Q" + input + "\\E").replace("*", "\\E.*\\Q");
	}
	
	public static <T> T find(Collection<T> collection, T example) {
		for (T element : collection) {
			if (element.equals(example)) {
				return element;
			}
		}
		collection.add(example);
		return example;
	}

//	public static String stem(String term) throws IOException {
//		TokenStream tokenStream = null;
//		try {
//			// tokenize
//			tokenStream = new ClassicTokenizer(Version.LUCENE_36, new StringReader(term));
//			// stem
//			tokenStream = new PorterStemFilter(tokenStream);
//
//			// add each token in a set, so that duplicates are removed
//			Set<String> stems = new HashSet<String>();
//			CharTermAttribute token = tokenStream.getAttribute(CharTermAttribute.class);
//			tokenStream.reset();
//			while (tokenStream.incrementToken()) {
//				stems.add(token.toString());
//			}
//
//			// if no stem or 3+ stems have been found, return null
//			if (stems.size() == 0 || stems.size() >= 3) {
//				return null;
//			}
//			String stem = stems.iterator().next();
//			// if the stem has non-alphanumerical chars, return null
//			if (!stem.matches("[a-zA-Z0-9-]+")) {
//				return null;
//			}
//			return stem;
//		} finally {
//			if (tokenStream != null) {
//				tokenStream.close();
//			}
//		}
//
//	}

	public static String stem(String term) {
		// add each token in a set, so that duplicates are removed
		Set<String> stems = new HashSet<String>();
		String[] arr = term.split("\\s+");
		for (String st : arr) {
			stems.add(st);
		}

		// if no stem or 3+ stems have been found, return null
		if (stems.size() == 0 || stems.size() >= 3) {
			return null;
		}
		String stem = stems.iterator().next();
		// if the stem has non-alphanumerical chars, return null
		if (!stem.matches("[a-zA-Z0-9-]+")) {
			return null;
		}
		return stem;
	}

	public static List<Keyword> guessFromString(String input) throws IOException {
		TokenStream tokenStream = null;
		try {
//			// hack to keep dashed words (e.g. "non-specific" rather than "non" and "specific")
//			input = input.replaceAll("-+", "-0");
//			// replace any punctuation char but apostrophes and dashes by a space
//			input = input.replaceAll("[\\p{Punct}&&[^'-]]+", " ");
//			// replace most common english contractions
//			input = input.replaceAll("(?:'(?:[tdsm]|[vr]e|ll))+\\b", "");

			// tokenize input
			Analyzer analyzer = new SimpleAnalyzer();
			tokenStream = analyzer.tokenStream(null, new StringReader(input));
			tokenStream = new LowerCaseFilter(tokenStream); // to lower-case
			tokenStream = new ASCIIFoldingFilter(tokenStream); // convert any char to ASCII
//			tokenStream = new ClassicFilter(tokenStream); // remove dots from acronyms (and "'s" but already done manually above
//			tokenStream = new StopFilter(tokenStream, EnglishAnalyzer.getDefaultStopSet());		// remove English stop words
//			tokenStream = new PorterStemFilter(tokenStream);
//			tokenStream = new SnowballFilter(tokenStream, "English");
			
//			List<String> filter_words = Arrays.asList("road", "close");
//			CharArraySet chars = new CharArraySet(filter_words, false);
//			tokenStream = new KeepWordFilter(tokenStream, chars);
			
//			ShingleFilter sf = new ShingleFilter(tokenStream, 2, 2);
//			// sf.setOutputUnigrams(false);
//			tokenStream = sf;
			
			
			
			
			

			

			List<Keyword> keywords = new LinkedList<Keyword>();
			CharTermAttribute token = tokenStream.getAttribute(CharTermAttribute.class);
			tokenStream.reset();
			while (tokenStream.incrementToken()) {
				String term = token.toString();
//				String stem = stem(term);					// stem
//				String stem = term.replaceAll("-0", "-");	// no stem
				String stem = term;
				if (stem != null) {
					// create the keyword or get the existing one if any
					Keyword keyword = find(keywords, new Keyword(stem));
					// add its corresponding initial token
					keyword.add(term);
				}
			}

			// reverse sort by frequency
			Collections.sort(keywords);
			return keywords;
		} finally {
			if (tokenStream != null) {
				tokenStream.close();
			}
		}
	}
}
