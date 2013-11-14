package br.unb.hypermap.text;

import org.apache.lucene.benchmark.quality.QualityQuery;
import org.apache.lucene.benchmark.quality.QualityQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParserBase;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;

/**
 * Created with IntelliJ IDEA.
 * User: alexandrelucchesi
 * Date: 11/14/13
 * Time: 5:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class HyperMapQQAnalyzer implements QualityQueryParser {

    ThreadLocal<QueryParser> queryParser = new ThreadLocal<QueryParser>();

    private String indexField;

    public String qqNames[];


    public HyperMapQQAnalyzer(String qqNames[], String indexField) {
        this.qqNames = qqNames;
        this.indexField = indexField;
    }

    public HyperMapQQAnalyzer(String qqName, String indexField) {
        this(new String[]{qqName}, indexField);
    }

    @Override
    public Query parse(QualityQuery qq) throws ParseException {
        QueryParser qp = (QueryParser) queryParser.get();
        if (qp == null) {
            qp = new QueryParser(Version.LUCENE_44, indexField, new HyperMapAnalyzer(Version.LUCENE_44));
            queryParser.set(qp);
        }
        BooleanQuery bq = new BooleanQuery();
        for (int i = 0; i < qqNames.length; i++) {
            bq.add(qp.parse(QueryParserBase.escape(qq.getValue(qqNames[i]))), BooleanClause.Occur.SHOULD);
        }
        return bq;
    }

}
