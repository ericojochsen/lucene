package com.erico.lucene;

import org.apache.lucene.analysis.id.*;

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.miscellaneous.SetKeywordMarkerFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.standard.std40.StandardTokenizer40;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;
import org.apache.lucene.util.Version;

/**
 * CustomizedIndonesianAnalyzer if you want to customized the analyzer with or
 * without stopword removal and stemming process
 * Modified from org.apache.lucene.analysis.core.id.IndonesianAnalyzer
 
 * @author Remmy Zen
 * @version 1.0 2016-03-20
 */
public class CustomizedIndonesianAnalyzer extends StopwordAnalyzerBase {
	private boolean useStemmer;
	private boolean useStopword;

	/** File containing default Indonesian stopwords. */
	public final static String DEFAULT_STOPWORD_FILE = "stopwords.txt";

	/**
	 * Returns an unmodifiable instance of the default stop-words set.
	 * 
	 * @return an unmodifiable instance of the default stop-words set.
	 */
	public static CharArraySet getDefaultStopSet() {
		return DefaultSetHolder.DEFAULT_STOP_SET;
	}

	/**
	 * Atomically loads the DEFAULT_STOP_SET in a lazy fashion once the outer
	 * class accesses the static final set the first time.;
	 */
	private static class DefaultSetHolder {
		static final CharArraySet DEFAULT_STOP_SET;

		static {
			try {
				DEFAULT_STOP_SET = loadStopwordSet(false,
						IndonesianAnalyzer.class, DEFAULT_STOPWORD_FILE, "#");
			} catch (IOException ex) {
				// default set should always be present as it is part of the
				// distribution (JAR)
				throw new RuntimeException(
						"Unable to load default stopword set");
			}
		}
	}

	private final CharArraySet stemExclusionSet;

	/**
	 * Builds an analyzer with the default stop words:
	 * {@link #DEFAULT_STOPWORD_FILE}.
	 */
	public CustomizedIndonesianAnalyzer() {
		this(DefaultSetHolder.DEFAULT_STOP_SET);
	}

	/**
	 * Builds an analyzer with the given stop words
	 * 
	 * @param stopwords
	 *            a stopword set
	 */
	public CustomizedIndonesianAnalyzer(CharArraySet stopwords) {
		this(stopwords, CharArraySet.EMPTY_SET);
	}

	/**
	 * Builds an analyzer with the given stop word. If a none-empty stem
	 * exclusion set is provided this analyzer will add a
	 * {@link SetKeywordMarkerFilter} before {@link IndonesianStemFilter}.
	 * 
	 * @param stopwords
	 *            a stopword set
	 * @param stemExclusionSet
	 *            a set of terms not to be stemmed
	 */
	public CustomizedIndonesianAnalyzer(CharArraySet stopwords,
			CharArraySet stemExclusionSet) {
		super(stopwords);
		this.stemExclusionSet = CharArraySet.unmodifiableSet(CharArraySet
				.copy(stemExclusionSet));
	}

	/**
	 * CustomizedIndonesianAnalyzer with stemmer and stopword removal option
	 * 
	 * @param useStemmer
	 *            true if want to use the stemmer, and vice versa
	 * @param useStopword
	 *            true if want to remove stopword, and vice versa
	 */
	public CustomizedIndonesianAnalyzer(boolean useStemmer, boolean useStopword) {
		this();
		this.useStemmer = useStemmer;
		this.useStopword = useStopword;

	}

	/**
	 * Creates {@link org.apache.lucene.analysis.Analyzer.TokenStreamComponents}
	 * used to tokenize all the text in the provided {@link Reader}.
	 * 
	 * @return {@link org.apache.lucene.analysis.Analyzer.TokenStreamComponents}
	 *         built from an {@link StandardTokenizer} filtered with
	 *         {@link StandardFilter}, {@link LowerCaseFilter},
	 *         {@link StopFilter}, {@link SetKeywordMarkerFilter} if a stem
	 *         exclusion set is provided and {@link IndonesianStemFilter}.
	 */
	@Override
	protected TokenStreamComponents createComponents(String fieldName) {
		final Tokenizer source;
		if (getVersion().onOrAfter(Version.LUCENE_4_7_0)) {
			source = new StandardTokenizer();
		} else {
			source = new StandardTokenizer40();
		}
		TokenStream result = new StandardFilter(source);
		result = new LowerCaseFilter(result);
		if (useStopword) {
			result = new StopFilter(result, stopwords);
		}
		if (useStemmer) {
			if (!stemExclusionSet.isEmpty()) {
				result = new SetKeywordMarkerFilter(result, stemExclusionSet);
			}
			return new TokenStreamComponents(source, new IndonesianStemFilter(
					result));
		} else {
			return new TokenStreamComponents(source, result);
		}
	}
}
