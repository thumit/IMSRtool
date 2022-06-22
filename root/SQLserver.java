/* Note: 
 * https://blog.sqlauthority.com/2019/03/01/sql-server-sql-server-configuration-manager-missing-from-start-menu/
 * https://docs.microsoft.com/en-us/answers/questions/499956/jdbc-connection-issue.html
 * https://docs.microsoft.com/en-us/sql/database-engine/configure-windows/configure-a-windows-firewall-for-database-engine-access?view=sql-server-ver16
 * https://stackoverflow.com/questions/18841744/jdbc-connection-failed-error-tcp-ip-connection-to-host-failed
 * Important: use JDBC driver 9.4 JRE 11 because it is compatible with JDK15 
 * Important: add sqljdbc_xa.dll to the Native Location of the jar in the Build Path
 */
package root;
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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.ClassicAnalyzer;
import org.apache.lucene.analysis.standard.ClassicFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
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
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;

public class SQLserver {

	public SQLserver() {
		// Connect to your database.
		String conn_SIT2015 = "jdbc:sqlserver://localhost:1433;databaseName=SIT2015;integratedSecurity=true";
        ResultSet resultSet = null;
		try (Connection connection = DriverManager.getConnection(conn_SIT2015); Statement statement = connection.createStatement();) {
			// Create and execute a SELECT SQL statement.
//			String selectSql = "SELECT [LIFE_SAFETY_HEALTH_STATUS_NARR] FROM [SIT209_HISTORY_INCIDENT_209_REPORTS]";
			String selectSql = "SELECT [CURRENT_THREAT_12]FROM [SIT209_HISTORY_INCIDENT_209_REPORTS]";
			resultSet = statement.executeQuery(selectSql);
			// Print results from select statement
//			while (resultSet.next()) {
//				System.out.println(resultSet.getString(1));
//			}
			
			// Apache Lucene: https://stackoverflow.com/questions/17447045/java-library-for-keywords-extraction-from-input-text
			List<String> row_data = new ArrayList<String>();
			String combine_st = "";
			while (resultSet.next()) {
				String st = resultSet.getString(1);
				combine_st = combine_st + ". " + st;
				row_data.add(st);
			}
			
			try {
				// Identify Keywords and frequency
				List<Keyword> kw = guessFromString(combine_st);
				for (Keyword i : kw) {
					if (i.getFrequency() > 100) System.out.println(i.getStem() + "\t" + i.getFrequency());
				}
				
				// Search using keyword
				int doc_ID = 0;
				int count = 0;
				for (String st : row_data) {
					if (st != null) {
						try {
							// Reference: https://stackoverflow.com/questions/49066168/search-a-text-string-with-a-lucene-query-in-java
							
							// 0. Specify the analyzer for tokenizing text. The same analyzer should be used for indexing and searching
					        Analyzer analyzer = new ClassicAnalyzer();
							
					        // 1. create the index
					        Directory index = new ByteBuffersDirectory();
					        IndexWriterConfig config = new IndexWriterConfig(analyzer);
					        IndexWriter w = new IndexWriter(index, config);
							doc_ID++;
					        Document doc = new Document();
					        String title = String.valueOf(doc_ID);
					        String content = st;
					        doc.add(new StringField("title", title, Field.Store.YES)); // adding title field
					        doc.add(new TextField("content", content, Field.Store.YES)); // adding content field
					        w.addDocument(doc);
					        w.close();

					        // 2. query
					        String searh_word = "fire";
							Query query = new QueryParser("content", analyzer).parse(searh_word);

					        // 3. search
					        int hitsPerPage = 10;
					        IndexReader reader = DirectoryReader.open(index);
					        IndexSearcher searcher = new IndexSearcher(reader);
					        TopDocs docs = searcher.search(query, hitsPerPage);
					        ScoreDoc[] hits = docs.scoreDocs;

					        // 4. display results
					        int number_of_hits = hits.length;
					        if (number_of_hits > 0) {
//					        	System.out.println(number_of_hits + " hits.");
//								for (int i = 0; i < hits.length; ++i) {
//									int docId = hits[i].doc;
//									Document d = searcher.doc(docId);
//									System.out.println((i + 1) + ". " + d.get("title") + "\t" + d.get("content"));
//								}
								count++;
					        }

					        // 5. reader can only be closed when there is no need to access the documents any more.
					        reader.close();
						} catch (ParseException e) {
							e.printStackTrace();
						}
					}
				}
				System.out.println("Total records that contain the word 'fire' = " + count);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
        catch (SQLException e) {
            e.printStackTrace();
        }
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
			Analyzer analyzer = new ClassicAnalyzer();
			tokenStream = analyzer.tokenStream(null, new StringReader(input));
			tokenStream = new LowerCaseFilter(tokenStream);		// to lower-case
			tokenStream = new ClassicFilter(tokenStream);		// remove dots from acronyms (and "'s" but already done manually above
			tokenStream = new ASCIIFoldingFilter(tokenStream);	// convert any char to ASCII
//			tokenStream = new StopFilter(tokenStream, EnglishAnalyzer.getDefaultStopSet());		// remove English stop words
//			tokenStream = new PorterStemFilter(tokenStream);
//			tokenStream = new SnowballFilter(tokenStream, "English");
			
//			ShingleFilter sf = new ShingleFilter(tokenStream, 2, 2);
//			// sf.setOutputUnigrams(false);
//			tokenStream = sf;
			
			
			
			
			
			

			List<Keyword> keywords = new LinkedList<Keyword>();
			CharTermAttribute token = tokenStream.getAttribute(CharTermAttribute.class);
			tokenStream.reset();
			while (tokenStream.incrementToken()) {
				String term = token.toString();
//				String stem = stem(term);					// stem
				String stem = term.replaceAll("-0", "-");	// no stem
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