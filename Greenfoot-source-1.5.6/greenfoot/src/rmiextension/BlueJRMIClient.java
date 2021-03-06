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
package rmiextension;

import java.io.File;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Properties;

import rmiextension.wrappers.RBlueJ;
import rmiextension.wrappers.RPackage;
import rmiextension.wrappers.RProject;
import bluej.BlueJPropStringSource;
import bluej.Config;
import bluej.extensions.ProjectNotOpenException;

/**
 * The RMI client that establishes the initial connection to the BlueJ RMI server
 * 
 * @author Poul Henriksen <polle@mip.sdu.dk>
 * @version $Id: BlueJRMIClient.java 6216 2009-03-30 13:41:07Z polle $
 */
public class BlueJRMIClient implements BlueJPropStringSource
{

    RBlueJ blueJ = null;
    private static BlueJRMIClient instance;

    private RPackage pkg;

    public BlueJRMIClient(String prjDir, String rmiServiceName)
    {
        BlueJRMIServer.forceHostForServer();
        instance = this;

        try {
        	blueJ = (RBlueJ) Naming.lookup(rmiServiceName);
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        }
        catch (RemoteException e) {
            e.printStackTrace();
        }
        catch (NotBoundException e) {
            e.printStackTrace();
        }

        if (blueJ != null) {
            try {
                RProject[] openProjects = blueJ.getOpenProjects();
                RProject prj = null;
                for (int i = 0; i < openProjects.length; i++) {
                    prj = openProjects[i];
                    File passedDir = new File(prjDir);
                    if (prj.getDir().equals(passedDir)) {
                        break;
                    }
                }
                pkg = prj.getPackage("");

            }
            catch (RemoteException e1) {
                e1.printStackTrace();
            }
            catch (ProjectNotOpenException e) {
                e.printStackTrace();
            }
        }
    }

    public static BlueJRMIClient instance()
    {
        return instance;
    }

    /**
     * Returns the remote BlueJ instance.
     */
    public RBlueJ getBlueJ()
    {
        return blueJ;
    }
    
    /**
     * Returns the remote BlueJ package.
     */
    public RPackage getPackage()
    {
        return pkg;
    }
    
    // Implementation for "BlueJPropStringSource" interface
    
    public String getBlueJPropertyString(String property, String def)
    {
        try {
            String val = blueJ.getExtensionPropertyString(property, null);
            if (val == null)
                val = blueJ.getBlueJPropertyString(property, def);
            return val;
        }
        catch (RemoteException re) {
            return def;
        }
    }

    public String getLabel(String key)
    {
        return Config.getString(key, key);
    }
    
    public void setUserProperty(String property, String val)
    {
        try {
            blueJ.setExtensionPropertyString(property, val);
        }
        catch (RemoteException re) {}
    }
    
    public Properties getInitialCommandLineProperties()
    {
        try {
            return blueJ.getInitialCommandLineProperties();
        }
        catch (RemoteException e) {
            return null;
        }
    }
}