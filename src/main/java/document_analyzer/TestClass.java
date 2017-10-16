package document_analyzer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class TestClass {

	private static String indexDirectoryPath = "C:\\Users\\i322373\\Desktop";
	private static String dataDirectoryPath = "C:\\Users\\i322373\\Desktop\\textFiles";

	public static void main(String args[]) {

		ArrayList<Map> termVectors;
		ArrayList<ArrayList<Double>> similarityMatrix;

		try {

			LuceneClass luceneClass = new LuceneClass(indexDirectoryPath);
			
			luceneClass.indexDirectory(dataDirectoryPath);
			
			luceneClass.close();
			
			termVectors = luceneClass.getTermVectors();
			
			similarityMatrix = luceneClass.getSimilarityMatrix(termVectors);
			
			for(ArrayList<Double> row : similarityMatrix){
				for(Double CosineSimilarity : row){
					System.out.print(CosineSimilarity+"\t");
				}
				System.out.println("\n");
			}
			
			//System.out.println(termVectors.toString());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
}
