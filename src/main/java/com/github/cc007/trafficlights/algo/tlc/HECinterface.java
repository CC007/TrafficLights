package com.github.cc007.trafficlights.algo.tlc;

/*
   HEC Interface, deze interface toggled bij een TLC die een implementatie
   heeft van hec, de hec versie aan of uit.

*/


import com.github.cc007.trafficlights.*;


public interface HECinterface
{
    public void setHecAddon(boolean b, Controller c);
}
