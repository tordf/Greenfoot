/*
 This file is part of the Greenfoot program. 
 Copyright (C) 2005-2009  Poul Henriksen and Michael Kolling 
 
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
package greenfoot.localdebugger;

import greenfoot.Actor;
import greenfoot.World;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bluej.Config;
import bluej.debugger.DebuggerClass;
import bluej.debugger.DebuggerObject;
import bluej.debugger.gentype.GenTypeClass;
import bluej.debugger.gentype.JavaType;
import bluej.debugger.gentype.Reflective;
import bluej.utility.JavaNames;
import bluej.utility.JavaReflective;
import bluej.utility.JavaUtils;

import com.sun.jdi.ObjectReference;

/**
 * A class to represent a local object as a DebuggerObject
 *  
 * @author Davin McCall
 * @version $Id: LocalObject.java 6216 2009-03-30 13:41:07Z polle $
 */
public class LocalObject extends DebuggerObject
{
    // static fields
    private static Field [] noFields = new Field[0];
    
    // instance fields
    protected Object object;
    private Map genericParams = null; // Map of parameter names to types
   
    private static String[] actorIncludeFields = new String[]{"x", "y", "rotation", "image", "world"};
    private static String[] worldIncludeFields = new String[]{"width", "height", "cellSize", "backgroundImage"};
   
    
    public static LocalObject getLocalObject(Object o)
    {
        if (o != null && o.getClass().isArray()) {
            if (o instanceof boolean[]) {
                return new LocalBooleanArray((boolean []) o);
            }
            else if (o instanceof byte[]) {
                return new LocalByteArray((byte []) o);
            }
            else if (o instanceof char[]) {
                return new LocalCharArray((char []) o);
            }
            else if (o instanceof int[]) {
                return new LocalIntArray((int []) o);
            }
            else if (o instanceof long[]) {
                return new LocalLongArray((long []) o);
            }
            else if (o instanceof float[]) {
                return new LocalFloatArray((float []) o);
            }
            else if (o instanceof double[]) {
                return new LocalDoubleArray((double []) o);
            }
            
            return new LocalArray((Object []) o);
        }
        else {
            return new LocalObject(o);
        }
    }
    
    public static LocalObject getLocalObject(Object o, Map genericParams)
    {        
        if (o != null && o.getClass().isArray()) {
            if (o instanceof boolean[]) {
                return new LocalBooleanArray((boolean []) o);
            }
            else if (o instanceof byte[]) {
                return new LocalByteArray((byte []) o);
            }
            else if (o instanceof char[]) {
                return new LocalCharArray((char []) o);
            }
            else if (o instanceof int[]) {
                return new LocalIntArray((int []) o);
            }
            else if (o instanceof long[]) {
                return new LocalLongArray((long []) o);
            }
            else if (o instanceof float[]) {
                return new LocalFloatArray((float []) o);
            }
            else if (o instanceof double[]) {
                return new LocalDoubleArray((double []) o);
            }
            
            // TODO generic arrays
            return new LocalArray((Object []) o);
        }
        else {
            return new LocalObject(o, genericParams);
        }
    }
    
    /**
     * Construct a LocalObject to represent a local object as a DebuggerObject.
     * @param o  The local object to represent
     */
    protected LocalObject(Object o)
    {
        object = o;
    }
    
    /**
     * Construct a LocalObject of generic type
     * @param o   The local object to represent
     * @param genericParams  The mapping of type parameter names to types
     *                       (String to GenType).
     */
    protected LocalObject(Object o, Map genericParams)
    {
        object = o;
        this.genericParams = genericParams;
    }
    
    // hash and equality defined in terms of the underlying object
    
    public int hashCode()
    {
        return object.hashCode();
    }
    
    public boolean equals(Object other)
    {
        if (other instanceof LocalObject) {
            Object otherObj = ((LocalObject) other).object;
            return object.equals(otherObj);
        }
        return false;
    }
    
    
    /* (non-Javadoc)
     * @see bluej.debugger.DebuggerObject#getClassName()
     */
    public String getClassName()
    {
        return object.getClass().getName();
    }

    /* (non-Javadoc)
     * @see bluej.debugger.DebuggerObject#getGenClassName()
     */
    public String getGenClassName()
    {
        if (object == null)
            return "";
        if(genericParams != null)
            return new GenTypeClass(new JavaReflective(object.getClass()),
                    genericParams).toString();
        else
            return getClassName();
    }

    /* (non-Javadoc)
     * @see bluej.debugger.DebuggerObject#getStrippedGenClassName()
     */
    public String getStrippedGenClassName()
    {
        if(object == null)
            return "";
        if(genericParams != null)
            return new GenTypeClass(new JavaReflective(object.getClass()),
                    genericParams).toString(true);
        else
            return JavaNames.stripPrefix(getClassName());
    }

    /* (non-Javadoc)
     * @see bluej.debugger.DebuggerObject#getClassRef()
     */
    public DebuggerClass getClassRef()
    {
        return new LocalClass(object.getClass());
    }

    /* (non-Javadoc)
     * @see bluej.debugger.DebuggerObject#getGenType()
     */
    public GenTypeClass getGenType()
    {
        Reflective r = new JavaReflective(object.getClass());
        if(genericParams != null)
            return new GenTypeClass(r, genericParams);
        else
            return new GenTypeClass(r);
    }

    /* (non-Javadoc)
     * @see bluej.debugger.DebuggerObject#getGenericParams()
     */
    public Map getGenericParams()
    {
        Map r = null;
        if( genericParams != null ) {
            r = new HashMap();
            r.putAll(genericParams);
        }
        else if (! isRaw())
            r = new HashMap();
        return r;
    }

    /**
     * Determine whether this is a raw object. That is, an object of a class
     * which has formal type parameters, but for which no actual types have
     * been given.
     * @return  true if the object is raw, otherwise false.
     */
    private boolean isRaw()
    {
        if ((! JavaUtils.getJavaUtils().getTypeParams(object.getClass()).isEmpty())
                && genericParams == null) {
            return true;
        }
        else {
            return false;
        }
    }

    /* (non-Javadoc)
     * @see bluej.debugger.DebuggerObject#isArray()
     */
    public boolean isArray()
    {
        return object.getClass().isArray();
    }

    /* (non-Javadoc)
     * @see bluej.debugger.DebuggerObject#isNullObject()
     */
    public boolean isNullObject()
    {
        return object == null;
    }

    /**
     * Convenience method to get all fields, instance and static, public
     * and private. Fields are returned in order - first those declared in this
     * class, then the superclass, and so on.
     */
    private Field [] getAllFields()
    {
        if (object == null) {
            return noFields;
        }
        
        ArrayList allFields = new ArrayList();
        Class c = object.getClass();
        
        while (c != null) {
            Field [] declFields = c.getDeclaredFields();
            AccessibleObject.setAccessible(declFields, true);
            
            for (int j = 0; j < declFields.length; j++) {
                Field field = declFields[j];
                // Filter out some fields that we want to hide.
                if(keepField(c, field)) {
                    allFields.add(field);
                }
            }
            
            c = c.getSuperclass();
        }

        return (Field []) allFields.toArray(noFields);
    }
    
    /**
     * Convenience method to get a reference to a static field by its slot
     * number
     * 
     * @param slot   The slot number of the field to retrieve
     * @return       The requested Field
     */
    private Field getStaticFieldSlot(int slot)
    {
        Field [] fields = getAllFields();
        int staticCount = -1;
        int index = 0;
        while (staticCount != slot) {
            if ((fields[index].getModifiers() & Modifier.STATIC) != 0)
                staticCount++;
            index++;
        }
        
        return fields[index - 1];
    }
    
    /**
     * Convenience method to get a reference to an instance field by its slot
     * number
     * 
     * @param slot   The slot number of the field to retrieve
     * @return       The requested Field
     */
    private Field getInstanceFieldSlot(int slot)
    {
        Field [] fields = getAllFields();
        int instanceCount = -1;
        int index = 0;
        while (instanceCount != slot) {
            if ((fields[index].getModifiers() & Modifier.STATIC) == 0)
                instanceCount++;
            index++;
        }
        
        return fields[index - 1];
    }
    
    /* (non-Javadoc)
     * @see bluej.debugger.DebuggerObject#getStaticFieldCount()
     */
    public int getStaticFieldCount()
    {
        Field [] fields = getAllFields();
        int staticCount = 0;
        for (int i = 0; i < fields.length; i++) {
            if ((fields[i].getModifiers() & Modifier.STATIC) != 0)
                staticCount++;
        }
        
        return staticCount;
    }

    /* (non-Javadoc)
     * @see bluej.debugger.DebuggerObject#getInstanceFieldCount()
     */
    public int getInstanceFieldCount()
    {
        Field [] fields = getAllFields();
        int instanceCount = 0;
        for (int i = 0; i < fields.length; i++) {
            if ((fields[i].getModifiers() & Modifier.STATIC) == 0)
                instanceCount++;
        }
        
        return instanceCount;
    }

    /* (non-Javadoc)
     * @see bluej.debugger.DebuggerObject#getStaticFieldName(int)
     */
    public String getStaticFieldName(int slot)
    {
        return getStaticFieldSlot(slot).getName();
    }

    /* (non-Javadoc)
     * @see bluej.debugger.DebuggerObject#getInstanceFieldName(int)
     */
    public String getInstanceFieldName(int slot)
    {
        return getInstanceFieldSlot(slot).getName();
    }

    /**
     *  Return the type of the object field at 'slot'.
     *
     *@param  slot  The slot number to be checked
     *@return       The type of the field
     */
    @Override
    public String getInstanceFieldType(int slot)
    {
        Field f = getInstanceFieldSlot(slot);
        JavaType fieldType = JavaUtils.getJavaUtils().getFieldType(f);
        return fieldType.toString();
    }

    /**
     * Get an object with expected type from a field. This is used when the
     * type of the field is known to a greater extent than is represented by
     * the static type of the field.
     * 
     * @param field         The field
     * @param expectedType  The expected tyoe
     * @return a DebuggerObject representing the value and type of the field
     */
    private LocalObject getFieldObject(Field field, JavaType expectedType)
    {
        GenTypeClass expectedCtype = expectedType.asClass();
        try {
            if (expectedCtype != null && !isRaw()) {
                Object o = field.get(object);
                if (o != null) { // The return value might be null
                    Class c = o.getClass();
                    if (genericParams != null)
                        expectedType.mapTparsToTypes(genericParams);
                    GenTypeClass g = expectedCtype.mapToDerived(new JavaReflective(c));
                    Map m = g.getMap();
                    return getLocalObject(o, m);
                }
            }

            // raw
            Object o = field.get(object);
            return getLocalObject(o);
        }
        catch (IllegalAccessException iae) {
            return null;
        }
    }
    
    /* (non-Javadoc)
     * @see bluej.debugger.DebuggerObject#getStaticFieldObject(int)
     */
    public DebuggerObject getStaticFieldObject(int slot)
    {
        Field field = getStaticFieldSlot(slot);
        try {
            return getLocalObject(field.get(object));
        }
        catch (IllegalAccessException iae) {}
        return null;
    }

    /* (non-Javadoc)
     * @see bluej.debugger.DebuggerObject#getInstanceFieldObject(int)
     */
    public DebuggerObject getInstanceFieldObject(int slot)
    {
        Field field = getInstanceFieldSlot(slot);
        try {
            return getLocalObject(field.get(object));
        }
        catch (IllegalAccessException iae) {}
        return null;
    }
 
    /* (non-Javadoc)
     * @see bluej.debugger.DebuggerObject#getInstanceFieldObject(int, bluej.debugger.gentype.GenType)
     */
    public DebuggerObject getInstanceFieldObject(int slot, JavaType expectedType)
    {
        Field field = getInstanceFieldSlot(slot);
        return getFieldObject(field, expectedType);
    }

    /* (non-Javadoc)
     * @see bluej.debugger.DebuggerObject#getFieldObject(int)
     */
    public DebuggerObject getFieldObject(int slot)
    {
        Field field = getAllFields()[slot];
        try {
            return getLocalObject(field.get(object));
        }
        catch (IllegalAccessException iae) {}
        return null;
    }

    /* (non-Javadoc)
     * @see bluej.debugger.DebuggerObject#getFieldObject(int, bluej.debugger.gentype.GenType)
     */
    public DebuggerObject getFieldObject(int slot, JavaType expectedType)
    {
        Field field = getAllFields()[slot];
        return getFieldObject(field, expectedType);
    }

    /* (non-Javadoc)
     * @see bluej.debugger.DebuggerObject#getFieldObject(java.lang.String)
     */
    public DebuggerObject getFieldObject(String name)
    {
        try {
            Field field = object.getClass().getField(name);
            return getLocalObject(field.get(object));
        }
        catch (Exception exc) {
            return null;
        }
    }

    /* (non-Javadoc)
     * @see bluej.debugger.DebuggerObject#getFieldValueString(int)
     */
    public String getFieldValueString(int slot)
    {
        Field f = getAllFields()[slot];
        Object v = null;
        try {
            v = f.get(object);

            // Reference types are handled specially            
            if (! f.getType().isPrimitive()) {
                if (v == null)
                    v = Config.getString("debugger.null");
                else if (v instanceof String) {
                    return "\"" + v + "\"";
                }
                else {
                    v = OBJECT_REFERENCE;
                }
            }
        }
        catch (IllegalAccessException iae) {}
        return v.toString();
    }

    /* (non-Javadoc)
     * @see bluej.debugger.DebuggerObject#getFieldValueTypeString(int)
     */
    public String getFieldValueTypeString(int slot)
    {
        Field f = getAllFields()[slot];
        JavaType fieldType = JavaUtils.getJavaUtils().getFieldType(f);
        return fieldType.toString();
    }

    /* (non-Javadoc)
     * @see bluej.debugger.DebuggerObject#getObjectReference()
     */
    public ObjectReference getObjectReference()
    {
        // No, this implementation is not Jdi related!
        return null;
    }

    /* (non-Javadoc)
     * @see bluej.debugger.DebuggerObject#getStaticFields(boolean)
     */
    public List getStaticFields(boolean includeModifiers)
    {
        if (object == null)
            return Collections.EMPTY_LIST;
        
        return getClassRef().getStaticFields(includeModifiers);
    }

    /* (non-Javadoc)
     * @see bluej.debugger.DebuggerObject#getInstanceFields(boolean)
     */
    public List getInstanceFields(boolean includeModifiers)
    {
        List r = new ArrayList();
        Set fieldNames = new HashSet();
        
        Field [] fields = getAllFields();
        for (int i = 0; i < fields.length; i++) {
            // skip non-instance fields
            int mods = fields[i].getModifiers();
            if ((mods & Modifier.STATIC) != 0) {
                continue;
            }
            
            
            String desc = "";
            if (includeModifiers) {
                if (Modifier.isPublic(mods)) {
                    desc = "public ";
                }
                else if (Modifier.isProtected(mods)) {
                    desc = "protected ";
                }
                else if (Modifier.isPrivate(mods)) {
                    desc = "private ";
                }
            }
            
            JavaType fieldType = JavaUtils.getJavaUtils().getFieldType(fields[i]);
            GenTypeClass fieldClassType = fieldType.asClass();
            if (fieldClassType != null) {
                GenTypeClass fieldDeclType = getGenType().mapToSuper(fields[i].getDeclaringClass().getName());
                fieldType = fieldType.mapTparsToTypes(fieldDeclType.getMap());
            }
            desc += fieldType.toString(true) + " ";
            desc += fields[i].getName();
            
            // the field declaration may be hidden by a declaration in a
            // derived class
            if (! fieldNames.add(fields[i].getName())) {
                desc += " (hidden)";
            }
            
            desc += " = ";
            try {
                if (fields[i].getType().isPrimitive()) {
                    desc += fields[i].get(object);
                }
                else {
                    Object fieldval = fields[i].get(object);
                    if (fieldval instanceof String) {
                        desc += '\"' + fieldval.toString() + '\"';
                    }
                    else if (fieldval == null) {
                        desc += Config.getString("debugger.null");
                    }
                    else {
                        desc += OBJECT_REFERENCE;
                    }
                }
            }
            catch (IllegalAccessException iae) {
                desc += "?";
            }
            
            r.add(desc);
        }
        
        return r;
    }

    /* (non-Javadoc)
     * @see bluej.debugger.DebuggerObject#staticFieldIsPublic(int)
     */
    public boolean staticFieldIsPublic(int slot)
    {
        Field field = getStaticFieldSlot(slot);
        int mods = field.getModifiers();
        return Modifier.isPublic(mods);
    }

    /* (non-Javadoc)
     * @see bluej.debugger.DebuggerObject#instanceFieldIsPublic(int)
     */
    public boolean instanceFieldIsPublic(int slot)
    {
        Field field = getInstanceFieldSlot(slot);
        int mods = field.getModifiers();
        return Modifier.isPublic(mods);
    }

    /* (non-Javadoc)
     * @see bluej.debugger.DebuggerObject#staticFieldIsObject(int)
     */
    public boolean staticFieldIsObject(int slot)
    {
        Field field = getStaticFieldSlot(slot);
        return ! field.getType().isPrimitive()
            && fieldNotNull(field);
    }

    /* (non-Javadoc)
     * @see bluej.debugger.DebuggerObject#instanceFieldIsObject(int)
     */
    public boolean instanceFieldIsObject(int slot)
    {
        Field field = getInstanceFieldSlot(slot);
        return ! field.getType().isPrimitive()
            && fieldNotNull(field);
    }

    /* (non-Javadoc)
     * @see bluej.debugger.DebuggerObject#fieldIsObject(int)
     */
    public boolean fieldIsObject(int slot)
    {
        Field field = getAllFields()[slot];
        return ! field.getType().isPrimitive()
            && fieldNotNull(field);
    }

    public boolean fieldNotNull(Field field)
    {
        try {
            Object v = field.get(object);
            return v != null;
        }
        catch (IllegalAccessException iae) { return false; }
    }

    public List getAllFields(boolean includeModifiers)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    /**
     * Whether a given field should be used.
     * @return True if the field should be used, false if it should be ignored
     */
    private boolean keepField(Class cls, Field field) 
    {
        if(cls.equals(World.class)) {
            for (int i = 0; i < worldIncludeFields.length; i++) {
                String includeName = worldIncludeFields[i];
                if(includeName.equals(field.getName())) {
                    return true;
                }
            }
            return false;
        }
        else if(cls.equals(Actor.class)) {
            for (int i = 0; i < actorIncludeFields.length; i++) {
                String includeName = actorIncludeFields[i];
                if(includeName.equals(field.getName())) {
                    return true;
                }
            }
            return false;
        }
        return true;            
    }

    /**
     * Returns the object that this LocalObject represents.
     */
    public Object getObject()
    {
        return object;
    }
}
