package org.rambo.nfis.util;

import java.io.*;

/**
 * @author Rambo Yang
 */
public class UnicodeReader extends Reader {
    /** 默认编码 */
    private String defaultEncoding;

    private PushbackInputStream internalStream;

    private InputStreamReader internalStream2;

    private static final int BOM_SIZE = 4;

    public UnicodeReader(InputStream in, String defaultEncoding) {
        this.internalStream = new PushbackInputStream(in, BOM_SIZE);
        this.defaultEncoding = defaultEncoding;
    }

    protected void init() throws IOException {
        if (internalStream2 != null) {
            return;
        }

        String encoding;

        byte[] bom = new byte[BOM_SIZE];
        int n = internalStream.read(bom, 0, bom.length);
        int unRead;
        if ( (bom[0] == (byte)0x00) && (bom[1] == (byte)0x00) &&
                (bom[2] == (byte)0xFE) && (bom[3] == (byte)0xFF) ) {
            encoding = "UTF-32BE";
            unRead = n - 4;
        }
        else if ( (bom[0] == (byte)0xFF) && (bom[1] == (byte)0xFE) &&
                (bom[2] == (byte)0x00) && (bom[3] == (byte)0x00) ) {
            encoding = "UTF-32LE";
            unRead = n - 4;
        }
        else if (  (bom[0] == (byte)0xEF) && (bom[1] == (byte)0xBB) &&
                (bom[2] == (byte)0xBF) ) {
            encoding = "UTF-8";
            unRead = n - 3;
        }
        else if ( (bom[0] == (byte)0xFE) && (bom[1] == (byte)0xFF) ) {
            encoding = "UTF-16BE";
            unRead = n - 2;
        }
        else if ( (bom[0] == (byte)0xFF) && (bom[1] == (byte)0xFE) ) {
            encoding = "UTF-16LE";
            unRead = n - 2;
        }
        else {
            // Unicode BOM mark not found, unread all bytes
            encoding = defaultEncoding;
            unRead = n;
        }

        if (unRead > 0) {
            internalStream.unread(bom, (n - unRead), unRead);
        }

        //
        if (encoding == null) {
            internalStream2 = new InputStreamReader(internalStream);
        }
        else {
            internalStream2 = new InputStreamReader(internalStream, encoding);
        }
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        init();
        return internalStream2.read(cbuf, off, len);
    }

    @Override
    public void close() throws IOException {
        init();
        internalStream2.close();
    }
}
