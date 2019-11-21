/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers.learningmodel;

import tools.*;
import core.game.Observation;
import core.game.StateObservation;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Observable;

import ontology.Types;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

/**
 *
 * @author yuy
 */
public class RLDataExtractor {
    public FileWriter filewriter;
    private static int num_row = 28;
    private static int num_col = 31;
    private static int num_extra = 4;
    private static int num_timestep = 4;
    private static int total_feat=num_row * num_col * num_timestep + num_extra;
    private static int size_square = 30;
    public static int has_init=0;
    
    public static Instances s_datasetHeader = datasetHeader();
    public RLDataExtractor(String filename) throws Exception{
        
        filewriter = new FileWriter(filename+".arff");
        filewriter.write(s_datasetHeader.toString());
        /*
                // ARFF File header
        filewriter.write("@RELATION AliensData\n");
        // Each row denotes the feature attribute
        // In this demo, the features have four dimensions.
        filewriter.write("@ATTRIBUTE gameScore  NUMERIC\n");
        filewriter.write("@ATTRIBUTE avatarSpeed  NUMERIC\n");
        filewriter.write("@ATTRIBUTE avatarHealthPoints NUMERIC\n");
        filewriter.write("@ATTRIBUTE avatarType NUMERIC\n");
        // objects
        for(int y=0; y<14; y++)
            for(int x=0; x<32; x++)
                filewriter.write("@ATTRIBUTE object_at_position_x=" + x + "_y=" + y + " NUMERIC\n");
        // The last row of the ARFF header stands for the classes
        filewriter.write("@ATTRIBUTE Class {0,1,2}\n");
        // The data will recorded in the following.
        filewriter.write("@Data\n");*/
        // total_feat = num_row * num_col * num_timestep + num_extra;
    }
    
    public static Instance makeInstance(double[] features, int action, double reward){
        features[total_feat] = action;
        // System.out.println(reward);
        features[total_feat+1] = reward;
        Instance ins = new Instance(1, features);
        ins.setDataset(s_datasetHeader);
        return ins;
    }
    
    public static double[] featureExtract(StateObservation obs){
        // System.out.println(total_feat);

        double[] feature = new double[total_feat+2];  // 868 + 4 + 1(action) + 1(Q)
        
        // 448 locations
        int[][] map = new int[num_row][num_col];
        // Extract features
        LinkedList<Observation> allobj = new LinkedList<>();
        if( obs.getImmovablePositions()!=null )
            for(ArrayList<Observation> l : obs.getImmovablePositions()) allobj.addAll(l);
        if( obs.getMovablePositions()!=null )
            for(ArrayList<Observation> l : obs.getMovablePositions()) allobj.addAll(l);
        if( obs.getNPCPositions()!=null )
            for(ArrayList<Observation> l : obs.getNPCPositions()) allobj.addAll(l);
        
        for(Observation o : allobj){
            Vector2d p = o.position;
            // System.out.println(p.x);
            int x = (int)(p.x/size_square); //squre size is 20 for pacman
            int y= (int)(p.y/size_square);
            map[x][y] = o.itype;
        }
        if (has_init==0)
        {
            has_init = 1;
            for(int k=0; k<num_timestep; k++)
                for(int y=0; y<num_col; y++)
                    for(int x=0; x<num_row; x++)
                        feature[k*num_col*num_row+y*num_row+x] = map[x][y];
        }
        else 
        {
            for(int k=0; k<num_timestep-1; k++)
                for(int y=0; y<num_col; y++)
                    for(int x=0; x<num_row; x++)
                        feature[(k+1)*num_col*num_row+y*num_row+x] = feature[k*num_col*num_row+y*num_row+x];
            for(int y=0; y<num_col; y++)
                for(int x=0; x<num_row; x++)
                    feature[(num_timestep-1)*num_col*num_row+y*num_row+x] = map[x][y];
        }
               // 4 states
        feature[num_row*num_col*num_timestep] = obs.getGameTick();
        feature[num_row*num_col*num_timestep+1] = obs.getAvatarSpeed();
        feature[num_row*num_col*num_timestep+2] = obs.getAvatarHealthPoints();
        feature[num_row*num_col*num_timestep+3] = obs.getAvatarType();
        
        return feature;
    }
    
    public static Instances datasetHeader(){
        
        if (s_datasetHeader!=null)
            return s_datasetHeader;
        
        FastVector attInfo = new FastVector();
        // 448 locations
        // System.out.println(num_col);
        // System.out.println(num_timestep);
        for (int k = 0; k<4; k++){
            for(int y=0; y<31; y++){
                for(int x=0; x<28; x++){
                    Attribute att = new Attribute("timestep" + k + "object_at_position_x=" + x + "_y=" + y);
                    attInfo.addElement(att);
                }
            }
        }
        Attribute att = new Attribute("GameTick" ); attInfo.addElement(att);
        att = new Attribute("AvatarSpeed" ); attInfo.addElement(att);
        att = new Attribute("AvatarHealthPoints" ); attInfo.addElement(att);
        att = new Attribute("AvatarType" ); attInfo.addElement(att);
        //action
        FastVector actions = new FastVector();
        actions.addElement("0");
        actions.addElement("1");
        actions.addElement("2");
        actions.addElement("3");
        att = new Attribute("actions", actions);        
        attInfo.addElement(att);
        // Q value
        att = new Attribute("Qvalue");
        attInfo.addElement(att);
        
        Instances instances = new Instances("PacmanQdata", attInfo, 0);
        instances.setClassIndex( instances.numAttributes() - 1);
        
        return instances;
    }
    
}
