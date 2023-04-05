package org.cs_cnu.morsecode;

import android.util.Log;

import java.util.Map;

public class MorseMicrophoneTextGenerator {

    final String morse_code;
    final Map<String, String> map;
    final String text;

    public MorseMicrophoneTextGenerator(String morse_code, Map<String, String> map) {
        this.morse_code = morse_code;
        this.map = map;

        StringBuilder sb = new StringBuilder();
        String[] tokens = morse_code.split(" ");
        for (String str : tokens) {
            if (str.equals("/")) {
                sb.append(" ");
            } else {
                for (Map.Entry<String, String> entry : this.map.entrySet()) {
                    if (str.equals(entry.getValue())) {
                        sb.append(entry.getKey());
                        break;
                    }
                }
            }
        }
        this.text = sb.toString();
        Log.i("Sound input", text);
    }

    public String getText() {
        return this.text;
    }
}
