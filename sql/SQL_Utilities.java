package sql;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

public class SQL_Utilities {

	public static boolean find_term (String[] term, String field) {
		for (String st : term) {
			if (Pattern.compile(get_Regex(st)).matcher(field.toLowerCase()).find()) return true;
		}
		return false;
	}
	
	public static String get_Regex (String input) {
		return ("\\Q" + input + "\\E").replace("*", "\\E.*\\Q");	// Regex guide: https://dev.to/kooin/wildcard-type-search-in-java-pattern-3h54
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

//	public static String stem(String term) {
//		// add each token in a set, so that duplicates are removed
//		Set<String> stems = new HashSet<String>();
//		String[] arr = term.split("\\s+");
//		for (String st : arr) {
//			stems.add(st);
//		}
//
//		// if no stem or 3+ stems have been found, return null
//		if (stems.size() == 0 || stems.size() >= 3) {
//			return null;
//		}
//		String stem = stems.iterator().next();
//		// if the stem has non-alphanumerical chars, return null
//		if (!stem.matches("[a-zA-Z0-9-]+")) {
//			return null;
//		}
//		return stem;
//	}

	public static TokenStream customizeTokenStream(Analyzer analyzer, String input) {
//		// hack to keep dashed words (e.g. "non-specific" rather than "non" and "specific")
//		input = input.replaceAll("-+", "-0");
//		// replace any punctuation char but apostrophes and dashes by a space
//		input = input.replaceAll("[\\p{Punct}&&[^'-]]+", " ");
//		// replace most common english contractions
//		input = input.replaceAll("(?:'(?:[tdsm]|[vr]e|ll))+\\b", "");
		
		TokenStream tokenStream = analyzer.tokenStream(null, new StringReader(input));
		tokenStream = new LowerCaseFilter(tokenStream); // to lower-case
		tokenStream = new ASCIIFoldingFilter(tokenStream); // convert any char to ASCII
//		tokenStream = new ClassicFilter(tokenStream); // remove dots from acronyms (and "'s" but already done manually above
//		tokenStream = new StopFilter(tokenStream, EnglishAnalyzer.getDefaultStopSet());		// remove English stop words
//		tokenStream = new PorterStemFilter(tokenStream);
//		tokenStream = new SnowballFilter(tokenStream, "English");
		
//		List<String> filter_words = Arrays.asList("road", "close");
//		CharArraySet chars = new CharArraySet(filter_words, false);
//		tokenStream = new KeepWordFilter(tokenStream, chars);
		
//		ShingleFilter sf = new ShingleFilter(tokenStream, 2, 2);
//		// sf.setOutputUnigrams(false);
//		tokenStream = sf;
		return tokenStream;
	}
	
	public static List<Keyword> guessFromString(String input) throws IOException {
		TokenStream tokenStream = null;
		try {
			// tokenize input
			Analyzer analyzer = new WhitespaceAnalyzer();
			tokenStream = customizeTokenStream(analyzer, input);

			// store keywords
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
