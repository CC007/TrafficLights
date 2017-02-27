/*
 * TCRL.java
 *
 * Created on January 26, 2006, 1:37 PM
 *
 */

package gld.algo.tlc;

import gld.infra.Drivelane;
import gld.infra.Infrastructure;
import gld.infra.Roaduser;
import gld.infra.Sign;

/**
 *  This is a base class for reinforcement learners.
 * @author DOAS 06
 */
public abstract class TCRL extends TLController{
    
    /** Creates a new instance of TCRL */
    public TCRL(Infrastructure infra) {
        super(infra);
    }
    
    public void updateRoaduserMove(Roaduser ru, Drivelane prevlane, Sign prevsign, int prevpos, Drivelane dlanenow, Sign signnow, int posnow, PosMov[] posMovs, Drivelane desired){
        updateRoaduserMove(ru, prevlane, prevsign, prevpos, dlanenow, signnow, posnow, posMovs, desired, 0);
    }
    
    public abstract void updateRoaduserMove(Roaduser ru, Drivelane prevlane, Sign prevsign, int prevpos, Drivelane dlanenow, Sign signnow, int posnow, PosMov[] posMovs, Drivelane desired, int penalty);
    
}
