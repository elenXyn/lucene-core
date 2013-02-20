/**
 * 
 */
package com.iwe.index;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 * @author Austin
 *
 */
public class TestIndex {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//index dir : C:/source/JAVA-SE/Lucene-4.1.0/demo/src/test/org/apache/lucene/demo/test-files/docs
		String srcPath = "C:/source/JAVA-SE/Lucene-4.1.0/demo/src/test/org/apache/lucene/demo/test-files/docs";
		String indexPath = "indexs";
		File srcDoc = new File( srcPath );
		TestIndex t = new TestIndex();
		long start = System.currentTimeMillis();
		//test1
		
		try{
			
			Directory dir = FSDirectory.open(new File(indexPath));
			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_41);
			IndexWriterConfig iwc = new IndexWriterConfig( Version.LUCENE_41 , analyzer );
			iwc.setOpenMode(OpenMode.CREATE);
			IndexWriter writer = new IndexWriter( dir , iwc );
			t.indexText(writer, srcDoc );
			writer.forceMerge( 1 );
			writer.close();
			long end = System.currentTimeMillis();
			System.out.println( end - start + " total milliseconds");
		}catch( Exception e ){
			e.printStackTrace();
		}
		
		
	}
	
	
	public void indexText( IndexWriter writer, File file) throws IOException{
		 if (file.canRead()) {
		      if (file.isDirectory()) {
		        String[] files = file.list();
		        // an IO error could occur
		        if (files != null) {
		          for (int i = 0; i < files.length; i++) {
		        	  indexText(writer, new File(file, files[i]));
		          }
		        }
		      } else {
		    	  FileInputStream fis;
		          try {
		            fis = new FileInputStream(file);
		          } catch (FileNotFoundException fnfe) {
		            // at least on windows, some temporary files raise this exception with an "access denied" message
		            // checking if the file can be read doesn't help
		            return;
		          }
		          
		          
		          
		          try {

		              // make a new, empty document
		              Document doc = new Document();

		              // Add the path of the file as a field named "path".  Use a
		              // field that is indexed (i.e. searchable), but don't tokenize 
		              // the field into separate words and don't index term frequency
		              // or positional information:
		              Field pathField = new StringField("path", file.getPath(), Field.Store.YES);
		              doc.add(pathField);

		              // Add the last modified date of the file a field named "modified".
		              // Use a LongField that is indexed (i.e. efficiently filterable with
		              // NumericRangeFilter).  This indexes to milli-second resolution, which
		              // is often too fine.  You could instead create a number based on
		              // year/month/day/hour/minutes/seconds, down the resolution you require.
		              // For example the long value 2011021714 would mean
		              // February 17, 2011, 2-3 PM.
		              doc.add(new LongField("modified", file.lastModified(), Field.Store.NO));

		              // Add the contents of the file to a field named "contents".  Specify a Reader,
		              // so that the text of the file is tokenized and indexed, but not stored.
		              // Note that FileReader expects the file to be in UTF-8 encoding.
		              // If that's not the case searching for special characters will fail.
//		              doc.add(new TextField("contents", new BufferedReader(new InputStreamReader(fis, "UTF-8")) ) );
		              doc.add( new TextField( "contents", readText( fis ) , Field.Store.YES ) );
//		              doc.add( new StringField( "contents" , readText( fis ) , Field.Store.YES ) );
		              

		              if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
		                // New index, so we just add the document (no old document can be there):
		                System.out.println("adding " + file);
		                writer.addDocument(doc);
		              } else {
		                // Existing index (an old copy of this document may have been indexed) so 
		                // we use updateDocument instead to replace the old one matching the exact 
		                // path, if present:
		                System.out.println("updating " + file);
		                writer.updateDocument(new Term("path", file.getPath()), doc);
		              }
		              
		            } finally {
		              fis.close();
		            }
		      }
		 }
	}
	
	public String readText( FileInputStream fis ) throws IOException{
		BufferedReader reader = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
		String ret = null;
		StringBuffer buffer = new StringBuffer();
		while( ( ret = reader.readLine() ) != null ){
			buffer.append( ret );
			buffer.append( "\n");
		}
		fis.close();
		return buffer.toString();
	}

}
