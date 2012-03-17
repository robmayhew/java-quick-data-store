package com.robmayhew.qds;
/*
Copyright (c) 2002 JSON.org

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

The Software shall be used for Good, not Evil.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * A QDSJSONObject is an unordered collection of name/value pairs. Its
 * external form is a string wrapped in curly braces with colons between the
 * names and values, and commas between the values and names. The internal form
 * is an object having <code>get</code> and <code>opt</code> methods for
 * accessing the values by name, and <code>put</code> methods for adding or
 * replacing values by name. The values can be any of these types:
 * <code>Boolean</code>, <code>QDSJSONArray</code>, <code>QDSJSONObject</code>,
 * <code>Number</code>, <code>String</code>, or the <code>QDSJSONObject.NULL</code>
 * object. A QDSJSONObject constructor can be used to convert an external form
 * JSON text into an internal form whose values can be retrieved with the
 * <code>get</code> and <code>opt</code> methods, or to convert values into a
 * JSON text using the <code>put</code> and <code>toString</code> methods.
 * A <code>get</code> method returns a value if one can be found, and throws an
 * exception if one cannot be found. An <code>opt</code> method returns a
 * default value instead of throwing an exception, and so is useful for
 * obtaining optional values.
 * <p>
 * The generic <code>get()</code> and <code>opt()</code> methods return an
 * object, which you can cast or query for type. There are also typed
 * <code>get</code> and <code>opt</code> methods that do type checking and type
 * coercion for you. The opt methods differ from the get methods in that they
 * do not throw. Instead, they return a specified value, such as null.
 * <p>
 * The <code>put</code> methods add or replace values in an object. For example,
 * <pre>myString = new QDSJSONObject().put("JSON", "Hello, World!").toString();</pre>
 * produces the string <code>{"JSON": "Hello, World"}</code>.
 * <p>
 * The texts produced by the <code>toString</code> methods strictly conform to
 * the JSON syntax rules.
 * The constructors are more forgiving in the texts they will accept:
 * <ul>
 * <li>An extra <code>,</code>&nbsp;<small>(comma)</small> may appear just
 *     before the closing brace.</li>
 * <li>Strings may be quoted with <code>'</code>&nbsp;<small>(single
 *     quote)</small>.</li>
 * <li>Strings do not need to be quoted at all if they do not begin with a quote
 *     or single quote, and if they do not contain leading or trailing spaces,
 *     and if they do not contain any of these characters:
 *     <code>{ } [ ] / \ : , = ; #</code> and if they do not look like numbers
 *     and if they are not the reserved words <code>true</code>,
 *     <code>false</code>, or <code>null</code>.</li>
 * <li>Keys can be followed by <code>=</code> or <code>=></code> as well as
 *     by <code>:</code>.</li>
 * <li>Values can be followed by <code>;</code> <small>(semicolon)</small> as
 *     well as by <code>,</code> <small>(comma)</small>.</li>
 * </ul>
 * @author JSON.org
 * @version 2011-11-24
 */
public class QDSJSONObject
{

    /**
     * QDSJSONObject.NULL is equivalent to the value that JavaScript calls null,
     * whilst Java's null is equivalent to the value that JavaScript calls
     * undefined.
     */
     private static final class Null {

        /**
         * There is only intended to be a single instance of the NULL object,
         * so the clone method returns itself.
         * @return     NULL.
         */
        protected final Object clone() {
            return this;
        }

        /**
         * A Null object is equal to the null value and to itself.
         * @param object    An object to test for nullness.
         * @return true if the object parameter is the QDSJSONObject.NULL object
         *  or null.
         */
        public boolean equals(Object object) {
            return object == null || object == this;
        }

        /**
         * Get the "null" string value.
         * @return The string "null".
         */
        public String toString() {
            return "null";
        }
    }


    /**
     * The map where the QDSJSONObject's properties are kept.
     */
    private final Map map;


    /**
     * It is sometimes more convenient and less ambiguous to have a
     * <code>NULL</code> object than to use Java's <code>null</code> value.
     * <code>QDSJSONObject.NULL.equals(null)</code> returns <code>true</code>.
     * <code>QDSJSONObject.NULL.toString()</code> returns <code>"null"</code>.
     */
    public static final Object NULL = new Null();


    /**
     * Construct an empty QDSJSONObject.
     */
    public QDSJSONObject() {
        this.map = new HashMap();
    }


    /**
     * Construct a QDSJSONObject from a subset of another QDSJSONObject.
     * An array of strings is used to identify the keys that should be copied.
     * Missing keys are ignored.
     * @param jo A QDSJSONObject.
     * @param names An array of strings.
     * @throws QDSJSONException
     * @exception QDSJSONException If a value is a non-finite number or if a name is duplicated.
     */
    public QDSJSONObject(QDSJSONObject jo, String[] names) {
        this();
        for (int i = 0; i < names.length; i += 1) {
            try {
                this.putOnce(names[i], jo.opt(names[i]));
            } catch (Exception ignore) {
            }
        }
    }


    /**
     * Construct a QDSJSONObject from a QDSJSONTokener.
     * @param x A QDSJSONTokener object containing the source string.
     * @throws QDSJSONException If there is a syntax error in the source string
     *  or a duplicated key.
     */
    public QDSJSONObject(QDSJSONTokener x) throws QDSJSONException
    {
        this();
        char c;
        String key;

        if (x.nextClean() != '{') {
            throw x.syntaxError("A QDSJSONObject text must begin with '{'");
        }
        for (;;) {
            c = x.nextClean();
            switch (c) {
            case 0:
                throw x.syntaxError("A QDSJSONObject text must end with '}'");
            case '}':
                return;
            default:
                x.back();
                key = x.nextValue().toString();
            }

// The key is followed by ':'. We will also tolerate '=' or '=>'.

            c = x.nextClean();
            if (c == '=') {
                if (x.next() != '>') {
                    x.back();
                }
            } else if (c != ':') {
                throw x.syntaxError("Expected a ':' after a key");
            }
            this.putOnce(key, x.nextValue());

// Pairs are separated by ','. We will also tolerate ';'.

            switch (x.nextClean()) {
            case ';':
            case ',':
                if (x.nextClean() == '}') {
                    return;
                }
                x.back();
                break;
            case '}':
                return;
            default:
                throw x.syntaxError("Expected a ',' or '}'");
            }
        }
    }


    /**
     * Construct a QDSJSONObject from a Map.
     *
     * @param map A map object that can be used to initialize the contents of
     *  the QDSJSONObject.
     * @throws QDSJSONException
     */
    public QDSJSONObject(Map map) {
        this.map = new HashMap();
        if (map != null) {
            Iterator i = map.entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry e = (Map.Entry)i.next();
                Object value = e.getValue();
                if (value != null) {
                    this.map.put(e.getKey(), wrap(value));
                }
            }
        }
    }


    /**
     * Construct a QDSJSONObject from an Object using bean getters.
     * It reflects on all of the public methods of the object.
     * For each of the methods with no parameters and a name starting
     * with <code>"get"</code> or <code>"is"</code> followed by an uppercase letter,
     * the method is invoked, and a key and the value returned from the getter method
     * are put into the new QDSJSONObject.
     *
     * The key is formed by removing the <code>"get"</code> or <code>"is"</code> prefix.
     * If the second remaining character is not upper case, then the first
     * character is converted to lower case.
     *
     * For example, if an object has a method named <code>"getName"</code>, and
     * if the result of calling <code>object.getName()</code> is <code>"Larry Fine"</code>,
     * then the QDSJSONObject will contain <code>"name": "Larry Fine"</code>.
     *
     * @param bean An object that has getter methods that should be used
     * to make a QDSJSONObject.
     */
    public QDSJSONObject(Object bean) {
        this();
        this.populateMap(bean);
    }


    /**
     * Construct a QDSJSONObject from an Object, using reflection to find the
     * public members. The resulting QDSJSONObject's keys will be the strings
     * from the names array, and the values will be the field values associated
     * with those keys in the object. If a key is not found or not visible,
     * then it will not be copied into the new QDSJSONObject.
     * @param object An object that has fields that should be used to make a
     * QDSJSONObject.
     * @param names An array of strings, the names of the fields to be obtained
     * from the object.
     */
    public QDSJSONObject(Object object, String names[]) {
        this();
        Class c = object.getClass();
        for (int i = 0; i < names.length; i += 1) {
            String name = names[i];
            try {
                this.putOpt(name, c.getField(name).get(object));
            } catch (Exception ignore) {
            }
        }
    }


    /**
     * Construct a QDSJSONObject from a source JSON text string.
     * This is the most commonly used QDSJSONObject constructor.
     * @param source    A string beginning
     *  with <code>{</code>&nbsp;<small>(left brace)</small> and ending
     *  with <code>}</code>&nbsp;<small>(right brace)</small>.
     * @exception QDSJSONException If there is a syntax error in the source
     *  string or a duplicated key.
     */
    public QDSJSONObject(String source) throws QDSJSONException
    {
        this(new QDSJSONTokener(source));
    }


    /**
     * Construct a QDSJSONObject from a ResourceBundle.
     * @param baseName The ResourceBundle base name.
     * @param locale The Locale to load the ResourceBundle for.
     * @throws QDSJSONException If any JSONExceptions are detected.
     */
    public QDSJSONObject(String baseName, Locale locale) throws QDSJSONException
    {
        this();
        ResourceBundle bundle = ResourceBundle.getBundle(baseName, locale,
                Thread.currentThread().getContextClassLoader());

// Iterate through the keys in the bundle.

        Enumeration keys = bundle.getKeys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            if (key instanceof String) {

// Go through the path, ensuring that there is a nested QDSJSONObject for each
// segment except the last. Add the value using the last segment's name into
// the deepest nested QDSJSONObject.

                String[] path = ((String)key).split("\\.");
                int last = path.length - 1;
                QDSJSONObject target = this;
                for (int i = 0; i < last; i += 1) {
                    String segment = path[i];
                    QDSJSONObject nextTarget = target.optJSONObject(segment);
                    if (nextTarget == null) {
                        nextTarget = new QDSJSONObject();
                        target.put(segment, nextTarget);
                    }
                    target = nextTarget;
                }
                target.put(path[last], bundle.getString((String)key));
            }
        }
    }


    /**
     * Accumulate values under a key. It is similar to the put method except
     * that if there is already an object stored under the key then a
     * QDSJSONArray is stored under the key to hold all of the accumulated values.
     * If there is already a QDSJSONArray, then the new value is appended to it.
     * In contrast, the put method replaces the previous value.
     *
     * If only one value is accumulated that is not a QDSJSONArray, then the
     * result will be the same as using put. But if multiple values are
     * accumulated, then the result will be like append.
     * @param key   A key string.
     * @param value An object to be accumulated under the key.
     * @return this.
     * @throws QDSJSONException If the value is an invalid number
     *  or if the key is null.
     */
    public QDSJSONObject accumulate(
        String key,
        Object value
    ) throws QDSJSONException
    {
        testValidity(value);
        Object object = this.opt(key);
        if (object == null) {
            this.put(key, value instanceof QDSJSONArray
                    ? new QDSJSONArray().put(value)
                    : value);
        } else if (object instanceof QDSJSONArray) {
            ((QDSJSONArray)object).put(value);
        } else {
            this.put(key, new QDSJSONArray().put(object).put(value));
        }
        return this;
    }


    /**
     * Append values to the array under a key. If the key does not exist in the
     * QDSJSONObject, then the key is put in the QDSJSONObject with its value being a
     * QDSJSONArray containing the value parameter. If the key was already
     * associated with a QDSJSONArray, then the value parameter is appended to it.
     * @param key   A key string.
     * @param value An object to be accumulated under the key.
     * @return this.
     * @throws QDSJSONException If the key is null or if the current value
     *  associated with the key is not a QDSJSONArray.
     */
    public QDSJSONObject append(String key, Object value) throws QDSJSONException
    {
        testValidity(value);
        Object object = this.opt(key);
        if (object == null) {
            this.put(key, new QDSJSONArray().put(value));
        } else if (object instanceof QDSJSONArray) {
            this.put(key, ((QDSJSONArray)object).put(value));
        } else {
            throw new QDSJSONException("QDSJSONObject[" + key +
                    "] is not a QDSJSONArray.");
        }
        return this;
    }


    /**
     * Produce a string from a double. The string "null" will be returned if
     * the number is not finite.
     * @param  d A double.
     * @return A String.
     */
    public static String doubleToString(double d) {
        if (Double.isInfinite(d) || Double.isNaN(d)) {
            return "null";
        }

// Shave off trailing zeros and decimal point, if possible.

        String string = Double.toString(d);
        if (string.indexOf('.') > 0 && string.indexOf('e') < 0 &&
                string.indexOf('E') < 0) {
            while (string.endsWith("0")) {
                string = string.substring(0, string.length() - 1);
            }
            if (string.endsWith(".")) {
                string = string.substring(0, string.length() - 1);
            }
        }
        return string;
    }


    /**
     * Get the value object associated with a key.
     *
     * @param key   A key string.
     * @return      The object associated with the key.
     * @throws QDSJSONException if the key is not found.
     */
    public Object get(String key) throws QDSJSONException
    {
        if (key == null) {
            throw new QDSJSONException("Null key.");
        }
        Object object = this.opt(key);
        if (object == null) {
            throw new QDSJSONException("QDSJSONObject[" + quote(key) +
                    "] not found.");
        }
        return object;
    }


    /**
     * Get the boolean value associated with a key.
     *
     * @param key   A key string.
     * @return      The truth.
     * @throws QDSJSONException
     *  if the value is not a Boolean or the String "true" or "false".
     */
    public boolean getBoolean(String key) throws QDSJSONException
    {
        Object object = this.get(key);
        if (object.equals(Boolean.FALSE) ||
                (object instanceof String &&
                ((String)object).equalsIgnoreCase("false"))) {
            return false;
        } else if (object.equals(Boolean.TRUE) ||
                (object instanceof String &&
                ((String)object).equalsIgnoreCase("true"))) {
            return true;
        }
        throw new QDSJSONException("QDSJSONObject[" + quote(key) +
                "] is not a Boolean.");
    }


    /**
     * Get the double value associated with a key.
     * @param key   A key string.
     * @return      The numeric value.
     * @throws QDSJSONException if the key is not found or
     *  if the value is not a Number object and cannot be converted to a number.
     */
    public double getDouble(String key) throws QDSJSONException
    {
        Object object = this.get(key);
        try {
            return object instanceof Number
                ? ((Number)object).doubleValue()
                : Double.parseDouble((String)object);
        } catch (Exception e) {
            throw new QDSJSONException("QDSJSONObject[" + quote(key) +
                "] is not a number.");
        }
    }


    /**
     * Get the int value associated with a key.
     *
     * @param key   A key string.
     * @return      The integer value.
     * @throws QDSJSONException if the key is not found or if the value cannot
     *  be converted to an integer.
     */
    public int getInt(String key) throws QDSJSONException
    {
        Object object = this.get(key);
        try {
            return object instanceof Number
                ? ((Number)object).intValue()
                : Integer.parseInt((String)object);
        } catch (Exception e) {
            throw new QDSJSONException("QDSJSONObject[" + quote(key) +
                "] is not an int.");
        }
    }


    /**
     * Get the QDSJSONArray value associated with a key.
     *
     * @param key   A key string.
     * @return      A QDSJSONArray which is the value.
     * @throws QDSJSONException if the key is not found or
     *  if the value is not a QDSJSONArray.
     */
    public QDSJSONArray getJSONArray(String key) throws QDSJSONException
    {
        Object object = this.get(key);
        if (object instanceof QDSJSONArray) {
            return (QDSJSONArray)object;
        }
        throw new QDSJSONException("QDSJSONObject[" + quote(key) +
                "] is not a QDSJSONArray.");
    }


    /**
     * Get the QDSJSONObject value associated with a key.
     *
     * @param key   A key string.
     * @return      A QDSJSONObject which is the value.
     * @throws QDSJSONException if the key is not found or
     *  if the value is not a QDSJSONObject.
     */
    public QDSJSONObject getJSONObject(String key) throws QDSJSONException
    {
        Object object = this.get(key);
        if (object instanceof QDSJSONObject) {
            return (QDSJSONObject)object;
        }
        throw new QDSJSONException("QDSJSONObject[" + quote(key) +
                "] is not a QDSJSONObject.");
    }


    /**
     * Get the long value associated with a key.
     *
     * @param key   A key string.
     * @return      The long value.
     * @throws QDSJSONException if the key is not found or if the value cannot
     *  be converted to a long.
     */
    public long getLong(String key) throws QDSJSONException
    {
        Object object = this.get(key);
        try {
            return object instanceof Number
                ? ((Number)object).longValue()
                : Long.parseLong((String)object);
        } catch (Exception e) {
            throw new QDSJSONException("QDSJSONObject[" + quote(key) +
                "] is not a long.");
        }
    }


    /**
     * Get an array of field names from a QDSJSONObject.
     *
     * @return An array of field names, or null if there are no names.
     */
    public static String[] getNames(QDSJSONObject jo) {
        int length = jo.length();
        if (length == 0) {
            return null;
        }
        Iterator iterator = jo.keys();
        String[] names = new String[length];
        int i = 0;
        while (iterator.hasNext()) {
            names[i] = (String)iterator.next();
            i += 1;
        }
        return names;
    }


    /**
     * Get an array of field names from an Object.
     *
     * @return An array of field names, or null if there are no names.
     */
    public static String[] getNames(Object object) {
        if (object == null) {
            return null;
        }
        Class klass = object.getClass();
        Field[] fields = klass.getFields();
        int length = fields.length;
        if (length == 0) {
            return null;
        }
        String[] names = new String[length];
        for (int i = 0; i < length; i += 1) {
            names[i] = fields[i].getName();
        }
        return names;
    }


    /**
     * Get the string associated with a key.
     *
     * @param key   A key string.
     * @return      A string which is the value.
     * @throws QDSJSONException if there is no string value for the key.
     */
    public String getString(String key) throws QDSJSONException
    {
        Object object = this.get(key);
        if (object instanceof String) {
            return (String)object;
        }
        throw new QDSJSONException("QDSJSONObject[" + quote(key) +
            "] not a string.");
    }


    /**
     * Determine if the QDSJSONObject contains a specific key.
     * @param key   A key string.
     * @return      true if the key exists in the QDSJSONObject.
     */
    public boolean has(String key) {
        return this.map.containsKey(key);
    }


    /**
     * Increment a property of a QDSJSONObject. If there is no such property,
     * create one with a value of 1. If there is such a property, and if
     * it is an Integer, Long, Double, or Float, then add one to it.
     * @param key  A key string.
     * @return this.
     * @throws QDSJSONException If there is already a property with this name
     * that is not an Integer, Long, Double, or Float.
     */
    public QDSJSONObject increment(String key) throws QDSJSONException
    {
        Object value = this.opt(key);
        if (value == null) {
            this.put(key, 1);
        } else if (value instanceof Integer) {
            this.put(key, ((Integer)value).intValue() + 1);
        } else if (value instanceof Long) {
            this.put(key, ((Long)value).longValue() + 1);
        } else if (value instanceof Double) {
            this.put(key, ((Double)value).doubleValue() + 1);
        } else if (value instanceof Float) {
            this.put(key, ((Float)value).floatValue() + 1);
        } else {
            throw new QDSJSONException("Unable to increment [" + quote(key) + "].");
        }
        return this;
    }


    /**
     * Determine if the value associated with the key is null or if there is
     *  no value.
     * @param key   A key string.
     * @return      true if there is no value associated with the key or if
     *  the value is the QDSJSONObject.NULL object.
     */
    public boolean isNull(String key) {
        return QDSJSONObject.NULL.equals(this.opt(key));
    }


    /**
     * Get an enumeration of the keys of the QDSJSONObject.
     *
     * @return An iterator of the keys.
     */
    public Iterator keys() {
        return this.map.keySet().iterator();
    }


    /**
     * Get the number of keys stored in the QDSJSONObject.
     *
     * @return The number of keys in the QDSJSONObject.
     */
    public int length() {
        return this.map.size();
    }


    /**
     * Produce a QDSJSONArray containing the names of the elements of this
     * QDSJSONObject.
     * @return A QDSJSONArray containing the key strings, or null if the QDSJSONObject
     * is empty.
     */
    public QDSJSONArray names() {
        QDSJSONArray ja = new QDSJSONArray();
        Iterator  keys = this.keys();
        while (keys.hasNext()) {
            ja.put(keys.next());
        }
        return ja.length() == 0 ? null : ja;
    }

    /**
     * Produce a string from a Number.
     * @param  number A Number
     * @return A String.
     * @throws QDSJSONException If n is a non-finite number.
     */
    public static String numberToString(Number number)
            throws QDSJSONException
    {
        if (number == null) {
            throw new QDSJSONException("Null pointer");
        }
        testValidity(number);

// Shave off trailing zeros and decimal point, if possible.

        String string = number.toString();
        if (string.indexOf('.') > 0 && string.indexOf('e') < 0 &&
                string.indexOf('E') < 0) {
            while (string.endsWith("0")) {
                string = string.substring(0, string.length() - 1);
            }
            if (string.endsWith(".")) {
                string = string.substring(0, string.length() - 1);
            }
        }
        return string;
    }


    /**
     * Get an optional value associated with a key.
     * @param key   A key string.
     * @return      An object which is the value, or null if there is no value.
     */
    public Object opt(String key) {
        return key == null ? null : this.map.get(key);
    }


    /**
     * Get an optional boolean associated with a key.
     * It returns false if there is no such key, or if the value is not
     * Boolean.TRUE or the String "true".
     *
     * @param key   A key string.
     * @return      The truth.
     */
    public boolean optBoolean(String key) {
        return this.optBoolean(key, false);
    }


    /**
     * Get an optional boolean associated with a key.
     * It returns the defaultValue if there is no such key, or if it is not
     * a Boolean or the String "true" or "false" (case insensitive).
     *
     * @param key              A key string.
     * @param defaultValue     The default.
     * @return      The truth.
     */
    public boolean optBoolean(String key, boolean defaultValue) {
        try {
            return this.getBoolean(key);
        } catch (Exception e) {
            return defaultValue;
        }
    }


    /**
     * Get an optional double associated with a key,
     * or NaN if there is no such key or if its value is not a number.
     * If the value is a string, an attempt will be made to evaluate it as
     * a number.
     *
     * @param key   A string which is the key.
     * @return      An object which is the value.
     */
    public double optDouble(String key) {
        return this.optDouble(key, Double.NaN);
    }


    /**
     * Get an optional double associated with a key, or the
     * defaultValue if there is no such key or if its value is not a number.
     * If the value is a string, an attempt will be made to evaluate it as
     * a number.
     *
     * @param key   A key string.
     * @param defaultValue     The default.
     * @return      An object which is the value.
     */
    public double optDouble(String key, double defaultValue) {
        try {
            return this.getDouble(key);
        } catch (Exception e) {
            return defaultValue;
        }
    }


    /**
     * Get an optional int value associated with a key,
     * or zero if there is no such key or if the value is not a number.
     * If the value is a string, an attempt will be made to evaluate it as
     * a number.
     *
     * @param key   A key string.
     * @return      An object which is the value.
     */
    public int optInt(String key) {
        return this.optInt(key, 0);
    }


    /**
     * Get an optional int value associated with a key,
     * or the default if there is no such key or if the value is not a number.
     * If the value is a string, an attempt will be made to evaluate it as
     * a number.
     *
     * @param key   A key string.
     * @param defaultValue     The default.
     * @return      An object which is the value.
     */
    public int optInt(String key, int defaultValue) {
        try {
            return this.getInt(key);
        } catch (Exception e) {
            return defaultValue;
        }
    }


    /**
     * Get an optional QDSJSONArray associated with a key.
     * It returns null if there is no such key, or if its value is not a
     * QDSJSONArray.
     *
     * @param key   A key string.
     * @return      A QDSJSONArray which is the value.
     */
    public QDSJSONArray optJSONArray(String key) {
        Object o = this.opt(key);
        return o instanceof QDSJSONArray ? (QDSJSONArray)o : null;
    }


    /**
     * Get an optional QDSJSONObject associated with a key.
     * It returns null if there is no such key, or if its value is not a
     * QDSJSONObject.
     *
     * @param key   A key string.
     * @return      A QDSJSONObject which is the value.
     */
    public QDSJSONObject optJSONObject(String key) {
        Object object = this.opt(key);
        return object instanceof QDSJSONObject ? (QDSJSONObject)object : null;
    }


    /**
     * Get an optional long value associated with a key,
     * or zero if there is no such key or if the value is not a number.
     * If the value is a string, an attempt will be made to evaluate it as
     * a number.
     *
     * @param key   A key string.
     * @return      An object which is the value.
     */
    public long optLong(String key) {
        return this.optLong(key, 0);
    }


    /**
     * Get an optional long value associated with a key,
     * or the default if there is no such key or if the value is not a number.
     * If the value is a string, an attempt will be made to evaluate it as
     * a number.
     *
     * @param key          A key string.
     * @param defaultValue The default.
     * @return             An object which is the value.
     */
    public long optLong(String key, long defaultValue) {
        try {
            return this.getLong(key);
        } catch (Exception e) {
            return defaultValue;
        }
    }


    /**
     * Get an optional string associated with a key.
     * It returns an empty string if there is no such key. If the value is not
     * a string and is not null, then it is converted to a string.
     *
     * @param key   A key string.
     * @return      A string which is the value.
     */
    public String optString(String key) {
        return this.optString(key, "");
    }


    /**
     * Get an optional string associated with a key.
     * It returns the defaultValue if there is no such key.
     *
     * @param key   A key string.
     * @param defaultValue     The default.
     * @return      A string which is the value.
     */
    public String optString(String key, String defaultValue) {
        Object object = this.opt(key);
        return NULL.equals(object) ? defaultValue : object.toString();
    }


    private void populateMap(Object bean) {
        Class klass = bean.getClass();

// If klass is a System class then set includeSuperClass to false.

        boolean includeSuperClass = klass.getClassLoader() != null;

        Method[] methods = includeSuperClass
                ? klass.getMethods()
                : klass.getDeclaredMethods();
        for (int i = 0; i < methods.length; i += 1) {
            try {
                Method method = methods[i];
                if (Modifier.isPublic(method.getModifiers())) {
                    String name = method.getName();
                    String key = "";
                    if (name.startsWith("get")) {
                        if ("getClass".equals(name) ||
                                "getDeclaringClass".equals(name)) {
                            key = "";
                        } else {
                            key = name.substring(3);
                        }
                    } else if (name.startsWith("is")) {
                        key = name.substring(2);
                    }
                    if (key.length() > 0 &&
                            Character.isUpperCase(key.charAt(0)) &&
                            method.getParameterTypes().length == 0) {
                        if (key.length() == 1) {
                            key = key.toLowerCase();
                        } else if (!Character.isUpperCase(key.charAt(1))) {
                            key = key.substring(0, 1).toLowerCase() +
                                key.substring(1);
                        }

                        Object result = method.invoke(bean, (Object[])null);
                        if (result != null) {
                            this.map.put(key, wrap(result));
                        }
                    }
                }
            } catch (Exception ignore) {
            }
        }
    }


    /**
     * Put a key/boolean pair in the QDSJSONObject.
     *
     * @param key   A key string.
     * @param value A boolean which is the value.
     * @return this.
     * @throws QDSJSONException If the key is null.
     */
    public QDSJSONObject put(String key, boolean value) throws QDSJSONException
    {
        this.put(key, value ? Boolean.TRUE : Boolean.FALSE);
        return this;
    }


    /**
     * Put a key/value pair in the QDSJSONObject, where the value will be a
     * QDSJSONArray which is produced from a Collection.
     * @param key   A key string.
     * @param value A Collection value.
     * @return      this.
     * @throws QDSJSONException
     */
    public QDSJSONObject put(String key, Collection value) throws QDSJSONException
    {
        this.put(key, new QDSJSONArray(value));
        return this;
    }


    /**
     * Put a key/double pair in the QDSJSONObject.
     *
     * @param key   A key string.
     * @param value A double which is the value.
     * @return this.
     * @throws QDSJSONException If the key is null or if the number is invalid.
     */
    public QDSJSONObject put(String key, double value) throws QDSJSONException
    {
        this.put(key, new Double(value));
        return this;
    }


    /**
     * Put a key/int pair in the QDSJSONObject.
     *
     * @param key   A key string.
     * @param value An int which is the value.
     * @return this.
     * @throws QDSJSONException If the key is null.
     */
    public QDSJSONObject put(String key, int value) throws QDSJSONException
    {
        this.put(key, new Integer(value));
        return this;
    }


    /**
     * Put a key/long pair in the QDSJSONObject.
     *
     * @param key   A key string.
     * @param value A long which is the value.
     * @return this.
     * @throws QDSJSONException If the key is null.
     */
    public QDSJSONObject put(String key, long value) throws QDSJSONException
    {
        this.put(key, new Long(value));
        return this;
    }


    /**
     * Put a key/value pair in the QDSJSONObject, where the value will be a
     * QDSJSONObject which is produced from a Map.
     * @param key   A key string.
     * @param value A Map value.
     * @return      this.
     * @throws QDSJSONException
     */
    public QDSJSONObject put(String key, Map value) throws QDSJSONException
    {
        this.put(key, new QDSJSONObject(value));
        return this;
    }


    /**
     * Put a key/value pair in the QDSJSONObject. If the value is null,
     * then the key will be removed from the QDSJSONObject if it is present.
     * @param key   A key string.
     * @param value An object which is the value. It should be of one of these
     *  types: Boolean, Double, Integer, QDSJSONArray, QDSJSONObject, Long, String,
     *  or the QDSJSONObject.NULL object.
     * @return this.
     * @throws QDSJSONException If the value is non-finite number
     *  or if the key is null.
     */
    public QDSJSONObject put(String key, Object value) throws QDSJSONException
    {
        if (key == null) {
            throw new QDSJSONException("Null key.");
        }
        if (value != null) {
            testValidity(value);
            this.map.put(key, value);
        } else {
            this.remove(key);
        }
        return this;
    }


    /**
     * Put a key/value pair in the QDSJSONObject, but only if the key and the
     * value are both non-null, and only if there is not already a member
     * with that name.
     * @param key
     * @param value
     * @return his.
     * @throws QDSJSONException if the key is a duplicate
     */
    public QDSJSONObject putOnce(String key, Object value) throws QDSJSONException
    {
        if (key != null && value != null) {
            if (this.opt(key) != null) {
                throw new QDSJSONException("Duplicate key \"" + key + "\"");
            }
            this.put(key, value);
        }
        return this;
    }


    /**
     * Put a key/value pair in the QDSJSONObject, but only if the
     * key and the value are both non-null.
     * @param key   A key string.
     * @param value An object which is the value. It should be of one of these
     *  types: Boolean, Double, Integer, QDSJSONArray, QDSJSONObject, Long, String,
     *  or the QDSJSONObject.NULL object.
     * @return this.
     * @throws QDSJSONException If the value is a non-finite number.
     */
    public QDSJSONObject putOpt(String key, Object value) throws QDSJSONException
    {
        if (key != null && value != null) {
            this.put(key, value);
        }
        return this;
    }


    /**
     * Produce a string in double quotes with backslash sequences in all the
     * right places. A backslash will be inserted within </, producing <\/,
     * allowing JSON text to be delivered in HTML. In JSON text, a string
     * cannot contain a control character or an unescaped quote or backslash.
     * @param string A String
     * @return  A String correctly formatted for insertion in a JSON text.
     */
    public static String quote(String string) {
        if (string == null || string.length() == 0) {
            return "\"\"";
        }

        char         b;
        char         c = 0;
        String       hhhh;
        int          i;
        int          len = string.length();
        StringBuffer sb = new StringBuffer(len + 4);

        sb.append('"');
        for (i = 0; i < len; i += 1) {
            b = c;
            c = string.charAt(i);
            switch (c) {
            case '\\':
            case '"':
                sb.append('\\');
                sb.append(c);
                break;
            case '/':
                if (b == '<') {
                    sb.append('\\');
                }
                sb.append(c);
                break;
            case '\b':
                sb.append("\\b");
                break;
            case '\t':
                sb.append("\\t");
                break;
            case '\n':
                sb.append("\\n");
                break;
            case '\f':
                sb.append("\\f");
                break;
            case '\r':
                sb.append("\\r");
                break;
            default:
                if (c < ' ' || (c >= '\u0080' && c < '\u00a0') ||
                               (c >= '\u2000' && c < '\u2100')) {
                    hhhh = "000" + Integer.toHexString(c);
                    sb.append("\\u" + hhhh.substring(hhhh.length() - 4));
                } else {
                    sb.append(c);
                }
            }
        }
        sb.append('"');
        return sb.toString();
    }

    /**
     * Remove a name and its value, if present.
     * @param key The name to be removed.
     * @return The value that was associated with the name,
     * or null if there was no value.
     */
    public Object remove(String key) {
        return this.map.remove(key);
    }

    /**
     * Try to convert a string into a number, boolean, or null. If the string
     * can't be converted, return the string.
     * @param string A String.
     * @return A simple JSON value.
     */
    public static Object stringToValue(String string) {
        Double d;
        if (string.equals("")) {
            return string;
        }
        if (string.equalsIgnoreCase("true")) {
            return Boolean.TRUE;
        }
        if (string.equalsIgnoreCase("false")) {
            return Boolean.FALSE;
        }
        if (string.equalsIgnoreCase("null")) {
            return QDSJSONObject.NULL;
        }

        /*
         * If it might be a number, try converting it.
         * If a number cannot be produced, then the value will just
         * be a string. Note that the plus and implied string
         * conventions are non-standard. A JSON parser may accept
         * non-JSON forms as long as it accepts all correct JSON forms.
         */

        char b = string.charAt(0);
        if ((b >= '0' && b <= '9') || b == '.' || b == '-' || b == '+') {
            try {
                if (string.indexOf('.') > -1 ||
                        string.indexOf('e') > -1 || string.indexOf('E') > -1) {
                    d = Double.valueOf(string);
                    if (!d.isInfinite() && !d.isNaN()) {
                        return d;
                    }
                } else {
                    Long myLong = new Long(string);
                    if (myLong.longValue() == myLong.intValue()) {
                        return new Integer(myLong.intValue());
                    } else {
                        return myLong;
                    }
                }
            }  catch (Exception ignore) {
            }
        }
        return string;
    }


    /**
     * Throw an exception if the object is a NaN or infinite number.
     * @param o The object to test.
     * @throws QDSJSONException If o is a non-finite number.
     */
    public static void testValidity(Object o) throws QDSJSONException
    {
        if (o != null) {
            if (o instanceof Double) {
                if (((Double)o).isInfinite() || ((Double)o).isNaN()) {
                    throw new QDSJSONException(
                        "JSON does not allow non-finite numbers.");
                }
            } else if (o instanceof Float) {
                if (((Float)o).isInfinite() || ((Float)o).isNaN()) {
                    throw new QDSJSONException(
                        "JSON does not allow non-finite numbers.");
                }
            }
        }
    }


    /**
     * Produce a QDSJSONArray containing the values of the members of this
     * QDSJSONObject.
     * @param names A QDSJSONArray containing a list of key strings. This
     * determines the sequence of the values in the result.
     * @return A QDSJSONArray of values.
     * @throws QDSJSONException If any of the values are non-finite numbers.
     */
    public QDSJSONArray toJSONArray(QDSJSONArray names) throws QDSJSONException
    {
        if (names == null || names.length() == 0) {
            return null;
        }
        QDSJSONArray ja = new QDSJSONArray();
        for (int i = 0; i < names.length(); i += 1) {
            ja.put(this.opt(names.getString(i)));
        }
        return ja;
    }

    /**
     * Make a JSON text of this QDSJSONObject. For compactness, no whitespace
     * is added. If this would not result in a syntactically correct JSON text,
     * then null will be returned instead.
     * <p>
     * Warning: This method assumes that the data structure is acyclical.
     *
     * @return a printable, displayable, portable, transmittable
     *  representation of the object, beginning
     *  with <code>{</code>&nbsp;<small>(left brace)</small> and ending
     *  with <code>}</code>&nbsp;<small>(right brace)</small>.
     */
    public String toString() {
        try {
            Iterator     keys = this.keys();
            StringBuffer sb = new StringBuffer("{");

            while (keys.hasNext()) {
                if (sb.length() > 1) {
                    sb.append(',');
                }
                Object o = keys.next();
                sb.append(quote(o.toString()));
                sb.append(':');
                sb.append(valueToString(this.map.get(o)));
            }
            sb.append('}');
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * Make a prettyprinted JSON text of this QDSJSONObject.
     * <p>
     * Warning: This method assumes that the data structure is acyclical.
     * @param indentFactor The number of spaces to add to each level of
     *  indentation.
     * @return a printable, displayable, portable, transmittable
     *  representation of the object, beginning
     *  with <code>{</code>&nbsp;<small>(left brace)</small> and ending
     *  with <code>}</code>&nbsp;<small>(right brace)</small>.
     * @throws QDSJSONException If the object contains an invalid number.
     */
    public String toString(int indentFactor) throws QDSJSONException
    {
        return this.toString(indentFactor, 0);
    }


    /**
     * Make a prettyprinted JSON text of this QDSJSONObject.
     * <p>
     * Warning: This method assumes that the data structure is acyclical.
     * @param indentFactor The number of spaces to add to each level of
     *  indentation.
     * @param indent The indentation of the top level.
     * @return a printable, displayable, transmittable
     *  representation of the object, beginning
     *  with <code>{</code>&nbsp;<small>(left brace)</small> and ending
     *  with <code>}</code>&nbsp;<small>(right brace)</small>.
     * @throws QDSJSONException If the object contains an invalid number.
     */
    String toString(int indentFactor, int indent) throws QDSJSONException
    {
        int i;
        int length = this.length();
        if (length == 0) {
            return "{}";
        }
        Iterator     keys = this.keys();
        int          newindent = indent + indentFactor;
        Object       object;
        StringBuffer sb = new StringBuffer("{");
        if (length == 1) {
            object = keys.next();
            sb.append(quote(object.toString()));
            sb.append(": ");
            sb.append(valueToString(this.map.get(object), indentFactor,
                    indent));
        } else {
            while (keys.hasNext()) {
                object = keys.next();
                if (sb.length() > 1) {
                    sb.append(",\n");
                } else {
                    sb.append('\n');
                }
                for (i = 0; i < newindent; i += 1) {
                    sb.append(' ');
                }
                sb.append(quote(object.toString()));
                sb.append(": ");
                sb.append(valueToString(this.map.get(object), indentFactor,
                        newindent));
            }
            if (sb.length() > 1) {
                sb.append('\n');
                for (i = 0; i < indent; i += 1) {
                    sb.append(' ');
                }
            }
        }
        sb.append('}');
        return sb.toString();
    }


    /**
     * Make a JSON text of an Object value. If the object has an
     * value.toJSONString() method, then that method will be used to produce
     * the JSON text. The method is required to produce a strictly
     * conforming text. If the object does not contain a toJSONString
     * method (which is the most common case), then a text will be
     * produced by other means. If the value is an array or Collection,
     * then a QDSJSONArray will be made from it and its toJSONString method
     * will be called. If the value is a MAP, then a QDSJSONObject will be made
     * from it and its toJSONString method will be called. Otherwise, the
     * value's toString method will be called, and the result will be quoted.
     *
     * <p>
     * Warning: This method assumes that the data structure is acyclical.
     * @param value The value to be serialized.
     * @return a printable, displayable, transmittable
     *  representation of the object, beginning
     *  with <code>{</code>&nbsp;<small>(left brace)</small> and ending
     *  with <code>}</code>&nbsp;<small>(right brace)</small>.
     * @throws QDSJSONException If the value is or contains an invalid number.
     */
    public static String valueToString(Object value) throws QDSJSONException
    {
        if (value == null || value.equals(null)) {
            return "null";
        }
        if (value instanceof QDSJSONString) {
            Object object;
            try {
                object = ((QDSJSONString)value).toJSONString();
            } catch (Exception e) {
                throw new QDSJSONException(e);
            }
            if (object instanceof String) {
                return (String)object;
            }
            throw new QDSJSONException("Bad value from toJSONString: " + object);
        }
        if (value instanceof Number) {
            return numberToString((Number) value);
        }
        if (value instanceof Boolean || value instanceof QDSJSONObject ||
                value instanceof QDSJSONArray) {
            return value.toString();
        }
        if (value instanceof Map) {
            return new QDSJSONObject((Map)value).toString();
        }
        if (value instanceof Collection) {
            return new QDSJSONArray((Collection)value).toString();
        }
        if (value.getClass().isArray()) {
            return new QDSJSONArray(value).toString();
        }
        return quote(value.toString());
    }


    /**
     * Make a prettyprinted JSON text of an object value.
     * <p>
     * Warning: This method assumes that the data structure is acyclical.
     * @param value The value to be serialized.
     * @param indentFactor The number of spaces to add to each level of
     *  indentation.
     * @param indent The indentation of the top level.
     * @return a printable, displayable, transmittable
     *  representation of the object, beginning
     *  with <code>{</code>&nbsp;<small>(left brace)</small> and ending
     *  with <code>}</code>&nbsp;<small>(right brace)</small>.
     * @throws QDSJSONException If the object contains an invalid number.
     */
     static String valueToString(
         Object value,
         int    indentFactor,
         int    indent
     ) throws QDSJSONException
     {
        if (value == null || value.equals(null)) {
            return "null";
        }
        try {
            if (value instanceof QDSJSONString) {
                Object o = ((QDSJSONString)value).toJSONString();
                if (o instanceof String) {
                    return (String)o;
                }
            }
        } catch (Exception ignore) {
        }
        if (value instanceof Number) {
            return numberToString((Number) value);
        }
        if (value instanceof Boolean) {
            return value.toString();
        }
        if (value instanceof QDSJSONObject) {
            return ((QDSJSONObject)value).toString(indentFactor, indent);
        }
        if (value instanceof QDSJSONArray) {
            return ((QDSJSONArray)value).toString(indentFactor, indent);
        }
        if (value instanceof Map) {
            return new QDSJSONObject((Map)value).toString(indentFactor, indent);
        }
        if (value instanceof Collection) {
            return new QDSJSONArray((Collection)value).toString(indentFactor, indent);
        }
        if (value.getClass().isArray()) {
            return new QDSJSONArray(value).toString(indentFactor, indent);
        }
        return quote(value.toString());
    }


     /**
      * Wrap an object, if necessary. If the object is null, return the NULL
      * object. If it is an array or collection, wrap it in a QDSJSONArray. If
      * it is a map, wrap it in a QDSJSONObject. If it is a standard property
      * (Double, String, et al) then it is already wrapped. Otherwise, if it
      * comes from one of the java packages, turn it into a string. And if
      * it doesn't, try to wrap it in a QDSJSONObject. If the wrapping fails,
      * then null is returned.
      *
      * @param object The object to wrap
      * @return The wrapped value
      */
     public static Object wrap(Object object) {
         try {
             if (object == null) {
                 return NULL;
             }
             if (object instanceof QDSJSONObject || object instanceof QDSJSONArray ||
                     NULL.equals(object)      || object instanceof QDSJSONString ||
                     object instanceof Byte   || object instanceof Character  ||
                     object instanceof Short  || object instanceof Integer    ||
                     object instanceof Long   || object instanceof Boolean    ||
                     object instanceof Float  || object instanceof Double     ||
                     object instanceof String) {
                 return object;
             }

             if (object instanceof Collection) {
                 return new QDSJSONArray((Collection)object);
             }
             if (object.getClass().isArray()) {
                 return new QDSJSONArray(object);
             }
             if (object instanceof Map) {
                 return new QDSJSONObject((Map)object);
             }
             Package objectPackage = object.getClass().getPackage();
             String objectPackageName = objectPackage != null
                 ? objectPackage.getName()
                 : "";
             if (
                 objectPackageName.startsWith("java.") ||
                 objectPackageName.startsWith("javax.") ||
                 object.getClass().getClassLoader() == null
             ) {
                 return object.toString();
             }
             return new QDSJSONObject(object);
         } catch(Exception exception) {
             return null;
         }
     }


     /**
      * Write the contents of the QDSJSONObject as JSON text to a writer.
      * For compactness, no whitespace is added.
      * <p>
      * Warning: This method assumes that the data structure is acyclical.
      *
      * @return The writer.
      * @throws QDSJSONException
      */
     public Writer write(Writer writer) throws QDSJSONException
     {
        try {
            boolean  commanate = false;
            Iterator keys = this.keys();
            writer.write('{');

            while (keys.hasNext()) {
                if (commanate) {
                    writer.write(',');
                }
                Object key = keys.next();
                writer.write(quote(key.toString()));
                writer.write(':');
                Object value = this.map.get(key);
                if (value instanceof QDSJSONObject) {
                    ((QDSJSONObject)value).write(writer);
                } else if (value instanceof QDSJSONArray) {
                    ((QDSJSONArray)value).write(writer);
                } else {
                    writer.write(valueToString(value));
                }
                commanate = true;
            }
            writer.write('}');
            return writer;
        } catch (IOException exception) {
            throw new QDSJSONException(exception);
        }
     }
}