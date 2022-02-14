# CatChat
## A P2P voice call app

### Instructions
* Open the source files in an Android Studio project to attach a debugger to the app process and see
console messages
* Alternatively, download the included .apk file and install it on two devices
* Grant the application its required permissions before running it on both devices.
* Make sure the app is running and on the connection screen on the receiving device before
attempting to make a connection from the other device.
* Find the device's local IP address in its Wifi settings. (The IP address displayed in the app is a
public address, which cannot be used to connect, likely because of firewall / network configuration
issues).

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
I believe "Sample" and "Frame" are synonyms.

## Implementation notes
### Streaming audio over a network
`AudioRecord` records to an internal buffer. The `read()` function is continuously called, which
blocks until there are at least `Globals.packetSizeInBytes` bytes in the buffer, at which point it
reads this data as an array of bytes. The array of bytes is then compressed, and its length written
to the output stream, followed by the data itself.

The other device reads the integer from the stream, followed by that number of bytes, forming a full
compressed packet. It then uncompresses it to its original size (`Globals.packetSizeInBytes`) and
adds it to the `AudioTrack`'s buffer. When this buffer is sufficiently full, audio starts playing.
If the buffer becomes full and the newly received data would cause a buffer overflow, the `write()`
function blocks until enough audio plays to make space in the buffer. This can cause significant
delays in the audio. The buffer can become full due to a slow connection.

Audio data is processed in *packets* of half a second's worth of samples. Because of this, there is
a delay of at least half a second between the input on one device and the output on the other.

### Networking and connections
Each device has a `ServerThread` and a `ClientThread`. `ServerThread` listens for connections,
`ClientThread` tries to connect to another process's `ServerThread`. Once a connection is made,
communication is symmetrical, as each device starts an `InCommThread` and an `OutCommThread`.

The port used is 25565.

### Finding device IP address
`IpFinderThread` makes a request to [a web server](https://myip.dnsomatic.com/) that responds with
the IP address the request came from.

### BetterSocket
Wraps the `Socket` and its I/O streams, and provides reading and writing methods.
This class exists for two reasons:
* The input and output streams are both used in multiple separate places in the code. However, once
they are opened and then closed (at the end of the scope they are opened in), they cannot be
reopened. Therefore, it is necessary to store the Input and Output streams and reuse them.
* Other stream wrapper classes such as `Scanner` and `OutputStreamWriter` do not provide support for
both arrays of bytes and integers.

### Error handling and recovery
If errors happen during the call (connection lost, other person hung up, microphone could not be
accessed, etc.) the call is ended and the reason written to the status box.

### Bugs / issues
* The IP server does not always correctly return the IP address. Three attempts are made to increase
the likelihood of success, but it still occasionally fails.
* The public IP address displayed on the screen cannot be used to connect. Additionally, devices
cannot be connected over Wifi, regardless of which IP address is used. These connection attempts
either throw an "Address unreachable" error or time out. I was only able to successfully make a
connection using the local IP address between two (non-emulator) devices with one of them connected
to the others' mobile hotspot.
* Factors such as internet speed can cause significant delays between recording on one device and
playing on the other.

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
* StackOverflow threads on very specific topics / errors, including but not limited to:
    * https://stackoverflow.com/questions/16466521/modify-view-from-a-different-thread
    * https://stackoverflow.com/questions/2383265/convert-4-bytes-to-int#2383729
    * https://stackoverflow.com/questions/6374915/java-convert-int-to-byte-array-of-4-bytes#6374970
    * https://stackoverflow.com/questions/2139134/how-to-send-an-object-from-one-android-activity-to-another-using-intents/7984845#7984845
    * https://stackoverflow.com/questions/2983835/how-can-i-interrupt-a-serversocket-accept-method
    * https://community.oracle.com/tech/developers/discussion/1520309/socket-close-on-both-sides
    * https://stackoverflow.com/questions/42840555/how-to-avoid-keyboard-pushing-layout-up-on-android-react-native#43951807
    * https://stackoverflow.com/questions/28629635/android-appcompat-theme-always-shows-black-color-actionbar/28629843#28629843
    * many threads on connection issues: timed out errors and host unreachable errors.
* [UnicornRecorder](https://github.com/mirunaish/audio-recorder-app), the app I submitted in the
last application cycle (used as example for ConnectRequest ListView)
* CS10 (multithreading, networking)
* CS50 (error handling, documentation, style)
* DALI help sessions: Deflater/Inflater debugging; networking over Wifi timeout; AudioRecord
buffering; IP address fetching
* Suggestions made by my IDE (Android Studio)

## Future features, in order of priority
* If a slow connection causes a delay in the input stream, and there is a delay between one user's
recorded audio and the other user's played audio, the audio still stored in the buffer is sped up
until it catches up. The minimum delay is `Globals.packetSizeInSeconds`.
* Common call features such as speaker, mute, and displaying call duration
* An account system and DNS-like server, to return the current IP address of an user with a given
username.
* Handling connection attempts while in a call or the app is closed
* A desktop version