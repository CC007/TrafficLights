package com.github.cc007.trafficlights.algo.heuristic;

/*
   Statisch bibliotheek classe voor een functie die kan worden gebruikt voor een
   eventuele HEC implementatie bij een TLC, TC1TLCOpt en CBG gebruiken op dit moment deze
   classe.
*/

import com.github.cc007.trafficlights.infra.InfraException;
import com.github.cc007.trafficlights.infra.Node;
import com.github.cc007.trafficlights.algo.dp.DrivingPolicy;
import com.github.cc007.trafficlights.sim.SimModel;
import com.github.cc007.trafficlights.infra.DriveLane;
import com.github.cc007.trafficlights.infra.Roaduser;

public class HEC
{
       public static float getCongestion(Roaduser ru,DriveLane currentLane, Node currentNode)
       {
         DrivingPolicy dp = SimModel.getDrivingPolicy();
         DriveLane destLane;
         float percWaiting = 0;

         try
         {
            destLane = dp.getDirection(ru, currentLane, currentNode);
            if (destLane == null)
            {  //Edgenode, functie returns 0;
               return 1;
            }

            percWaiting = (float)destLane.getNumBlocksWaiting() / (float)destLane.getLength();

          }
         catch(InfraException e) {
           System.out.println(e.getMessage());
         }
         return (1-percWaiting);

       }

}
