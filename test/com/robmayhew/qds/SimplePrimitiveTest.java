/**
 *
 * Copyright 2012 Rob Mayhew
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

import junit.framework.TestCase;

import java.io.File;


public class SimplePrimitiveTest extends TestCase
{
    
    private QuickDataStore store;
    String filePath = "stringTest";
    public void setUp()
    {        
        store = new QuickDataStore(filePath);
        File testFile = new File(filePath);
        testFile.delete();
    }
    
    public void tearDown()
    {
        File testFile = new File(filePath);
        testFile.delete();        
    }
    
    
    public void testString()
    {
        String key = "String";
        String value = "Testing \n Still Testing";        
        store.save(key,value);
        String result = (String)store.load(key);
        assertEquals(value,result);
    }
    
    public void testBoolean()
    {
        String key = "boolean";
        boolean value = true;
        store.save(key,value);
        boolean result = (Boolean)store.load(key);
        assertEquals(value,result);
    }
    
    public void testInt()
    {
        String key = "int";
        int value = 89771314;
        store.save(key,value);
        int result = (Integer)store.load(key);
        assertEquals(value,result);       
    }
    
    public void testDouble()
    {
        String key = "double";
        double value  = 452.6123451f;
        store.save(key,value);
        double result = (Double)store.load(key);
        assertEquals(value,result);
        assertNull("Nothing",store.load("nothing"));
    }

    public void testNotFound()
    {
        assertNull(store.load("nothing"));
    }
}
