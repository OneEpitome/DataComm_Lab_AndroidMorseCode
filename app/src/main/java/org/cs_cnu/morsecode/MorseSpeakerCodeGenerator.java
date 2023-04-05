package org.cs_cnu.morsecode;

import android.util.Log;

import java.util.Iterator;
import java.util.Map;

public class MorseSpeakerCodeGenerator implements MorseSpeakerThread.MorseSpeakerIterator {

    final String message;
    final Map<String, String> map;
    final String morse_code;

    public MorseSpeakerCodeGenerator(String message, Map<String, String> map) {
        this.message = message.toUpperCase();
        this.map = map;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.message.length(); i++) {
            String str = Character.toString(this.message.charAt(i));
            if (str.equals(" ")) {
                sb.append("/");
            } else {
                for (Map.Entry<String, String> entry : this.map.entrySet()) {
                    if (str.equals(entry.getKey())) {
                        sb.append(entry.getValue());
                        break;
                    }
                }
            }
            sb.append(" ");
        }

        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }

        this.morse_code = sb.toString();
        Log.i("MorseCode", this.morse_code);
    }

    public String getMorseCode() {
        return this.morse_code;
    }

    @Override
    public int getSize() {
        int size = 0;
        for (int i = 0; i < this.morse_code.length(); i++) {
            char ch = this.morse_code.charAt(i);
            if (ch == '/') {
                size = size + 1;
            } else if (ch == ' ') {
                size = size + 1;
            } else if (ch == '.') {
                size = size + 1;
            } else if (ch == '-') {
                size = size + 3;
            }
        }
        size = size + this.morse_code.length();
        return size;
    }

    @Override
    public Iterator<String> iterator() {
        return new Iterator<String> () {
            boolean start = false;
            boolean end = false;
            int i = 0;

            @Override
            public boolean hasNext() {
                if (!start || !end) {
                    return true;
                }
                return false;
            }

            @Override
            public String next() {
                if (!start) {
                    start = true;
                    i = 0;
                }
                if (morse_code.length() > i) {
                    String value = Character.toString(morse_code.charAt(i));
                    i = i + 1;
                    return value;
                } else {
                    end = true;
                }
                return "";
            }
        };
    }
}
