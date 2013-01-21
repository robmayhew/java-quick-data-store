/**
 *
 * Copyright 2013 Rob Mayhew
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.robmayhew.qds;

import java.io.*;


public class FileValueStore implements ValueStore
{
    private final String filePath;

    public FileValueStore(String filePath)
    {
        this.filePath = filePath;
    }

    public void writeValue(String key, String value) throws QDSException
    {
        File f = new File(filePath);
        File swapFile = new File(filePath + ".swap");
        if (swapFile.exists())
        {
            if (!swapFile.delete())
                throw new QDSException("Unable to delete swap file "
                        + swapFile.getPath());
        }
        try
        {

            renameToSwapFile(f, swapFile);
            if (!swapFile.exists())
            {
                // First write
                PrintWriter writer = new PrintWriter(new FileWriter(filePath));
                try
                {
                    writer.println(key + "=" + value);
                } finally
                {
                    writer.flush();
                    writer.close();
                }
                return;
            }
            replaceValueInFile(key, value, swapFile);
        } catch (Exception e)
        {
            throw new RuntimeException("Error writing file", e);
        }
    }

    private void replaceValueInFile(String key, String value, File swapFile)
            throws IOException
    {
        PrintWriter writer = new PrintWriter(new FileWriter(filePath));
        BufferedReader reader = new BufferedReader(new FileReader(swapFile));
        boolean valueWritten = false;
        try
        {
            while (reader.ready())
            {
                String line = reader.readLine();
                if (line.startsWith(key + "="))
                {
                    writer.println(key + "=" + value);
                    valueWritten = true;
                } else
                {
                    writer.println(line);
                }
            }
            if(!valueWritten)
                writer.println(key + "=" + value);
        } finally
        {
            reader.close();
            writer.flush();
            writer.close();
            cleanupSwapFile(swapFile);
        }
    }

    private void renameToSwapFile(File f, File swapFile)
    {
        if (f.exists())
        {
            if (!f.renameTo(swapFile))
                throw new RuntimeException("Unable to rename file " +
                        filePath + " for writing");
        }
    }

    private void cleanupSwapFile(File swapFile)
    {
        if (!swapFile.delete())
        {
            System.err.println("Unable to delete swap file " + swapFile.getPath());
        }
    }

    public String loadValue(String key)
    {
        File f = new File(filePath);
        if (!f.exists())
            return null;
        String jsonString = null;
        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            try
            {
                while (reader.ready())
                {
                    String line = reader.readLine();
                    if (line.startsWith(key + "="))
                    {
                        int i = line.indexOf("=");
                        jsonString = line.substring(i + 1);
                    }
                }
            } finally
            {
                reader.close();
            }
        } catch (Exception e)
        {
            throw new RuntimeException("Error loading " + filePath, e);
        }
        return jsonString;
    }

}
