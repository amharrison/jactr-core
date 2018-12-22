package org.commonreality.modalities.visual;

/*
 * default logging
 */

public class Color implements java.io.Serializable
{
  private static final long serialVersionUID = 7548054708042677361L;

//  private final int        _red;
//
//  private final int        _blue;
//
//  private final int        _green;
//
//  private final int        _alpha;
  
  private final int _value;

  public Color(int r, int g, int b, int a)
  {
//    _red =  r;
//    _blue =  b;
//    _green =  g;
//    _alpha =  a;
    _value = ((a & 0xff) << 24 | (r & 0xff)<< 16 | (g& 0xff) << 8 | (b & 0xff) << 0);
  }

  public Color(int r, int g, int b)
  {
    this(r, g, b, 255);
  }

  public Color(float r, float g, float b, float a)
  {
    this((int) (r * 255 + 0.5),(int) (g * 255 + 0.5),(int) (b * 255 + 0.5),(int) (a * 255 + 0.5));
  }

  public Color(float r, float g, float b)
  {
    this(r, g, b, 1f);
  }

  public int getRed()
  {
    return (_value >> 16) & 0xff;
  }

  public int getBlue()
  {
    return (_value >> 0 ) & 0xff;
  }

  public int getGreen()
  {
    return (_value >> 8 ) & 0xff;
  }

  public int getAlpha()
  {
    return (_value >> 24 ) & 0xff;
  }
  
  public String toString()
  {
    return "Color("+getRed()+","+getGreen()+","+getBlue()+","+getAlpha()+")";
  }

  @Override
  public int hashCode()
  {
    return _value;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    final Color other = (Color) obj;
    return _value == other._value;
  }
}
