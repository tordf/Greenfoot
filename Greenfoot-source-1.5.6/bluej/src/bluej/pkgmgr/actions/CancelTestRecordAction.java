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
package bluej.pkgmgr.actions;

import bluej.Config;
import bluej.pkgmgr.PkgMgrFrame;

/**
 * Cancel recording of a test method. Also removes from the bench objects which
 * were created since recording began.
 * 
 * @author Davin McCall
 * @version $Id: CancelTestRecordAction.java 6215 2009-03-30 13:28:25Z polle $
 */
final public class CancelTestRecordAction extends PkgMgrAction
{
    static private CancelTestRecordAction instance = null;
    
    /**
     * Factory method. This is the way to retrieve an instance of the class,
     * as the constructor is private.
     * @return an instance of the class.
     */
    static public CancelTestRecordAction getInstance()
    {
        if(instance == null)
            instance = new CancelTestRecordAction();
        return instance;
    }
    
    private CancelTestRecordAction()
    {
        super("menu.tools.cancel");
        putValue(SHORT_DESCRIPTION, Config.getString("tooltip.test.cancel"));
    }
    
    public void actionPerformed(PkgMgrFrame pmf)
    {
        pmf.doCancelTest();
    }
}
