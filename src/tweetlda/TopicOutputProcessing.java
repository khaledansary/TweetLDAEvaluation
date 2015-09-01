/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tweetlda;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author khaledd
 */
public class TopicOutputProcessing {
    
    public static void main(String args[])
    {
        TopicOutputProcessing obj = new TopicOutputProcessing();
        //obj.topicDocumentFrequency("K:\\LDA\\Data\\topic_cluster.txt", "K:\\LDA\\Data\\topic_document_freq_90_topics");
        obj.worldCloudFile("K:\\LDA\\Data\\topTopics.txt","K:\\LDA\\Data\\wordCloudfile");
    }
    public void worldCloudFile(String inputFile1, String outputFile)
    {
        BufferedReader br1=null,br2=null;
        PrintWriter writer=null;
        String line =     "";
       
	try {
          
                br1 = new BufferedReader(new FileReader(inputFile1));
               
                //writer = new PrintWriter(outputFile, "UTF-8");
                int count=0;
                
                while ((line = br1.readLine()) != null) {
                    String str[]=line.split("\t");
                    String words[]=str[1].split(" ");
                    
                    String word="";
                    for(int i=0;i<words.length;i++)
                    {
                        
                        if(i%2==0)
                        {
                            
                            //writer.print(words[i]+",");
                            word =words[i]+",";
                        }
                        else
                        {
                            int freq= Integer.parseInt(words[i].replace("(", "").replace(")",""));
                            if(freq>12000)
                            {
                                count++;
                                System.out.println(count+" "+word +" "+freq );
                            }
                            /*for(int j=0;j<freq;j++)
                            {
                                writer.print(word+" ");
                            }*/
                            
                        }
                        
                    }
                    
                }
                int inputcount=count;
                System.out.println(inputFile1+ " input file rows"+inputcount);
                
                
                //writer.close();
                br1.close();
                //br2.close();
                //System.out.println(inputFile2+" input file rows"+inputcount);
                System.out.println("Write to file-->" + outputFile +"Total row: "+count); 
            }catch(Exception e){
                System.out.println(e);
            }
            System.out.println("Done");
    }
    public void topicDocumentFrequency(String inputFile1, String outputFile)
    {
        BufferedReader br1=null,br2=null;
        PrintWriter writer=null;
        String line =     "";
       
	try {
          
                br1 = new BufferedReader(new FileReader(inputFile1));
               
                writer = new PrintWriter(outputFile, "UTF-8");
                int count=0;
                while ((line = br1.readLine()) != null) {
                    String str[]=line.split("\t");
                    
                    writer.println(str[0]+"\t"+str[1]);
                }
                int inputcount=count;
                System.out.println(inputFile1+ " input file rows"+inputcount);
                
                
                writer.close();
                br1.close();
                //br2.close();
                //System.out.println(inputFile2+" input file rows"+inputcount);
                System.out.println("Write to file-->" + outputFile +"Total row: "+count); 
            }catch(Exception e){
                System.out.println(e);
            }
            System.out.println("Done");
    }
}
