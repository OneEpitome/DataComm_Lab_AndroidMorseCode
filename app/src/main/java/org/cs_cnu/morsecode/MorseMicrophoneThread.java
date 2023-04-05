package org.cs_cnu.morsecode;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.provider.MediaStore;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.nio.ByteBuffer;
import java.util.Map;

public class MorseMicrophoneThread extends Thread {
    public interface MorseMicrophoneCallback {
        void onProgress(String value);
        void onDone(String value);
    }


    final short MORSE_THRESHOLD = Short.MAX_VALUE / 4;
    final float UNSEEN_THRESHOLD = 3.0f;

    final int sample_rate;
    final float frequency;
    final float unit;
    final int unit_size;
    final int buffer_size;

    final MorseMicrophoneThread.MorseMicrophoneCallback callback;

    public MorseMicrophoneThread(MorseMicrophoneThread.MorseMicrophoneCallback callback,
                                 int sample_rate, float frequency, float unit) {
        this.callback = callback;
        this.sample_rate = sample_rate;
        this.frequency = frequency;
        this.unit = unit;
        this.unit_size = (int) Math.ceil(this.sample_rate * this.unit);
        this.buffer_size = (int) AudioRecord.getMinBufferSize(sample_rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        setPriority(Thread.MAX_PRIORITY);
    }

    @Override
    public void run() {
        @SuppressLint("MissingPermission")
        final AudioRecord record = new AudioRecord(
                MediaRecorder.AudioSource.DEFAULT,
                this.sample_rate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                2 * sample_rate);

        final short[] samples = new short[unit_size];
        StringBuilder sb = new StringBuilder();

        record.startRecording();

        boolean tuning = true;
        int offset;
        int abs_sum = 0;
        int count = 0;
        int unseen = 0;
        while (true) {
            int result = record.read(samples, 0, unit_size);
            if (result < 0) {
                break;
            }
            if (tuning) {
                offset = unit_size;
                for (int i = 0; i < unit_size; i++) {
                    if (samples[i] > MORSE_THRESHOLD) {
                        Log.i("Tuning", "End");
                        offset = i;
                        tuning = false;
                        break;
                    }
                }
            } else {
                offset = 0;
            }
            for (int i = offset; i < unit_size; i++) {
                // 사용 변수 : abs_sum, Math.abs(), samples, sb, unseen, count
                abs_sum = abs_sum + Math.abs(samples[i]);
                count = count + 1;
                if (count == unit_size) {
                    // 작성할 부분
                     if (abs_sum / count >= MORSE_THRESHOLD) {
                         sb.append('.');
                         unseen = 0;
                     }  else {
                         sb.append(' ');
                         unseen = unseen +1;
                     }
                     count = 0;
                     abs_sum = 0;
                    callback.onProgress(sb.toString());
                }
            }
            if (unseen >= (UNSEEN_THRESHOLD/unit)) {
                record.stop();
                record.release();
                String morse = sb.toString();
                Log.i("RawMorse", morse);
                morse = morse.replaceAll("^[ ]+", "");
                morse = morse.replaceAll("[ ]+$", "");
                morse = morse.replaceAll("\\.\\.\\.", "-");
                morse = morse.replaceAll("       ", "m");
                morse = morse.replaceAll("   ", "s");
                morse = morse.replaceAll(" ", "");
                morse = morse.replaceAll("s", " ");
                morse = morse.replaceAll("m", " / ");
                Log.i("Morse", morse);
                callback.onDone(morse);
                break;
            }
        }
    }
}
