package dk.amavin.projectfysapp.bodymodel;


import android.content.Context;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class BMPTextureLoader {
    private byte[] data;
    private int width;
    private int height;
    public BMPTextureLoader(Context context, String file)
    {
        //Inspiration:
        //http://www.opengl-tutorial.org/beginners-tutorials/tutorial-5-a-textured-cube/

        DataInputStream in = null;
        byte[] header = new byte[54]; // Each BMP file begins by a 54-byte  header
        int imageSize;   // = width*height*3

        // Actual RGB data
        try {
            in = new DataInputStream(context.getAssets().open(file));

            if(in.read(header, 0, 54) != 54)
            {
                //failed to read header
                return;
            }
            if ( header[0]!='B' || header[1]!='M' ){
                //Not a correct BMP file
                return;
            }

            ByteBuffer buffer = ByteBuffer.wrap(header);
            buffer.order(ByteOrder.LITTLE_ENDIAN);

            width = buffer.getInt(0x12);
            height = buffer.getInt(0x16);
            imageSize = buffer.getInt(0x22);

            // Some BMP files are misformatted, guess missing information
            if (imageSize==0)    imageSize=width*height*3; // 3 : one byte for each Red, Green and Blue component

            data = new byte [imageSize];
            in.read(data, 0, imageSize);
        }
        catch(IOException ex)
        {

        }
        finally {
            if(in != null)
                try {
                    in.close();
                }
            catch(IOException ex)
            {

            }
        }
    }

    public byte[] getData() {
        return data;
    }

    public ByteBuffer getByteBuffer()
    {
        return ByteBuffer.wrap(data);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
