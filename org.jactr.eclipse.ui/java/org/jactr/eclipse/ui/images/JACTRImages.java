/*
 * Created on Jul 8, 2004 Copyright (C) 2001-4, Anthony Harrison anh23@pitt.edu
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version. This library is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details. You should have received a copy of
 * the GNU Lesser General Public License along with this library; if not, write
 * to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 */

package org.jactr.eclipse.ui.images;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.jactr.eclipse.ui.UIPlugin;

/**
 * The images provided by the debug plugin.
 */
public class JACTRImages
{

  static public final String   BASIC_PROPERTIES = "basic.properties";

  static public final String   BASIC_FILTER     = "basic.filter";

  static public final String   CLEAR            = "clear";

  static public final String   CHUNK_TYPE       = "chunktype";

  static public final String   CHUNK            = "chunk";

  static public final String   PRODUCTION       = "production";

  static public final String   BUFFER           = "buffer";
  
  static public final String   CLOSE            = "close";

  static public final String   ADD              = "add";

  static public final String   REMOVE           = "remove";

  static public final String   MODIFY           = "modify";

  static public final String   QUERY            = "query";

  static public final String   CHECK            = "check";

  static public final String   EXTENSION        = "extension";

  static public final String   PARAMETER        = "parameter";

  static public final String   CONTAINER        = "container";

  static public final String   SCRIPT           = "script";

  static public final String   SLOT             = "slot";

  static public final String   MODEL            = "model";

  static public final String   LIBRARY          = "library";

  static public final String   OUTPUT           = "output";

  static public final String   PROJECT          = "project";

  static public final String   LOG_CLEAR        = "logClear";

  static public final String   LOG_SAVE         = "logSave";

  static public final String   LOG_FILTER       = "logFilter";

  static public final String   RUN              = "run";

  static public final String   ITERATIVE        = "iterative";

  static public final String   TOOL             = "tool";

  static public final String   SYNCH            = "synced";

  static public final String   TERIMINATE       = "terminate";

  static public final String   SUSPEND          = "suspend";

  static public final String   RESUME           = "resume";

  /**
   * The image registry containing <code>Image</code>s.
   */
  private static ImageRegistry imageRegistry;

  /**
   * A table of all the <code>ImageDescriptor</code>s.
   */
  private static Map           imageDescriptors;

  /* Declare Common paths */
  private static URL           ICON_BASE_URL    = null;

  static
  {
    String pathSuffix = "icons/full/"; //$NON-NLS-1$
    ICON_BASE_URL = UIPlugin.getDefault().getBundle().getEntry(pathSuffix);
    declareImages();
  }

  // Use IPath and toOSString to build the names to ensure they have the slashes
  // correct

  private final static String  BASIC            = "basic/";          // basic

  // colors
  // - size
  // 16x16
  // //$NON-NLS-1$

  private final static String  DISABLED         = "disabled/";       // disabled

  // - size
  // 16x16
  // //$NON-NLS-1$

  private final static String  ENABLED          = "enabled/";        // enabled

  // - size
  // 16x16
  // //$NON-NLS-1$

  /**
   * Declare all images
   */
  private static void declareImages()
  {
    // Actions
    getImageRegistry();
    // local toolbars
    declareRegistryImage(BASIC_PROPERTIES, BASIC + "prop_ps.gif"); //$NON-NLS-1$
    declareRegistryImage(BASIC_FILTER, BASIC + "filter_tsk.gif");
    declareRegistryImage(CHUNK_TYPE, BASIC + "chunktype.gif");
    declareRegistryImage(CHUNK, BASIC + "chunk.gif");
    declareRegistryImage(PRODUCTION, BASIC + "production.gif");
    declareRegistryImage(BUFFER, BASIC + "buffer.gif");
    declareRegistryImage(ADD, BASIC + "add.gif");
    declareRegistryImage(REMOVE, BASIC + "remove.gif");
    declareRegistryImage(MODIFY, BASIC + "modify.gif");
    declareRegistryImage(QUERY, BASIC + "query.gif");
    declareRegistryImage(CHECK, BASIC + "pattern.gif");
    declareRegistryImage(PARAMETER, BASIC + "parameter.gif");
    declareRegistryImage(MODEL, BASIC + "jactr.gif");
    declareRegistryImage(EXTENSION, BASIC + "extension.gif");
    declareRegistryImage(CONTAINER, BASIC + "buffer-container.gif");
    declareRegistryImage(SLOT, BASIC + "slot.gif");
    declareRegistryImage(LIBRARY, BASIC + "library.gif");
    declareRegistryImage(OUTPUT, BASIC + "output.gif");
    declareRegistryImage(PROJECT, BASIC + "jactr-project.gif");
    declareRegistryImage(CLEAR, BASIC + "disconnect_co.gif");
    declareRegistryImage(LOG_CLEAR, BASIC + "clear.gif");
    declareRegistryImage(LOG_SAVE, BASIC + "save.gif");
    declareRegistryImage(RUN, BASIC + "jactr-run.gif");
    declareRegistryImage(ITERATIVE, BASIC + "jactr-iterative.gif");
    declareRegistryImage(TOOL, BASIC + "jactr-tools.gif");
    declareRegistryImage(SYNCH, BASIC + "synced.gif");
    declareRegistryImage(TERIMINATE, BASIC + "terminate.gif");
    declareRegistryImage(CLOSE, BASIC + "close.gif");
    declareRegistryImage(SUSPEND, BASIC+"suspend.gif");
    declareRegistryImage(RESUME, BASIC + "resume.gif");

  }

  /**
   * Declare an Image in the registry table.
   * 
   * @param key
   *            The key to use when registering the image
   * @param path
   *            The path where the image can be found. This path is relative to
   *            where this plugin class is found (i.e. typically the packages
   *            directory)
   */
  private final static void declareRegistryImage(String key, String path)
  {
    ImageDescriptor desc = ImageDescriptor.getMissingImageDescriptor();
    try
    {
      desc = ImageDescriptor.createFromURL(makeIconFileURL(path));
    }
    catch (MalformedURLException me)
    {
      UIPlugin.log(me);
    }
    imageRegistry.put(key, desc);
    imageDescriptors.put(key, desc);
  }

  /**
   * Returns the ImageRegistry.
   */
  public static ImageRegistry getImageRegistry()
  {
    if (imageRegistry == null) initializeImageRegistry();
    return imageRegistry;
  }

  /**
   * Initialize the image registry by declaring all of the required graphics.
   * This involves creating JFace image descriptors describing how to
   * create/find the image should it be needed. The image is not actually
   * allocated until requested. Prefix conventions Wizard Banners WIZBAN_
   * Preference Banners PREF_BAN_ Property Page Banners PROPBAN_ Color toolbar
   * CTOOL_ Enable toolbar ETOOL_ Disable toolbar DTOOL_ Local enabled toolbar
   * ELCL_ Local Disable toolbar DLCL_ Object large OBJL_ Object small OBJS_
   * View VIEW_ Product images PROD_ Misc images MISC_ Where are the images? The
   * images (typically gifs) are found in the same location as this plugin
   * class. This may mean the same package directory as the package holding this
   * class. The images are declared using this.getClass() to ensure they are
   * looked up via this plugin class.
   * 
   * @see org.eclipse.jface.resource.ImageRegistry
   */
  public static ImageRegistry initializeImageRegistry()
  {
    imageRegistry = new ImageRegistry(UIPlugin.getStandardDisplay());
    imageDescriptors = new HashMap(30);
    declareImages();
    return imageRegistry;
  }

  /**
   * Returns the <code>Image<code> identified by the given key,
   * or <code>null</code> if it does not exist.
   */
  public static Image getImage(String key)
  {
    return getImageRegistry().get(key);
  }

  /**
   * Returns the <code>ImageDescriptor<code> identified by the given key,
   * or <code>null</code> if it does not exist.
   */
  public static ImageDescriptor getImageDescriptor(String key)
  {
    if (imageDescriptors == null) initializeImageRegistry();
    return (ImageDescriptor) imageDescriptors.get(key);
  }

  private static URL makeIconFileURL(String iconPath)
      throws MalformedURLException
  {
    if (ICON_BASE_URL == null) throw new MalformedURLException();

    return new URL(ICON_BASE_URL, iconPath);
  }
}