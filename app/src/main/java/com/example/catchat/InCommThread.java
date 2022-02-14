package com.example.catchat;

import android.media.AudioManager;
import android.media.AudioTrack;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * The input communication thread.
 * Handles input from the network and output to the AudioTrack.
 */
public class InCommThread extends Thread {
    private CallActivity activity = null;  // communicating for
    private AudioTrack audioOut = null;  // output audio stream to speaker

    /**
     * Instantiates an inbound communications thread.
     * Creates an AudoTrack audioOut.
     */
    public InCommThread(CallActivity activity) {
        this.activity = activity;

        // create audio track to play audio
        audioOut = new AudioTrack(
                Globals.audioAttributes,
                Globals.Play.format,
                Globals.Play.bufferCapacity,
                AudioTrack.MODE_STREAM,
                AudioManager.AUDIO_SESSION_ID_GENERATE
        );

        // the buffer size influences the minimum size that must be reached before playback starts
        // this is not the same as the buffer capacity
        audioOut.setBufferSizeInFrames(Globals.Play.bufferSize);
    }

    /**
     * Until this thread is interrupted, receives audio from socket input stream and writes it to
     * the AudioTrack.
     * Releases the AudioTrack when done.
     */
    @Override
    public void run() {

        // fill buffer before starting playing
        receiveNextAudioPacket();
        audioOut.play();

        while (!this.isInterrupted()) {
            receiveNextAudioPacket();
        }

        this.audioOut.release();
    }

    /**
     * Gets a packet from the socket input stream, uncompresses it, and writes it to the AudioTrack
     * buffer.
     */
    private void receiveNextAudioPacket() {

        // try to read; blocks until data is read
        byte[] compressedData;
        try {
            compressedData = getCompressedData();
            if (compressedData == null) throw new IOException();
        } catch (IOException e) {
            activity.endCall("disconnected");
            return;
        }

        // try to uncompress data
        byte[] uncompressedData;
        try {
            uncompressedData = uncompress(compressedData);
        } catch (DataFormatException e) {
            e.printStackTrace();
            activity.endCall("data corrupted");
            return;
        }

        // add the data to the player buffer
        addToPlayBuffer(uncompressedData);
    }

    /**
     * Gets the next packet from the network.
     * Blocks until entire packet is read.
     * @return the data in an array of bytes
     * @throws IOException if could not read
     */
    private byte[] getCompressedData() throws IOException {
        int length;
        try {
            length = Globals.sock.readInt();
            return Globals.sock.readBytes(length);
        } catch (NullPointerException e) {
            // someone else called activity.endCall(); do nothing
            return null;
        }
    }

    /**
     * Uncompresses an array of bytes.
     * @param compressedData the array of bytes to be uncompressed.
     * @return the uncompressed data in an array of bytes
     * @throws DataFormatException if data could not be uncompressed
     */
    private byte[] uncompress(byte[] compressedData) throws DataFormatException {
        Inflater decompresser = new Inflater();
        decompresser.setInput(compressedData, 0, compressedData.length);

        // uncompresser cannot compress everything at once; uncompress pieces and write to a stream
        ByteArrayOutputStream stream = new ByteArrayOutputStream(Globals.packetSizeInBytes);
        byte[] buffer = new byte[1024];
        while (!decompresser.finished()) {
            int count = decompresser.inflate(buffer);
            stream.write(buffer, 0, count);
        }

        try {
            stream.close();
        } catch (IOException e) {
            // do nothing
        }
        decompresser.end();

        return stream.toByteArray();
    }

    /**
     * Adds the audio data to the AudioTrack's buffer to be played.
     * @param data the audio data to be played
     */
    private void addToPlayBuffer(byte[] data) {

        // add to audio player buffer, from start to end
        audioOut.write(data, 0, data.length);
    }
}
