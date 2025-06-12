package edu.auburn.pFogSim.orchestrator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Datacenter;


import edu.auburn.pFogSim.util.MobileDevice;
import edu.boun.edgecloudsim.core.SimManager;
import edu.boun.edgecloudsim.edge_orchestrator.EdgeOrchestrator;
import edu.boun.edgecloudsim.edge_server.EdgeHost;
import edu.boun.edgecloudsim.sample_voronoi_app.Point;
import edu.boun.edgecloudsim.sample_voronoi_app.Voronoi;
import edu.boun.edgecloudsim.utils.Location;

public class VoronoiOrchestrator extends EdgeOrchestrator {

    /**
     * The Voronoi Object containing the current partition
     */
    Voronoi partition;

    Map<Point, EdgeHost> pointToHostMap = new HashMap<Point, EdgeHost>();

    public VoronoiOrchestrator(String _policy, String _simScenario) {
            super(_policy, _simScenario);
            //TODO Auto-generated constructor stub
        }
    
    @Override
    public void initialize() {
        List<Point> sites = new ArrayList<Point>();

        List<EdgeHost> allHosts = new ArrayList<EdgeHost>();
		for (Datacenter node : SimManager.getInstance().getLocalServerManager().getDatacenterList()) {
			allHosts.add(((EdgeHost) node.getHostList().get(0)));
		}

        for (EdgeHost host : allHosts) {
            Location hostLocation = host.getLocation();
            Point point = new Point(hostLocation.getXPos(), hostLocation.getYPos());
            pointToHostMap.put(point, host);
            sites.add(point);
        }
        partition = new Voronoi(sites);
    }

    @Override
    public void assignHost(MobileDevice mobile) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'assignHost'");
    }
    
}
