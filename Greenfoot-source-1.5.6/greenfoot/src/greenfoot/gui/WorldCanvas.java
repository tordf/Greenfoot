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
package greenfoot.gui;

import greenfoot.Actor;
import greenfoot.ActorVisitor;
import greenfoot.GreenfootImage;
import greenfoot.ImageVisitor;
import greenfoot.World;
import greenfoot.WorldVisitor;
import greenfoot.util.GreenfootUtil;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

/**
 * The visual representation of the world.
 * 
 * @author Poul Henriksen
 * @version $Id: WorldCanvas.java 6732 2009-09-19 07:56:11Z davmac $
 */
public class WorldCanvas extends JPanel
    implements  DropTarget, Scrollable
{
    private World world;
    private DropTarget dropTargetListener;
    /** The actor being dragged. Null if no dragging. */ 
    private Actor dragActor;
    /** The current location where the object is dragged - in pixel coordinates relative to this canvas. */
    private Point dragLocation;
    /** Image used when dragging new actors on the world. Includes the drop shadow.*/
    private BufferedImage dragImage;
    
    public WorldCanvas(World world)
    {
        setWorld(world);
        setBackground(Color.WHITE);
        setOpaque(true);
    }

    
    /**
     * Sets the world that should be visualised by this canvas.
     * Call only from the Swing event thread.
     */
    public void setWorld(World world)
    {
        this.world = world;
        if (world != null) {
            this.setSize(getPreferredSize());
            revalidate();
            repaint();
        }
        else {
            this.setSize(0, 0);
        }
    }

    /**
     * Paints all the objects.
     * 
     * Must be synchronized on the World.lock.
     */
    public void paintObjects(Graphics2D g)
    {
        Set<Actor> objects = WorldVisitor.getObjectsListInPaintOrder(world);
        int paintSeq = 0;
        for (Iterator<Actor> iter = objects.iterator(); iter.hasNext();) {
            Actor thing = iter.next();
            int cellSize = WorldVisitor.getCellSize(world);

            GreenfootImage image = ActorVisitor.getDisplayImage(thing);
            if (image != null) {
                ActorVisitor.setLastPaintSeqNum(thing, paintSeq++);

                double halfWidth = image.getWidth() / 2.;
                double halfHeight = image.getHeight() / 2.;

                AffineTransform oldTx = null;
                try {
                    double xCenter = thing.getX() * cellSize + cellSize / 2.;
                    int paintX = (int) Math.floor(xCenter - halfWidth);
                    double yCenter = thing.getY() * cellSize + cellSize / 2.;
                    int paintY = (int) Math.floor(yCenter - halfHeight);

                    if (thing.getRotation() % 360 != 0) {
                        // don't bother transforming if it is not rotated at
                        // all.
                        oldTx = g.getTransform();
                        g.rotate(Math.toRadians(thing.getRotation()), xCenter, yCenter);
                    }

                    ImageVisitor.drawImage(image, g, paintX, paintY, this, true);
                }
                catch (IllegalStateException e) {
                    // We get this if the object has been removed from the
                    // world. That can happen when interactively invoking a
                    // method that removes an object from the world, while the
                    // scenario is executing.
                }

                // Restore the old state of the graphics
                if (oldTx != null) {
                    g.setTransform(oldTx);
                }
            }
        }
    }

    public void paintComponent(Graphics g)
    {   
        if (world == null) {
            return;
        }
        
        
        // We need to sync, so that objects are not added and removed when we
        // traverse the list.
        // But, we only try to get the lock for a brief period to avoid
        // deadlocks. A deadlock could otherwise happen if a modal dialog is
        // created from the user code in one of the act() methods.
        // We could do the sync only on the paintObjects, but that would mean
        // that the background will be reset and no objects painted, resulting
        // in a slightly broken look, if the user code is sleeping (with
        // Thread.sleep).
        try {
            ReentrantReadWriteLock lock = WorldVisitor.getLock(world);
            int timeout = WorldVisitor.getReadLockTimeout(world);
            if (lock.readLock().tryLock(timeout, TimeUnit.MILLISECONDS)) {
                try {
                    Graphics2D g2 = (Graphics2D) g;
                    paintBackground(g2);
                    paintObjects(g2);
                    paintDraggedObject(g2);
                    WorldVisitor.paintDebug(world, g2);
                }
                finally {
                    lock.readLock().unlock();

                    // Wake up any threads waiting. For instance the
                    // World.repaint() call.
                    if (lock.writeLock().tryLock()) {
                        try {
                            lock.writeLock().newCondition().signalAll();
                        }
                        finally {
                            lock.writeLock().unlock();
                        }
                    }
                }
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



    /**
     * If an object is being dragged, paint it.
     */
    private void paintDraggedObject(Graphics g)
    {
        if(dragImage != null) {
            int x = (int) dragLocation.getX();
            int y = (int) dragLocation.getY();
            int xCell =  WorldVisitor.toCellFloor(world, x);
            int yCell =  WorldVisitor.toCellFloor(world, y);
            int cellSize = WorldVisitor.getCellSize(world);
            x = (int) ((xCell + 0.5) * cellSize - dragImage.getWidth()/2);
            y = (int) ((yCell + 0.5) * cellSize - dragImage.getHeight()/2);
            
            g.drawImage(dragImage, x, y, null);            
        } 
    }

    /**
     * Paint the world background. This takes tiling into account: the
     * world image is painted either once or tiled onto this component.
     */
    public void paintBackground(Graphics2D g)
    {
        if (world != null) {
            GreenfootImage backgroundImage = WorldVisitor.getBackgroundImage(world);
            if (backgroundImage != null) {
                ImageVisitor.drawImage(backgroundImage, g, 0, 0, this, true);
            }
            else {
                Color oldColor = g.getColor();
                g.setColor(getBackground());
                g.fillRect(0, 0, this.getWidth(), this.getHeight());
                g.setColor(oldColor);
            }
        }
    }

  

    public Dimension getMinimumSize()
    {
        return getPreferredSize();
    }

    public Dimension getPreferredSize()
    {
        Dimension size = new Dimension();
        if (world != null) {
            size.width = WorldVisitor.getWidthInPixels(world) ;
            size.height = WorldVisitor.getHeightInPixels(world) ;
        }
        return size;
    }
    
    
    public void setDropTargetListener(DropTarget dropTargetListener)
    {
        this.dropTargetListener = dropTargetListener;
    }
    

    public boolean drop(Object o, Point p)
    {
        clearDragInfo();
        if (dropTargetListener != null) {
            return dropTargetListener.drop(o, p);
        }
        else {
            return false;
        }

    }

    /**
     * If it is a new actor, that has not been added to the world yet, the
     * dragging is handled here.
     */
    public boolean drag(Object o, Point p)
    {
        if(o instanceof Actor && ((Actor) o).getWorld() == null) {   
            if(!getVisibleRect().contains(p)) {
                return false;
            }
            if(o != dragActor) {
                // It is the first time we are dragging this actor. Create the drag image.
                dragActor = (Actor) o;          
                dragImage = GreenfootUtil.createDragShadow(ActorVisitor.getDragImage(dragActor).getAwtImage());
            }
            dragLocation = p;
            repaint();
            return true;            
        }        
        else if (dropTargetListener != null) {
            return dropTargetListener.drag(o, p);
        }
        else {        
            return false;
        }
    }
    
    public void dragEnded(Object o)
    {
        clearDragInfo();
        if (dropTargetListener != null) {
            dropTargetListener.dragEnded(o);
        }
        
    }

    /** 
     * End the drag by setting all the drag information to null. And request repaint to update the graphics.
     */
    private void clearDragInfo()
    {
        dragLocation = null;
        dragActor = null;
        dragImage = null;
        repaint();
    }

    public Dimension getPreferredScrollableViewportSize()
    {
        return getPreferredSize();
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction)
    {
        int cellSize = world.getCellSize();
        double scrollPos = 0;
        if(orientation == SwingConstants.HORIZONTAL) {
            //scrolling left
            if(direction < 0) {
                scrollPos = visibleRect.getMinX();
               
            }
            //scrolling right
            else if (direction > 0) {
                scrollPos = visibleRect.getMaxX();
            }
        } else {
            //scrolling up
            if(direction < 0) {
                scrollPos = visibleRect.getMinY();
            }
            //scrolling down
            else if (direction > 0) {
                scrollPos = visibleRect.getMaxY();
            }
        }
        int increment = Math.abs((int) Math.IEEEremainder(scrollPos, cellSize));
        if(increment == 0) {
            increment = cellSize;
        }
      
        return  increment;
    }

    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction)
    {
         return getScrollableUnitIncrement(visibleRect, orientation, direction);
    }

    public boolean getScrollableTracksViewportWidth()
    {
        return false;
    }

    public boolean getScrollableTracksViewportHeight()
    {
        return false;
    }
 
}
