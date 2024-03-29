/**
 * WavFileException class
 *
 * A.Greensted
 * http://www.labbookpages.co.uk
 *
 * File format is based on the information from
 * http://www.sonicspot.com/guide/wavefiles.html
 * http://www.blitter.com/~russtopia/MIDI/~jglatt/tech/wave.htm
 *
 * Version 1.0
 */

package uz.paynet.morsehelperui;

public class WavFileException extends Exception
{
    /**
     *
     */
    private static final long serialVersionUID = 8106597985536609621L;

    public WavFileException()
    {
        super();
    }

    public WavFileException(String message)
    {
        super(message);
    }

    public WavFileException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public WavFileException(Throwable cause)
    {
        super(cause);
    }
}
