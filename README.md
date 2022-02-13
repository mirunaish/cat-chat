# CatChat
## A P2P voice call app

### Instructions
* Make sure the app is running and on the connection screen on the receiving device before
attempting to make a connection from the other device.  

### Permissions
If these permissions are not granted from the device settings, the app will crash.
* Internet and network access
* Record audio

### Audio stats
| Property | Value |
| :-- | --: |
| Sample rate | 44100 |
| Encoding | PCM 8 bit |
| Channels | Mono |
| Packet size in seconds | 0.5 |
| Packet size in bytes | 88400 |

More can be found in the `Globals` class.
I believe Sample and Frame are synonyms.

## Implementation notes
### Audio processing
An instance of `AudioRecord` is created, with an internal buffer of size `Globals.bufferSize`. The
`read()` function is continuously called, and it blocks until there are at least
`Globals.packetSizeInBytes` bytes in the buffer. It then copies the buffered audio data into an
array of bytes. The array of bytes is then compressed and sent over the network.

Audio data is processed in *packets* or half a second's worth of samples. Because of this, there is
a delay of at least half a second between the input on one device and the output on the other.

### Networking and connections
Each device has a `ServerThread` and a `ClientThread`. `ServerThread` listens for connections,
`ClientThread` tries to connect to another process's `ServerThread`. Once a connection is made,
communication is symmetrical, as each device starts an `InCommThread` and an `OutCommThread`.

The port used is 25565.

### Streaming audio over a network
The audio data is read from the `AudioRecord` as an array of bytes. It is then compressed, and the
compressed size in bytes is written to the network stream. The compressed data, also an array of
bytes, is then written to the stream.

The other device reads an integer from the stream, and expects a packet of that number of bytes. It
then reads the packet, uncompresses it to its original size (`Globals.packetSizeInBytes`), and
writes the resulting array of bytes to the `AudioTrack`'s buffer.

### Finding device IP address
`IpFinderThread` makes a request to [a web server](https://myip.dnsomatic.com/) that responds with the IP address the request came
from.

### BetterSocket
Wraps the `Socket` and its I/O streams, and provides reading and writing methods.
This class exists for two reasons:
* The input and output streams are both used in two separate places in the code. However, once they
are opened and then closed (at the end of the scope they are opened in), they cannot be
reopened. Therefore, it is necessary to store the Input and Output streams and reuse them.
* Stream wrapper classes such as `Scanner` and `OutputStreamWriter` do not provide support for both
arrays of bytes and integers.

### Error handling and recovery
If errors happen during the call (connection reset, other person hung up, microphone could not be
accessed, etc.) the call is ended and the status box informs the user of the reason.

### Bugs / issues
* Error handling is not very robust and my testing was not extensive, so it is likely that crashing
the app is not difficult.
    * After a call is ended, a new call fails to connect.
    * When the connection ends on one device it may not end on the other
    * Behavior if a connection is made while the server device is in a call is undefined. The server
    thread cannot be stopped due to a port conflict on restarting
* Connecting to the IP address displayed on the screen always causes a timeout error. I believe
this is a public IP address, and connections only work when using local IP address.
* Connecting devices via Wifi is not very reliable. Even when using the public IP, all the tests I
made over wifi either threw an "Address Unreacheable" error or timed out. I was only able to
successfully make a connection using the local address in two cases:
    * two physical phones with one of them connected to the others' mobile hotspot
    * two emulators running on the same device
* Console warns about an unfreed resource
* ConnectActivity layout breaks when the keyboard is opened

## Resources / dependencies
* https://myip.dnsomatic.com/ for getting device IP address

## Sources
* Documentation, especially:
    * https://developer.android.com/training/basics/firstapp/starting-activity
    * https://developer.android.com/reference/android/media/AudioRecord
    * https://developer.android.com/reference/android/media/AudioTrack.html
    * https://docs.oracle.com/javase/6/docs/api/java/util/zip/Deflater.html
    * https://docs.oracle.com/javase/7/docs/api/java/nio/ByteBuffer.html
* StackOverflow threads on very specific topics / errors (especially connection problems), including
but not limited to:
    * https://stackoverflow.com/questions/16466521/modify-view-from-a-different-thread
    * https://stackoverflow.com/questions/2383265/convert-4-bytes-to-int#2383729
    * https://stackoverflow.com/questions/6374915/java-convert-int-to-byte-array-of-4-bytes#6374970
    * https://stackoverflow.com/questions/2139134/how-to-send-an-object-from-one-android-activity-to-another-using-intents/7984845#7984845
* [UnicornRecorder](https://github.com/mirunaish/audio-recorder-app), the app I submitted in the
last application cycle (used as example for ConnectRequest ListView)
* CS10 (multithreading, networking)
* CS50 (error handling, documentation, style)
* Dali Lab help sessions: Deflater/Inflater debugging; networking over Wifi timeout; AudioRecord
buffering; IP address fetching
* Suggestions made by my IDE (Android Studio)

## Future features, in order of priority
* Common call features such as speaker and mute
* If a slow connection causes a delay in the input stream, and there is a delay between one user's
recorded audio and the other user's played audio, the audio still stored in the buffer is sped up
until it catches up. The minimum delay is `Globals.packetSizeInSeconds`.
* An account system and DNS-like server, to return the current IP address of an user with a given
username.
* Handling connection attempts while in a call or the app is closed
* A desktop version