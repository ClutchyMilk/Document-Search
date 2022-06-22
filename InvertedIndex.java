
import java.util.HashMap;
import java.util.Map;

class InvertedIndex {                 //The purpose of this class is to serve as a container for all relevent information each word needs to hold,
                                        //Such as its term frequency in every document it appears in, its IDF, etc.
      private double idf;
      private Map<String, Double> termFrequency = new HashMap<>();
      
      public InvertedIndex(){
          idf = 0;
      }
      
      public double giveIDF(){
          this.idf = ((double)1)/this.termFrequency.size();
                                                              
          return this.idf;
      }
      
      public void updateTermFrequency(String w){
          if (this.termFrequency.containsKey(w)) {                     
              this.termFrequency.put(w, (this.termFrequency.get(w)+1));   
          }
          else{
              this.termFrequency.put(w, 1.0);
          }
      }
      
      public Map<String, Double> giveTermFrequency(){
          return this.termFrequency;
      }
      
      
  }
