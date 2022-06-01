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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.ASCIIFoldingFilter;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.PorterStemFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.ClassicFilter;
import org.apache.lucene.analysis.standard.ClassicTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

public class SQLserver {

	public SQLserver() {
		// Connect to your database.
		String conn_SIT2015 = "jdbc:sqlserver://localhost:1433;databaseName=SIT2015;integratedSecurity=true";
        ResultSet resultSet = null;
		try (Connection connection = DriverManager.getConnection(conn_SIT2015); Statement statement = connection.createStatement();) {
			// Create and execute a SELECT SQL statement.
			String selectSql = "SELECT [LIFE_SAFETY_HEALTH_STATUS_NARR] FROM [SIT209_HISTORY_INCIDENT_209_REPORTS]";
			resultSet = statement.executeQuery(selectSql);
			// Print results from select statement
//			while (resultSet.next()) {
//				System.out.println(resultSet.getString(1));
//			}
			
			
			
			// Apache Lucene: https://stackoverflow.com/questions/17447045/java-library-for-keywords-extraction-from-input-text
			String st = "";
			while (resultSet.next()) {
				st = st + ". " + resultSet.getString(1);
			}
			
			try {
				List<Keyword> kw = guessFromString(st);
				for (Keyword i : kw) {
					System.out.println(i.getStem() + "\t" + i.getFrequency());
				}
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


	public static String stem(String term) throws IOException {
		TokenStream tokenStream = null;
		try {
			// tokenize
			tokenStream = new ClassicTokenizer(Version.LUCENE_36, new StringReader(term));
			// stem
			tokenStream = new PorterStemFilter(tokenStream);

			// add each token in a set, so that duplicates are removed
			Set<String> stems = new HashSet<String>();
			CharTermAttribute token = tokenStream.getAttribute(CharTermAttribute.class);
			tokenStream.reset();
			while (tokenStream.incrementToken()) {
				stems.add(token.toString());
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
		} finally {
			if (tokenStream != null) {
				tokenStream.close();
			}
		}

	}


	public static List<Keyword> guessFromString(String input) throws IOException {
		TokenStream tokenStream = null;
		try {
			// hack to keep dashed words (e.g. "non-specific" rather than "non" and
			// "specific")
			input = input.replaceAll("-+", "-0");
			// replace any punctuation char but apostrophes and dashes by a space
			input = input.replaceAll("[\\p{Punct}&&[^'-]]+", " ");
			// replace most common english contractions
			input = input.replaceAll("(?:'(?:[tdsm]|[vr]e|ll))+\\b", "");

			// tokenize input
			tokenStream = new ClassicTokenizer(Version.LUCENE_36, new StringReader(input));
			// to lowercase
			tokenStream = new LowerCaseFilter(Version.LUCENE_36, tokenStream);
			// remove dots from acronyms (and "'s" but already done manually above)
			tokenStream = new ClassicFilter(tokenStream);
			// convert any char to ASCII
			tokenStream = new ASCIIFoldingFilter(tokenStream);
			// remove english stop words
			tokenStream = new StopFilter(Version.LUCENE_36, tokenStream, EnglishAnalyzer.getDefaultStopSet());

			List<Keyword> keywords = new LinkedList<Keyword>();
			CharTermAttribute token = tokenStream.getAttribute(CharTermAttribute.class);
			tokenStream.reset();
			while (tokenStream.incrementToken()) {
				String term = token.toString();
				// stem each term
				String stem = stem(term);
				if (stem != null) {
					// create the keyword or get the existing one if any
					Keyword keyword = find(keywords, new Keyword(stem.replaceAll("-0", "-")));
					// add its corresponding initial token
					keyword.add(term.replaceAll("-0", "-"));
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
