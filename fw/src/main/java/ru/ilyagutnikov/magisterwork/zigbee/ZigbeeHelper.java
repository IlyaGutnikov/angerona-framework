package ru.ilyagutnikov.magisterwork.zigbee;

import java.util.Random;

public class ZigbeeHelper {

	private final static int NUMBER_OF_CHARS = 10;

	public static String getRandomHexString(){
        Random r = new Random();
        StringBuffer sb = new StringBuffer();
        while(sb.length() < NUMBER_OF_CHARS){
            sb.append(Integer.toHexString(r.nextInt()));
        }

        return sb.toString().substring(0, NUMBER_OF_CHARS);
    }
}
