import java.io.*;
import java.util.*;
import java.lang.*;

 

class IRSystem
{
  private File[] collection;
  
  private ArrayList<String> seen = new ArrayList<String>();
  
  private Map<String, String> permutermIndex = new TreeMap<>();
  
  private ArrayList<String> permutermSeen = new ArrayList<String>();
  
  private Map<String, InvertedIndex> completeInvertedIndex = new HashMap<>();   //This Hashmap assigns every word in the collection with its own InvertedIndex object.
                                                                                //This InvertedIndex object will contain all relevant information needed to calculate its TFIDF.

  private Map<String, Double> documentWordCounts = new HashMap<>();             //Stores the word counts of every document, important for calculaing the Term Frequency later
  
  public IRSystem()
  {
    collection = getFiles();
  }
  
  public File[] getFiles()
  {
    File[] files = null;
    try
    {
      System.out.println();
      System.out.print("Enter the filepath of a directory> ");
      Scanner scan = new Scanner(System.in);
      File dir = new File(scan.nextLine());
      files = dir.listFiles();
      System.out.println();
    }
    catch (Exception e)
    {
      System.out.println("Caught error in getFiles: " + e.toString());
    }
    return files;
  }
  


  public String process(String w)       //Standardizes every word by eliminating any non alphanumeric character
  {
      int ascii = 0; 
      
      for (int i = 0; i < w.length(); i++) {
          if(Character.isUpperCase(w.charAt(i))){    
             w = w.toLowerCase();                   
          }
          
          ascii = (int) w.charAt(i);                                             
          if (!((ascii > 96 && ascii < 123) || (ascii > 64 && ascii < 91) || (ascii > 47 && ascii < 58))) {    
              w = w.substring(0, i) + w.substring((i+1), w.length());            
              --i;
          }

      }
      

    return w;
  }

  public boolean seenBefore(String w)
  {
      if (seen.contains(w)) {            
          return true;
      }
      else {             
          seen.add(w); 
          return false;
      }
     
  }
  


  
  
  public String rotateAsterisk (String w) {                  //Rotates the permuterm so that the asterisk gets moved to the end.
      int asteriskIndex = w.indexOf("*");                    
      w += "$";
          for (int i = 0; i < (asteriskIndex + 1); i++) {
             w = w + w.charAt(0);
             w = w.substring(1, ((w.length())));
          }
      return w;                                             //Returns permuterm version of the initial query, with asterisk at the end
    }
  
  
  

  
  public String getClosestWord (String w ){       //Input: permuterm of the initial query. Compares this permuterm to every permuterm in the index for the closes match.
      
      if(w.contains("*")){                  //First, word is adjusted for use in the permuterm index (in case of submissions with wildcard(*) characters)         
          w = rotateAsterisk(w);
      }
      else {
          w += "$";
      }
      
      
      
      
      //This algorithmn compares the edit distance between the query and every word found in the document collection
      int min = Integer.MAX_VALUE;
      int f = 0;
      String s1;
      String s2;
      String realWord = "";
      
      
      for (String x : permutermIndex.keySet()) {
        s1 = x;
        s2 = w;                     
        
        int len1 = s1.length();
        int len2 = s2.length();
        int m[][] = new int[len1 + 1][len2 + 1];
            
        for (int i = 0; i < len1 + 1; i++) {
            m[i][0] = i;
        }
        
        for (int i = 0; i < len2 + 1; i++) {
            m[0][i] = i;
        }
        
        for (int i = 1; i < (len1 + 1); i++) {
            for (int j = 1; j < (len2 + 1); j++) {
                
                if (s1.charAt(i-1) == s2.charAt(j-1)) {
                    f = 0;
                }
                else {
                    f = 1;
                }
                
                m[i][j] = Math.min((Math.min( 
                (m[i-1][j-1] + f),
                (m[i-1][j] + 1))),
                (m[i][j-1] + 1)
                );
            }
        }
        //end of edit distance calculator
        
        
        //checks if a closer match has been found. If it has, it stores the root word of the closest match, and records new minimum
        if (m[len1][len2] < min) {
            min = m[len1][len2];
            realWord = permutermIndex.get(x);
        }
        
      }
      System.out.println("Word within collection that closest matches query:" + realWord);
      return realWord;    //returns closest root word match to the initial query
      
  }
  
  public void indexPermuterms (String w){
      if (!(permutermSeen.contains(w))) {
          permutermSeen.add(w);
          String originalString = w;
          
          w += "$";
          for (int i = 0; i < w.length(); i++) {    
              permutermIndex.put(w, originalString);
              w = w + w.charAt(0);
              w = w.substring(1, ((w.length())));
              
          }
          
          
      }
  }
  

  public void getTFIDF (String w){                                                       //Calculates the TFIDF of the query and prints out all documents relevant to query, ranked by said TFIDF
      Map<Double, String> rankedDocuments = new TreeMap<>(Collections.reverseOrder());
      double idf = completeInvertedIndex.get(w).giveIDF();
      double tf = 0;
      double tfIdf = 0;
      for (Map.Entry<String, Double> i : completeInvertedIndex.get(w).giveTermFrequency().entrySet()) {    
          tf = i.getValue() / documentWordCounts.get(i.getKey()); 
          tfIdf = tf * idf;
          rankedDocuments.put(tfIdf, (i.getKey()));                
                                                                  
      }
      
 
      
      System.out.println("Results: ");
      for (Map.Entry<Double, String> i : rankedDocuments.entrySet()) {
          System.out.println(i.getValue());
      }
      
  }  
  

  public void start()
  {
    try
    {
      for (File f : collection)
      {
        double wordCount = 0;
        Scanner sc = new Scanner(f);
        while (sc.hasNextLine())
        {
          StringTokenizer st = new StringTokenizer(sc.nextLine());
          while (st.hasMoreTokens())
          {       
            String inputWord = st.nextToken();
            String outputWord = process(inputWord);
              if (outputWord.equals("")) {  //sole purpose is to catch and skip typos, ie 'words' consisting of a stray periods or commas in the document
                  continue;
              }
            indexPermuterms(outputWord);
            
            if (!seenBefore(outputWord))
            {
              InvertedIndex data = new InvertedIndex();         //If this is the first time a word has been seen, it is added to the inverted index hashmap, 
                                                                //and assigned its personal InvertedIndex object to store information in.
              completeInvertedIndex.put(outputWord, data);
            }
           
            completeInvertedIndex.get(outputWord).updateTermFrequency(f.getName()); //increments the raw term frequency by one every time a word is encountered.
            wordCount++;
          }
          
        }
        documentWordCounts.put(f.getName(), wordCount);   //Once a file is fully processed, its word count is stored for future calculations.
          
          
        
      }
      
    }
    catch(Exception e)
    {
      System.out.println("Error in start:  " + e.toString());
    }
  }
}