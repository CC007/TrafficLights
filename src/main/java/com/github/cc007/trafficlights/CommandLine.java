/*
 * CommandLine.java
 *
 * Created on January 27, 2006, 7:59 PM
 *
 */

package gld;

import gld.algo.dp.DPFactory;
import gld.algo.tlc.TLCFactory;
import gld.algo.tlc.TLController;
import gld.sim.SimController;
import gld.sim.SimModel;
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author DOAS 06
 */
public class CommandLine {
    
    /** Creates a new instance of CommandLine */
    public CommandLine() {
    }
    
    /** Parses command line arguments into a hash map <name(string), value(string)>
     * 
     *  Arguments are expected to be in the form "name1=value1 name2=value2".
     */
    private static HashMap processArguments(String[] args){
        HashMap arguments = new HashMap();
        
        for(int i = 0; i <  args.length; i++){
            String[] s = args[i].split("=");
            arguments.put(s[0].toLowerCase(), s[1]);
        }
        
        return arguments;
    }
    
    private static void printHelp(){
        System.err.println("This is a command line interface of the Green Light District simulator. It runs a series of experiments and exits.");
        System.err.println("Parameters are supposed to be in the form 'name=value'");
        System.err.println("The following paramters are recognized:");
        System.err.println("\tinfra=path OBLIGATORY. Path to the file with infrastructure.");
        System.err.println("\ttlc=xml_name XML name of the traffic light controller to be used.");
        System.err.println("\thec=on|off Toggle HEC add-on on/off.");
        System.out.println("\taccidents=on|off Toggle accidents on/off.");
        System.err.println("\tremove_stuck_cars=on|off Toggle removing stuck cars on/off.");
        System.err.println("\tmax_waiting_time=value Threshold for stuck cars removing.");
        System.err.println("\tpenalty=value Penalty for removed stuck car.");
        System.err.println("\trerouting=on|off Toggle rerouting on/off.");
        System.err.println("\tdriving_policy=xml_name XML name of the driving policy.");
        System.err.println("\tseries=value Number of series to be run.");
        System.err.println("\tseries_steps=value Number of cycles in one serie.");
        System.err.println();
        System.err.println("On|off values may be also set to true|false.");
        System.err.println("Unrecognized parameters are passed to the traffic light controller.");
    }
    
    /** Returns true, if the parameter is set to "true" o "on".
     */
    private static boolean isOn(String parameter){
        return parameter.equals("true") || parameter.equals("on");
    }

    public static void main(String[] args){
        SimModel simModel = new SimModel();
        SimController simController = new SimController(simModel, false);
        
        
        HashMap arguments = processArguments(args);
        
        String infra = (String) arguments.remove("infra");
        if(infra != null){
            simController.tryLoad(infra);
        } else {
            printHelp();
            System.exit(1);
        }
        
        try{
            
            String tlcName = (String) arguments.remove("tlc");
            TLController tlc = null;
            if(tlcName != null){
                TLCFactory tlcFactory = new TLCFactory(simModel.getInfrastructure());

                tlc = tlcFactory.getInstanceForLoad(
                                                tlcFactory.getNumberByXMLTagName(tlcName) );

                tlc.loadArgs(arguments);

                simModel.setTLController(tlc);
            }
            
            String hec = (String) arguments.remove("hec");
            if(hec != null){
                simController.setHecAddon(isOn(hec));
            }
            
            String accidents = (String) arguments.remove("accidents");
            if(accidents != null){
                simController.setAccidents(isOn(accidents));
            }
            
            String removeStuckCars = (String) arguments.remove("remove_stuck_cars");
            if(removeStuckCars != null){
                simController.setRemoveStuckCars(isOn(removeStuckCars), true);
            }
            
            String maxWaitingTime = (String) arguments.remove("max_waiting_time");
            if(maxWaitingTime != null){
                simController.setMaxWaitingTime(Integer.parseInt(maxWaitingTime));
            }
            
            String penalty = (String) arguments.remove("penalty");
            if(penalty != null){
                simController.setPenalty(Integer.parseInt(penalty));
            }
            
            String rerouting = (String) arguments.remove("rerouting");
            if(rerouting != null){
                simController.setRerouting(isOn(rerouting));
            }

            String drivingPolicy = (String) arguments.remove("driving_policy");
            if(drivingPolicy != null){
                DPFactory dpFactory = new DPFactory(simModel, tlc);
                simController.setDrivingPolicy(dpFactory.getNumberByXMLTagName(drivingPolicy));
            }
            
            String series = (String) arguments.remove("series");
            if(series != null){
                simModel.setNumSeries(Integer.parseInt(series));
            }
            
            String seriesSteps = (String) arguments.remove("series_steps");
            if(seriesSteps != null){
                simModel.setSeriesSteps(Integer.parseInt(seriesSteps));
            }
            
            simController.setQuitAfterSeries(true);
            

            for(Iterator it = arguments.keySet().iterator(); it.hasNext(); ){
                System.err.println("Unknown parameter '" + it.next() + "'");
            }
            
            simController.runSeries();
            
        }catch(Exception e){
            e.printStackTrace();
            System.err.println("Sorry, something bad happened.");
        }
    }
}
