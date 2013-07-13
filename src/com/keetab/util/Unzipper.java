package com.keetab.util;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class Unzipper {

	final static int BUFFER_SIZE = 8192;
	
    public static void unzipStream(ZipInputStream zis, File destDir) {
        try {
            ZipEntry entry;
            byte buffer[] = new byte[BUFFER_SIZE];
            int count;
            while((entry = zis.getNextEntry()) != null) {
                File destFile = new File(destDir, entry.getName());
                
                int i = entry.getName().lastIndexOf('/');
                if (i >= 0) {
                    String dir = entry.getName().substring(0, i);
                    new File(destDir, dir).mkdirs();
                } 
                
                if (entry.isDirectory()) {
                    continue;
                } else if (destFile.exists()) {
                    destFile.delete();
                }
                
                count = 0;
                FileOutputStream fos = new FileOutputStream(destFile);
                while ((count = zis.read(buffer, 0, BUFFER_SIZE)) != -1) {
                    fos.write(buffer, 0, count);
                }
                fos.flush();
                fos.close();
            }
            zis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
