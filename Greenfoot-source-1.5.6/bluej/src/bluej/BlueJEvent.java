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

package bluej;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
  * Class to handle (throw and deliver) BlueJ events. Event are defined
  * for things that might be caused by low level parts of the system which
  * other parts of the system might be interested in. Objects can register
  * themselves as event listeners. They then get notified of events.
  *
  * A BlueJEvent has one argument. The argument passed differs for every 
  * event type.
  *
  * Event types and their arguments:<PRE>
  *
  *  type       argument        sent when...
  *  -----------------------------------------------------------------------
  *  CREATE_VM      (unused)        creation of VM has started
  * 
  *  CREATE_VM_FAILED (unused)      creation of VM has failed
  *
  *  CREATE_VM_DONE (unused)        creation of VM completed
  *
  *  METHOD_CALL    a String representing   an interactive method call
  *                      the call           was started or completed
  *
  *  EXECUTION_RESULT  bluej.debugmgr.ExecutionEvent   VM execution finished
  *
  *  GENERATING_DOCU    (unused)        documentation generation started
  *
  *  DOCU_GENERATED (unused)        documentation generation finished
  *
  *  DOCU_ABORTED   (unused)        documentation generation aborted
  *
  * </PRE>
  * @author Michael Kolling
  * @version $Id: BlueJEvent.java 6421 2009-07-08 04:47:39Z davmac $
  *
  */

public class BlueJEvent
{
    // BlueJ event types

    public static final int CREATE_VM           = 0;
    public static final int CREATE_VM_FAILED    = CREATE_VM + 1;
    public static final int CREATE_VM_DONE      = CREATE_VM_FAILED + 1;
    public static final int METHOD_CALL         = CREATE_VM_DONE + 1;
    public static final int METHOD_CALL_FAILED  = METHOD_CALL + 1;
    public static final int EXECUTION_RESULT    = METHOD_CALL_FAILED + 1;
    public static final int GENERATING_DOCU     = EXECUTION_RESULT + 1;
    public static final int DOCU_GENERATED      = GENERATING_DOCU + 1;
    public static final int DOCU_ABORTED        = DOCU_GENERATED + 1;
    


    // other variables

    private static List listeners = Collections.synchronizedList(new ArrayList());

    /**
     * Raise a BlueJ event with an argument. All registered listeners
     * will be informed of this event.
     */
    public static void raiseEvent(int eventId, Object arg)
    {
        Object[] listenersCopy = listeners.toArray();
        for (int i = listenersCopy.length - 1; i >= 0; i--) {
            BlueJEventListener listener = (BlueJEventListener) listenersCopy[i];
            listener.blueJEvent(eventId, arg);
        }
    }
    
    /**
     * Add a listener object. The object must implement the
     * BlueJEventListener interface.
     */
    public static void addListener(BlueJEventListener listener)
    {
        listeners.add(listener);
    }
    
    /**
     * Remove a listener object from the known listener set.
     */
    public static void removeListener(BlueJEventListener listener)
    {
        listeners.remove(listener);
    }
    
}

