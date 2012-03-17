package com.robmayhew.qds;

/**
 * The <code>QDSJSONString</code> interface allows a <code>toJSONString()</code>
 * method so that a class can change the behavior of 
 * <code>QDSJSONObject.toString()</code>, <code>QDSJSONArray.toString()</code>,
 * and <code>QDSJSONWriter.value(</code>Object<code>)</code>. The
 * <code>toJSONString</code> method will be used instead of the default behavior 
 * of using the Object's <code>toString()</code> method and quoting the result.
 */
public interface QDSJSONString
{
    /**
     * The <code>toJSONString</code> method allows a class to produce its own JSON 
     * serialization. 
     * 
     * @return A strictly syntactically correct JSON text.
     */
    public String toJSONString();
}
