/*
 * Title:        EdgeCloudSim - Edge Orchestrator
 * 
 * Description: 
 * EdgeOrchestrator is an abstract class which is used for selecting VM
 * for each client requests. For those who wants to add a custom 
 * Edge Orchestrator to EdgeCloudSim should extend this class and provide
 * a concrete instance via ScenarioFactory
 *               
 * License:      GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2017, Bogazici University, Istanbul, Turkey
 */

package edu.boun.edgecloudsim.edge_orchestrator;

import edu.boun.edgecloudsim.edge_server.EdgeHost;
import edu.boun.edgecloudsim.edge_server.EdgeVM;

import java.util.HashMap;
import java.util.LinkedList;

import org.cloudbus.cloudsim.Datacenter;
import edu.auburn.pFogSim.netsim.ESBModel;
import edu.auburn.pFogSim.netsim.NodeSim;
import edu.auburn.pFogSim.util.MobileDevice;
import edu.boun.edgecloudsim.core.SimManager;
import edu.boun.edgecloudsim.edge_client.Task;


/**
 * 
 * @author szs0117
 *
 */
public abstract class EdgeOrchestrator {
	protected String policy;
	protected String simScenario;
	protected Datacenter cloud;
	
	// Metrics
	protected int avgNumMessages;
	protected int avgNumProspectiveHosts;
	
	public EdgeOrchestrator(String _policy, String _simScenario){
		policy = _policy;
		simScenario = _simScenario;
		this.avgNumProspectiveHosts = 0;
		this.avgNumMessages = 0;		
	}
	
	
	/*
	 * initialize edge orchestrator if needed
	 */
	public abstract void initialize();
	
	
	/*
	 * decides where to offload
	 */
	public int getDeviceToOffload(Task task) {
		try {
			return getHost(task).getId();
		} catch (NullPointerException e) {
			return -1;
		}
	}
	
	/*
	 * returns proper VM from the related edge orchestrator point of view
	 */
	public EdgeVM getVmToOffload(Task task) {
		try {
			return (EdgeVM) getHost(task).getVmList().get(0);
		} catch (NullPointerException e) {
			return null;
		}
	}

	/**
	 * Find the Host
	 * 
	 * @param task
	 * @return
	 */
	protected EdgeHost getHost(Task task) {
		MobileDevice mb = SimManager.getInstance().getMobileDeviceManager().getMobileDevices()
				.get(task.getMobileDeviceId());
		task.setPath(mb.getPath());
		return mb.getHost();
	}
	
	// Added by sks0099@auburn.edu: diagnostic instrumentation - confirming/refuting whether goodHost()'s
	// ESBModel.findPath(EdgeHost, MobileDevice) call (a second, separate call into the same expensive
	// router.findPath() routine ESBModel.getDelay() already has its own instrumented breakdown for) accounts
	// for the ~74% of Voronoi's assignHost() wall time not explained by the existing loop-breakdown /
	// getDelay-breakdown diagnostics. Shared by every orchestrator that calls goodHost() (Voronoi, HAFA, etc.),
	// so results should be read per-sweep-log, not assumed Voronoi-specific.
	public static long totalGoodHostCalls = 0;
	public static long totalGoodHostEarlyExitNanos = 0;
	public static long totalGoodHostEarlyExitCalls = 0;
	public static long totalGoodHostFindPathNanos = 0;
	public static long totalGoodHostFindPathCalls = 0;
	public static long totalGoodHostBwLoopNanos = 0;
	public static long totalGoodHostBwLoopCalls = 0;
	public static void printGoodHostBreakdown() {
		System.out.println("---- EdgeOrchestrator.goodHost() breakdown diagnostic ----");
		System.out.println("total calls: " + totalGoodHostCalls);
		System.out.println("early-exit checks (isMIPSAvailable/isBWAvailable/isLatencySatisfactory): "
				+ totalGoodHostEarlyExitCalls + " calls, " + (totalGoodHostEarlyExitNanos / 1_000_000) + " ms");
		System.out.println("findPath(host, mb): " + totalGoodHostFindPathCalls + " calls, "
				+ (totalGoodHostFindPathNanos / 1_000_000) + " ms");
		System.out.println("path bandwidth-check loop: " + totalGoodHostBwLoopCalls + " calls, "
				+ (totalGoodHostBwLoopNanos / 1_000_000) + " ms");
		System.out.println("-----------------------------------------------------------");
	}

	/**
	 * is the host capable of servicing the task
	 * @param host
	 * @param task
	 * @return
	 */
	protected static boolean goodHost(EdgeHost host, MobileDevice mb) {
		//if (host == null)
			//return false;
		
		//System.out.print(host.getId()+" ");
		totalGoodHostCalls++;
		long __eeT0 = System.nanoTime();
		boolean __earlyExit = !host.isMIPSAvailable(mb) || !host.isBWAvailable(mb) || !host.isLatencySatisfactory(mb);
		totalGoodHostEarlyExitNanos += System.nanoTime() - __eeT0;
		totalGoodHostEarlyExitCalls++;
		if (__earlyExit) {
			return false;
		}
		long __fpT0 = System.nanoTime();
		LinkedList<NodeSim> path = ((ESBModel)SimManager.getInstance().getNetworkModel()).findPath(host, mb);
		totalGoodHostFindPathNanos += System.nanoTime() - __fpT0;
		totalGoodHostFindPathCalls++;
		long __bwT0 = System.nanoTime();
		for (NodeSim node: path) {
			EdgeHost tempHost = SimManager.getInstance().getLocalServerManager().findHostByWlanId(node.getLocation().getServingWlanId());
			if (!tempHost.isBWAvailable(mb)) {
				totalGoodHostBwLoopNanos += System.nanoTime() - __bwT0;
				totalGoodHostBwLoopCalls++;
				return false;
			}
		}
		totalGoodHostBwLoopNanos += System.nanoTime() - __bwT0;
		totalGoodHostBwLoopCalls++;
		//System.out.println(" is good.");
        //System.out.println("HostId: "+host.getId()+" is good."); // sks0099 commented on 10/04/2025
		return true;
		
	}

    protected static boolean goodHostWithoutCheckingLatency(EdgeHost host, MobileDevice mb) {
        //if (host == null)
        //return false;

        //System.out.print(host.getId()+" ");
        if (!host.isMIPSAvailable(mb) || !host.isBWAvailable(mb)) {
            return false;
        }
        LinkedList<NodeSim> path = ((ESBModel)SimManager.getInstance().getNetworkModel()).findPath(host, mb);
        for (NodeSim node: path) {
            EdgeHost tempHost = SimManager.getInstance().getLocalServerManager().findHostByWlanId(node.getLocation().getServingWlanId());
            if (!tempHost.isBWAvailable(mb)) {
                return false;
            }
        }
        //System.out.println(" is good.");
        //System.out.println("HostId: "+host.getId()+" is good."); // sks0099 commented on 10/04/2025
        return true;

    }
	
	
	/**
	 * 
	 * @param _cloud
	 */
	public void setCloud(Datacenter _cloud ) {
		cloud = _cloud;
	}

	
	/**
	 * @author Qian
	 *	@param mobile
	 */
	public abstract void assignHost(MobileDevice mobile);

	
	/**
	 * @return the policy
	 */
	public String getPolicy() {
		return policy;
	}

	
	/**
	 * @param policy the policy to set
	 */
	public void setPolicy(String policy) {
		this.policy = policy;
	}

	
	/**
	 * @return the simScenario
	 */
	public String getSimScenario() {
		return simScenario;
	}

	
	/**
	 * @param simScenario the simScenario to set
	 */
	public void setSimScenario(String simScenario) {
		this.simScenario = simScenario;
	}

	
	/**
	 * @return the cloud
	 */
	public Datacenter getCloud() {
		return cloud;
	}
		
	/**
	 * 
	 * @return
	 */
	public double getAvgNumProspectiveHosts() {
		return this.avgNumProspectiveHosts;
	}
		
	/**
	 * 
	 * @param deviceId
	 * @param msgCount
	 */
	public void addNumMessages(int deviceId, int msgCount) {
	}
	
	/**
	 * 
	 * @return
	 */
	public double getAvgNumMessages() {
		return this.avgNumMessages;
	}
	
	/**
	 * 
	 * @param deviceId
	 * @param pudCount
	 */
	public void addNumPuddlesSearched(int deviceId, int pudCount) {
	}
	
	/**
	 * 
	 * @return
	 */
	public double getAvgNumPuddlesSearched() {
		return 0.0;
	}

    public double getAvgNumHostsSearched() {
        return 0.0;
    }
	
	/**
	 * Return detailed metrics of HAFA orchestrator - Number of prospective hosts per service request (device)
	 */
	public int[] getNumProspectiveHosts() {	return null; }
	

	/**
	 * Return detailed metrics of HAFA orchestrator - Number of messages exchanged per service request (device)
	 */
	public int[] getNumMessages() {	return null; }


	/**
	 * Return detailed metrics of HAFA orchestrator - Number of Puddles searched per service request (device)
	 */
	public int[] getNumPuddlesSearched() { return null; }

    public int[] getNumHostsSearched() { return null; }

	public HashMap<MobileDevice, EdgeHost> getMobileAssignment(){ return null;}

    public int getUnassignedMobileDeviceCount() {	return 0; }

    public double getAverageHops() { return 0; }

    public double getAverageSearchHops() { return 0; }

    public int getAssignedToCloudMobileDeviceCount(){ return 0; }
}
