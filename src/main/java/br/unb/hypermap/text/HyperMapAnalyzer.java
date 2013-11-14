package br.unb.hypermap.text;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseTokenizer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.benchmark.quality.QualityQuery;
import org.apache.lucene.benchmark.quality.QualityQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;

import java.io.Reader;
import java.util.List;

public class HyperMapAnalyzer extends Analyzer {

    private CharArraySet stopWords;

    private Version version;

    public HyperMapAnalyzer(Version version) {
        this.version = version;
        stopWords = StopAnalyzer.ENGLISH_STOP_WORDS_SET;
    }

    public HyperMapAnalyzer(Version version, List<String> stopWords) {
        this.version = version;
        this.stopWords = StopFilter.makeStopSet(version, stopWords);
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
        Tokenizer source = new LowerCaseTokenizer(version, reader);
        TokenStream stopFilter = new StopFilter(version,
                                               source,
                                               stopWords);
        TokenStream result = new PorterStemFilter(stopFilter);

        return new TokenStreamComponents(source, result);
    }

}
