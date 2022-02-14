package com.example.catchat;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioRecord;

/**
 * Holds static constants and variables, for easy access and sharing between activities
 */
public class Globals {
    // https://stackoverflow.com/a/7984845
    public static BetterSocket sock = null;

    public static final AudioAttributes audioAttributes = new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build();

    public static final int port = 25565;

    // constant keys / messages
    public static final String intentReason = "REASON";
    public static final int acceptCall = Integer.MAX_VALUE;

    private static final int sampleRate = 44100;  // in Hz

    // information about packets
    private static final double packetSizeInSeconds = 0.5;  // half a second's worth of samples
    public static final int packetSizeInFrames = (int) (sampleRate * packetSizeInSeconds);
    private static final int frameSizeInBytes = 1;  // 8-bit in one channel: one byte samples
    public static final int packetSizeInBytes = packetSizeInFrames * frameSizeInBytes;

    // base audio format
    private static final AudioFormat.Builder formatBuilder = new AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_PCM_8BIT)
            .setSampleRate(sampleRate);

    // audio recording attributes
    public static class Record {
        public static final AudioFormat format = Globals.formatBuilder
                .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
                .build();

        // AudioRecord buffer
        public static final int bufferSize = AudioRecord.getMinBufferSize(
                format.getSampleRate(),
                format.getChannelMask(),
                format.getEncoding()
        ) + packetSizeInBytes + packetSizeInBytes / 2;  // accommodates a bit more than 1.5 packets
    }

    // audio playing attributes
    public static class Play {
        public static final AudioFormat format = formatBuilder
                .setChannelMask(AudioFormat.CHANNEL_OUT_DEFAULT)
                .build();

        // AudioTrack buffer
        public static final int bufferCapacity = packetSizeInBytes * 3;  // fits 3 packets
        public static final int bufferSize = packetSizeInBytes / 2;  // fits half a packet
    }
}