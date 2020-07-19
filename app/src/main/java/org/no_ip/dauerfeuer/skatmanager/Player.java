package org.no_ip.dauerfeuer.skatmanager;

import java.io.Serializable;

public class Player implements Serializable {
    private String mPlayerName;
    private int mPlayerPoints;

    public Player(String name) {
        this.mPlayerName = name;
        this.mPlayerPoints = 0;
    }

    public Player(String name, int points) {
        this.mPlayerName = name;
        this.mPlayerPoints = points;
    }

    public String getPlayerName() {
        return mPlayerName;
    }

    public void setPlayerName(String s) {
        this.mPlayerName = s;
    }

    public int getPlayerPoints() {
        return mPlayerPoints;
    }

    public void setPlayerPoints(int points) {
        this.mPlayerPoints = points;
    }
}
