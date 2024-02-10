/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uz.paynet.morsehelperui.models;

/**
 *
 * @author muhammad
 */
public class Settings {
    int channel;
    int bits;

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public int getBits() {
        return bits;
    }

    public void setBits(int bits) {
        this.bits = bits;
    }

    public int getFrameRate() {
        return frameRate;
    }

    public void setFrameRate(int frameRate) {
        this.frameRate = frameRate;
    }
    int frameRate;
}
