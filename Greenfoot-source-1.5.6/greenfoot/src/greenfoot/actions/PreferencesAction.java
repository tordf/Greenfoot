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
package greenfoot.actions;

import bluej.Config;
import bluej.prefmgr.PrefMgrDialog;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;

/**
 * @author Michael Kolling
 * @version $Id: PreferencesAction.java 6216 2009-03-30 13:41:07Z polle $
 */
public class PreferencesAction extends AbstractAction
{
    private static PreferencesAction instance;
    
     /**
     * Singleton factory method for action.
     */
    public static PreferencesAction getInstance()
    {
        if(instance == null)
            instance = new PreferencesAction();
        return instance;
    }
    
    
    private PreferencesAction()
    {
        super(Config.getString("greenfoot.preferences"));
    }

    public void actionPerformed(ActionEvent e)
    {
        //PrefMgrDialog.showDialog();
        // this does currently not work. The preferences manager runs usually on the BlueJ VM,
        // and we are here on the Greenfoot VM...
        // TODO: class PrefMgr properly
    }
}