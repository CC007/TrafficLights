
/*-----------------------------------------------------------------------
 * Copyright (C) 2001 Green Light District Team, Utrecht University 
 *
 * This program (Green Light District) is free software.
 * You may redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by
 * the Free Software Foundation (version 2 or later).
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * See the documentation of Green Light District for further information.
 *------------------------------------------------------------------------*/

package com.github.cc007.trafficlights.algo.edit;

import com.github.cc007.trafficlights.infra.*;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * This class will determine for each node which sign-configurations are possible.
 * @author Algo-group
 */
 
 /* Possible Enhancements:
  * Make the calculation work with multiple roaduser-types
  * Optimization
  * Collision-detection only works for Junctions
  */
 
public class SignConfigCalculator
{
	/**
		This method calculates all Sign configurations of a node
		@param nd The node that wants the information
	*/
	public Sign [][] calcSC(Node nd) throws InfraException
	{
		ArrayList possibleConfigs = new ArrayList();  // This list contains all posible Configurations
		Config cfg1 = null, cfg2 = null, newcfg = null;
		
		// add all possibilities of only one lane green
		DriveLaneTemp [] dls = nd.getInboundLanes();
		
		for (int i=0; i<dls.length; i++)
		{
			Config cfg = new Config(nd);
			cfg.addLane(dls[i]);
			possibleConfigs.add(cfg);
		}
		
		int numOneSign = dls.length;
		
		// Find all joined configurations, that are allowed
		for (int counter1 = 0; counter1 < possibleConfigs.size(); counter1++)
		{
			cfg1 = (Config) possibleConfigs.get(counter1);
			for (int i=0; i<numOneSign; i++)
			{
				newcfg = new Config(nd,cfg1,null);
				newcfg.addLane(dls[i]);
				if (!possibleConfigs.contains(newcfg))
					if (newcfg.configAllowed()) 
						possibleConfigs.add(newcfg);	
			}
		}
		
		// Remove all configurations that are subsets of other configs
		for (Iterator it1 = possibleConfigs.iterator(); it1.hasNext(); )
		{
			cfg1 = (Config) it1.next();
			boolean subset = false;
			for (Iterator it2=possibleConfigs.iterator(); it2.hasNext(); )			
			{
				cfg2 = (Config) it2.next();
				if (cfg1!=cfg2 && cfg1.subsetOf(cfg2))
				{
					subset = true;
					break;
				}
			}
			if (subset) it1.remove();			
		}
		
		
		// return the correct result-type
		Sign [][] result = new Sign[possibleConfigs.size()][];
		
		for (int i=0; i<possibleConfigs.size(); i++)
		{
			Config cfg = (Config) possibleConfigs.get(i);
			result[i] = cfg.getResult();			
			
		}
		return result;
	}

	
	private class Config
	{
		ArrayList green; //Indicates which inbound-lanes are green in this config
		Node nd;
		
		public Config(Node nd)
		{
			this.nd = nd;
			green = new ArrayList();
		}
	
		// this constructor joins two other configs
		public Config(Node nd, Config cfg1, Config cfg2)
		{
			Green gr;
			
			this.nd = nd;
			green = new ArrayList();
			
			
			// add elements of cfg1
			for (Iterator it=cfg1.green.iterator(); it.hasNext(); )
			{
				gr = (Green) it.next();
				green.add(gr.getClone());
			}
			
			// add elements of cfg2, if they were not already in cfg1
			if (cfg2!=null)
				for (Iterator it=cfg2.green.iterator(); it.hasNext(); )
			{
				gr = (Green) it.next();
				if (!green.contains(gr)) green.add(gr.getClone());
			}
		}

		public boolean configAllowed() throws InfraException
		{
			Green elem1, elem2;
		
			// Check each pair of configuration wheter there are collisions or not
			for (int i=0 ; i<green.size(); i++) 
			{
				elem1 = (Green) green.get(i);
				for (int f = i+1;f<green.size(); f++)
				{
					elem2 = (Green) green.get(f);
					if (!elem1.equals(elem2))
						if (collisionDetect(elem1,elem2)) return false;
				}
			}
		
		
			// When no combinations cause collisions this configuration is allowed.
			return true;  
		}
		
		private boolean collisionDetect(Green gr1, Green gr2) throws InfraException
		{
			for (int i=0; i<gr1.outlanes.length;i++)
			{
				for (int j=0; j<gr2.outlanes.length;j++)
				{
					boolean collision=false;
					DriveLaneTemp il1=gr1.inlane, il2=gr2.inlane, ol1=gr1.outlanes[i], ol2=gr2.outlanes[j];
					if (il1==null || il2==null || ol1==null || ol2==null) throw new InfraException("il/ol 1/2 shouldn't be null");
					if (il1.getRoad()==il2.getRoad()) continue;  // Buggy solution, crossing some invalid configs are valid with this.
					DriveLaneTemp [] cw = ((Junction) nd).getAllLanesCW(il1); // TO DO: it shouldn't be always Junction

					int k=0;
					while (k<cw.length)
					{
						if (cw[k]==il2) collision=!collision;
						if (cw[k]==ol1) break;
						if (cw[k]==ol2) collision=!collision;
						k++;
					}
					if (ol1==ol2) return true;
					
					if (k>=cw.length) 
					{
						System.out.println("ERROR IN getAllLanesCW in Class Junction/Node, reported by SignConfigCalculator.Config.CollisionDetect");
						return false;
					}
					if (collision) return true;					
				}
			}
			
			return false;	
		}
	

		
		public void addLane(DriveLaneTemp dl) throws InfraException
		{
			DriveLaneTemp [] leadingfr=nd.getLanesLeadingFrom(dl,0);

			Green gr = new Green(dl,leadingfr);
			if (!green.contains(gr)) green.add(gr);
			
		}
		
		// Tests equality of the object "o" and this object
		public boolean equals(Object o)
		{
			if (!(o instanceof Config)) return false;
			
			Config cfg = (Config) o;
			if (nd!=cfg.nd) return false;
			
			// if this equals cfg then all elements of this.green are equal to all elements of cfg.green,
			// that means cfg.green is a subset of this.green and andersom
			
			if (!this.subsetOf(cfg )) return false;
			if (!cfg. subsetOf(this)) return false;
					
			return true;
		}	


		// Determines wheter this configuration is a subset of the configuration "cfg"
		public boolean subsetOf(Config cfg)
		{
			Green gr1;
			for (Iterator it=green.iterator() ; it.hasNext() ; )
			{
				gr1 = (Green) it.next();
				if (!cfg.green.contains(gr1)) return false;
			}
			return true;
		}
		
		public Sign [] getResult()
		{
			Sign [] result= new Sign[green.size()];
			for (int j=0; j<green.size(); j++)
			{
				Green gr = (Green) green.get(j);
				result[j] = gr.inlane.getSign();
			}
			
			return result;
		}
		
		private class Green
		{
			DriveLaneTemp inlane;
			DriveLaneTemp [] outlanes;
			
			public Green(DriveLaneTemp inl, DriveLaneTemp [] outl)
			{
				this.inlane   = inl;
				this.outlanes = outl;
			}
			
			// Clones this object, Object.clone() doesn't work, so we'll do it this way			
			public Green getClone()
			{
				return new Green(inlane, outlanes);
			}
		
		
			// Tests equality of the object "o" and this object
			public boolean equals(Object o)
			{
				if (!(o instanceof Green)) return false;
			
				Green gr=(Green) o;
			
				if (inlane!=gr.inlane) return false;
				
				// When the inlanes are equal, the outlanes of both green should also be equal, by definition.


				return true;
			}
		}
	}
}


