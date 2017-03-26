/*
 * TCRL.java
 *
 * Created on January 26, 2006, 1:37 PM
 *
 */

package com.github.cc007.trafficlights.algo.tlc;

import com.github.cc007.trafficlights.infra.DriveLane;
import com.github.cc007.trafficlights.infra.Infrastructure;
import com.github.cc007.trafficlights.infra.Roaduser;
import com.github.cc007.trafficlights.infra.Sign;

/**
 *  This is a base class for reinforcement learners.
 * @author DOAS 06
 */
public abstract class TCRL extends TLController{
    
    /** Creates a new instance of TCRL */
    public TCRL(Infrastructure infra) {
        super(infra);
    }
    
    public void updateRoaduserMove(Roaduser ru, DriveLane prevlane, Sign prevsign, int prevpos, DriveLane dlanenow, Sign signnow, int posnow, PosMov[] posMovs, DriveLane desired){
        updateRoaduserMove(ru, prevlane, prevsign, prevpos, dlanenow, signnow, posnow, posMovs, desired, 0);
    }
    
    public abstract void updateRoaduserMove(Roaduser ru, DriveLane prevlane, Sign prevsign, int prevpos, DriveLane dlanenow, Sign signnow, int posnow, PosMov[] posMovs, DriveLane desired, int penalty);
    
}
