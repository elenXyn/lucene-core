/**
 * 
 */
package com.iwe.search;




import java.io.File;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 * @author Austin
 *
 */
public class TestSearch {

	static final String FIELD_NAME = "contents";
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try{
			TestSearch t = new TestSearch();
			long start = System.currentTimeMillis();
			t.search( FIELD_NAME , "GNU" , "indexs" );
			System.out.println( "search ps : " + (System.currentTimeMillis() - start ) + " ms");
		}catch( Exception e ){
			e.printStackTrace();
		}
	}
	
	public void search( String field , String searchStr , String index )throws Exception{
		IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(index)));
	    IndexSearcher searcher = new IndexSearcher(reader);
	    Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_41);
	    
	    QueryParser parser = new QueryParser(Version.LUCENE_41, field, analyzer);
	    
	    
	    Query query = parser.parse(searchStr);
	    TopDocs results = searcher.search(query , null , 10 );
	    ScoreDoc[] hits = results.scoreDocs;
	    
	    QueryScorer scorer = new QueryScorer( query , FIELD_NAME );
	    
	    Highlighter highlighter = new Highlighter(scorer);
	    
	    int numTotalHits = results.totalHits;
	    System.out.println(numTotalHits + " total matching documents");
	    
	    for( int i = 0; i < hits.length; i++ ){
	    	System.out.println("doc="+hits[i].doc+" score="+hits[i].score);
	    	Document doc = searcher.doc(hits[i].doc);
	    	
	    	String path = doc.get("path");
	        if (path != null) {
	          System.out.println((i+1) + ". " + path);
	          String title = doc.get("title");
	          if (title != null) {
	            System.out.println("   Title: " + doc.get("title"));
	          }
	        } else {
	          System.out.println((i+1) + ". " + "No path for this document");
	        }	    	
	        
	        System.out.println( "==============highlighter========================" );
	        String storedField = doc.get(FIELD_NAME);
	        
	        
	        TokenStream stream = TokenSources.getAnyTokenStream(searcher.getIndexReader(), results.scoreDocs[i].doc , FIELD_NAME, doc, analyzer);

	        Fragmenter fragmenter = new SimpleSpanFragmenter(scorer);

	        highlighter.setTextFragmenter(fragmenter);

	        String fragment = highlighter.getBestFragment(stream, storedField);

	        System.out.println(fragment);
	        
	        System.out.println( "==============highlighter end ========================" );
	    }
	}

}
