package br.unb.hypermap.text;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseTokenizer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.Version;

import java.io.Reader;
import java.util.List;

public class HyperMapAnalyzer extends Analyzer {

    private CharArraySet stopWords;

    public HyperMapAnalyzer() {
        stopWords = StopAnalyzer.ENGLISH_STOP_WORDS_SET;
    }

    public HyperMapAnalyzer(List<String> stopWords) {
        this.stopWords = StopFilter.makeStopSet(Version.LUCENE_44, stopWords);
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
        Tokenizer source = new LowerCaseTokenizer(Version.LUCENE_44, reader);
        TokenStream stopFilter = new StopFilter(Version.LUCENE_44,
                                               source,
                                               stopWords);
        TokenStream result = new PorterStemFilter(stopFilter);

        return new TokenStreamComponents(source, result);
    }

}
