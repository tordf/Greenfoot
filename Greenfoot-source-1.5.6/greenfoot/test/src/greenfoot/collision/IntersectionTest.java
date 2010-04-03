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
package greenfoot.collision;

import greenfoot.World;
import greenfoot.TestObject;
import greenfoot.WorldCreator;
import greenfoot.core.WorldHandler;

import java.util.Collection;

import junit.framework.TestCase;

/**
 * Test for collisions between Actors
 * 
 * @author Poul Henriksen
 */
public class IntersectionTest extends TestCase
{
    private World world;

    public void testIntersectingSingleCell()
    {
        world = WorldCreator.createWorld(10, 10, 10);
        TestObject o1 = new TestObject(10,10);
        world.addObject(o1, 2, 2);
        
        TestObject o2 = new TestObject(10,10);
        world.addObject(o2, 2, 2);
        
        assertTrue(o1.intersectsP(o2));
        assertTrue(o2.intersectsP(o1));
        
        Collection c = o1.getIntersectingObjectsP(TestObject.class);
        assertTrue(c.contains(o2));
        assertEquals(1, c.size());
        
        o2.setLocation(3,2);
        assertFalse(o1.intersectsP(o2));
        assertFalse(o2.intersectsP(o1));
    }
    
    public void testIntersectingPixelLevelOdd()
    {
        world = WorldCreator.createWorld(70, 70, 1);

        WorldHandler.initialise();
        WorldHandler.getInstance().setWorld(world);
        
        TestObject o1 = new TestObject(7,7);
        world.addObject(o1, 0 ,0);
        
        TestObject o2 = new TestObject(7,7);
        world.addObject(o2, 6, 6);
        
        assertTrue(o1.intersectsP(o2));
        assertTrue(o2.intersectsP(o1));
        
        Collection c = o1.getIntersectingObjectsP(TestObject.class);
        assertTrue(c.contains(o2));
        assertEquals(1, c.size());
        
        o2.setLocation(7,7);
        assertFalse(o1.intersectsP(o2));
        assertFalse(o2.intersectsP(o1));
    }
    
    public void testIntersectingPixelLevelEven()
    {
        world = WorldCreator.createWorld(80, 80, 1);
        TestObject o1 = new TestObject(8,8);
        world.addObject(o1, 0 ,0);
        
        TestObject o2 = new TestObject(8,8);
        world.addObject(o2, 7, 7);
        
        assertTrue(o1.intersectsP(o2));
        assertTrue(o2.intersectsP(o1));
        
        Collection c = o1.getIntersectingObjectsP(TestObject.class);
        assertTrue(c.contains(o2));
        assertEquals(1, c.size());
        
        o2.setLocation(8,8);
        assertFalse(o1.intersectsP(o2));
        assertFalse(o2.intersectsP(o1));
        
        o2.setLocation(9,9);
        assertFalse(o1.intersectsP(o2));
        assertFalse(o2.intersectsP(o1));
    }
    
    public void testRotationIntersection45()
    {
        world = WorldCreator.createWorld(200, 200, 1);
        TestObject o1 = new TestObject(50,50);
        world.addObject(o1, 0 ,0);
        TestObject o2 = new TestObject(50,50);
        world.addObject(o2, 55 ,0);

        assertNull( o2.getOneIntersectingObjectP(TestObject.class));        
        o2.setRotation(45);
        assertEquals(o1, o2.getOneIntersectingObjectP(TestObject.class));
        o2.setLocation(55, 55);

        // Now the axis aligned bounding boxes will collide, so only a
        // intersection test that uses rotated bounding boxes will succeed here
        assertNull(o2.getOneIntersectingObjectP(TestObject.class));
        o1.setRotation(45);
        assertEquals(o1, o2.getOneIntersectingObjectP(TestObject.class));        
    }
    
    public void testRotationIntersection90()
    {
        world = WorldCreator.createWorld(200, 200, 1);
        TestObject o1 = new TestObject(100,10);
        world.addObject(o1, 0 ,0);
        TestObject o2 = new TestObject(10,10);
        world.addObject(o2, 0, 40);

        assertNull( o2.getOneIntersectingObjectP(TestObject.class));        
        o1.setRotation(90);
        assertEquals(o1, o2.getOneIntersectingObjectP(TestObject.class));      
    }
}