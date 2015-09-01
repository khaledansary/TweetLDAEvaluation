/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tweetlda;

import cc.mallet.util.*;
import cc.mallet.types.*;
import cc.mallet.pipe.*;
import cc.mallet.pipe.iterator.*;
import cc.mallet.topics.*;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author khaledd
 */
public class TweetLDA {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, InterruptedException, Exception {
        TweetLDA obj = new TweetLDA();
        
          //  obj.init("K:\\LDA\\train_file.txt", "K:\\LDA\\test_file.txt","E:\\","H:\\nlp jar files\\mallet-2.0.7\\mallet-2.0.7\\stoplists\\en.txt",10,2);
        obj.init(args[0], args[1],args[2],args[3],Integer.parseInt(args[4]),Integer.parseInt(args[5]));
        
        
    }
    public void init(String trainingFile,String testingFile,String outputDir,String stopwords,int numberofIteration,int numberofThread) throws UnsupportedEncodingException, FileNotFoundException, InterruptedException, Exception
    {
        ArrayList<Pipe> pipeList = new ArrayList<Pipe>();

        // Pipes: lowercase, tokenize, remove stopwords, map to features
        pipeList.add( new CharSequenceLowercase() );
        pipeList.add( new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")) );
        pipeList.add( new TokenSequenceRemoveStopwords(new File(stopwords), "UTF-8", false, false, false) );
        pipeList.add( new TokenSequence2FeatureSequence() );
        InstanceList triaingInstances = new InstanceList (new SerialPipes(pipeList));
        triaingInstances.save(new File("trainInstances"));
        
        //create training instances
        Reader trainingSet = new InputStreamReader(new FileInputStream(new File(trainingFile)), "UTF-8");
        triaingInstances.addThruPipe(new CsvIterator (trainingSet, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"),3, 2, 1)); // data, label, name fields

        InstanceList testingSetInstances = new InstanceList (new SerialPipes(pipeList));
        Reader testingSet = new InputStreamReader(new FileInputStream(new File(testingFile)), "UTF-8");
        testingSetInstances.addThruPipe(new CsvIterator (testingSet, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"),3, 2, 1)); // data, label, name fields*/

        
        //int numberofTopic=10;
        //int numberofIteration=500;
        //int numberofThread=6;
      
        double alpha[]={1.0};
        double beta[]={0.01,0.02,0.03,0.04,0.05,0.06,0.07,0.08,0.09,0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1.0};
        //LDAModel(5,1.0,0.01,10,2,outputDir,triaingInstances,testingSetInstances); 
        for(int k=0;k<beta.length;k++)
        {
        
            LDAModel(90,0.2,beta[k],numberofIteration,numberofThread,outputDir,triaingInstances,testingSetInstances); 
            //TimeUnit.SECONDS.sleep(30);
        }
        /*for(int k=21;k<=24;k=k+1)
        {
        
            LDAModel(k,1.0,0.01,numberofIteration,numberofThread,outputDir,triaingInstances,testingSetInstances); 
            TimeUnit.SECONDS.sleep(30);
        }*/
    }
    public static void LDAModel(int numofK,double alpha,double beta,int numbofIteration,int numberofThread,String outputDir,InstanceList triaingInstances,InstanceList testingInstances) throws Exception
    {
       
        int numTopics = numofK;
        //ParallelTopicModel model = new ParallelTopicModel(numTopics, 1.0, 0.01);
        ParallelTopicModel model = new ParallelTopicModel(numTopics, alpha, beta);
        //model.setTopicDisplay(50, 10);
        model.addInstances(triaingInstances);
        model.printLogLikelihood = true;
        model.setNumThreads(numberofThread);
        model.setNumIterations(numbofIteration);
        model.estimate();
        
         
        
        Alphabet dataAlphabet = triaingInstances.getDataAlphabet();
        FeatureSequence tokens = (FeatureSequence) model.getData().get(0).instance.getData();
        LabelSequence topics = model.getData().get(0).topicSequence;
        
        Formatter out = new Formatter(new StringBuilder(), Locale.US);
        for (int position = 0; position < tokens.getLength(); position++) {
            out.format("%s-%d ", dataAlphabet.lookupObject(tokens.getIndexAtPosition(position)), topics.getIndexAtPosition(position));
             
        }
        System.out.println("first output start:");
        System.out.println(out);
        System.out.println("first output end:");
        
        // Estimate the topic distribution of the first instance, 
        // given the current Gibbs state.
        double[] topicDistribution = model.getTopicProbabilities(0);
        // Get an array of sorted sets of word ID/count pairs
        ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();
     
        // Show top 10 words in topics with proportions for the first document
        
        System.out.println("Second output start:");
        String topicsoutput="";
        for (int topic = 0; topic < numTopics; topic++) {
            Iterator<IDSorter> iterator = topicSortedWords.get(topic).iterator();
            
            out = new Formatter(new StringBuilder(), Locale.US);
            out.format("%d\t%.3f\t", topic, topicDistribution[topic]);
            int rank = 0;
            while (iterator.hasNext() && rank < 10) {
                IDSorter idCountPair = iterator.next();
                out.format("%s (%.0f) ", dataAlphabet.lookupObject(idCountPair.getID()), idCountPair.getWeight());
                rank++;
            }
            
            System.out.println(out);
            topicsoutput+=out+"\n";
        }
        System.out.println("Second output end:");
        
        StringBuilder topicZeroText = new StringBuilder();
        Iterator<IDSorter> iterator = topicSortedWords.get(0).iterator();
        int rank = 0;
        while (iterator.hasNext() && rank < 10) {
            IDSorter idCountPair = iterator.next();
            topicZeroText.append(dataAlphabet.lookupObject(idCountPair.getID()) + " ");
            rank++;
       
        }

        // Create a new instance named "test instance" with empty target and source fields.
            //double modelLikelihood= model.modelLogLikelihood();
            //System.out.println("Model Likelihood: "+modelLikelihood);
            File outDir = new File(outputDir + File.separator+ "NumofTopics"+numTopics+"_"+alpha+"_"+beta);	//FIXME replace all strings with constants
            outDir.mkdir();
            
            String outDirPath = outDir.getPath();
            System.out.println(outDirPath);
            
            MarginalProbEstimator evaluator = model.getProbEstimator();
            InstanceList testing = new InstanceList(testingInstances.getPipe());
            testing.addThruPipe(new Instance(topicZeroText.toString(), null, "test instance", null));
            System.out.println("Loaded test instances");
            PrintStream docProbabilityStream = new PrintStream(outDirPath+File.separator+"docProbabilityFile.txt");
            
            double dTotalLogLikeliHood = evaluator.evaluateLeftToRight(testing, 10, false, docProbabilityStream);
            System.out.println(outDirPath+File.separator+dTotalLogLikeliHood);
            docProbabilityStream.close();
            int iTotalWords = 0;
            PrintStream doclengthsStream = new PrintStream(outDirPath+File.separator+"doclengths.txt");
            
            for (Instance instance : testing) {
                    if (!(instance.getData() instanceof FeatureSequence)) {
                        System.err.println("DocumentLengths is only applicable to FeatureSequence objects "
                               + "(use --keep-sequence when importing)");
                       System.exit(1);
                    }

                    FeatureSequence words = (FeatureSequence) instance.getData();
                    doclengthsStream.println(words.size());
                    iTotalWords += words.size();
                }
            System.out.println("Total words: "+iTotalWords);
            doclengthsStream.close();
            double dPerplexity = Math.exp((-1.0 * dTotalLogLikeliHood) / iTotalWords);
            System.out.println("Perplexity:" + dPerplexity);
           

        
            
            
            TopicInferencer inferencer = model.getInferencer();
            double[] testProbabilities = inferencer.getSampledDistribution(testing.get(0), 100, 10, 10);
            System.out.println("0\t" + testProbabilities[0]);
            
            
                                 
            String outputDocTopicsFile = outDirPath+File.separator+"output_doc_topics.txt";
            //String topicKeysFile = outDirPath+File.separator+"output_topic_keys";
            String topicKeysFile_fromProgram = outDirPath+File.separator+"output_topic";
            PrintWriter fResults_Perplexity = new PrintWriter(outDirPath+File.separator+"perplexity.txt");
            
            fResults_Perplexity.println("TotalLogLikeliHood\tiTotalWords\tPerplexity");
            fResults_Perplexity.print(-1.0 * dTotalLogLikeliHood + "\t");
            fResults_Perplexity.print(iTotalWords + "\t");

            fResults_Perplexity.println(Double.toString(dPerplexity));
            fResults_Perplexity.close();
        
            try {
                PrintWriter writer = new PrintWriter(topicKeysFile_fromProgram, "UTF-8");
                
                writer.print(topicsoutput);
                               
                writer.close();
            } catch (Exception e) {
                    e.printStackTrace();
            }
            //model.printTopWords(new File(topicKeysFile), 11, false);           
            model.printDocumentTopics(new File (outputDocTopicsFile));
            findTopicDocuments(1000,numTopics,outDirPath+File.separator);
    
            try{
    		
                    File file = new File(outDirPath+File.separator+"topic_distribution.txt");

                    if(file.delete()){
                            System.out.println(file.getName() + " is deleted!");
                    }else{
                            System.out.println("Delete operation is failed.");
                    }

            }catch(Exception e){

                    e.printStackTrace();

            }
            try{
    		
                    File file = new File(outputDocTopicsFile);

                    if(file.delete()){
                            System.out.println(file.getName() + " is deleted!");
                    }else{
                            System.out.println("Delete operation is failed.");
                    }

            }catch(Exception e){

                    e.printStackTrace();

            }
            
            
            

         
        
        
          }
    public static void findTopicDocuments(int numofDocument,int numofTopic,String Dir) 
    {
        BufferedReader br = null;
        NumberFormat formatter = new DecimalFormat("#0.00");    
        try {
          br = new BufferedReader(new FileReader(Dir+"output_doc_topics.txt"));
          PrintWriter writer = new PrintWriter(Dir+"topic_distribution.txt");
          String line = null;
          int i=0;
          int index=0;
           ArrayList<Cluster> cluster = new ArrayList<Cluster>();
           Map<String, Integer> map = new HashMap <>();
          while((line = br.readLine()) != null){
              
              String[] proportion=line.split("\\t");
             // if(i<numofDocument)
              //{
                  if(i==0)
                  {
                      writer.println("Doc,Topic,Distribution");
                  }
                  else
                  {
                                        
                    if(Double.parseDouble(proportion[3])>0.5)
                    {
                        String topic=proportion[2];
                        String doc= proportion[0];
                        double dist=Double.parseDouble(proportion[3]);
                        Cluster clusteritem= new Cluster();          
                        clusteritem.setTopic(Integer.parseInt(topic));
                        clusteritem.setDoc(Integer.parseInt(doc));
                        clusteritem.setDistribution(dist);
                        cluster.add(clusteritem);
                        map.put(doc, index);
                        index++;
                        writer.println(proportion[0]+","+proportion[2]+","+proportion[3]);
                        //System.out.println(proportion[0]+","+proportion[2]+","+proportion[3]);
                    }

                  }
                  i++;
               //}
              //else
              //{
                //  break;
              //}
           }
          br.close();
          System.out.println("Total Doc: "+index);
        writer.close();
        writer = new PrintWriter(Dir+"topic_cluster.txt");
        //PrintWriter writer2 = new PrintWriter(Dir+"topic_cluster1.txt");
        
        
        List<Cluster> toRemove = new ArrayList<Cluster>();
        StringBuilder output = new StringBuilder();
        Collections.sort(cluster, new Comparator<Cluster>() {
        public int compare(Cluster c1, Cluster c2) {
          if (c1.topic < c2.topic) return -1;
          if (c1.topic > c2.topic) return 1;
          return 0;
        }});
        
        
        //int nTopic[]=new int[numofTopic];
        
        int countdoc=0;  
        double avg=0.0;
        HashMap<Integer, List<Cluster>> hashMap=new HashMap<Integer, List<Cluster>>();
        

        String docs="";
        String printDocs="";
        String tempTopic="0";
        for (Cluster p: cluster) {
            if(!hashMap.containsKey(p.getTopic())){
                List<Cluster> list= new ArrayList<Cluster>();
                list.add(p);
                hashMap.put(p.getTopic(),list);
            }
            else
            {
                hashMap.get(p.getTopic()).add(p);
            }    

             
        }
        List<ClusterAvgCount> topicAvg= new ArrayList<ClusterAvgCount>();  
        for(int k=0;k<numofTopic;k++)
        {
            ClusterAvgCount clusterAvg= new ClusterAvgCount();
            System.out.println(hashMap.get(k).size());
            writer.print(k+"\t"+hashMap.get(k).size()+"\t");
            avg=0.0;
            
            clusterAvg.setTopic(""+k);
          //  writer2.print(k+"\t");
            for(int l=0;l<hashMap.get(k).size();l++)
            {
                //System.out.println(hashMap.get(k).get(l).getDoc());
                avg+=hashMap.get(k).get(l).getDistribution();
                //docs+=hashMap.get(k).get(l).getDoc()+",";
                //printDocs+=hashMap.get(k).get(l).getDoc()+"("+formatter.format(hashMap.get(k).get(l).getDistribution())+"),";
                writer.print(hashMap.get(k).get(l).getDoc()+"("+formatter.format(hashMap.get(k).get(l).getDistribution())+")");
            //    writer2.print(hashMap.get(k).get(l).getDoc());
                if(l<hashMap.get(k).size()-2)
                {
                    writer.print(",");
              //      writer2.print(",");
                }
            }   
            writer.print("\t"+avg/hashMap.get(k).size());
            //writer2.print("\t"+avg/hashMap.get(k).size());
            clusterAvg.setAvg(avg/hashMap.get(k).size());
            topicAvg.add(clusterAvg);
            System.out.println("Topic write: "+k);
            
            writer.println();
            //writer2.println();
            
        }
        System.out.println("Size of Topic: "+topicAvg.size());
        writer.close();
        //writer2.println();
       /* double totalsilhoutte=0.0;
        int totalDocs=0;
        br = new BufferedReader(new FileReader(Dir+"topic_cluster1.txt"));
        while((line = br.readLine()) != null)
        {
            String[] topicCluster=line.split("\\t");
            double onebyOneTopicSilhoutte=0.0;
            
            int t=Integer.parseInt(topicCluster[0]);
            System.out.println("Topic: "+t);
            String getDocs[]=topicCluster[1].split(","); //get number of documents in a topic
            
            
            //System.out.println(line);
            
            
            for(int p=0;p<getDocs.length;p++)   // each document in a topic
            {
                
                double avgSimilarity=0.0;
                double similarity=0.0;
                int numofDocsInTopic=0;
                totalDocs++;
                for(int q=p+1;q<getDocs.length-1;q++) //compaare each document in same cluster
                {
                    //System.out.println("Avg Similarity of Doc: "+p+"& "+q+" is "+getEuclidianDistance(cluster.get(map.get(getDocs[p])).getDistribution(),cluster.get(map.get(getDocs[q])).getDistribution()));
                    similarity+=getEuclidianDistance(cluster.get(map.get(getDocs[p])).getDistribution(),cluster.get(map.get(getDocs[q])).getDistribution());
                   // System.out.println("similarity here: "+similarity );
                    numofDocsInTopic++;
                            
                }
                avgSimilarity=similarity/getDocs.length;
                
                //System.out.println("a: "+avgSimilarity);
                //System.out.println("Avg Similarity of Doc: "+p+"is "+avgSimilarity);
                double dissimilairty=0.0;
                double avgDisSimilarity=0.0;
                int numOfTopicCluster=0;
                double distance =1.0;
                for(int tp=0;tp<numofTopic;tp++) //find dissimilarity of different cluster for each document
                {
                    if(t!=tp)
                    {
                       // System.out.println("Topic AVG: "+p+" :  "+cluster.get(map.get(getDocs[p])).getDistribution());
                        double temp=getEuclidianDistance(cluster.get(map.get(getDocs[p])).getDistribution(),Double.parseDouble(topicCluster[2]));//red
                        if(temp<distance)
                        {
                            distance=temp;
                        }
                    }
                 }   
                avgDisSimilarity=distance;
                //System.out.println("b: "+avgDisSimilarity);
                double docSilhoutte=0.0;
                if(avgSimilarity<avgDisSimilarity)
                {
                    docSilhoutte=1 -(avgSimilarity/avgDisSimilarity);
                }
                else if(avgSimilarity>avgDisSimilarity)
                {
                    docSilhoutte=(avgDisSimilarity/avgSimilarity)-1;
                }
                else{
                    docSilhoutte=avgDisSimilarity=avgSimilarity;
                }
                    
                //System.out.println("Silhoutte: "+p+"is "+docSilhoutte);
                onebyOneTopicSilhoutte+=docSilhoutte;
                //System.out.println("Avg Similarity of Doc: "+p+"is "+avgDisSimilarity);
                
            }
            totalsilhoutte+=onebyOneTopicSilhoutte;
            System.out.println("total for topic "+ t +" : "+totalsilhoutte);
        }
        System.out.println("Total No. of Docs"+totalDocs);
        double ldaModel=totalsilhoutte/totalDocs;        
        System.out.println("LDA Quality: "+ldaModel);*/
      } catch (FileNotFoundException e) {
          e.printStackTrace();
      } catch (IOException e) {
          e.printStackTrace();
      } finally{
          try{if(br != null) br.close();}catch(Exception ex){}
      }
        
        
    }
    
    
}

