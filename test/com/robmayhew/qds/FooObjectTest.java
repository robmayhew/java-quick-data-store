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


public class FooObjectTest extends TestCase
{
        
    private QuickDataStore store;
    String filePath = "fooTest";
    public void setUp()
    {        
        store = new QuickDataStore(new FileValueStore(filePath));
        store = new QuickDataStore(new PreferencesValueStore(FooObjectTest.class.getSimpleName()));
        File testFile = new File(filePath);
        testFile.delete();
    }
    
    public void tearDown()
    {
        File testFile = new File(filePath);
        testFile.delete();        
    } 
    
    public void testSaveFoo()
    {
        Foo value = new Foo();
        String key = "foo";
        store.save(key,value);
        Foo result = (Foo)store.load(key);
        assertEquals(value,result);
    }

    public void testSaveFooWithData()
    {
        Foo value = new Foo();
        value.setActive(true);
        value.setName("Testing\nYES testing");
        value.setAge(321);
        value.setValue(09709.3434);
        String key = "foo";
        store.save(key,value);
        Foo result = (Foo)store.load(key);
        assertEquals(value,result);
    }

}
