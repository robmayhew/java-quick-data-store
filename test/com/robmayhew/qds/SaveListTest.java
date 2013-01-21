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
import java.util.ArrayList;
import java.util.List;


public class SaveListTest extends TestCase
{
    private QuickDataStore store;
    String filePath = "listTest";
    
    public void setUp()
    {        
        store = new QuickDataStore(new FileValueStore(filePath));
        File testFile = new File(filePath);
        testFile.delete();
    }
    
    public void tearDown()
    {
        File testFile = new File(filePath);
        testFile.delete();        
    } 
    
    public void testSaveStringList() throws Exception
    {
        String key = "list";
        List<String> value = new ArrayList<String>();
        value.add("1");
        value.add("2");
        value.add("3");
        store.save(key,value);
        List result = (List)store.load(key);
        for(int i = 0; i < value.size(); i++)
        {
            assertEquals(value.get(i),result.get(i));
        }
    }

    public void testSaveIntList() throws Exception
    {
        String key = "list";
        List<Integer> value = new ArrayList<Integer>();
        value.add(1);
        value.add(2);
        value.add(3);
        store.save(key, value);
        List result = (List)store.load(key);
        for(int i = 0; i < value.size(); i++)
        {
            assertEquals(value.get(i),result.get(i));
        }
    }
    
    public void testSaveFooList() throws Exception
    {
        String key = "list";
        List<Foo> value = new ArrayList<Foo>();
        value.add(new Foo(1241,"A",true,334412.34));
        value.add(new Foo(12351,"B",false,12.34));
        value.add(new Foo(11,"C",true,312.34));
        store.save(key,value);
        List result = (List)store.load(key);
        for(int i = 0; i < value.size(); i++)
        {
            assertEquals(value.get(i),result.get(i));
        }
    }
}
