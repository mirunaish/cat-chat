package com.example.catchat;

import android.media.AudioRecord;
import android.media.MediaRecorder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;

/**
 * Outbound communications thread
 * Handles input from AudioRecord and output to the network.
 */
public class OutCommThread extends Thread {
    private CallActivity activity = null;  // the activity that started this thread
    private AudioRecord audioIn = null;

    /**
     * Instantiates an outbound communications thread.
     * Creates an AudioRecord audioIn.
     */
    public OutCommThread(CallActivity activity) {
        this.activity = activity;

        // get input stream from microphone
        try {
            this.audioIn = this.getAudioInputStream();
        } catch (Exception e) {
            activity.endCall("could not initialize audio recorder");
        }
    }

    /**
     * Creates the AudioRecord object and sets audio formatting
     * @return the AudioRecord object.
     * @throws Exception if AudioRecord could not be initialized
     */
    private AudioRecord getAudioInputStream() throws Exception {
        // create audio record object, used to get packets of audio bytes from the audio stream
        AudioRecord recorder = new AudioRecord (
                 MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                 Globals.Record.format.getSampleRate(),
                 Globals.Record.format.getChannelMask(),
                 Globals.Record.format.getEncoding(),
                 Globals.Record.bufferSize  // slightly more than two full packets
        );

        if (recorder.getState() != AudioRecord.STATE_INITIALIZED) {
            throw new Exception();
        }

        return recorder;
    }

    /**
     * Until this thread is interrupted, reads audio from microphone and sends it to the socket
     * output stream.
     * Releases the AudioRecord when done.
     */
    @Override
    public void run() {
        this.audioIn.startRecording();

        while (!this.isInterrupted()) {
            sendNextAudioPacket();  // will block until sufficient data is read from the buffer
        }

        this.audioIn.release();
    }

    /**
     * Gets a packet from the microphone, compresses it, and sends it to the socket output stream.
     */
    private void sendNextAudioPacket() {
        byte[] packet;

        try {
            packet = getNextPacket();
        } catch (Exception e) {
            activity.endCall("could not record audio");
            return;
        }

        byte[] compressedPacket = compress(packet);

        try {
            send(compressedPacket);
        } catch (IOException e) {
            activity.endCall("connection ended");
        }
    }

    /**
     * Gets the next packet from the AudioRecord buffer.
     * Blocks until entire packet is read.
     * @return the packet as a byte array
     * @throws Exception if AudioRecord data could not be read
     */
    private byte[] getNextPacket() throws Exception {
        // create a buffer into which to read audio data
        byte[] data = new byte[Globals.packetSizeInBytes];
        int returnCode = this.audioIn.read(data, 0, Globals.packetSizeInBytes, AudioRecord.READ_BLOCKING);
        if (returnCode < 0) {  // error code
            throw new Exception();
        }

        return data;
    }

    /**
     * Compresses an array of bytes.
     * @param data the array of bytes to be compressed
     * @return the compressed array of bytes
     */
    private byte[] compress(byte[] data) {
        Deflater compresser = new Deflater(Deflater.DEFLATED, false);
        compresser.setInput(data);
        compresser.finish();

        // compresser cannot compress everything at once; compress in pieces and write to a stream
        ByteArrayOutputStream stream = new ByteArrayOutputStream(data.length);
        byte[] buffer = new byte[1024];
        while (!compresser.finished()) {
            int count = compresser.deflate(buffer);
            stream.write(buffer, 0, count);
        }
        try {
            stream.close();
        } catch (IOException e) {
            // do nothing
        }

        return stream.toByteArray();
    }

    /**
     * Sends a compressed packet (or any array of bytes) to the Global socket's output stream.
     * @param data array of bytes to be sent
     * @throws IOException if writing to stream failed
     */
    private void send(byte[] data) throws IOException {
        // write the length in bytes of the packet
        Globals.sock.writeInt(data.length);

        // write the data to the stream's buffer, from 0 to its length (all the data)
        Globals.sock.writeBytes(data);
    }
}
