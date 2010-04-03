/*
 This file is part of the BlueJ program. 
 Copyright (C) 1999-2009  Michael Kolling and John Rosenberg 
 
 This program is free software; you can redistribute it and/or 
 modify it under the terms of the GNU General Public License 
 as published by the Free Software Foundation; either version 2 
 of the License, or (at your option) any later version. 
 
 This program is distributed in the hope that it will be useful, 
 but WITHOUT ANY WARRANTY; without even the implied warranty of 
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 GNU General Public License for more details. 
 
 You should have received a copy of the GNU General Public License 
 along with this program; if not, write to the Free Software 
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA. 
 
 This file is subject to the Classpath exception as provided in the  
 LICENSE.txt file that accompanied this code.
 */
package bluej.debugmgr;


import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import bluej.views.TypeParamView;

/** 
 ** Manages an invocation history of arguments used in a package when objects 
 ** created on the ObjectBench
 **
 ** @author Bruce Quig
 **
 **/
public class CallHistory
{
    private Map objectTypes = null;
    private List objectClasses = null;
    private List objectParams = null;
    private Map typeParams = null;

    private int historyLength;

    static final int DEFAULT_LENGTH = 6;

    static final String INT_NAME = "int";
    static final String BOOLEAN_NAME = "boolean";
    static final String LONG_NAME = "long";
    static final String FLOAT_NAME = "float";
    static final String DOUBLE_NAME = "double";
    static final String SHORT_NAME = "short";
    static final String STRING_NAME = "java.lang.String"; 


    public CallHistory()
    {
        this(DEFAULT_LENGTH);
    }


    public CallHistory(int length)
    {
        historyLength = length;
        objectTypes = new HashMap(8);
        objectTypes.put(INT_NAME, new ArrayList(length));
        objectTypes.put(LONG_NAME, new ArrayList(length));
        objectTypes.put(BOOLEAN_NAME, new ArrayList(length)); 
        objectTypes.put(FLOAT_NAME, new ArrayList(length));
        objectTypes.put(DOUBLE_NAME, new ArrayList(length));
        objectTypes.put(SHORT_NAME, new ArrayList(length));
        objectTypes.put(STRING_NAME, new ArrayList(length));
        objectClasses = new ArrayList();
        objectParams = new ArrayList();
        typeParams = new HashMap();

    }


    /**
     * Gets the appropriate history for the specified data type.
     * 
     * @param objectClass
     *            the name of the object's class
     * @return the List containing the appropriate history of invocations
     */
    public List getHistory(Class objectClass)
    {
        List history = null;
        // if listed in hashtable ie primitive or String
        if( objectTypes.containsKey(objectClass.getName())) {
            history = (List)objectTypes.get(objectClass.getName());
        }
        // otherwise get general object history
        else {
            history = new ArrayList();
            for(int i = 0; i < objectClasses.size(); i++) {
                // if object parameter can be assigned from element in Class 
                // vector add to history
                if (objectClass.isAssignableFrom((Class)objectClasses.get(i)))
                    history.add(objectParams.get(i));
            }
        }
        return history;
    }


    /**
     * Gets the appropriate history for the type param
     * 
     * @param typeParam
     *            the type parameter
     * @return the List containing the appropriate history of invocations
     */
    public List getHistory(TypeParamView typeParam)
    {
        List history = (List) typeParams.get(typeParam.toString());
        return history;
    }

    public void addCall(TypeParamView typeParam, String parameter)
    {
        List history = (List) typeParams.get(typeParam.toString());
        if(history == null) {
            history = new ArrayList();
            typeParams.put(typeParam.toString(), history);
        }
        history.add(parameter);        
    }

    /**
     * Adds a call to the history of a particular datatype
     * 
     * @param objectType
     *            the object's class
     * @param argument
     *            the parameter
     */
    public void addCall(Class objectType, String argument)
    {
        if(argument != null) {
            // if a primitive or String
            if(objectTypes.containsKey(objectType.getName())) {

                List history = getHistory(objectType);
                int index = history.indexOf(argument);
    
                // if first no change
                if( index != 0) {
                    // if already there remove
                    if(index > 0)
                        history.remove(index);
                    history.add(0, argument);
                }
                // trim to size if necessary
                if(history.size() > historyLength)
                    history.remove(historyLength);
            }
            //else add to other object's class and param vectors
            else {
                int index = objectParams.indexOf(argument);
        
                // if first no change
                if( index != 0) {
                    // if already there remove
                    if(index > 0) {
                        objectParams.remove(index);
                        objectClasses.remove(index);
                    }
                    objectClasses.add(0, objectType);
                    objectParams.add(0, argument);
                }
            }
        }
    }
}
