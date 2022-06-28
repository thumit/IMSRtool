package root;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.FilteringTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

public final class KeepOnlyFilter extends FilteringTokenFilter {

	private final CharArraySet keepWords;
	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

	public KeepOnlyFilter(TokenStream in, CharArraySet keepWords) {
		super(in);
		this.keepWords = keepWords;
	}

	@Override
	protected boolean accept() {
		return keepWords.contains(termAtt.buffer(), 0, termAtt.length());
	}
}