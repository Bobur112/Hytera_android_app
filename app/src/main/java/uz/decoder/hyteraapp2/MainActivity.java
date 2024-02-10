package uz.decoder.hyteraapp2;

import static android.app.ProgressDialog.show;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

import uz.paynet.morsehelperui.MorseProcessor;
import uz.paynet.morsehelperui.WavFileException;

public class MainActivity extends AppCompatActivity {
    private static final int SAMPLING_RATE_IN_HZ = 44100;

    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;

    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int BUFFER_SIZE_FACTOR = 2;

    /**
     * Size of the buffer where the audio data is stored by Android
     */
    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLING_RATE_IN_HZ,
            CHANNEL_CONFIG, AUDIO_FORMAT) * BUFFER_SIZE_FACTOR;

    /**
     * Signals whether a recording is in progress (true) or not (false).
     */
    private final AtomicBoolean recordingInProgress = new AtomicBoolean(false);

    private AudioRecord recorder = null;

    private Thread recordingThread = null;

    private Button startButton;

    private Button stopButton;
    private SeekBar shovqintf;
    private File file;
    private EditText resultText;
    private WavClass wavObj;

    private class AsyncTaskRunner extends AsyncTask<String, String, String> {

        private String resp;
        ProgressDialog progressDialog;

        @Override
        protected String doInBackground(String... params) {
            MorseProcessor m_proc = null;
            try {
                m_proc = new MorseProcessor(new File(wavObj.getTempWavFile()));
                m_proc.setSHOVQIN((double) Integer.parseInt(params[0]) /50);
                m_proc.process();
                String result = m_proc.result();
//                    Toast.makeText(MainActivity.this, "result: " + result + "; progress: " + progress, Toast.LENGTH_SHORT).show();
                return result;
            } catch (WavFileException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


        @Override
        protected void onPostExecute(String result) {
            // execution of result of Long time consuming operation
            resultText.setText(result);
        }


        @Override
        protected void onPreExecute() {
        }


        @Override
        protected void onProgressUpdate(String... text) {
            resultText.setText(text[0]);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        wavObj = new WavClass(Environment.getExternalStorageDirectory().getPath());

        shovqintf = findViewById(R.id.shovqin_seekbar);

        resultText = findViewById(R.id.result_text);

        shovqintf.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                AsyncTaskRunner runner = new AsyncTaskRunner();
                runner.execute(String.valueOf(seekBar.getProgress()));
            }
        });

        startButton =  findViewById(R.id.qabul_button);
        startButton.setOnClickListener(v -> {
            Toast.makeText(this, "Start1", Toast.LENGTH_SHORT).show();
//            startRecording();
            wavObj.startRecording();
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
        });

        stopButton = findViewById(R.id.stop_button);
        stopButton.setOnClickListener(v -> {
//            stopRecording();
            wavObj.stopRecording();
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        startButton.setEnabled(true);
        stopButton.setEnabled(false);
    }

    @Override
    protected void onPause() {
        super.onPause();

        stopRecording();
    }

    private void startRecording() {
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                    Manifest.permission.READ_EXTERNAL_STORAGE}, 2222);
//            Toast.makeText(this, "Start3", Toast.LENGTH_SHORT).show();
//        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE}, 1111);
        }else {
            recorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, SAMPLING_RATE_IN_HZ,
                    CHANNEL_CONFIG, AUDIO_FORMAT, BUFFER_SIZE);


            recorder.startRecording();

            recordingInProgress.set(true);

            recordingThread = new Thread(new RecordingRunnable(), "Recording Thread");
            recordingThread.start();
        }
    }

    private void stopRecording() {
        if (null == recorder) {
            return;
        }

        recordingInProgress.set(false);

        recorder.stop();

        recorder.release();

        recorder = null;

        recordingThread = null;
    }

    private class RecordingRunnable implements Runnable {

        @Override
        public void run() {
            file = new File(Environment.getExternalStorageDirectory(), "Recordings/recording.wav");
            final ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);

            try (final FileOutputStream outStream = new FileOutputStream(file)) {
                while (recordingInProgress.get()) {
                    int result = recorder.read(buffer, BUFFER_SIZE);
                    if (result < 0) {
                        throw new RuntimeException("Reading of audio buffer failed: " +
                                getBufferReadFailureReason(result));
                    }
                    outStream.write(buffer.array(), 0, BUFFER_SIZE);
                    buffer.clear();
                }
            } catch (IOException e) {
                throw new RuntimeException("Writing of recorded audio failed", e);
            }

            MorseProcessor m_proc = null;
            try {
                m_proc = new MorseProcessor(file);
                m_proc.setSHOVQIN(shovqintf.getProgress());
                m_proc.process();
                String result = m_proc.result();
                resultText.setText(result);
            } catch (WavFileException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

        private String getBufferReadFailureReason(int errorCode) {
            switch (errorCode) {
                case AudioRecord.ERROR_INVALID_OPERATION:
                    return "ERROR_INVALID_OPERATION";
                case AudioRecord.ERROR_BAD_VALUE:
                    return "ERROR_BAD_VALUE";
                case AudioRecord.ERROR_DEAD_OBJECT:
                    return "ERROR_DEAD_OBJECT";
                case AudioRecord.ERROR:
                    return "ERROR";
                default:
                    return "Unknown (" + errorCode + ")";
            }
        }
    }

}