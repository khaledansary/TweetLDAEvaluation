/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tweetlda;

/**
 *
 * @author khaledd
 */
public class Cluster {
    int topic;
    int doc;
    int count;
    double distribution;

    public int getTopic() {
        return topic;
    }

    public void setTopic(int topic) {
        this.topic = topic;
    }

    public int getDoc() {
        return doc;
    }

    public void setDoc(int doc) {
        this.doc = doc;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public double getDistribution() {
        return distribution;
    }

    public void setDistribution(double distribution) {
        this.distribution = distribution;
    }

    
    /*public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getDoc() {
        return doc;
    }

    public void setDoc(String doc) {
        this.doc = doc;
    }
      
    public int compareTo(Cluster o) {
        int compareTo = o.getTopic().compareTo(topic);
        
        return compareTo;
    }*/
    
    
}
