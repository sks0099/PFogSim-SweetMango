package edu.auburn.pFogSim.orchestrator;

import java.util.*;

import edu.auburn.pFogSim.Puddle.Puddle;
import edu.auburn.pFogSim.Radix.BinaryHeap;
import edu.auburn.pFogSim.mobility.GPSVectorMobility;
import edu.auburn.pFogSim.netsim.ESBModel;
import edu.auburn.pFogSim.netsim.NodeSim;
import edu.auburn.pFogSim.util.DataInterpreter;
import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.edge_server.EdgeServerManager;
import edu.boun.edgecloudsim.utils.SimLogger;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.cloudbus.cloudsim.Datacenter;


import edu.auburn.pFogSim.util.MobileDevice;
import edu.boun.edgecloudsim.core.SimManager;
import edu.boun.edgecloudsim.edge_orchestrator.EdgeOrchestrator;
import edu.boun.edgecloudsim.edge_server.EdgeHost;
import edu.boun.edgecloudsim.sample_voronoi_app.Point;
import edu.boun.edgecloudsim.sample_voronoi_app.Voronoi;
import edu.boun.edgecloudsim.utils.Location;
import java.util.LinkedHashMap;

public class VoronoiSingleLayerOrchestrator extends EdgeOrchestrator {

    /**
     * The Voronoi Object containing the current partition
     */
    Voronoi partition;

    Map<Point, EdgeHost> pointToHostMapXY = new HashMap<Point, EdgeHost>();

    ArrayList<EdgeHost> allHosts;
    BidiMap<Integer, EdgeHost> bdEdgeHost = new DualHashBidiMap<>(); // Bidirectional map to store and manage edge host with host id.
    // Preferred hosts list contains the list of fog nodes which is used for search of fog node for host assignment.
    ArrayList<EdgeHost> preferredHosts;

    // allPreferredHosts contains the list of all the preferred fog nodes used for search of fog node for host assignment.
    ArrayList<EdgeHost> allPreferredHosts;

    List<EdgeHost>[] mipsBasedHostList;
    ArrayList<EdgeHost> preferredMipsBasedHostList;
    ArrayList<Integer> hostListIndexWiseMips;

    //ArrayList<EdgeHost> preferredHosts1;
    //ArrayList<EdgeHost> preferredHosts2;
    // Each index of preferred host corresponds to a list of fog nodes with same MIPS capacity. The lower index is
    // used for high capacity and higher index for lower capacity. Preferred host minimum index and maximum index
    // sets the limits of search of fog nodes within upper and lower bounds of MIPS capacities. If preferred host
    // index ascending boolean variable is true, search will start with higher MIPS capacity and gradually move
    // towards lower MIPS capacities. On the other hand, if preferred host index ascending boolean variable
    // is false, search will start with lower MIPS capacity and gradually move towards higher MIPS capacities.
    // Lower MIPS capacity nodes are in large number and, therefore, the chances of search are better but
    // since their capacities are smaller, they cannot process more tasks that require very high MIPS.
    Integer preferredHostMinIndex; //= 0;//0;//1;
    Integer preferredHostMaxIndex; //= 2;//1;//4;
    boolean preferredHostIndexAscending = SimSettings.getInstance().getPreferredHostIndexAscending_for_slv(); // true;
    boolean checkLatencyConditionForCloud = true;
    // The following array to hold preferredHostMaxIndex ArrayLists
    List<EdgeHost>[] preferredHostLists;// = new List[preferredHostMaxIndex+1];//-preferredHostMinIndex+1];

    // Mobile access host is the fog node of the Voronoi cell within which a mobile device lies. A mobile device must
    // connect to the network through this host. If this host has enough resources available to run the task the mobile
    // device desires, it will do so, otherwise the task will be passed on to an appropriate node for the execution.
    EdgeHost mobileAccessHost;

    // The following hash maps are used to store path table generated using Djikstra algorithm for traversing from source node
    // to destination node. In the instant orchestrator, source node is the mobile access host and destination node is the node
    // that is assigned for a mobile device before the simulation starts. If mobile access host can take care of the jobs
    // of a mobile device within its Voronoi cell, source node and destination node will be mobile access host.
    HashMap<NodeSim,HashMap<NodeSim, LinkedList<NodeSim>>> pathTable;
    HashMap<Location,HashMap<NodeSim, LinkedList<NodeSim>>> pathTableUsingLocation;
    HashMap<Integer,HashMap<NodeSim, LinkedList<NodeSim>>> pathTableUsingMobileAccessHostId;

    // Network model is the network model for the experiment. Currently, it is using the same network model
    // that is used in HAFA. In future, different network models can be tried.
    ESBModel networkModel;

    // Host_AssignedMobileCount tracks the count of mobile devices assigned to a host
    HashMap<EdgeHost, Integer> Host_AssignedMobileCount = new HashMap<EdgeHost, Integer>();

    // mobileHostAssignment stores the mobile device and its assigned host. Multiple mobile devices may be assigned to a host.
    HashMap<MobileDevice, EdgeHost> mobileHostAssignment = new HashMap<>();

    // mobileHostHop stores the number of hops in traversing Voronoi sites in searching the host from the mobile device
    HashMap<MobileDevice, Integer> mobileHostHop = new HashMap<>();

    // searchHop stores the number of hops in searching the host from the mobile device. All the nodes in traversal that
    // are not searched do not add to hop count for search
    HashMap<MobileDevice, Integer> searchHop = new HashMap<>();

    //Integer maximumMobileAssignment = 10;

    // maxNumOfMobileForHost is the maximum number of mobile devices a given host can serve. This is determined
    // on the basis of the MIPS capacity of a host and MIPS requirement of the mobile device.
    HashMap<EdgeHost, Integer> maxNumOfMobileForHost = new HashMap<EdgeHost, Integer>();

    // HostId_Latency is used during assignment of host for a given mobile device by storing host id and latency for
    // the mobile device. The stored values are used for comparison and other processings.
    HashMap<Integer, Double> HostId_Latency = new HashMap<Integer, Double>();


    // HostId_Hops is used to store number of hops from a given mobile device by storing host id and hops for
    // the mobile device. The hops represent hops traversed and not the hops during search or actual hops during
    // simulation.
    HashMap<Integer, Integer> HostId_Hops = new HashMap<Integer, Integer>();

    // HostId_SearchHops is used to store number of search hops from a given mobile device by storing host id and
    // search hops for the mobile device. The search hops represent hops traversed during search and not actual
    // hops during traversal of Voronoi site or during simulation.
    HashMap<Integer, Integer> HostId_SearchHops = new HashMap<Integer, Integer>();

    // selectHostByLeastLatency is a boolean variable which determines whether final selection of host assignment
    // should be based on least latency or not.
    // selectHostByLeastCost is a boolean variable which determines whether final selection of host assignment
    // should be based on cost or not.
    // Currently, selectHostByLeastCost is not being used independently. The program uses both the least latency
    // and the least cost using rankings in latency as well as in cost.
    Boolean selectHostByLeastLatency = true;
    Boolean selectHostByLeastCost = false;

    // debug is used to print outputs on console at various stages to facilitate debugging.
    Boolean debug = false;

    Boolean searchBadNodes = false;

    // Integer array numProspectiveHosts keeps track of number of prospective hosts during the process of assignment of
    // hosts.
    public int[] numProspectiveHosts;
    public int[] numMessages;
    public int[] numHostsSearched;
    public int[] hops;
    public int[] searchHops;

    // unassignedMobileDeviceCount keeps track of the number of mobile devices that could not be assigned a host.
    public int unassignedMobileDeviceCount;
    public int assignedToCloudMobileDeviceCount;

    public VoronoiSingleLayerOrchestrator(String _policy, String _simScenario) {
            super(_policy, _simScenario);
            //TODO Auto-generated constructor stub
            //initialize();
        }
    
    @Override
    public void initialize() {
        allHosts = new ArrayList<EdgeHost>();
        //allPreferredHosts = new ArrayList<EdgeHost>();

        hostListIndexWiseMips = new ArrayList<>();
        //mipsBasedHostList = new ArrayList<EdgeHost>();
        //preferredMipsBasedHostList = new ArrayList<>();

        //preferredHosts1 = new ArrayList<EdgeHost>();
        //preferredHosts2 = new ArrayList<EdgeHost>();
        // 2. Initialize each element of the array with a new ArrayList
        /*for (int i = 0; i < preferredHostLists.length; i++) {
            preferredHostLists[i] = new ArrayList<>();
        }*/
        Integer currentMIPS = 0;
        Integer preferredHostListsIndex = 0;
        unassignedMobileDeviceCount = 0;
        assignedToCloudMobileDeviceCount = 0;

        Integer mipsBasedIndex = -1;
        for (Datacenter node : SimManager.getInstance().getLocalServerManager().getDatacenterList()) {
            //allHosts.add(((EdgeHost) node.getHostList().get(0)));
            //bdEdgeHost.put(((EdgeHost) node.getHostList().get(0)).getId(), ((EdgeHost) node.getHostList().get(0)));
            //System.out.println("EdgeHost: Id=" + ((EdgeHost) node.getHostList().get(0)).getId() + " Total MIPS=" + ((EdgeHost) node.getHostList().get(0)).getTotalMips());
            if (((EdgeHost) node.getHostList().get(0)).getId() == 0) {
                currentMIPS = ((EdgeHost) node.getHostList().get(0)).getTotalMips();
                hostListIndexWiseMips.add(currentMIPS);
            }
            if (((EdgeHost) node.getHostList().get(0)).getId() == 1) {
                currentMIPS = ((EdgeHost) node.getHostList().get(0)).getTotalMips();
                hostListIndexWiseMips.add(currentMIPS);
            }
            if (((EdgeHost) node.getHostList().get(0)).getId() == 2) {
                currentMIPS = ((EdgeHost) node.getHostList().get(0)).getTotalMips();
                hostListIndexWiseMips.add(currentMIPS);
            }
            if(((EdgeHost) node.getHostList().get(0)).getId() > 2 &&
                    ((EdgeHost) node.getHostList().get(0)).getTotalMips() != currentMIPS){
                //preferredHostListsIndex++;
                currentMIPS = ((EdgeHost) node.getHostList().get(0)).getTotalMips();
                hostListIndexWiseMips.add(currentMIPS);
            }
        }

        mipsBasedHostList = new List[hostListIndexWiseMips.size()];
        for (int i = 0; i < (hostListIndexWiseMips.size()); i++) {
            mipsBasedHostList[i] = new ArrayList<>();
        }

        mipsBasedIndex = -1;
        for (Datacenter node : SimManager.getInstance().getLocalServerManager().getDatacenterList()) {
            allHosts.add(((EdgeHost) node.getHostList().get(0)));
            bdEdgeHost.put(((EdgeHost) node.getHostList().get(0)).getId(), ((EdgeHost) node.getHostList().get(0)));
            //System.out.println("EdgeHost: Id=" + ((EdgeHost) node.getHostList().get(0)).getId() + " Total MIPS=" + ((EdgeHost) node.getHostList().get(0)).getTotalMips());
            /*if (((EdgeHost) node.getHostList().get(0)).getId() == 0) {
                currentMIPS = ((EdgeHost) node.getHostList().get(0)).getTotalMips();
                hostListIndexWiseMips.add(currentMIPS);
            }
            if (((EdgeHost) node.getHostList().get(0)).getId() == 1) {
                currentMIPS = ((EdgeHost) node.getHostList().get(0)).getTotalMips();
                hostListIndexWiseMips.add(currentMIPS);
            }*/
            if(((EdgeHost) node.getHostList().get(0)).getId() == 0) {
                currentMIPS = ((EdgeHost) node.getHostList().get(0)).getTotalMips();
                //hostListIndexWiseMips.add(currentMIPS);
                mipsBasedIndex++;
            }
            if(((EdgeHost) node.getHostList().get(0)).getId() > -1 &&
                    ((EdgeHost) node.getHostList().get(0)).getTotalMips() != currentMIPS){
                //preferredHostListsIndex++;
                mipsBasedIndex++;
                currentMIPS = ((EdgeHost) node.getHostList().get(0)).getTotalMips();
            }
            /*if(preferredHostListsIndex < preferredHostMaxIndex && preferredHostListsIndex > preferredHostMinIndex) {
                if (((EdgeHost) node.getHostList().get(0)).getTotalMips() == currentMIPS) {
                    preferredHostLists[preferredHostListsIndex].add(((EdgeHost) node.getHostList().get(0)));
                }
            }*/
            /*if(preferredHostListsIndex < preferredHostLists.length) {
                if (((EdgeHost) node.getHostList().get(0)).getTotalMips() == currentMIPS) {
                    //preferredHostLists[preferredHostListsIndex].add(((EdgeHost) node.getHostList().get(0)));
                    allPreferredHosts.add(((EdgeHost) node.getHostList().get(0)));
                }
            }*/

            if (((EdgeHost) node.getHostList().get(0)).getTotalMips() == currentMIPS) {
                mipsBasedHostList[mipsBasedIndex].add(((EdgeHost) node.getHostList().get(0)));
                //allPreferredHosts.add(((EdgeHost) node.getHostList().get(0)));
            }
            /*if(((EdgeHost) node.getHostList().get(0)).getId() > 0 &&
                    ((EdgeHost) node.getHostList().get(0)).getId() < 33) { //142 //62 //12 //33 works well
                preferredHostLists[0].add(((EdgeHost) node.getHostList().get(0)));
                //System.out.println("EdgeHost: Id=" + ((EdgeHost) node.getHostList().get(0)).getId() + " Total MIPS=" + ((EdgeHost) node.getHostList().get(0)).getTotalMips());
            }else if(((EdgeHost) node.getHostList().get(0)).getId() > 32 &&
                    ((EdgeHost) node.getHostList().get(0)).getId() < 63) { //142 //62 //12 //33 works well
                preferredHosts2.add(((EdgeHost) node.getHostList().get(0)));
                //System.out.println("EdgeHost: Id=" + ((EdgeHost) node.getHostList().get(0)).getId() + " Total MIPS=" + ((EdgeHost) node.getHostList().get(0)).getTotalMips());
            }else if(((EdgeHost) node.getHostList().get(0)).getId() > 63 &&
                    ((EdgeHost) node.getHostList().get(0)).getId() < 142) { //142 //62 //12 //33 works well
                preferredHosts3.add(((EdgeHost) node.getHostList().get(0)));
                //System.out.println("EdgeHost: Id=" + ((EdgeHost) node.getHostList().get(0)).getId() + " Total MIPS=" + ((EdgeHost) node.getHostList().get(0)).getTotalMips());
            }*/
        }

        /*if(debug) {
            for (int i = 0; i < preferredHostLists.length; i++) {
                System.out.print("List " + i + ": ");
                for (EdgeHost item : preferredHostLists[i]) {
                    System.out.print(item.getId() + ": " + item.getTotalMips() + " ");
                }
                System.out.println();
            }
        }*/
        //System.exit(57568);
        pathTable = new HashMap<>();
        pathTableUsingLocation = new HashMap<>();
        pathTableUsingMobileAccessHostId = new HashMap<>();
        networkModel = (ESBModel)(SimManager.getInstance().getNetworkModel());
        // The following nested for loop creates pathTable from each source host to destination host.
        // A mobile device gets connected to the source host which the fog node in the Voronoi cell in which
        // the mobile device lies
        /*for (NodeSim srcHost: networkModel.getNetworkTopology().getNodes()) { // srcHost is the source host
            HashMap<NodeSim, LinkedList<NodeSim>> tempMap = new HashMap<>();
            for (NodeSim desHost: networkModel.getNetworkTopology().getNodes()) {
                LinkedList<NodeSim> tempList = new LinkedList<>();
                tempList = networkModel.findPath(srcHost, desHost); // desHost is the destination host
                tempMap.put(desHost, tempList);
            }
            pathTable.put(new NodeSim(srcHost.getLocation().getXPos(),srcHost.getLocation().getYPos()), tempMap);
        }*/

        /*List<Point> sites = new ArrayList<Point>();

        List<EdgeHost> allHosts = new ArrayList<EdgeHost>();
        EdgeServerManager esm = new EdgeServerManager();
        try {
            esm.startDatacenters();
        }catch(Exception e){

        }
        allHosts = esm.getHostList();*/
		/*for (Datacenter node : SimManager.getInstance().getLocalServerManager().getDatacenterList()) {
			allHosts.add(((EdgeHost) node.getHostList().get(0)));
		}*/

        /*for (EdgeHost host : allHosts) {
            Location hostLocation = host.getLocation();
            Point point = new Point(hostLocation.getXPos(), hostLocation.getYPos());
            pointToHostMap.put(point, host);
            sites.add(point);
        }*/
        //String  fogNodeFileName = "fogNode_loc.json";
        //partition = new Voronoi(fogNodeFileName);

        System.out.println("allHosts size = "+allHosts.size());
        /*List<EdgeHost> allHosts = new ArrayList<EdgeHost>();
        EdgeServerManager esm = new EdgeServerManager();
        List<Datacenter> datacenterList = esm.getLocalDatacenters();//.getDatacenterList();
        System.out.println("datacenterList size = "+datacenterList.size());

        datacenterList = esm.getDatacenterList();
        System.out.println("datacenterList size = "+datacenterList.size());
        System.exit(8797);
        allHosts = esm.getHostList();*/
        for (EdgeHost host : allHosts) {
            Location hostLocation = host.getLocation();
            Point point = new Point(hostLocation.getXPos(), hostLocation.getYPos());
            pointToHostMapXY.put(point.convertLongLatPointToXYCoordinates(), host);
            //sites.add(point);
        }
        partition = new Voronoi();

        // Initialize metrics
        /*int fogNodeCount = partition.fogNodesLongLat.size() ;//SimManager.getInstance().getNumOfMobileDevice();
        numProspectiveHosts = new int[fogNodeCount];
        numMessages = new int[fogNodeCount];
        //numPuddlesSearched = new int[devCount];

        for (int i=0; i<fogNodeCount; i++) {
            this.numProspectiveHosts[i] = 0;
            this.numMessages[i] = 0;
            //this.numPuddlesSearched[i] = 0;
        }*/

        int devCount = SimManager.getInstance().getNumOfMobileDevice();
        numProspectiveHosts = new int[devCount];
        numMessages = new int[devCount];
        numHostsSearched = new int[devCount];
        hops = new int[devCount];
        searchHops = new int[devCount];

        for (int i=0; i<devCount; i++) {
            this.numProspectiveHosts[i] = 0;
            this.numMessages[i] = 0;
            this.numHostsSearched[i] = 0;
            this.hops[i] = 0;
            this.searchHops[i] = 0;
        }
    }

    private void populatePrferredHostList(Integer preferredHostMinIndex, Integer preferredHostMaxIndex) {
        preferredHostLists = new List[preferredHostMaxIndex+1];
        allPreferredHosts = new ArrayList<EdgeHost>();
        for (int preferredHostListsIndex = 0; preferredHostListsIndex < (preferredHostMaxIndex+1); preferredHostListsIndex++) {
            preferredHostLists[preferredHostListsIndex] = mipsBasedHostList[preferredHostListsIndex];
            allPreferredHosts.addAll(preferredHostLists[preferredHostListsIndex]);
        }
    }

    /**
     *
     * @param deviceId
     * @param hostCount
     */
    public void addNumProspectiveHosts(int deviceId, int hostCount) {
        this.numProspectiveHosts[deviceId] += hostCount;
    }


    /**
     *
     * @return
     */
    @Override
    public double getAvgNumProspectiveHosts() {
        int devCount = SimManager.getInstance().getNumOfMobileDevice();
        int assignedDevCount = 0;
        int totalNumProspectiveHosts = 0;

        for (int i=0; i<devCount; i++) {
            MobileDevice mb = SimManager.getInstance().getMobileDeviceManager().getMobileDevices().get(i);
            if (mb.getHost() != null) {
                assignedDevCount++;
                totalNumProspectiveHosts += this.numProspectiveHosts[i];
            }
        }

        return ((double)totalNumProspectiveHosts / assignedDevCount );
    }

    public int[] getNumMessages() {
        return this.numMessages;
    }

    public int[] getHops() {
        return this.hops;
    }

    public int[] getSearchHops() {
        return this.searchHops;
    }

    private void populateMaxMobileForHost(MobileDevice mobile){
        maxNumOfMobileForHost.clear();
        Location mobileLocation = mobile.getLocation();
        double mobileLocationX = mobileLocation.getXPos();
        double mobileLocationY = mobileLocation.getYPos();
        Point mobilePoint = new Point(mobileLocationX, mobileLocationY);

        Point hostPoint = partition.getHostXY(mobilePoint);
        EdgeHost nearest = pointToHostMapXY.get(hostPoint);

        HashMap<Point, List<Point>> myVoronoiNeighborSiteMap = partition.getVoronoiNeighborSiteMap();
        List<Point> myVoronoiNeighborsOfNearest = myVoronoiNeighborSiteMap.get(new Point(nearest.getLocation().getXPos(), nearest.getLocation().getYPos()));
        for (Point p : myVoronoiNeighborsOfNearest) {
            //System.out.println("Processing node with coordinates: (" + p.getX() + ", " + p.getY() + ")");
            EdgeHost pEdgeHost = pointToHostMapXY.get(p.convertLongLatPointToXYCoordinates());
            //System.out.println("pEdgeHost.getTotalMips(): "+pEdgeHost.getTotalMips());
            //Integer maxMobileForHost = (int)(pEdgeHost.getTotalMips()/(2 * mobile.getTaskLengthRequirement()));
            Integer maxMobileForHost = (int)(pEdgeHost.getAvailableMips()/(3.0 * mobile.getTaskLengthRequirement()));
            maxNumOfMobileForHost.put(pEdgeHost, maxMobileForHost);
        }
        /*for (EdgeHost eh: maxNumOfMobileForHost.keySet()) {
            String key = eh.toString();
            String value = maxNumOfMobileForHost.get(key).toString();
            System.out.println(key + ": " + value);
        }*/
        maxNumOfMobileForHost.forEach((key, value) -> System.out.println(key.getId() + ": " + value));
    }

    /*static <K,V extends Comparable<? super V>>
    SortedSet<Map.Entry<K,V>> entriesSortedByValues(Map<K,V> map) {
        SortedSet<Map.Entry<K,V>> sortedEntries = new TreeSet<Map.Entry<K,V>>(
                new Comparator<Map.Entry<K,V>>() {
                    @Override public double compare(Map.Entry<K,V> e1, Map.Entry<K,V> e2) {
                        int res = e1.getValue().compareTo(e2.getValue());
                        return res != 0 ? res : 1;
                    }
                }
        );
        sortedEntries.addAll(map.entrySet());
        return sortedEntries;
    }*/

    private ArrayList<EdgeHost> getProspectiveNodes(Integer preferredHostListsIndex, MobileDevice mobile){
        //ArrayList<EdgeHost> retProspectiveNodes = new ArrayList<>();

        Location mobileLocation = mobile.getLocation();
        double mobileLocationX = mobileLocation.getXPos();
        double mobileLocationY = mobileLocation.getYPos();
        Point mobilePoint = new Point(mobileLocationX, mobileLocationY);

        preferredHosts = new ArrayList<>();
        preferredHosts.addAll(preferredHostLists[preferredHostListsIndex]);
        //Integer maxProspectiveNodeCount = (int)(((preferredHosts.size())*1.0)+1);//(int)((preferredHosts.size()*0.1))+1;//10;
        //Integer maxProspectiveNodeCount = (int)(((preferredHosts.size())*0.1))+1;//(int)((preferredHosts.size()*0.1))+1;//10;
        Integer maxProspectiveNodeCount = preferredHosts.size();
        Double latencySafetyFactor = 1.0;
        Double mobileTaskLengthRequirementMultiplier = 1.0;
        Double reserveMultiplier = 1.0;
        //populateMaxMobileForHost(mobile);

        /*switch(preferredHostListsIndex){
            case 3:
                latencySafetyFactor = 0.75;
                break;
            case 4:
                latencySafetyFactor = 0.5;
                break;
            default:
                latencySafetyFactor = 1.0;
        }*/

        //ArrayList<EdgeHost> prospectiveNodes = getProspectiveNodes(mobile, maxProspectiveNodeCount);
        ArrayList<EdgeHost> prospectiveNodes = new ArrayList<EdgeHost>();
        ArrayList<EdgeHost> badNodes = new ArrayList<EdgeHost>();
        ArrayList<EdgeHost> NeighborsOfNodesAddedToProspectiveNodes = new ArrayList<EdgeHost>();

        Point hostPoint = partition.getHostXY(mobilePoint);
        EdgeHost nearest = pointToHostMapXY.get(hostPoint);
        mobileAccessHost = nearest;

        HashMap<Point, List<Point>> myVoronoiNeighborSiteMap = partition.getVoronoiNeighborSiteMap();
        double hostProcessingDelay = 0.0;
        double hostProcessingDelayUsingAvailableMIPS = 0.0;
        double acceptableLatency = 0.0;//mobile.getLatencyRequirement();
        double hostNetworkDelay = 0.0;
        double totalDelay = 0.0;
        Integer preferredHostsCheckedCount = 0;

        NeighborsOfNodesAddedToProspectiveNodes.add(mobileAccessHost);

        //System.exit(8686899);
        HostId_Latency = new HashMap<Integer, Double>();
        EdgeHost selHostConsideringLatencyOnly = null;
        System.out.print("Prospective node processing hostIds for mobile device Id: "+mobile.getId()+
                " preferredHostListsIndex: "+preferredHostListsIndex+"\n");

        while(prospectiveNodes.size() < maxProspectiveNodeCount && preferredHosts.size() > preferredHostsCheckedCount){
            //System.out.println("myVoronoiNeighborSiteMap size = " + myVoronoiNeighborSiteMap.size());
            List<Point> myVoronoiNeighborsOfNearest = myVoronoiNeighborSiteMap.get(new Point(NeighborsOfNodesAddedToProspectiveNodes.get(0).getLocation().getXPos(),
                    NeighborsOfNodesAddedToProspectiveNodes.get(0).getLocation().getYPos()));
            //Add hop values with host id
            Integer HopsFromMobileDevice = HostId_Hops.get(NeighborsOfNodesAddedToProspectiveNodes.get(0).getId());
            Integer SearchHopsFromMobileDevice = HostId_SearchHops.get(NeighborsOfNodesAddedToProspectiveNodes.get(0).getId());
            //System.out.println("Hops of the nearest from the mobile device: "+HopsFromMobileDevice);
            for(Point p : myVoronoiNeighborsOfNearest){
                EdgeHost pEdgeHost = pointToHostMapXY.get(p.convertLongLatPointToXYCoordinates());
                /*if(pEdgeHost.getId() == 0){
                    System.out.println("");
                }*/
                if(!HostId_Hops.containsKey(pEdgeHost)) {
                    HostId_Hops.put(pEdgeHost.getId(), (HopsFromMobileDevice+1));
                    if(debug) {
                        System.out.println("Hops of Host Id:"+pEdgeHost.getId()+" from the mobile device: "+(HopsFromMobileDevice+1));
                    }
                }
                if(!HostId_SearchHops.containsKey(pEdgeHost)){
                    //if(allPreferredHosts.contains(pEdgeHost)) {
                    if(preferredHosts.contains(pEdgeHost)) {
                        HostId_SearchHops.put(pEdgeHost.getId(), (SearchHopsFromMobileDevice + 1));
                        if(debug) {
                            System.out.println("Search Hops of Host Id:" + pEdgeHost.getId() + " from the mobile device: " + (HopsFromMobileDevice + 1));
                        }
                    }else{
                        HostId_SearchHops.put(pEdgeHost.getId(), (SearchHopsFromMobileDevice));
                        if(debug) {
                            System.out.println("Search Hops of Host Id:" + pEdgeHost.getId() + " from the mobile device: " + (HopsFromMobileDevice + 1));
                        }
                    }
                }
            }
            //System.exit(878686);
            NeighborsOfNodesAddedToProspectiveNodes.remove(NeighborsOfNodesAddedToProspectiveNodes.get(0));
            //populateMaxMobileForHost();
            Double consumerDistance = 0.0;
            for (Point p : myVoronoiNeighborsOfNearest) {
                EdgeHost pEdgeHost = pointToHostMapXY.get(p.convertLongLatPointToXYCoordinates());
                if(!NeighborsOfNodesAddedToProspectiveNodes.contains(pEdgeHost)) NeighborsOfNodesAddedToProspectiveNodes.add(pEdgeHost);
                if(!prospectiveNodes.contains(pEdgeHost) && preferredHosts.contains(pEdgeHost)){
                    preferredHostsCheckedCount++;
                    //nearest = pEdgeHost;
                    this.addNumMessages(mobile.getId(), 1);

                    //System.out.println("Prospective node processing for neighbor with coordinates: (" + p.getX() + ", " + p.getY() + ")");
                    //System.out.println("Prospective node processing hostId: "+pEdgeHost.getId());
                    //System.out.print(pEdgeHost.getId()+", ");
                    if(!maxNumOfMobileForHost.containsKey(pEdgeHost)){
                        Integer maxMobileForHost = (int)(pEdgeHost.getTotalMips()/(mobileTaskLengthRequirementMultiplier * mobile.getTaskLengthRequirement()));
                        maxNumOfMobileForHost.put(pEdgeHost, maxMobileForHost);
                    }
                    if (pEdgeHost.isMIPSCapacitySufficient(mobile) &&
                            (!Host_AssignedMobileCount.containsKey(pEdgeHost) ||
                                    (Host_AssignedMobileCount.containsKey(pEdgeHost) &&
                                            Host_AssignedMobileCount.get(pEdgeHost) < maxNumOfMobileForHost.get(pEdgeHost)))) {
                        this.addNumHostsSearched(mobile.getId(), 1);
                        //Host_AssignedMobileCount.get(pEdgeHost) < maximumMobileAssignment))) {
                        hostProcessingDelay = (mobile.getTaskLengthRequirement()) / pEdgeHost.getVmScheduler().getPeCapacity();
                        hostProcessingDelayUsingAvailableMIPS =
                                (mobile.getTaskLengthRequirement()) / (1.0*(pEdgeHost.getTotalMips()-pEdgeHost.getReserveMips()));

                        acceptableLatency = mobile.getLatencyRequirement();
                        hostNetworkDelay = ((ESBModel)SimManager.getInstance().getNetworkModel()).getDelay(mobile.getLocation(), pEdgeHost.getLocation());

                        //Consider round trip latency - assuming user is co-located with device, in current test environment.
                        hostNetworkDelay += hostNetworkDelay;
                        if(hostProcessingDelayUsingAvailableMIPS > hostProcessingDelay){
                            //System.out.println("fhfty4223");
                            totalDelay = hostProcessingDelayUsingAvailableMIPS + hostNetworkDelay;
                        }else {
                            totalDelay = hostProcessingDelay + hostNetworkDelay;
                        }
                        if(preferredHostListsIndex > 0) {
                            if (totalDelay < latencySafetyFactor * acceptableLatency) {
                                //System.out.println();
                                //return true;
                                //prospectiveNodes.add(pointToHostMapXY.get(hostPoint.convertLongLatPointToXYCoordinates()));
                                prospectiveNodes.add(pEdgeHost);
                                HostId_Latency.put(pEdgeHost.getId(), totalDelay);
//                            System.out.println("HostId: "+pEdgeHost.getId()+" Estimated latency: "+totalDelay);
                            }
                        }else{
                            if(checkLatencyConditionForCloud){
                                if (totalDelay < latencySafetyFactor * acceptableLatency) {
                                    //System.out.println();
                                    //return true;
                                    //prospectiveNodes.add(pointToHostMapXY.get(hostPoint.convertLongLatPointToXYCoordinates()));
                                    prospectiveNodes.add(pEdgeHost);
                                    HostId_Latency.put(pEdgeHost.getId(), totalDelay);
//                            System.out.println("HostId: "+pEdgeHost.getId()+" Estimated latency: "+totalDelay);
                                }
                            }else {
                                prospectiveNodes.add(pEdgeHost);
                                HostId_Latency.put(pEdgeHost.getId(), totalDelay);
                            }
                        }
                        //prospectiveNodes.add(pointToHostMapXY.get(p.convertLongLatPointToXYCoordinates()));
                        //nearest = pointToHostMapXY.get(p.convertLongLatPointToXYCoordinates());
                        //break;
                    }
                }
            }
            //System.out.println("");
        }

        return prospectiveNodes;
    }

    private Map<Integer, Integer> getRank(Map<Integer, Double> inputMap){
        Map<Integer, Integer> assignedRank = new LinkedHashMap<>();
        Integer currentRank = 1;
        Double currentValue = 0.0;
        Double previousValue = 0.0;
        Integer elementCount = 0;
        Integer tieCount = 0;
        for (Map.Entry<Integer, Double> entry : inputMap.entrySet()) {
            //System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
            if(elementCount == 0){
                assignedRank.put(entry.getKey(), currentRank);
                currentValue = entry.getValue();
                tieCount = 0;
            }else{
                previousValue = currentValue;
                currentValue = entry.getValue();
                if(currentValue.equals(previousValue)){
                    tieCount++;
                }else{
                    currentRank += tieCount;
                    currentRank++;
                    tieCount = 0;
                }
                assignedRank.put(entry.getKey(), currentRank);
            }
            elementCount++;
        }
        return assignedRank;
    }

    private ArrayList<EdgeHost> getGoodNodes(ArrayList<EdgeHost> prospectiveNodes, MobileDevice mobile){
        ArrayList<EdgeHost> goodNodes = new ArrayList<>();
        ArrayList<EdgeHost> badNodes = new ArrayList<>();
        int prospectiveNodesSize = prospectiveNodes.size();
        int nodeCheckedIndex = 0;
        while (prospectiveNodes.size() != 0 && nodeCheckedIndex < prospectiveNodesSize) {
            this.addNumProspectiveHosts(mobile.getId(), prospectiveNodes.size());
            BinaryHeap sort = new BinaryHeap(prospectiveNodes.size(), mobile.getLocation(), prospectiveNodes);
            LinkedList<EdgeHost> nodes = sort.sortNodes();

            //BinaryHeap sort = new BinaryHeap(prospectiveNodes.size(), mobile, prospectiveNodes);
            //LinkedList<EdgeHost> nodes = sort.sortNodesByCostPerSec();

            //BinaryHeap sort = new BinaryHeap(prospectiveNodes.size(), mobile, prospectiveNodes);
            //LinkedList<EdgeHost> nodes = sort.sortNodesByMIPS(); // Sorting is in descending order

            EdgeHost prosHost = nodes.poll();
            nodeCheckedIndex++;
            // Find the nearest node capable of hosting the application.
            /*while (!goodHost(prosHost, mobile)) {
                // if mobile flag is not due-to-latency --> set latencylimitflag to 0;
                badNodes.add(prosHost);
                prosHost = nodes.poll();
                if (prosHost == null) {
                    break;
                }
            }*/

            while(prosHost != null){
                if(goodHost(prosHost, mobile)){
                    goodNodes.add(prosHost);
                }else{
                    badNodes.add(prosHost);
                }
                prosHost = nodes.poll();
                nodeCheckedIndex++;
            }
            /*if (prosHost != null) {
                // Good host found for current fog layer. Save it and initiate search for next higher layer.
                goodNodes.add(prosHost);
                break;
                //nearest = prosHost;
            }*/
            // if latencylimitflag is 1 --> all prospective nodes has sufficient mips, but failed due to latency;
            // give just one more chance to expand search, beyond which we assume the nodes are too far anyway ( heuristic) & may have higher latency
            // if latencylimitflag = 2; then print latency unacceptable for this layer; and break
            // else set latencylimitflag ++ i.e. set to 2 i.e. give a second chance to expand search; or set to 1 to restart the racking prcess.
            // and continue executing the fllowing code..

            // Search unsuccessful, hence expand search to neighbors using parent subtree.
            // Get list of other prospective nodes belonging to this layer in the parent subtree
            //prospectiveNodes = SimManager.getInstance().getEdgeServerManager().getCousins(pud, levelIter, mobile.getId());


//            if(searchBadNodes){
//                // If the search with the existing prospective nodes fails, include neighboring nodes
//                if (prosHost == null && goodNodes.size() == 0) {
//                    List<Point> myVoronoiNeighborsOfNearest = myVoronoiNeighborSiteMap.get(new Point(badNodes.get(0).getLocation().getXPos(), nearest.getLocation().getYPos()));
//                    NeighborsOfNodesAddedToProspectiveNodes.add(badNodes.get(0));
//                    if(myVoronoiNeighborsOfNearest != null){
//                        for (Point p : myVoronoiNeighborsOfNearest) {
//                            //System.out.println("Point: (" + p.getX() + ", " + p.getY() + ")");
//                            EdgeHost pEdgeHost = pointToHostMapXY.get(p.convertLongLatPointToXYCoordinates());
//                            if(!badNodes.contains(pEdgeHost) && !NeighborsOfNodesAddedToProspectiveNodes.contains(pEdgeHost) &&
//                                    !prospectiveNodes.contains(pEdgeHost) &&
//                                    preferredHosts.contains(pEdgeHost) &&
//                                    (!Host_AssignedMobileCount.containsKey(pEdgeHost) ||
//                                            (Host_AssignedMobileCount.containsKey(pEdgeHost) &&
//                                                    Host_AssignedMobileCount.get(pEdgeHost) < maxNumOfMobileForHost.get(pEdgeHost)))){
//                                //Host_AssignedMobileCount.get(pEdgeHost) < maximumMobileAssignment))){
//                                if (pEdgeHost.isMIPSCapacitySufficient(mobile)) {
//                                    hostProcessingDelay = (mobile.getTaskLengthRequirement()) / pEdgeHost.getVmScheduler().getPeCapacity();
//                                    hostProcessingDelayUsingAvailableMIPS = (mobile.getTaskLengthRequirement()) / pEdgeHost.getAvailableMips();
//                                    if(hostProcessingDelayUsingAvailableMIPS > hostProcessingDelay){
//                                        System.out.println("dhdry35534636");
//                                    }
//                                    acceptableLatency = mobile.getLatencyRequirement();
//                                    hostNetworkDelay = ((ESBModel) SimManager.getInstance().getNetworkModel()).getDelay(mobile.getLocation(), pEdgeHost.getLocation());
//
//                                    //Consider round trip latency - assuming user is co-located with device, in current test environment.
//                                    hostNetworkDelay += hostNetworkDelay;
//
//                                    totalDelay = hostProcessingDelay + hostNetworkDelay;
//                                    if (totalDelay < latencySafetyFactor * acceptableLatency) {
//                                        //System.out.println();
//                                        //return true;
//                                        //prospectiveNodes.add(pointToHostMapXY.get(hostPoint.convertLongLatPointToXYCoordinates()));
//                                        prospectiveNodes.add(pEdgeHost);
//
//                                    }
//                                    //prospectiveNodes.add(pointToHostMapXY.get(p.convertLongLatPointToXYCoordinates()));
//                                    //nearest = pointToHostMapXY.get(p.convertLongLatPointToXYCoordinates());
//                                    //break;
//                                }
//                            }
//                        }
//                        if(prospectiveNodes.contains(badNodes.get(0))){
//                            prospectiveNodes.remove(badNodes.get(0));
//                        }
//                        badNodes.remove(badNodes.get(0));
//                    }else{
//                        break;
//                    }
//                }
//            }else{
//                break;
//            }


            // If search unsuccessful in entire system
            // i.e. no node in this fog layer has sufficient resources to host the application.
            if ((prospectiveNodes.size() == 0 && goodNodes.size()==0) || goodNodes.size() > 0) {
                break;
            }

            /*
            // Update root of subtree which is being searched
            pud = SimManager.getInstance().getEdgeServerManager().findPuddleById(pud.getLevel()+1, pud.getParentPuddleId());
            */
        }
        return goodNodes;
    }

    @Override
    public void assignHost(MobileDevice mobile) {
        // TODO Auto-generated method stub

        Location mobileLocation = mobile.getLocation();
        double mobileLocationX = mobileLocation.getXPos();
        double mobileLocationY = mobileLocation.getYPos();
        Point mobilePoint = new Point(mobileLocationX, mobileLocationY);

        preferredHostMinIndex = -1;
        preferredHostMaxIndex = -1;
        System.out.println("mobile task length requirement: "+mobile.getTaskLengthRequirement());
        for(int i=0; i < hostListIndexWiseMips.size(); i++){
            //System.out.println("host list index: "+hostListIndexWiseMips.get(i));
            if(debug) {
                System.out.println("mips of host list index[" + i + "]: " + mipsBasedHostList[i].get(0).getTotalMips());
            }
            if(mipsBasedHostList[i].get(0).isMIPSCapacitySufficient(mobile)){
                if(preferredHostMinIndex == -1){
                    preferredHostMinIndex = i;
                }
                if(preferredHostMaxIndex == -1){
                    preferredHostMaxIndex = i;
                }else{
                    preferredHostMaxIndex = i;
                }
            }
        }
        if(debug) {
            System.out.println("preferred host min index: " + preferredHostMinIndex);
            System.out.println("preferred host max index: " + preferredHostMaxIndex);
        }

        populatePrferredHostList(preferredHostMinIndex, preferredHostMaxIndex);
        //System.exit(8696980);

        //mobile.setHost(eh);
        // Part-B: Find cost-optimal node from the set of good nodes identified one per fog layer

        // Find the total cost of execution at each prospective fog node
        // cost of execution and cost of data transfer depends on the type of application accessed from mobile device
        Map<Double, List<NodeSim>> costMap = new HashMap<>();
        NodeSim src = ((ESBModel)SimManager.getInstance().getNetworkModel()).getNetworkTopology().findNode(mobile.getLocation(), false);

        //ArrayList<EdgeHost> goodNodesPerLayer = new ArrayList<EdgeHost>();
        ArrayList<EdgeHost> goodNodes = new ArrayList<EdgeHost>();
        preferredHosts = new ArrayList<>();
        Integer preferredHostListsIndex = 0;
        preferredHosts.addAll(preferredHostLists[preferredHostListsIndex]);
        Integer maxProspectiveNodeCount = (int)(((preferredHosts.size()+1)*0.1));//(int)(preferredHosts.size()*0.1)+1;//10;
        Double latencySafetyFactor = 1.0;
        Double mobileTaskLengthRequirementMultiplier = 1.0;
        Double reserveMultiplier = 1.0;
        //populateMaxMobileForHost(mobile);

        //ArrayList<EdgeHost> prospectiveNodes = getProspectiveNodes(mobile, maxProspectiveNodeCount);
        ArrayList<EdgeHost> prospectiveNodes = new ArrayList<EdgeHost>();
        ArrayList<EdgeHost> badNodes = new ArrayList<EdgeHost>();
        ArrayList<EdgeHost> NeighborsOfNodesAddedToProspectiveNodes = new ArrayList<EdgeHost>();
        HostId_Hops = new HashMap<Integer, Integer>();
        HostId_SearchHops = new HashMap<>();

        Point hostPoint = partition.getHostXY(mobilePoint);
        EdgeHost nearest = pointToHostMapXY.get(hostPoint);
        mobileAccessHost = nearest;
        HostId_Hops.put(mobileAccessHost.getId(), 1);
        if(allPreferredHosts.contains(mobileAccessHost.getId())){
            HostId_SearchHops.put(mobileAccessHost.getId(), 1);
        }else{
            HostId_SearchHops.put(mobileAccessHost.getId(), 0);
        }

        //if(!preferredHosts.contains(mobileAccessHost)) preferredHosts.add(mobileAccessHost);

        // Part-A: Find one good node per fog layer, Repeat the following for each layer.
        //for (int levelIter=1; levelIter <= 1; levelIter++) {

            // Find fog node nearest to mobile device, belonging to this fog layer.
            //EdgeHost nearest = SimManager.getInstance().getEdgeServerManager().findNearestHostByLayer(levelIter, mobile.getLocation());

        HashMap<Point, List<Point>> myVoronoiNeighborSiteMap = partition.getVoronoiNeighborSiteMap();
        double hostProcessingDelay = 0.0;
        double hostProcessingDelayUsingAvailableMIPS = 0.0;
        double acceptableLatency = 0.0;//mobile.getLatencyRequirement();
        double hostNetworkDelay = 0.0;
        double totalDelay = 0.0;
        Integer preferredHostsCheckedCount = 0;
        /*double hostProcessingDelay = (mobile.getTaskLengthRequirement()*mobileTaskLengthRequirementMultiplier) / nearest.getVmScheduler().getPeCapacity();
        double hostProcessingDelayUsingAvailableMIPS = (mobile.getTaskLengthRequirement()*mobileTaskLengthRequirementMultiplier) / (nearest.getTotalMips()-nearest.getReserveMips());
        if(hostProcessingDelayUsingAvailableMIPS > hostProcessingDelay){
            System.out.println("yiikgh3463453");
        }
        double acceptableLatency = mobile.getLatencyRequirement();
        double hostNetworkDelay = ((ESBModel)SimManager.getInstance().getNetworkModel()).getDelay(mobile.getLocation(), nearest.getLocation());

        //Consider round trip latency - assuming user is co-located with device, in current test environment.
        hostNetworkDelay += hostNetworkDelay;
        if(!maxNumOfMobileForHost.containsKey(nearest)){
            Integer maxMobileForHost = (int)(nearest.getAvailableMips()/(mobileTaskLengthRequirementMultiplier * mobile.getTaskLengthRequirement()));
            maxNumOfMobileForHost.put(nearest, maxMobileForHost);
        }
        double totalDelay = hostProcessingDelay + hostNetworkDelay;
        if (totalDelay < latencySafetyFactor * acceptableLatency &&
                preferredHosts.contains(nearest) &&
                (!Host_AssignedMobileCount.containsKey(nearest) ||
                        (Host_AssignedMobileCount.containsKey(nearest) &&
                                Host_AssignedMobileCount.get(nearest) < maxNumOfMobileForHost.get(nearest)))) {
                                //Host_AssignedMobileCount.get(nearest) < maximumMobileAssignment))) {
            //System.out.println();
            //return true;
            //prospectiveNodes.add(pointToHostMapXY.get(hostPoint.convertLongLatPointToXYCoordinates()));
            prospectiveNodes.add(nearest);
        }*/
        NeighborsOfNodesAddedToProspectiveNodes.add(mobileAccessHost);//nearest);

        //System.exit(8686899);
        HostId_Latency = new HashMap<Integer, Double>();
        EdgeHost selHostConsideringLatencyOnly = null;
        System.out.print("Prospective node processing hostIds for mobile device Id: "+mobile.getId()+"\n");
        /*while(prospectiveNodes.size() < maxProspectiveNodeCount && preferredHosts.size() > preferredHostsCheckedCount){
            //System.out.println("myVoronoiNeighborSiteMap size = " + myVoronoiNeighborSiteMap.size());
            List<Point> myVoronoiNeighborsOfNearest = myVoronoiNeighborSiteMap.get(new Point(NeighborsOfNodesAddedToProspectiveNodes.get(0).getLocation().getXPos(),
                    NeighborsOfNodesAddedToProspectiveNodes.get(0).getLocation().getYPos()));
            NeighborsOfNodesAddedToProspectiveNodes.remove(NeighborsOfNodesAddedToProspectiveNodes.get(0));
            //populateMaxMobileForHost();
            Double consumerDistance = 0.0;
            for (Point p : myVoronoiNeighborsOfNearest) {
                EdgeHost pEdgeHost = pointToHostMapXY.get(p.convertLongLatPointToXYCoordinates());
                if(!NeighborsOfNodesAddedToProspectiveNodes.contains(pEdgeHost)) NeighborsOfNodesAddedToProspectiveNodes.add(pEdgeHost);
                if(!prospectiveNodes.contains(pEdgeHost) && preferredHosts.contains(pEdgeHost)){
                    preferredHostsCheckedCount++;
                    nearest = pEdgeHost;
                    //System.out.println("Prospective node processing for neighbor with coordinates: (" + p.getX() + ", " + p.getY() + ")");
                    //System.out.println("Prospective node processing hostId: "+pEdgeHost.getId());
                    //System.out.print(pEdgeHost.getId()+", ");
                    if(!maxNumOfMobileForHost.containsKey(pEdgeHost)){
                        Integer maxMobileForHost = (int)(pEdgeHost.getTotalMips()/(mobileTaskLengthRequirementMultiplier * mobile.getTaskLengthRequirement()));
                        maxNumOfMobileForHost.put(pEdgeHost, maxMobileForHost);
                    }
                    if (pEdgeHost.isMIPSCapacitySufficient(mobile) &&
                            (!Host_AssignedMobileCount.containsKey(pEdgeHost) ||
                                    (Host_AssignedMobileCount.containsKey(pEdgeHost) &&
                                            Host_AssignedMobileCount.get(pEdgeHost) < maxNumOfMobileForHost.get(pEdgeHost)))) {
                        //Host_AssignedMobileCount.get(pEdgeHost) < maximumMobileAssignment))) {
                        hostProcessingDelay = (mobile.getTaskLengthRequirement()) / pEdgeHost.getVmScheduler().getPeCapacity();
                        hostProcessingDelayUsingAvailableMIPS =
                                (mobile.getTaskLengthRequirement()) / (1.0*(pEdgeHost.getTotalMips()-pEdgeHost.getReserveMips()));

                        acceptableLatency = mobile.getLatencyRequirement();
                        hostNetworkDelay = ((ESBModel)SimManager.getInstance().getNetworkModel()).getDelay(mobile.getLocation(), pEdgeHost.getLocation());

                        //Consider round trip latency - assuming user is co-located with device, in current test environment.
                        hostNetworkDelay += hostNetworkDelay;
                        if(hostProcessingDelayUsingAvailableMIPS > hostProcessingDelay){
                            //System.out.println("fhfty4223");
                            totalDelay = hostProcessingDelayUsingAvailableMIPS + hostNetworkDelay;
                        }else {
                            totalDelay = hostProcessingDelay + hostNetworkDelay;
                        }
                        if (totalDelay < latencySafetyFactor * acceptableLatency) {
                            //System.out.println();
                            //return true;
                            //prospectiveNodes.add(pointToHostMapXY.get(hostPoint.convertLongLatPointToXYCoordinates()));
                            prospectiveNodes.add(pEdgeHost);
                            HostId_Latency.put(pEdgeHost.getId(),totalDelay);
                            System.out.println("HostId: "+pEdgeHost.getId()+" Estimated latency: "+totalDelay);
                        }
                        //prospectiveNodes.add(pointToHostMapXY.get(p.convertLongLatPointToXYCoordinates()));
                        //nearest = pointToHostMapXY.get(p.convertLongLatPointToXYCoordinates());
                        //break;
                    }
                }
            }
            //System.out.println("");
        }*/

        if (SimSettings.getInstance().traceEnable()) {
            SimLogger.printLine("prospectiveNodes.size(): "+
                    prospectiveNodes.size());
            //SimLogger.printLine("Mobile location is: ("+mobile.getLocation().getXPos()+","+mobile.getLocation().getYPos()+")");
            //SimLogger.printLine("Host location is: ("+selHost.getLocation().getXPos()+","+selHost.getLocation().getYPos()+")");
        }
        System.out.println();
        System.out.println("Prospective nodes size: "+prospectiveNodes.size());
        //preferredHostListsIndex = -1;
        boolean assignmentDone = false;
        if(preferredHostIndexAscending) {
            preferredHostListsIndex = preferredHostMinIndex+1; // Exclude the cloud
//            while (preferredHostListsIndex < (preferredHostMaxIndex+1)
//                    && prospectiveNodes.size() == 0 && goodNodes.size() == 0) {
            while (preferredHostListsIndex < (preferredHostMaxIndex+1)){
                if (preferredHostListsIndex > (preferredHostMinIndex - 1)
                        && preferredHostListsIndex < (preferredHostMaxIndex+1)) {
                    prospectiveNodes = getProspectiveNodes(preferredHostListsIndex, mobile);
                    goodNodes = getGoodNodes(prospectiveNodes, mobile);
                    assignmentDone = attemptHostAssignment(goodNodes, mobile);
                    if(assignmentDone){break;}
                }
                preferredHostListsIndex++;
            }
        }else{
            preferredHostListsIndex = preferredHostMaxIndex;
//            while (preferredHostListsIndex > (preferredHostMinIndex-1)
//                    && prospectiveNodes.size() == 0 && goodNodes.size() == 0) {
            while (preferredHostListsIndex > (preferredHostMinIndex)) { // Exclude cloud
                if (preferredHostListsIndex > (preferredHostMinIndex)
                        && preferredHostListsIndex < (preferredHostMaxIndex+1)) {
                    if(preferredHostListsIndex == (preferredHostMaxIndex-1)){
                        System.out.println("preferredHostListsIndex: "+preferredHostListsIndex);
                    }
                    prospectiveNodes = getProspectiveNodes(preferredHostListsIndex, mobile);
                    goodNodes = getGoodNodes(prospectiveNodes, mobile);
                    assignmentDone = attemptHostAssignment(goodNodes, mobile);
                    if(assignmentDone){break;}
                }
                preferredHostListsIndex--;
            }
        }
        //If none of the fog nodes fail to host, try to host on cloud
        if(!assignmentDone) {
            preferredHostListsIndex = 0;
            if(checkLatencyConditionForCloud) {
                prospectiveNodes = getProspectiveNodes(preferredHostListsIndex, mobile);
                goodNodes = getGoodNodes(prospectiveNodes, mobile);
                assignmentDone = attemptHostAssignment(goodNodes, mobile);
                if (assignmentDone) {
                    assignedToCloudMobileDeviceCount++;
                }
            }else{
                prospectiveNodes = getProspectiveNodes(preferredHostListsIndex, mobile);
                goodNodes = prospectiveNodes;//getGoodNodes(prospectiveNodes, mobile);
                assignmentDone = attemptHostAssignment(goodNodes, mobile);
                if (assignmentDone) {
                    assignedToCloudMobileDeviceCount++;
                }
                /*int selHostId = 0;//preferredHosts(preferredHostListsIndex);
                EdgeHost selHost = bdEdgeHost.get(selHostId);
                LinkedList<NodeSim> path = ((ESBModel) SimManager.getInstance().getNetworkModel()).findPath(selHost, mobile);
                mobile.setPath(path);
                mobile.setHost(selHost);
                //System.out.println("Before reservation: "+selHost.getTotalMips()+", "+selHost.getAvailableMips()+", "+selHost.getReserveMips());
                //mobile.setAppType(SimSettings.APP_TYPES.AUGMENTED_REALITY); // added by sks0099 on 09112025 for testing
                //mobile.makeReservation(); // added by sks0099 on 09112025 for testing
                //mobile.makeReservation(); // added by sks0099 on 09112025 for testing
                mobile.makeReservation();
                //System.out.println("After reservation: "+selHost.getTotalMips()+", "+selHost.getAvailableMips()+", "+selHost.getReserveMips());
                //System.exit(97979);
                Integer currentAssignmentCount = 0;
                if (Host_AssignedMobileCount.containsKey(selHost)) {
                    currentAssignmentCount = Host_AssignedMobileCount.get(selHost);
                }
                Host_AssignedMobileCount.put(selHost, currentAssignmentCount + 1);
                mobileHostHop.put(mobile, HostId_Hops.get(selHostId));
                selHost.setHop(HostId_Hops.get(selHostId));
                this.hops[mobile.getId()] = HostId_Hops.get(selHostId);
                System.out.println("  Assigned host: " + selHost.getId() + " total assignment: " + (currentAssignmentCount + 1) + " Estimated latency: " + HostId_Latency.get(selHost.getId())
                        + " Voronoi traversal hops (mobile to the host): " + HostId_Hops.get(selHostId));

                searchHop.put(mobile, HostId_SearchHops.get(selHostId));
                selHost.setSearchHop(HostId_SearchHops.get(selHostId));
                this.searchHops[mobile.getId()] = HostId_SearchHops.get(selHostId);
                if(debug) {
                    System.out.println("  Assigned host: " + selHost.getId() + " total assignment: " + (currentAssignmentCount + 1) + " Estimated latency: " + HostId_Latency.get(selHost.getId())
                            + " Search hops (mobile to the host): " + HostId_SearchHops.get(selHostId));
                }

                mobileHostAssignment.put(mobile, selHost);

                double distanceBetweenMobileAndHost = DataInterpreter.measure(mobile.getLocation().getYPos(), mobile.getLocation().getXPos(),
                        selHost.getLocation().getYPos(), selHost.getLocation().getXPos());
                if (SimSettings.getInstance().traceEnable()) {
                    SimLogger.printLine("Distance from mobile to the host is: " +
                            distanceBetweenMobileAndHost + " m");
                    SimLogger.printLine("Mobile location is: (" + mobile.getLocation().getXPos() + "," + mobile.getLocation().getYPos() + ")");
                    SimLogger.printLine("Host location is: (" + selHost.getLocation().getXPos() + "," + selHost.getLocation().getYPos() + ")");
                }*/

            }
        }
        if(!assignmentDone){
            System.out.println("Host assignment failed.");
            unassignedMobileDeviceCount++;
        }
        /*if(prospectiveNodes.size()==0){
            System.out.println("No prospective node could be found.");
            //System.exit(880808);
        }
        if(goodNodes.size()==0){
            System.out.println("No good node could be found.");
            //System.exit(880808);
        }*/

//        while (prospectiveNodes.size() != 0) {
//            BinaryHeap sort = new BinaryHeap(prospectiveNodes.size(), mobile.getLocation(), prospectiveNodes);
//            LinkedList<EdgeHost> nodes = sort.sortNodes();
//
//            //BinaryHeap sort = new BinaryHeap(prospectiveNodes.size(), mobile, prospectiveNodes);
//            //LinkedList<EdgeHost> nodes = sort.sortNodesByCostPerSec();
//
//            //BinaryHeap sort = new BinaryHeap(prospectiveNodes.size(), mobile, prospectiveNodes);
//            //LinkedList<EdgeHost> nodes = sort.sortNodesByMIPS(); // Sorting is in descending order
//
//            EdgeHost prosHost = nodes.poll();
//
//            // Find the nearest node capable of hosting the application.
//            /*while (!goodHost(prosHost, mobile)) {
//                // if mobile flag is not due-to-latency --> set latencylimitflag to 0;
//                badNodes.add(prosHost);
//                prosHost = nodes.poll();
//                if (prosHost == null) {
//                    break;
//                }
//            }*/
//
//            while(prosHost != null){
//                if(goodHost(prosHost, mobile)){
//                    goodNodes.add(prosHost);
//                }else{
//                    badNodes.add(prosHost);
//                }
//                prosHost = nodes.poll();
//            }
//            /*if (prosHost != null) {
//                // Good host found for current fog layer. Save it and initiate search for next higher layer.
//                goodNodes.add(prosHost);
//                break;
//                //nearest = prosHost;
//            }*/
//            // if latencylimitflag is 1 --> all prospective nodes has sufficient mips, but failed due to latency;
//            // give just one more chance to expand search, beyond which we assume the nodes are too far anyway ( heuristic) & may have higher latency
//            // if latencylimitflag = 2; then print latency unacceptable for this layer; and break
//            // else set latencylimitflag ++ i.e. set to 2 i.e. give a second chance to expand search; or set to 1 to restart the racking prcess.
//            // and continue executing the fllowing code..
//
//            // Search unsuccessful, hence expand search to neighbors using parent subtree.
//            // Get list of other prospective nodes belonging to this layer in the parent subtree
//            //prospectiveNodes = SimManager.getInstance().getEdgeServerManager().getCousins(pud, levelIter, mobile.getId());
//
//
//            if(searchBadNodes){
//                // If the search with the existing prospective nodes fails, include neighboring nodes
//                if (prosHost == null && goodNodes.size() == 0) {
//                    List<Point> myVoronoiNeighborsOfNearest = myVoronoiNeighborSiteMap.get(new Point(badNodes.get(0).getLocation().getXPos(), nearest.getLocation().getYPos()));
//                    NeighborsOfNodesAddedToProspectiveNodes.add(badNodes.get(0));
//                    if(myVoronoiNeighborsOfNearest != null){
//                        for (Point p : myVoronoiNeighborsOfNearest) {
//                            //System.out.println("Point: (" + p.getX() + ", " + p.getY() + ")");
//                            EdgeHost pEdgeHost = pointToHostMapXY.get(p.convertLongLatPointToXYCoordinates());
//                            if(!badNodes.contains(pEdgeHost) && !NeighborsOfNodesAddedToProspectiveNodes.contains(pEdgeHost) &&
//                                    !prospectiveNodes.contains(pEdgeHost) &&
//                                    preferredHosts.contains(pEdgeHost) &&
//                                    (!Host_AssignedMobileCount.containsKey(pEdgeHost) ||
//                                            (Host_AssignedMobileCount.containsKey(pEdgeHost) &&
//                                                    Host_AssignedMobileCount.get(pEdgeHost) < maxNumOfMobileForHost.get(pEdgeHost)))){
//                                //Host_AssignedMobileCount.get(pEdgeHost) < maximumMobileAssignment))){
//                                if (pEdgeHost.isMIPSCapacitySufficient(mobile)) {
//                                    hostProcessingDelay = (mobile.getTaskLengthRequirement()) / pEdgeHost.getVmScheduler().getPeCapacity();
//                                    hostProcessingDelayUsingAvailableMIPS = (mobile.getTaskLengthRequirement()) / pEdgeHost.getAvailableMips();
//                                    if(hostProcessingDelayUsingAvailableMIPS > hostProcessingDelay){
//                                        System.out.println("dhdry35534636");
//                                    }
//                                    acceptableLatency = mobile.getLatencyRequirement();
//                                    hostNetworkDelay = ((ESBModel) SimManager.getInstance().getNetworkModel()).getDelay(mobile.getLocation(), pEdgeHost.getLocation());
//
//                                    //Consider round trip latency - assuming user is co-located with device, in current test environment.
//                                    hostNetworkDelay += hostNetworkDelay;
//
//                                    totalDelay = hostProcessingDelay + hostNetworkDelay;
//                                    if (totalDelay < latencySafetyFactor * acceptableLatency) {
//                                        //System.out.println();
//                                        //return true;
//                                        //prospectiveNodes.add(pointToHostMapXY.get(hostPoint.convertLongLatPointToXYCoordinates()));
//                                        prospectiveNodes.add(pEdgeHost);
//
//                                    }
//                                    //prospectiveNodes.add(pointToHostMapXY.get(p.convertLongLatPointToXYCoordinates()));
//                                    //nearest = pointToHostMapXY.get(p.convertLongLatPointToXYCoordinates());
//                                    //break;
//                                }
//                            }
//                        }
//                        if(prospectiveNodes.contains(badNodes.get(0))){
//                            prospectiveNodes.remove(badNodes.get(0));
//                        }
//                        badNodes.remove(badNodes.get(0));
//                    }else{
//                        break;
//                    }
//
//                }
//            }else{
//                break;
//            }
//
//
//            // If search unsuccessful in entire system
//            // i.e. no node in this fog layer has sufficient resources to host the application.
//            if ((prospectiveNodes.size() == 0 && goodNodes.size()==0) || goodNodes.size() > 0) {
//                break;
//            }
//
//            /*
//            // Update root of subtree which is being searched
//            pud = SimManager.getInstance().getEdgeServerManager().findPuddleById(pud.getLevel()+1, pud.getParentPuddleId());
//            */
//        }


        // Part-B: Find cost-optimal node from the set of good nodes identified one per fog layer
        // In this part network topology comes into play because now we check how good nodes are actually connected
        // with mobileAccessHost or sourceHost. If a node has no connection from the source host to
        // good node, it is worthless. Added advantage of this exercise is to find out the cost.

        // We find the total cost of execution at each prospective fog node
        // cost of execution and cost of data transfer depends on the type of application accessed from mobile device
        //Map<Double, List<NodeSim>> costMap = new HashMap<>();
        //NodeSim src = ((ESBModel)SimManager.getInstance().getNetworkModel()).getNetworkTopology().findNode(mobile.getLocation(), false);

        // get the set of paths for all nodes from current location of mobile device
        //EdgeHost srcHost = partition.getNearestHost(mobile); // sks0099 commented this line on 10/4/2025
//        EdgeHost srcHost = mobileAccessHost; // sks0099 added thisline on 10/4/2025
//        NodeSim srcNodeSim = new NodeSim(srcHost.getLocation().getXPos(), srcHost.getLocation().getYPos());
//
//        HashMap<NodeSim, LinkedList<NodeSim>> tempMap = new HashMap<>();
//        LinkedList<NodeSim> tempList = new LinkedList<>();
//        for (NodeSim srcNode: networkModel.getNetworkTopology().getNodes()) { // srcNode is the source host
//            if(srcNode.getLocation().getXPos()==srcNodeSim.getLocation().getXPos() &&
//                    srcNode.getLocation().getYPos()==srcNodeSim.getLocation().getYPos()){
//                //System.out.println("ertgutr577");
//                //HashMap<NodeSim, LinkedList<NodeSim>> tempMap = new HashMap<>();
//                for (NodeSim desHost: networkModel.getNetworkTopology().getNodes()) {
//                    //tempList.removeAll(tempList);// = new LinkedList<>();
//                    tempList = new LinkedList<>();
//                    tempList = networkModel.findPath(srcNode, desHost); // desHost is the destination host
//                    tempMap.put(desHost, tempList);
//                }
//                //pathTable.put(new NodeSim(srcNode.getLocation().getXPos(),srcNode.getLocation().getYPos()), tempMap);
//                pathTableUsingLocation.put(srcNode.getLocation(), tempMap);
//                pathTableUsingMobileAccessHostId.put(srcHost.getId(), tempMap);
//            }
//        }
//
//        //Map<NodeSim, LinkedList<NodeSim>> desMap = pathTable.get(srcNodeSim);
//        /*if(pathTableUsingLocation.containsKey(srcNodeSim.getLocation())){
//            System.out.println("exists");
//        }else{
//            System.out.println("doesn't exist");
//        }*/
//        //Map<NodeSim, LinkedList<NodeSim>> desMap = pathTableUsingLocation.get(srcNodeSim.getLocation());
//        Map<NodeSim, LinkedList<NodeSim>> desMap = tempMap;
//        /*Boolean foundSource = false;
//        Integer maxNumberOfElements = pathTable.size();
//        Integer countElementsSearched = 0;
//        for (NodeSim key : pathTable.keySet()) {
//            //System.out.println("Key: " + key + ", Value.get(0): " + pathTable.get(key).get(0));
//            if(key.equals(srcNodeSim)){
//                System.out.println("hi");
//            }
//        }*/
//
//        /*while(!foundSource && countElementsSearched < maxNumberOfElements){
//            if(pathTable)
//        }*/
//
//        // Prune the set of paths to include only the prospective fog nodes from goodNodesPerLayer list
//
//
//
//        Map<Integer, Integer> latencyRank = new LinkedHashMap<>();
//        if(selectHostByLeastLatency){
//            //HostId_Latency.forEach();
//            TreeMap<Integer, Double> HostId_LatencyTreeMap = new TreeMap<>();
//            for (Map.Entry<Integer, Double> HostId_LatencyEntry : HostId_Latency.entrySet()) {
//                //System.out.println(entry.getKey() + ": " + entry.getKey().getId());
//                //bdMobileDevice.put(entry.getKey().getId(), entry.getKey());
//                //bdEdgeHost.put(entry.getValue().getId(), entry.getValue());
//                HostId_LatencyTreeMap.put(HostId_LatencyEntry.getKey(), HostId_LatencyEntry.getValue());
//
//            }
//
//            // Convert entries to a List
//            List<Map.Entry<Integer, Double>> distanceEntryList = new ArrayList<>(HostId_LatencyTreeMap.entrySet());
//
//            // Sort the list by Double values in ascending order
//            Collections.sort(distanceEntryList, new Comparator<Map.Entry<Integer, Double>>() {
//                @Override
//                public int compare(Map.Entry<Integer, Double> entry1, Map.Entry<Integer, Double> entry2) {
//                    // Compare values
//                    int valueComparison = entry1.getValue().compareTo(entry2.getValue());
//
//                    // If values are equal, sort by key to ensure stable sorting and prevent loss of entries in case of re-insertion into a map that doesn't allow duplicate keys (like a TreeMap).
//                    if (valueComparison == 0) {
//                        return entry1.getKey().compareTo(entry2.getKey());
//                    }
//                    return valueComparison;
//                }
//            });
//
//            // Create a new LinkedHashMap to maintain the sorted order by value
//            Map<Integer, Double> sortedByValueMap = new LinkedHashMap<>();
//            //Integer firstKey = 0;
//            for (Map.Entry<Integer, Double> distanceEntry : distanceEntryList) {
//                if(goodHostWithoutCheckingLatency(bdEdgeHost.get(distanceEntry.getKey()), mobile)) {
//                    sortedByValueMap.put(distanceEntry.getKey(), distanceEntry.getValue());
//                }
//                //firstKey = entry.getKey();
//                //break;
//            }
//            //firstKey = sortedByValueMap.keySet().iterator().next();
//
//            latencyRank = getRank(sortedByValueMap);
//            //sortedHostId_LatencyMap.forEach((key, value) -> System.out.println(key + ": " + value +","+bdEdgeHost.get(value).getTotalMips()+","+bdEdgeHost.get(value).getReserveMips()));
//            //TreeMap<Integer, Double> sortedHostId_LatencyMap = new TreeMap<>();
//            //entriesSortedByValues(HostId_LatencyTreeMap);
//            //sortedByValueMap.forEach((key, value) -> System.out.println(key + ": " + value)+" Rank: "+latencyRank.get(key));
//            for (Map.Entry<Integer, Double> entry : sortedByValueMap.entrySet()) {
//                System.out.println(entry.getKey() + ": " + entry.getValue()+" Rank: "+latencyRank.get(entry.getKey()));
//            }
//            //System.out.println("firstKey: "+firstKey);
//
//            //if (selHostConsideringLatencyOnly != null) {
//            //    System.out.println("  Mobile device: "+mobile.getId()+"  WAP: "+mobile.getLocation().getServingWlanId()+"  Assigned host:  "+selHost.getId());
//                //return;
//            //}
//        }
//        //System.exit(765467376);
//        Map<NodeSim, LinkedList<NodeSim>> selectedDesMap = new HashMap<>();
//        Map<Integer, LinkedList<NodeSim>> selectedDesMapUsingHostId = new HashMap<>();
//
//        for (int i=0; i<goodNodes.size(); i++) {
//
//            // for each host, get the NodeSim object
//            Location hostLoc = goodNodes.get(i).getLocation();
//            NodeSim hostNode = ((ESBModel)networkModel).getNetworkTopology().findNode(hostLoc, false);
//
//            // for each such NodeSim object, retrieve the row and add it to selectedDesMap
//            selectedDesMap.put(hostNode, desMap.get(hostNode));
//            selectedDesMapUsingHostId.put(goodNodes.get(i).getId(), desMap.get(hostNode));
//        }
//
//        // continue with processing as earlier.
//        TreeMap<Integer, Double> HostId_CostTreeMap = new TreeMap<>();
//
//        // Convert entries to a List
//
//        for (Map.Entry<NodeSim, LinkedList<NodeSim>> entry: selectedDesMap.entrySet()) {
//            double cost = 0;
//            NodeSim des = entry.getKey();
//            LinkedList<NodeSim> path = entry.getValue();
//            if (path == null || path.size() == 0) {
//                EdgeHost k = SimManager.getInstance().getLocalServerManager().findHostByLoc(mobile.getLocation().getXPos(), mobile.getLocation().getYPos(), mobile.getLocation().getAltitude());
//
//                //double bwCost = mobile.getBWRequirement() * k.getCostPerBW();
//                double bwCost = (mobile.getBWRequirement()*8 / (double)1024) * k.getCostPerBW(); //mobile.getBWRequirement() in KB * 8b/B ==>Kb / 1024 = Mb; k.getCostPerBW() in $/Mb -- Shaik modified
//
//                //double exCost = (double)mobile.getTaskLengthRequirement() / k.getTotalMips() * k.getCostPerSec();
//                double exCost = (double)mobile.getTaskLengthRequirement() / (k.getPeList().get(0).getMips()) * k.getCostPerSec(); // Shaik modified - May 07, 2019.
//
//                cost = cost + bwCost;
//                //SimLogger.getInstance().getCentralizeLogPrinter().println("Level:\t" + des.getLevel() + "\tNode:\t" + des.getWlanId() + "\tBWCost:\t" + bwCost + "\tTotalBWCost:\t" + cost);
//                //SimLogger.getInstance().getCentralizeLogPrinter().println("Total data:\t" + mobile.getBWRequirement() + "\tBWCostPerSec:\t" + k.getCostPerBW());
//                cost = cost + exCost;
//                //SimLogger.getInstance().getCentralizeLogPrinter().println("Destination:\t"+ des.getWlanId() + "\tExecuteCost:\t" + exCost + "\tTotalCost:\t" + cost);
//                //SimLogger.getInstance().getCentralizeLogPrinter().println("Service CPu Time:\t" + ((double)mobile.getTaskLengthRequirement() / k.getTotalMips()) + "\tMipsCostPerSec:\t" + k.getCostPerSec());
//                //System.out.println("Host Id: "+k.getId()+" cost: "+cost);
//                HostId_CostTreeMap.put(k.getId(), cost);
//            } else {
//                //SimLogger.getInstance().getCentralizeLogPrinter().println("**********Path From " + src.getWlanId() + " To " + des.getWlanId() + "**********");
//                for (NodeSim node: path) {
//                    EdgeHost k = SimManager.getInstance().getLocalServerManager().findHostByLoc(node.getLocation().getXPos(), node.getLocation().getYPos(), node.getLocation().getAltitude());
//                    //double bwCost = mobile.getBWRequirement() * k.getCostPerBW();
//                    double bwCost = (mobile.getBWRequirement()*8 / (double)1024) * k.getCostPerBW(); //mobile.getBWRequirement() in KB * 8b/B ==>Kb / 1024 = Mb; k.getCostPerBW() in $/Mb -- Shaik modified
//
//                    cost = cost + bwCost;
//                    //SimLogger.getInstance().getCentralizeLogPrinter().println("Level:\t" + node.getLevel() + "\tNode:\t" + node.getWlanId() + "\tBWCost:\t" + bwCost + "\tTotalBWCost:\t" + cost);
//                    //SimLogger.getInstance().getCentralizeLogPrinter().println("Total data:\t" + mobile.getBWRequirement() + "\tBWCostPerSec:\t" + k.getCostPerBW());
//                }
//                EdgeHost desHost = SimManager.getInstance().getLocalServerManager().findHostByLoc(des.getLocation().getXPos(), des.getLocation().getYPos(), des.getLocation().getAltitude());
//                //double exCost = desHost.getCostPerSec() * ((double)mobile.getTaskLengthRequirement() / desHost.getTotalMips());
//                double exCost = (double)mobile.getTaskLengthRequirement() / (desHost.getPeList().get(0).getMips()) * desHost.getCostPerSec(); // Shaik modified - May 07, 2019.
//
//                cost = cost + exCost;
//                //SimLogger.getInstance().getCentralizeLogPrinter().println("Destination:\t"+ des.getWlanId() + "\tExecuteCost:\t" + exCost + "\tTotalCost:\t" + cost);
//                //SimLogger.getInstance().getCentralizeLogPrinter().println("Service CPU time:\t" + ((double)mobile.getTaskLengthRequirement() / desHost.getTotalMips()) + "\tMipsCostPerSec:\t" + desHost.getCostPerSec());
//                //System.out.println("Host Id: "+desHost.getId()+" cost: "+cost);
//                HostId_CostTreeMap.put(desHost.getId(), cost);
//            }
//
//
//            if (costMap.containsKey(cost)) {
//                if (!costMap.get(cost).contains(des)) {
//                    costMap.get(cost).add(des);
//                }
//            }
//            else {
//                ArrayList<NodeSim> desList = new ArrayList<>();
//                desList.add(des);
//                costMap.put(cost, desList);
//            }
//
//        }
//
//        //Sort the list of prospective nodes by increasing cost
//        LinkedList<EdgeHost> hostsSortedByCost = new LinkedList<EdgeHost>();
//
//        //Create a sorted list of costs
//        List<Double> costList = new ArrayList<Double> (costMap.keySet());
//        Collections.sort(costList);
//
//        List<Map.Entry<Integer, Double>> costEntryList = new ArrayList<>(HostId_CostTreeMap.entrySet());
//        // Create a new LinkedHashMap to maintain the sorted order by value
//        Map<Integer, Double> sortedByCostValueMap = new LinkedHashMap<>();
//        //Integer firstKey = 0;
//
//        // Sort the list by Double values in ascending order
//        Collections.sort(costEntryList, new Comparator<Map.Entry<Integer, Double>>() {
//            @Override
//            public int compare(Map.Entry<Integer, Double> entry1, Map.Entry<Integer, Double> entry2) {
//                // Compare values
//                int valueComparison = entry1.getValue().compareTo(entry2.getValue());
//
//                // If values are equal, sort by key to ensure stable sorting and prevent loss of entries in case of re-insertion into a map that doesn't allow duplicate keys (like a TreeMap).
//                if (valueComparison == 0) {
//                    return entry1.getKey().compareTo(entry2.getKey());
//                }
//                return valueComparison;
//            }
//        });
//
//        for (Map.Entry<Integer, Double> costEntry : costEntryList) {
//            //if(goodHostWithoutCheckingLatency(bdEdgeHost.get(distanceEntry.getKey()), mobile)) {
//            sortedByCostValueMap.put(costEntry.getKey(), costEntry.getValue());
//            //}
//        }
//        //firstKey = sortedByValueMap.keySet().iterator().next();
//        Map<Integer, Integer> costRank = new LinkedHashMap<>();
//        //Collections.sort(sortedByCostValueMap);
//        costRank = getRank(sortedByCostValueMap);
//        //sortedHostId_LatencyMap.forEach((key, value) -> System.out.println(key + ": " + value +","+bdEdgeHost.get(value).getTotalMips()+","+bdEdgeHost.get(value).getReserveMips()));
//        //TreeMap<Integer, Double> sortedHostId_LatencyMap = new TreeMap<>();
//        //entriesSortedByValues(HostId_LatencyTreeMap);
//        //sortedByValueMap.forEach((key, value) -> System.out.println(key + ": " + value)+" Rank: "+latencyRank.get(key));
//        for (Map.Entry<Integer, Double> costEntry : sortedByCostValueMap.entrySet()) {
//            System.out.println(costEntry.getKey() + ": " + costEntry.getValue()+" Rank: "+costRank.get(costEntry.getKey()));
//        }
//        Map<Integer, Integer> combinedRank = new LinkedHashMap<>();
//        Integer selHostId = -1;
//        Integer currentCombinedRank = 0;
//        Integer minimumCombinedRank = 100000;
//        Integer entryCount = 0;
//        for (Map.Entry<Integer, Double> costEntry : sortedByCostValueMap.entrySet()) {
//            System.out.println(costEntry.getKey() + ": " + costEntry.getValue()+" Rank: "+costRank.get(costEntry.getKey())
//            +" LatencyRank: "+ latencyRank.get(costEntry.getKey())+" combined rank: "+
//                    (costRank.get(costEntry.getKey())+latencyRank.get(costEntry.getKey())));
//            if(entryCount==0) {
//                selHostId = costEntry.getKey();
//                currentCombinedRank = (costRank.get(costEntry.getKey())+latencyRank.get(costEntry.getKey()));
//                minimumCombinedRank = currentCombinedRank;
//            }else{
//                currentCombinedRank = (costRank.get(costEntry.getKey())+latencyRank.get(costEntry.getKey()));
//                if(currentCombinedRank < minimumCombinedRank) {
//                    minimumCombinedRank = currentCombinedRank;
//                    selHostId = costEntry.getKey();
//                }
//            }
//            entryCount++;
//        }
//        System.out.println("Selected Host Id: "+selHostId);
//        if(selHostId==-1) {
//            System.out.println("  Mobile device: "+mobile.getId()+"  WAP: "+mobile.getLocation().getServingWlanId()+"  Assigned host:  NULL");
//            unassignedMobileDeviceCount++;
//            return;
//        }else{
//            EdgeHost selHost = bdEdgeHost.get(selHostId);
//            LinkedList<NodeSim> path = ((ESBModel)SimManager.getInstance().getNetworkModel()).findPath(selHost, mobile);
//            mobile.setPath(path);
//            mobile.setHost(selHost);
//            //System.out.println("Before reservation: "+selHost.getTotalMips()+", "+selHost.getAvailableMips()+", "+selHost.getReserveMips());
//            //mobile.setAppType(SimSettings.APP_TYPES.AUGMENTED_REALITY); // added by sks0099 on 09112025 for testing
//            //mobile.makeReservation(); // added by sks0099 on 09112025 for testing
//            //mobile.makeReservation(); // added by sks0099 on 09112025 for testing
//            mobile.makeReservation();
//            //System.out.println("After reservation: "+selHost.getTotalMips()+", "+selHost.getAvailableMips()+", "+selHost.getReserveMips());
//            //System.exit(97979);
//            Integer currentAssignmentCount = 0;
//            if(Host_AssignedMobileCount.containsKey(selHost)) {
//                currentAssignmentCount = Host_AssignedMobileCount.get(selHost);
//            }
//            Host_AssignedMobileCount.put(selHost, currentAssignmentCount + 1);
//            mobileHostHop.put(mobile, HostId_Hops.get(selHostId));
//            System.out.println("  Assigned host: " + selHost.getId()+" total assignment: "+(currentAssignmentCount + 1)+" Estimated latency: "+HostId_Latency.get(selHost.getId())
//                    +" Voronoi traversal hops (mobile to the host): "+HostId_Hops.get(selHostId));
//
//            searchHop.put(mobile, HostId_SearchHops.get(selHostId));
//            selHost.setSearchHop(HostId_SearchHops.get(selHostId));
//            this.searchHops[mobile.getId()] = HostId_SearchHops.get(selHostId);
//            System.out.println("  Assigned host: " + selHost.getId()+" total assignment: "+(currentAssignmentCount + 1)+" Estimated latency: "+HostId_Latency.get(selHost.getId())
//                    +" Search hops (mobile to the host): "+HostId_SearchHops.get(selHostId));
//
//            mobileHostAssignment.put(mobile, selHost);
//        /*if(currentAssignmentCount+1>maximumMobileAssignment){
//            System.out.println("Total mobile assignment to host exceeded the maximum limit. Debugging needed!");
//            System.exit(8896);
//        }*/
//            double distanceBetweenMobileAndHost = DataInterpreter.measure(mobile.getLocation().getYPos(), mobile.getLocation().getXPos(),
//                    selHost.getLocation().getYPos(), selHost.getLocation().getXPos());
//            if (SimSettings.getInstance().traceEnable()) {
//                SimLogger.printLine("Distance from mobile to the host is: "+
//                        distanceBetweenMobileAndHost +" m");
//                SimLogger.printLine("Mobile location is: ("+mobile.getLocation().getXPos()+","+mobile.getLocation().getYPos()+")");
//                SimLogger.printLine("Host location is: ("+selHost.getLocation().getXPos()+","+selHost.getLocation().getYPos()+")");
//            }
//            //return;
//        }
//        //Access the map entries in order sorted by cost
//        /*EdgeHost host = null;
//        for (Double totalCost : costList) {
//            // Get the list of prospective nodes available at that cost
//            for(NodeSim desNode: costMap.get(totalCost)) {
//                host = SimManager.getInstance().getLocalServerManager().findHostByLoc(desNode.getLocation().getXPos(), desNode.getLocation().getYPos(), desNode.getLocation().getAltitude());
//
//                hostsSortedByCost.add(host);
//                //}
//                System.out.println("Hosts in sorted order of costs:  "+host.getId()+"  at cost:  "+totalCost);
//            }
//        }
//
//        //Find a cost-optimal node with available resources
//        if (hostsSortedByCost.size() == 0) {
//            System.out.println("  Mobile device: "+mobile.getId()+"  WAP: "+mobile.getLocation().getServingWlanId()+"  Assigned host:  NULL");
//            unassignedMobileDeviceCount++;
//            return;
//        }*/
//
//        /*EdgeHost selHost = null;
//        selHost = hostsSortedByCost.poll();
//        if (selHost == null) {
//            System.out.println("  Mobile device: "+mobile.getId()+"  WAP: "+mobile.getLocation().getServingWlanId()+"  Assigned host:  NULL");
//            unassignedMobileDeviceCount++;
//            return;
//        }
//
//        System.out.print("Prospective host:  ");
//        while(!goodHost(selHost, mobile)) {
//            if (selHost == null) {selHost = hostsSortedByCost.poll();//find the next cost-optimal node capable of handling the task
//                break;
//            }
//        }
//        if (selHost != null) {
//            if(selectHostByLeastLatency && selHostConsideringLatencyOnly != null){
//                selHost = selHostConsideringLatencyOnly;
//            }
//            LinkedList<NodeSim> path = ((ESBModel)SimManager.getInstance().getNetworkModel()).findPath(selHost, mobile);
//            mobile.setPath(path);
//            mobile.setHost(selHost);
//            //System.out.println("Before reservation: "+selHost.getTotalMips()+", "+selHost.getAvailableMips()+", "+selHost.getReserveMips());
//            //mobile.setAppType(SimSettings.APP_TYPES.AUGMENTED_REALITY); // added by sks0099 on 09112025 for testing
//            //mobile.makeReservation(); // added by sks0099 on 09112025 for testing
//            //mobile.makeReservation(); // added by sks0099 on 09112025 for testing
//            mobile.makeReservation();
//            //System.out.println("After reservation: "+selHost.getTotalMips()+", "+selHost.getAvailableMips()+", "+selHost.getReserveMips());
//            //System.exit(97979);
//            Integer currentAssignmentCount = 0;
//            if(Host_AssignedMobileCount.containsKey(selHost)) {
//                currentAssignmentCount = Host_AssignedMobileCount.get(selHost);
//            }
//            Host_AssignedMobileCount.put(selHost, currentAssignmentCount + 1);
//            System.out.println("  Assigned host: " + selHost.getId()+" total assignment: "+(currentAssignmentCount + 1)+" Estimated latency: "+HostId_Latency.get(selHost.getId()));
//            mobileHostAssignment.put(mobile, selHost);
//
//            double distanceBetweenMobileAndHost = DataInterpreter.measure(mobile.getLocation().getYPos(), mobile.getLocation().getXPos(),
//                    selHost.getLocation().getYPos(), selHost.getLocation().getXPos());
//            if (SimSettings.getInstance().traceEnable()) {
//                SimLogger.printLine("Distance from mobile to the host is: "+
//                        distanceBetweenMobileAndHost +" m");
//                SimLogger.printLine("Mobile location is: ("+mobile.getLocation().getXPos()+","+mobile.getLocation().getYPos()+")");
//                SimLogger.printLine("Host location is: ("+selHost.getLocation().getXPos()+","+selHost.getLocation().getYPos()+")");
//            }
//        }
//        else{
//            System.out.println("  Mobile device: "+mobile.getId()+"  WAP: "+mobile.getLocation().getServingWlanId()+"  Assigned host:  NULL");
//            unassignedMobileDeviceCount++;
//            System.exit(2453757);
//        }*/
//        System.gc();
//        //System.exit(68689);
//        return;
//        //}// end for - levelIter
    }

    private boolean attemptHostAssignment(ArrayList<EdgeHost> goodNodes, MobileDevice mobile) {
        Map<Double, List<NodeSim>> costMap = new HashMap<>();

        EdgeHost srcHost = mobileAccessHost; // sks0099 added thisline on 10/4/2025
        if(debug){
            System.out.println("Mobile Access Host Id for MD Id "+mobile.getId()+": "+srcHost.getId());
        }
        NodeSim srcNodeSim = new NodeSim(srcHost.getLocation().getXPos(), srcHost.getLocation().getYPos());

        HashMap<NodeSim, LinkedList<NodeSim>> tempMap = new HashMap<>();
        LinkedList<NodeSim> tempList = new LinkedList<>();
        for (NodeSim srcNode : networkModel.getNetworkTopology().getNodes()) { // srcNode is the source host
            if (srcNode.getLocation().getXPos() == srcNodeSim.getLocation().getXPos() &&
                    srcNode.getLocation().getYPos() == srcNodeSim.getLocation().getYPos()) {
                //System.out.println("ertgutr577");
                //HashMap<NodeSim, LinkedList<NodeSim>> tempMap = new HashMap<>();
                for (NodeSim desHost : networkModel.getNetworkTopology().getNodes()) {
                    EdgeHost desEdgeHost = SimManager.getInstance().getLocalServerManager().
                            findHostByLoc(desHost.getLocation().getXPos(), desHost.getLocation().getYPos(),
                                    desHost.getLocation().getAltitude());
                    if(goodNodes.contains(desEdgeHost)) {
                        //tempList.removeAll(tempList);// = new LinkedList<>();
                        tempList = new LinkedList<>();
                        tempList = networkModel.findPath(srcNode, desHost); // desHost is the destination host
                        tempMap.put(desHost, tempList);
                    }
                }
                //pathTable.put(new NodeSim(srcNode.getLocation().getXPos(),srcNode.getLocation().getYPos()), tempMap);
                pathTableUsingLocation.put(srcNode.getLocation(), tempMap);
                pathTableUsingMobileAccessHostId.put(srcHost.getId(), tempMap);
            }
        }

        //Map<NodeSim, LinkedList<NodeSim>> desMap = pathTable.get(srcNodeSim);
        /*if(pathTableUsingLocation.containsKey(srcNodeSim.getLocation())){
            System.out.println("exists");
        }else{
            System.out.println("doesn't exist");
        }*/
        //Map<NodeSim, LinkedList<NodeSim>> desMap = pathTableUsingLocation.get(srcNodeSim.getLocation());
        Map<NodeSim, LinkedList<NodeSim>> desMap = tempMap;
        /*Boolean foundSource = false;
        Integer maxNumberOfElements = pathTable.size();
        Integer countElementsSearched = 0;
        for (NodeSim key : pathTable.keySet()) {
            //System.out.println("Key: " + key + ", Value.get(0): " + pathTable.get(key).get(0));
            if(key.equals(srcNodeSim)){
                System.out.println("hi");
            }
        }*/

        /*while(!foundSource && countElementsSearched < maxNumberOfElements){
            if(pathTable)
        }*/

        // Prune the set of paths to include only the prospective fog nodes from goodNodesPerLayer list


        Map<Integer, Integer> latencyRank = new LinkedHashMap<>();
        Integer selHostId = -1;
        if (selectHostByLeastLatency) {
            //HostId_Latency.forEach();
            TreeMap<Integer, Double> HostId_LatencyTreeMap = new TreeMap<>();
            for (Map.Entry<Integer, Double> HostId_LatencyEntry : HostId_Latency.entrySet()) {
                //System.out.println(entry.getKey() + ": " + entry.getKey().getId());
                //bdMobileDevice.put(entry.getKey().getId(), entry.getKey());
                //bdEdgeHost.put(entry.getValue().getId(), entry.getValue());
                HostId_LatencyTreeMap.put(HostId_LatencyEntry.getKey(), HostId_LatencyEntry.getValue());

            }

            // Convert entries to a List
            List<Map.Entry<Integer, Double>> distanceEntryList = new ArrayList<>(HostId_LatencyTreeMap.entrySet());

            // Sort the list by Double values in ascending order
            Collections.sort(distanceEntryList, new Comparator<Map.Entry<Integer, Double>>() {
                @Override
                public int compare(Map.Entry<Integer, Double> entry1, Map.Entry<Integer, Double> entry2) {
                    // Compare values
                    int valueComparison = entry1.getValue().compareTo(entry2.getValue());

                    // If values are equal, sort by key to ensure stable sorting and prevent loss of entries in case of re-insertion into a map that doesn't allow duplicate keys (like a TreeMap).
                    if (valueComparison == 0) {
                        return entry1.getKey().compareTo(entry2.getKey());
                    }
                    return valueComparison;
                }
            });

            // Create a new LinkedHashMap to maintain the sorted order by value
            Map<Integer, Double> sortedByValueMap = new LinkedHashMap<>();
            //Integer firstKey = 0;
            for (Map.Entry<Integer, Double> distanceEntry : distanceEntryList) {
                if (goodHostWithoutCheckingLatency(bdEdgeHost.get(distanceEntry.getKey()), mobile)) {
                    sortedByValueMap.put(distanceEntry.getKey(), distanceEntry.getValue());
                }
                //firstKey = entry.getKey();
                //break;
            }
            //firstKey = sortedByValueMap.keySet().iterator().next();

            latencyRank = getRank(sortedByValueMap);
            //sortedHostId_LatencyMap.forEach((key, value) -> System.out.println(key + ": " + value +","+bdEdgeHost.get(value).getTotalMips()+","+bdEdgeHost.get(value).getReserveMips()));
            //TreeMap<Integer, Double> sortedHostId_LatencyMap = new TreeMap<>();
            //entriesSortedByValues(HostId_LatencyTreeMap);
            //sortedByValueMap.forEach((key, value) -> System.out.println(key + ": " + value)+" Rank: "+latencyRank.get(key));
            if(debug) {
                for (Map.Entry<Integer, Double> entry : sortedByValueMap.entrySet()) {
                    System.out.println(entry.getKey() + ": " + entry.getValue() + " Rank: " + latencyRank.get(entry.getKey()));
                }
            }
            //System.out.println("firstKey: "+firstKey);

            //if (selHostConsideringLatencyOnly != null) {
            //    System.out.println("  Mobile device: "+mobile.getId()+"  WAP: "+mobile.getLocation().getServingWlanId()+"  Assigned host:  "+selHost.getId());
            //return;
            //}
            if(!selectHostByLeastCost) {
                for (Map.Entry<Integer, Double> entry : sortedByValueMap.entrySet()) {
                    //System.out.println(entry.getKey() + ": " + entry.getValue() + " Rank: " + latencyRank.get(entry.getKey()));
                    selHostId = entry.getKey();
                    break;
                }
            }else{
                Map<NodeSim, LinkedList<NodeSim>> selectedDesMap = new HashMap<>();
                Map<Integer, LinkedList<NodeSim>> selectedDesMapUsingHostId = new HashMap<>();

                for (int i = 0; i < goodNodes.size(); i++) {

                    // for each host, get the NodeSim object
                    Location hostLoc = goodNodes.get(i).getLocation();
                    NodeSim hostNode = ((ESBModel) networkModel).getNetworkTopology().findNode(hostLoc, false);

                    // for each such NodeSim object, retrieve the row and add it to selectedDesMap
                    selectedDesMap.put(hostNode, desMap.get(hostNode));
                    selectedDesMapUsingHostId.put(goodNodes.get(i).getId(), desMap.get(hostNode));
                }

                // continue with processing as earlier.
                TreeMap<Integer, Double> HostId_CostTreeMap = new TreeMap<>();

                // Convert entries to a List

                for (Map.Entry<NodeSim, LinkedList<NodeSim>> entry : selectedDesMap.entrySet()) {
                    double cost = 0;
                    NodeSim des = entry.getKey();
                    LinkedList<NodeSim> path = entry.getValue();
                    if (path == null || path.size() == 0) {
                        EdgeHost k = SimManager.getInstance().getLocalServerManager().findHostByLoc(mobile.getLocation().getXPos(), mobile.getLocation().getYPos(), mobile.getLocation().getAltitude());

                        //double bwCost = mobile.getBWRequirement() * k.getCostPerBW();
                        double bwCost = (mobile.getBWRequirement() * 8 / (double) 1024) * k.getCostPerBW(); //mobile.getBWRequirement() in KB * 8b/B ==>Kb / 1024 = Mb; k.getCostPerBW() in $/Mb -- Shaik modified

                        //double exCost = (double)mobile.getTaskLengthRequirement() / k.getTotalMips() * k.getCostPerSec();
                        double exCost = (double) mobile.getTaskLengthRequirement() / (k.getPeList().get(0).getMips()) * k.getCostPerSec(); // Shaik modified - May 07, 2019.

                        cost = cost + bwCost;
                        //SimLogger.getInstance().getCentralizeLogPrinter().println("Level:\t" + des.getLevel() + "\tNode:\t" + des.getWlanId() + "\tBWCost:\t" + bwCost + "\tTotalBWCost:\t" + cost);
                        //SimLogger.getInstance().getCentralizeLogPrinter().println("Total data:\t" + mobile.getBWRequirement() + "\tBWCostPerSec:\t" + k.getCostPerBW());
                        cost = cost + exCost;
                        //SimLogger.getInstance().getCentralizeLogPrinter().println("Destination:\t"+ des.getWlanId() + "\tExecuteCost:\t" + exCost + "\tTotalCost:\t" + cost);
                        //SimLogger.getInstance().getCentralizeLogPrinter().println("Service CPu Time:\t" + ((double)mobile.getTaskLengthRequirement() / k.getTotalMips()) + "\tMipsCostPerSec:\t" + k.getCostPerSec());
                        //System.out.println("Host Id: "+k.getId()+" cost: "+cost);
                        HostId_CostTreeMap.put(k.getId(), cost);
                    } else {
                        //SimLogger.getInstance().getCentralizeLogPrinter().println("**********Path From " + src.getWlanId() + " To " + des.getWlanId() + "**********");
                        for (NodeSim node : path) {
                            EdgeHost k = SimManager.getInstance().getLocalServerManager().findHostByLoc(node.getLocation().getXPos(), node.getLocation().getYPos(), node.getLocation().getAltitude());
                            //double bwCost = mobile.getBWRequirement() * k.getCostPerBW();
                            double bwCost = (mobile.getBWRequirement() * 8 / (double) 1024) * k.getCostPerBW(); //mobile.getBWRequirement() in KB * 8b/B ==>Kb / 1024 = Mb; k.getCostPerBW() in $/Mb -- Shaik modified

                            cost = cost + bwCost;
                            //SimLogger.getInstance().getCentralizeLogPrinter().println("Level:\t" + node.getLevel() + "\tNode:\t" + node.getWlanId() + "\tBWCost:\t" + bwCost + "\tTotalBWCost:\t" + cost);
                            //SimLogger.getInstance().getCentralizeLogPrinter().println("Total data:\t" + mobile.getBWRequirement() + "\tBWCostPerSec:\t" + k.getCostPerBW());
                        }
                        EdgeHost desHost = SimManager.getInstance().getLocalServerManager().findHostByLoc(des.getLocation().getXPos(), des.getLocation().getYPos(), des.getLocation().getAltitude());
                        //double exCost = desHost.getCostPerSec() * ((double)mobile.getTaskLengthRequirement() / desHost.getTotalMips());
                        double exCost = (double) mobile.getTaskLengthRequirement() / (desHost.getPeList().get(0).getMips()) * desHost.getCostPerSec(); // Shaik modified - May 07, 2019.

                        cost = cost + exCost;
                        //SimLogger.getInstance().getCentralizeLogPrinter().println("Destination:\t"+ des.getWlanId() + "\tExecuteCost:\t" + exCost + "\tTotalCost:\t" + cost);
                        //SimLogger.getInstance().getCentralizeLogPrinter().println("Service CPU time:\t" + ((double)mobile.getTaskLengthRequirement() / desHost.getTotalMips()) + "\tMipsCostPerSec:\t" + desHost.getCostPerSec());
                        //System.out.println("Host Id: "+desHost.getId()+" cost: "+cost);
                        HostId_CostTreeMap.put(desHost.getId(), cost);
                    }


                    if (costMap.containsKey(cost)) {
                        if (!costMap.get(cost).contains(des)) {
                            costMap.get(cost).add(des);
                        }
                    } else {
                        ArrayList<NodeSim> desList = new ArrayList<>();
                        desList.add(des);
                        costMap.put(cost, desList);
                    }

                }

                //Sort the list of prospective nodes by increasing cost
                LinkedList<EdgeHost> hostsSortedByCost = new LinkedList<EdgeHost>();

                //Create a sorted list of costs
                List<Double> costList = new ArrayList<Double>(costMap.keySet());
                Collections.sort(costList);

                List<Map.Entry<Integer, Double>> costEntryList = new ArrayList<>(HostId_CostTreeMap.entrySet());
                // Create a new LinkedHashMap to maintain the sorted order by value
                Map<Integer, Double> sortedByCostValueMap = new LinkedHashMap<>();
                //Integer firstKey = 0;

                // Sort the list by Double values in ascending order
                Collections.sort(costEntryList, new Comparator<Map.Entry<Integer, Double>>() {
                    @Override
                    public int compare(Map.Entry<Integer, Double> entry1, Map.Entry<Integer, Double> entry2) {
                        // Compare values
                        int valueComparison = entry1.getValue().compareTo(entry2.getValue());

                        // If values are equal, sort by key to ensure stable sorting and prevent loss of entries in case of re-insertion into a map that doesn't allow duplicate keys (like a TreeMap).
                        if (valueComparison == 0) {
                            return entry1.getKey().compareTo(entry2.getKey());
                        }
                        return valueComparison;
                    }
                });

                for (Map.Entry<Integer, Double> costEntry : costEntryList) {
                    //if(goodHostWithoutCheckingLatency(bdEdgeHost.get(distanceEntry.getKey()), mobile)) {
                    sortedByCostValueMap.put(costEntry.getKey(), costEntry.getValue());
                    //}
                }
                //firstKey = sortedByValueMap.keySet().iterator().next();
                Map<Integer, Integer> costRank = new LinkedHashMap<>();
                //Collections.sort(sortedByCostValueMap);
                costRank = getRank(sortedByCostValueMap);
                //sortedHostId_LatencyMap.forEach((key, value) -> System.out.println(key + ": " + value +","+bdEdgeHost.get(value).getTotalMips()+","+bdEdgeHost.get(value).getReserveMips()));
                //TreeMap<Integer, Double> sortedHostId_LatencyMap = new TreeMap<>();
                //entriesSortedByValues(HostId_LatencyTreeMap);
                //sortedByValueMap.forEach((key, value) -> System.out.println(key + ": " + value)+" Rank: "+latencyRank.get(key));
                if (debug) {
                    for (Map.Entry<Integer, Double> costEntry : sortedByCostValueMap.entrySet()) {
                        System.out.println(costEntry.getKey() + ": " + costEntry.getValue() + " Rank: " + costRank.get(costEntry.getKey()));
                    }
                }

                Map<Integer, Integer> combinedRank = new LinkedHashMap<>();
                //Integer selHostId = -1;
                Integer currentCombinedRank = 0;
                Integer minimumCombinedRank = 100000;
                Integer entryCount = 0;
                for (Map.Entry<Integer, Double> costEntry : sortedByCostValueMap.entrySet()) {
                /*System.out.println(costEntry.getKey() + ": " + costEntry.getValue() + " Rank: " + costRank.get(costEntry.getKey())
                        + " LatencyRank: " + latencyRank.get(costEntry.getKey()) + " combined rank: " +
                        (costRank.get(costEntry.getKey()) + latencyRank.get(costEntry.getKey())));*/
                    if (entryCount == 0) {
                        selHostId = costEntry.getKey();
                        currentCombinedRank = (costRank.get(costEntry.getKey()) + latencyRank.get(costEntry.getKey()));
                        minimumCombinedRank = currentCombinedRank;
                    } else {
                        currentCombinedRank = (costRank.get(costEntry.getKey()) + latencyRank.get(costEntry.getKey()));
                        if (currentCombinedRank < minimumCombinedRank) {
                            minimumCombinedRank = currentCombinedRank;
                            selHostId = costEntry.getKey();
                        }
                    }
                    entryCount++;
                }
            }
        }
        //System.exit(765467376);
        /*if(selectHostByLeastCost) {

        }*/
        if(debug) System.out.println("Selected Host Id: " + selHostId);
        if (selHostId == -1) {
            //System.out.println("  Mobile device: " + mobile.getId() + "  WAP: " + mobile.getLocation().getServingWlanId() + "  Assigned host:  NULL");
            //unassignedMobileDeviceCount++;
            return false;
        } else {
            EdgeHost selHost = bdEdgeHost.get(selHostId);
            LinkedList<NodeSim> path = ((ESBModel) SimManager.getInstance().getNetworkModel()).findPath(selHost, mobile);
            mobile.setPath(path);
            mobile.setHost(selHost);
            //System.out.println("Before reservation: "+selHost.getTotalMips()+", "+selHost.getAvailableMips()+", "+selHost.getReserveMips());
            //mobile.setAppType(SimSettings.APP_TYPES.AUGMENTED_REALITY); // added by sks0099 on 09112025 for testing
            //mobile.makeReservation(); // added by sks0099 on 09112025 for testing
            //mobile.makeReservation(); // added by sks0099 on 09112025 for testing
            mobile.makeReservation();
            //System.out.println("After reservation: "+selHost.getTotalMips()+", "+selHost.getAvailableMips()+", "+selHost.getReserveMips());
            //System.exit(97979);
            Integer currentAssignmentCount = 0;
            if (Host_AssignedMobileCount.containsKey(selHost)) {
                currentAssignmentCount = Host_AssignedMobileCount.get(selHost);
            }
            Host_AssignedMobileCount.put(selHost, currentAssignmentCount + 1);
            mobileHostHop.put(mobile, HostId_Hops.get(selHostId));
            selHost.setHop(HostId_Hops.get(selHostId));
            this.hops[mobile.getId()] = HostId_Hops.get(selHostId);
            System.out.println("  Assigned host: " + selHost.getId() + " total assignment: " + (currentAssignmentCount + 1) + " Estimated latency: " + HostId_Latency.get(selHost.getId())
                    + " Voronoi traversal hops (mobile to the host): " + HostId_Hops.get(selHostId));

            searchHop.put(mobile, HostId_SearchHops.get(selHostId));
            selHost.setSearchHop(HostId_SearchHops.get(selHostId));
            this.searchHops[mobile.getId()] = HostId_SearchHops.get(selHostId);
            if(debug) {
                System.out.println("  Assigned host: " + selHost.getId() + " total assignment: " + (currentAssignmentCount + 1) + " Estimated latency: " + HostId_Latency.get(selHost.getId())
                        + " Search hops (mobile to the host): " + HostId_SearchHops.get(selHostId));
            }

            mobileHostAssignment.put(mobile, selHost);
        /*if(currentAssignmentCount+1>maximumMobileAssignment){
            System.out.println("Total mobile assignment to host exceeded the maximum limit. Debugging needed!");
            System.exit(8896);
        }*/
            double distanceBetweenMobileAndHost = DataInterpreter.measure(mobile.getLocation().getYPos(), mobile.getLocation().getXPos(),
                    selHost.getLocation().getYPos(), selHost.getLocation().getXPos());
            if (SimSettings.getInstance().traceEnable()) {
                SimLogger.printLine("Distance from mobile to the host is: " +
                        distanceBetweenMobileAndHost + " m");
                SimLogger.printLine("Mobile location is: (" + mobile.getLocation().getXPos() + "," + mobile.getLocation().getYPos() + ")");
                SimLogger.printLine("Host location is: (" + selHost.getLocation().getXPos() + "," + selHost.getLocation().getYPos() + ")");
            }
            //return;
        }
        //Access the map entries in order sorted by cost
        /*EdgeHost host = null;
        for (Double totalCost : costList) {
            // Get the list of prospective nodes available at that cost
            for(NodeSim desNode: costMap.get(totalCost)) {
                host = SimManager.getInstance().getLocalServerManager().findHostByLoc(desNode.getLocation().getXPos(), desNode.getLocation().getYPos(), desNode.getLocation().getAltitude());

                hostsSortedByCost.add(host);
                //}
                System.out.println("Hosts in sorted order of costs:  "+host.getId()+"  at cost:  "+totalCost);
            }
        }

        //Find a cost-optimal node with available resources
        if (hostsSortedByCost.size() == 0) {
            System.out.println("  Mobile device: "+mobile.getId()+"  WAP: "+mobile.getLocation().getServingWlanId()+"  Assigned host:  NULL");
            unassignedMobileDeviceCount++;
            return;
        }*/

        /*EdgeHost selHost = null;
        selHost = hostsSortedByCost.poll();
        if (selHost == null) {
            System.out.println("  Mobile device: "+mobile.getId()+"  WAP: "+mobile.getLocation().getServingWlanId()+"  Assigned host:  NULL");
            unassignedMobileDeviceCount++;
            return;
        }

        System.out.print("Prospective host:  ");
        while(!goodHost(selHost, mobile)) {
            if (selHost == null) {selHost = hostsSortedByCost.poll();//find the next cost-optimal node capable of handling the task
                break;
            }
        }
        if (selHost != null) {
            if(selectHostByLeastLatency && selHostConsideringLatencyOnly != null){
                selHost = selHostConsideringLatencyOnly;
            }
            LinkedList<NodeSim> path = ((ESBModel)SimManager.getInstance().getNetworkModel()).findPath(selHost, mobile);
            mobile.setPath(path);
            mobile.setHost(selHost);
            //System.out.println("Before reservation: "+selHost.getTotalMips()+", "+selHost.getAvailableMips()+", "+selHost.getReserveMips());
            //mobile.setAppType(SimSettings.APP_TYPES.AUGMENTED_REALITY); // added by sks0099 on 09112025 for testing
            //mobile.makeReservation(); // added by sks0099 on 09112025 for testing
            //mobile.makeReservation(); // added by sks0099 on 09112025 for testing
            mobile.makeReservation();
            //System.out.println("After reservation: "+selHost.getTotalMips()+", "+selHost.getAvailableMips()+", "+selHost.getReserveMips());
            //System.exit(97979);
            Integer currentAssignmentCount = 0;
            if(Host_AssignedMobileCount.containsKey(selHost)) {
                currentAssignmentCount = Host_AssignedMobileCount.get(selHost);
            }
            Host_AssignedMobileCount.put(selHost, currentAssignmentCount + 1);
            System.out.println("  Assigned host: " + selHost.getId()+" total assignment: "+(currentAssignmentCount + 1)+" Estimated latency: "+HostId_Latency.get(selHost.getId()));
            mobileHostAssignment.put(mobile, selHost);

            double distanceBetweenMobileAndHost = DataInterpreter.measure(mobile.getLocation().getYPos(), mobile.getLocation().getXPos(),
                    selHost.getLocation().getYPos(), selHost.getLocation().getXPos());
            if (SimSettings.getInstance().traceEnable()) {
                SimLogger.printLine("Distance from mobile to the host is: "+
                        distanceBetweenMobileAndHost +" m");
                SimLogger.printLine("Mobile location is: ("+mobile.getLocation().getXPos()+","+mobile.getLocation().getYPos()+")");
                SimLogger.printLine("Host location is: ("+selHost.getLocation().getXPos()+","+selHost.getLocation().getYPos()+")");
            }
        }
        else{
            System.out.println("  Mobile device: "+mobile.getId()+"  WAP: "+mobile.getLocation().getServingWlanId()+"  Assigned host:  NULL");
            unassignedMobileDeviceCount++;
            System.exit(2453757);
        }*/
        System.gc();
        return true;
    }

    public HashMap<MobileDevice, EdgeHost> getMobileAssignment(){
        return mobileHostAssignment;
    }

    public int getUnassignedMobileDeviceCount(){
        return unassignedMobileDeviceCount;
    }

    public int getAssignedToCloudMobileDeviceCount(){
        return assignedToCloudMobileDeviceCount;
    }

    // getNearestGoodHost is a recursive method that starts from the given nearest node
    // and keeps getting neighbors, and their neighbors until a good host is found. If
    // all the nodes are checked and none is found good, null is returned.
    private EdgeHost getNearestGoodHost(Point nearest, MobileDevice md, ArrayList<Point> visitedNodes, ArrayList<Point> completedNodes){
        if(visitedNodes.size()==0) {
            return null;
        }
        EdgeHost nearestEH =  null;
        EdgeHost tempNearest = null;
        ArrayList<Point> Neighbors = new ArrayList<Point>();
        Integer likelyNeighborCount = 0;
        if(partition.getVoronoiNeighborSiteMapXY().size() == 0){
            return null;
        }
        for(Point neighbor: partition.getVoronoiNeighborSiteMapXY().get(nearest)) {
            //if(neighbor == null) return null;
            if (!visitedNodes.contains(neighbor) && !completedNodes.contains(neighbor)) {
                likelyNeighborCount++;
                // Request sent to nearest edge host node by the access point
                tempNearest = pointToHostMapXY.get(neighbor);
                this.addNumMessages(md.getId(), 1);

                if (goodHost(tempNearest, md)) {
                    return tempNearest;
                }else{
                    visitedNodes.add(neighbor);
                }
            }
            /*if (!visitedNodes.contains(neighbor)){
                visitedNodes.add(neighbor);
            }*/
            //Neighbors.add(neighbor);
        }
        if(likelyNeighborCount == 0) return null;
        visitedNodes.remove(nearest);
        completedNodes.add(nearest);
        return getNearestGoodHost(visitedNodes.get(0), md, visitedNodes, completedNodes);
        //return nearestEH;
    }

    @Override
    public void addNumMessages(int deviceId, int msgCount) {
        this.numMessages[deviceId] += msgCount;
    }


    /**
     *
     * @return
     */
    @Override
    public double getAvgNumMessages() {
        int devCount = SimManager.getInstance().getNumOfMobileDevice();
        int assignedDevCount = 0;
        int totalNumMessages = 0;

        for (int i=0; i<devCount; i++) {
            MobileDevice mb = SimManager.getInstance().getMobileDeviceManager().getMobileDevices().get(i);
            if (mb.getHost() != null) {
                assignedDevCount++;
                totalNumMessages += this.numMessages[i];
            }
        }

        return ((double)totalNumMessages / assignedDevCount );
    }

    // getAverageHops computes the average hops in assigning hosts
    public double getAverageHops() {
        int devCount = SimManager.getInstance().getNumOfMobileDevice();
        int assignedDevCount = 0;
        int totalNumHops = 0;

        for (int i=0; i<devCount; i++) {
            MobileDevice mb = SimManager.getInstance().getMobileDeviceManager().getMobileDevices().get(i);
            if (mb.getHost() != null) {
                assignedDevCount++;
                totalNumHops += this.hops[i];
            }
        }

        return ((double)totalNumHops / assignedDevCount );
    }

    // getAverageSearchHops computes the average search hops in assigning hosts where search hop means going from a
    // node that has been searched to another node that needs to be searched

    public double getAverageSearchHops() {
        int devCount = SimManager.getInstance().getNumOfMobileDevice();
        int assignedDevCount = 0;
        int totalNumSearchHops = 0;

        for (int i=0; i<devCount; i++) {
            MobileDevice mb = SimManager.getInstance().getMobileDeviceManager().getMobileDevices().get(i);
            if (mb.getHost() != null) {
                assignedDevCount++;
                totalNumSearchHops += this.searchHops[i];
            }
        }

        return ((double)totalNumSearchHops / assignedDevCount );
    }


    public void addNumHostsSearched(int deviceId, int numberOfHostsSearched) {
        this.numHostsSearched[deviceId] += numberOfHostsSearched;
    }

    public int[] getNumHostsSearched() {
        return this.numHostsSearched;
    }
    /**
     *
     * @return
     */
    @Override
    public double getAvgNumHostsSearched() {
        int devCount = SimManager.getInstance().getNumOfMobileDevice();
        int assignedDevCount = 0;
        int totalHostsSearched = 0;

        for (int i=0; i<devCount; i++) {
            MobileDevice mb = SimManager.getInstance().getMobileDeviceManager().getMobileDevices().get(i);
            if (mb.getHost() != null) {
                assignedDevCount++;
                totalHostsSearched += this.numHostsSearched[i];
            }
        }

        return ((double)totalHostsSearched / assignedDevCount );
    }

    public int[] getNumProspectiveHosts() {
        return this.numProspectiveHosts;
    }

}
