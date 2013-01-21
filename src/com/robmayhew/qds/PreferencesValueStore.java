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

import java.util.prefs.Preferences;


public class PreferencesValueStore implements ValueStore{

    private final String node;

    public PreferencesValueStore(String node)
    {
        this.node = node;
    }

    @Override
    public void writeValue(String key, String value) throws QDSException {
        Preferences preferences = Preferences.userRoot().node(node);
        preferences.put(key,value);

    }

    @Override
    public String loadValue(String key) {
        Preferences preferences = Preferences.userRoot().node(node);
        return preferences.get(key,null);
    }
}