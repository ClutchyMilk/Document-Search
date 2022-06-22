/*
   Description:          Analyzes a series of documents from a directory and 
                         processes them for use an Information Retrieval System.
                         To achieve this, every wrod in the ocllection is "standardized"
                         by having all punctuation removed and all cases be lowercase.

                         Additionally, this programs also allows for the searching of
                         a directory based on a single word query, in which documents
                         are returned based on their relevancy using the TFIDF model.
*/

import java.util.*;
import java.lang.*;

public class documentQuery
{
  public static void main (String args[]) throws Exception
  {
      Scanner scanner = new Scanner(System.in);
      
      IRSystem ir = new IRSystem();
      ir.start();
  
    while (true) {  
      System.out.println("Please enter your query(Type 0 to exit): ");
      String query = scanner.nextLine();
      if (query.toLowerCase().equals("0")) {
            System.out.println("Goodbye.");
            break;
      }            
      
      query = ir.getClosestWord(query);                //fetches closest match of the query in the collection      
      
      System.out.println("Searching for " + query + "...");
      
      ir.getTFIDF(query);                          //prints relevant doucments containing the query term, ranked by TFIDF(Descending order)

    }  
         
  }
 
}
