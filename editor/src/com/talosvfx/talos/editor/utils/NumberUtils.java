package com.talosvfx.talos.editor.utils;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.CharArray;

public class NumberUtils {
    private static final String[] formatMap = {"", "K", "M", "B", "T", "q", "Q", "s", "S", "O", "N", "d", "U", "D"}; // Add more if needed
    private static int startFormatFrom = 1000;
    private static CharArray stringBuilder = new CharArray();

    public static CharSequence roundToDecimalPlacesText (float number, int amountOfPlaces) {
        stringBuilder.setLength(0);
        stringBuilder.append(roundToDecimalPlaces(number, amountOfPlaces));
        return stringBuilder;
    }

    public static float roundToDecimalPlaces (float number, int amountOfPlaces) {
        float roundTo = (float) Math.pow(10, amountOfPlaces);
        return MathUtils.round(number * roundTo) / roundTo;
    }

    private static CharSequence getProperStringFromNumber(long number, int max) {
        int index = 0;
        if (number >= max * 10) {
            while (number >= max) {
                index++;
                number /= 1000;
            }
        }

        stringBuilder.setLength(0);
        stringBuilder.append(number);
        stringBuilder.append(formatMap[index]);
        return stringBuilder;
    }

    public static CharSequence getProperStringFromNumber(int number) {
        return getProperStringFromNumber((long) number, startFormatFrom);
    }

    public static CharSequence getProperStringFromNumber(long number) {
        return getProperStringFromNumber(number, startFormatFrom);
    }

    public static float roundIfWithinTolerance (float toRound, float tolerance) {
        int round = Math.round(toRound);
        float difference = Math.abs(round - toRound);
        if (difference <= tolerance) {
            return round;
        } else {
            return toRound;
        }
    }

    public static int floorIfWithinTolerance (float toFoor, float tolerance) {
        int result = (int) Math.floor(toFoor);
        if(toFoor - (int)toFoor > 1f - tolerance) {
            result = (int) Math.ceil(toFoor);
        }

        return result;
    }
}
