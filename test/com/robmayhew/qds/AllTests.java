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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AllTests extends TestCase{
    
    public static Test suite()
    {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(SimplePrimitiveTest.class);
        suite.addTestSuite(FooObjectTest.class);
        suite.addTestSuite(SaveListTest.class);
        suite.addTestSuite(QDSTest.class);
        return suite;
    }
}
