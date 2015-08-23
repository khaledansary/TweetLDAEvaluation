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
        
           // obj.init("K:\\LDA\\train_file.txt", "K:\\LDA\\test_file.txt","E:\\");
         obj.init(args[0], args[1],args[2],args[3]);
        
        
    }
    public void init(String trainingFile,String testingFile,String outputDir,String stopwords) throws UnsupportedEncodingException, FileNotFoundException, InterruptedException, Exception
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

        
        int numberofTopic=10;
        int numberofIteration=500;
        int numberofThread=6;
      
        for(int k=10;k<=100;k=k+5)
        {
        
            LDAModel(k,1.0,0.01,numberofIteration,numberofThread,"",triaingInstances,testingSetInstances); 
            TimeUnit.SECONDS.sleep(30);
        }
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
            MarginalProbEstimator evaluator = model.getProbEstimator();
            InstanceList testing = new InstanceList(testingInstances.getPipe());
            testing.addThruPipe(new Instance(topicZeroText.toString(), null, "test instance", null));
            System.out.println("Loaded test instances");
            PrintStream docProbabilityStream = new PrintStream("docProbabilityFile.txt");
            
            double dTotalLogLikeliHood = evaluator.evaluateLeftToRight(testing, 10, false, docProbabilityStream);
            System.out.println(dTotalLogLikeliHood);
            docProbabilityStream.close();
            int iTotalWords = 0;
            PrintStream doclengthsStream = new PrintStream("doclengths.txt");
            
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
            File outDir = new File(outputDir + File.separator+ "NumofTopics"+numTopics);	//FIXME replace all strings with constants
            outDir.mkdir();
            String outDirPath = outDir.getPath();
                                 
            String outputDocTopicsFile = outDirPath+File.separator+"output_doc_topics.txt";
            String topicKeysFile = outDirPath+File.separator+"output_topic_keys";
            String topicKeysFile_fromProgram = outDirPath+File.separator+"output_topic";
            PrintWriter fResults_Perplexity = new PrintWriter(outDirPath+File.separator+"perplexity.txt");
            
            fResults_Perplexity.println("TotalLogLikeliHood\tiTotalWords\tPerplexity");
            fResults_Perplexity.print(-1.0 * dTotalLogLikeliHood + "\t");
            fResults_Perplexity.print(iTotalWords + "\t");

            fResults_Perplexity.println(Double.toString(dPerplexity));

        
            try {
                PrintWriter writer = new PrintWriter(topicKeysFile_fromProgram, "UTF-8");
                
                writer.print(topicsoutput);
                               
                writer.close();
            } catch (Exception e) {
                    e.printStackTrace();
            }
            model.printTopWords(new File(topicKeysFile), 11, false);           
            model.printDocumentTopics(new File (outputDocTopicsFile));
        
    


            

         
        
        
          }
    
    
}

