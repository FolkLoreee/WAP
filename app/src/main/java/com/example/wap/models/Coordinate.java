package com.example.wap.models;

public class Coordinate {
    private double x;
    private double y;
    private double  distance;


    public Coordinate(){}

    public Coordinate(double x, double y){
        this.x = x;
        this.y = y;
    }

    public double getX(){
        return x;
    }
    public double getY(){
        return y;
    }
    public void setX(double x){
        this.x =x;
    }
    public void setY(double y){
        this.y =y;
    }
    //to get straight line distance
    public void calcDistance(){
        this.distance = Math.sqrt(Math.pow(x,2)+Math.pow(y,2));
    }
}
