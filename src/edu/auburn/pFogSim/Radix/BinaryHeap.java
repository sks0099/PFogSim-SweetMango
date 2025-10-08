package edu.auburn.pFogSim.Radix;


import edu.auburn.pFogSim.netsim.ESBModel;
import edu.auburn.pFogSim.util.DataInterpreter;
import edu.auburn.pFogSim.util.MobileDevice;
import edu.boun.edgecloudsim.core.SimManager;
import edu.boun.edgecloudsim.edge_server.EdgeHost;
import edu.boun.edgecloudsim.utils.Location;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.NoSuchElementException;



public class BinaryHeap {
	
	public class BinaryHeapElement{ //Java has no structs.
		public Double distance, latency, costPerSecDiff, mipsDiff;
		public EdgeHost edgeHost;
	}
	
	public enum HeapChoice {Distance, Latency, CostPerSec, MIPS};
	
	/** The number of children each node has **/
    private static final int d = 2;
    private int heapSize;
    /* To those who come after, This binary heap maintains two separate heaps due to the requirement
     * to sort by latency and distance both. When this class was starting to be created, this requirement was 
     * not known. As such, using this class instead of the Radix sort is a trade off, Radix is slower but uses
     * less memory in this case. */
    private BinaryHeapElement[] distanceHeap; 
    private BinaryHeapElement[] latencyHeap;
	private BinaryHeapElement[] costPerSecHeap;
	private BinaryHeapElement[] mipsHeap;
    private ArrayList<EdgeHost> nodes;
    private Location ref;
	private MobileDevice mobileRef;
	Boolean isDistance = false;
	Boolean isLatency = false;
	Boolean isCostPerSec = false;
	Boolean isMIPS = false;
 
    /** Constructor **/    
    public BinaryHeap(int capacity, Location _ref, ArrayList<EdgeHost> in)
    {
        heapSize = 0;
        distanceHeap = new BinaryHeapElement[capacity];
        latencyHeap = new BinaryHeapElement[capacity];
		costPerSecHeap = new BinaryHeapElement[capacity];
		mipsHeap = new BinaryHeapElement[capacity];
        ref = _ref;
        nodes = in;
		isDistance = true;
		isLatency = true;
		isCostPerSec = false;
		isMIPS = false;
        init();
    }

	public BinaryHeap(int capacity, MobileDevice _refMobile, ArrayList<EdgeHost> in)
	{
		heapSize = 0;
		distanceHeap = new BinaryHeapElement[capacity];
		latencyHeap = new BinaryHeapElement[capacity];
		costPerSecHeap = new BinaryHeapElement[capacity];
		mipsHeap = new BinaryHeapElement[capacity];
		mobileRef = _refMobile;
		ref = _refMobile.getLocation();
		nodes = in;
		isDistance = false;
		isLatency = false;
		isCostPerSec = false;
		isMIPS = true;
		init();
	}
    
    //Insert each node into heaps
    private void init() {
    	for (EdgeHost node : nodes) {
			this.insert(node);
		}
    }
 
    /** Function to check if heap is empty **/
    public boolean isEmpty( )
    {
        return heapSize == 0;
    }
 
    /** Check if heap is full **/
    public boolean isFull( )
    {
     	if(isDistance || isLatency) {
			return heapSize == distanceHeap.length;
		}else if(isCostPerSec){
			return heapSize == costPerSecHeap.length;
		}else{
			return heapSize == mipsHeap.length;
		}
    }
 
    /** Clear heap */
    public void makeEmpty( )
    {
        heapSize = 0;
    }
 
    /** Function to  get index parent of i **/
    private int parent(int i) 
    {
        return (i - 1)/d;
    }
 
    /** Function to get index of k th child of i **/
    private int kthChild(int i, int k) 
    {
        return d * i + k;
    }
 
    /** Function to insert element */
    public void insert(EdgeHost x)
    {
    	
    	BinaryHeapElement e = new BinaryHeapElement();
    	
    	//Fill out a HeapElement for EdgeHost x
		Location l = new Location(x.getLocation().getXPos(), x.getLocation().getYPos(), x.getLocation().getAltitude());
		// Zuaiter Correct swapped latitude/longitude
		if(isDistance || isLatency) {
			e.distance = DataInterpreter.measure(ref.getYPos(), ref.getXPos(), ref.getAltitude(), l.getYPos(), l.getXPos(), l.getAltitude());
			e.latency = ((ESBModel) SimManager.getInstance().getNetworkModel()).getDelay(ref, l);
		}else if(isCostPerSec){
			e.costPerSecDiff = ((ESBModel)SimManager.getInstance().getNetworkModel()).getCostPerSecDiff(mobileRef, x);
		}else if(isMIPS){
			e.mipsDiff = ((ESBModel)SimManager.getInstance().getNetworkModel()).getMIPSDiff(mobileRef, x);
		}
		e.edgeHost = x;
    	
    	
        if (isFull( ) )
            throw new NoSuchElementException("Overflow Exception");
        
        // Store new element in both heaps
		if(isDistance || isLatency) {
			distanceHeap[heapSize] = e;
			latencyHeap[heapSize++] = e;
		} else if (isCostPerSec) {
			costPerSecHeap[heapSize] = e;
		}else if(isMIPS)
		{
			mipsHeap[heapSize] = e;
		}
        //Percolate Up
		//heapifyUp(HeapChoice.Distance, heapSize - 1);
		//heapifyUp(HeapChoice.Latency, heapSize - 1);
		if(isDistance || isLatency) {
			heapifyUp(HeapChoice.Distance, heapSize - 1);
			heapifyUp(HeapChoice.Latency, heapSize - 1);
		}else if(isCostPerSec){
			heapifyUp(HeapChoice.CostPerSec, heapSize);
		}else if(isMIPS){
			heapifyUp(HeapChoice.MIPS, heapSize);
		}

		/*if(isMIPS) {
			printHeap(HeapChoice.MIPS);
		}else if(isDistance){
			printHeap(HeapChoice.Distance);
		}*/
    }
 
    /** Function to find least element 
     * @throws Exception **/
	public EdgeHost findMin( HeapChoice mode ) throws Exception
    {
        if (isEmpty() )
            throw new NoSuchElementException("Underflow Exception");           
        if (mode == HeapChoice.Distance) {
			return distanceHeap[0].edgeHost;
		}else if (mode == HeapChoice.Latency) {
			return latencyHeap[0].edgeHost;
		}else if (mode == HeapChoice.CostPerSec) {
			return costPerSecHeap[0].edgeHost;
		}else if (mode == HeapChoice.MIPS) {
			return mipsHeap[0].edgeHost;
		}else{
			throw new Exception();
		}
    }
 
    /** Function heapifyUp  **/
    private void heapifyUp(HeapChoice mode, int childInd)
    {
    	BinaryHeapElement tmp = null;
    	
    	switch (mode) { //Maintain BOTH heaps
			case Distance:
				tmp = distanceHeap[childInd];
				while (childInd > 0 && tmp.distance < distanceHeap[parent(childInd)].distance)
				{
					distanceHeap[childInd] = distanceHeap[ parent(childInd) ];
					childInd = parent(childInd);
				}
				distanceHeap[childInd] = tmp;
				break;
			case Latency:
				tmp = latencyHeap[childInd];
				while (childInd > 0 && tmp.latency < latencyHeap[parent(childInd)].latency)
				{
					latencyHeap[childInd] = latencyHeap[ parent(childInd) ];
					childInd = parent(childInd);
				}
				latencyHeap[childInd] = tmp;
				break;
			case CostPerSec:
				tmp = costPerSecHeap[childInd];
				while (childInd > 0 && tmp.costPerSecDiff < costPerSecHeap[parent(childInd)].costPerSecDiff)
				{
					costPerSecHeap[childInd] = costPerSecHeap[ parent(childInd) ];
					childInd = parent(childInd);
				}
				costPerSecHeap[childInd] = tmp;
				break;
			case MIPS:
				tmp = mipsHeap[childInd];
				while (childInd > 0 && tmp.mipsDiff > mipsHeap[parent(childInd)].mipsDiff) // Inequality sign changed for heap max
				{
					mipsHeap[childInd] = mipsHeap[ parent(childInd) ];
					childInd = parent(childInd);
				}
				mipsHeap[childInd] = tmp;
				break;
			default:
				break;
		}        
    }
 
    /** Function heapifyDown **/
    private void heapifyDown(HeapChoice mode, int ind)
    {
    	int child;
    	BinaryHeapElement tmp;
    	switch (mode){ //Maintain BOTH heaps
		case Distance:
	        tmp = distanceHeap[ ind ];
	        while (kthChild(ind, 1) < heapSize)
	        {
	            child = minChild(mode, ind);
	            if (distanceHeap[child].distance < tmp.distance)
	            	distanceHeap[ind] = distanceHeap[child];
	            else
	                break;
	            ind = child;
	        }
	        distanceHeap[ind] = tmp;
			break;
		
		case Latency:
	        tmp = latencyHeap[ ind ];
	        while (kthChild(ind, 1) < heapSize)
	        {
	            child = minChild(mode, ind);
	            if (latencyHeap[child].latency < tmp.latency)
	            	latencyHeap[ind] = latencyHeap[child];
	            else
	                break;
	            ind = child;
	        }
	        latencyHeap[ind] = tmp;
			break;
		case CostPerSec:
			tmp = costPerSecHeap[ ind ];
			while (kthChild(ind, 1) < heapSize)
			{
				child = minChild(mode, ind);
				if (costPerSecHeap[child].costPerSecDiff < tmp.costPerSecDiff)
					costPerSecHeap[ind] = costPerSecHeap[child];
				else
					break;
				ind = child;
			}
			costPerSecHeap[ind] = tmp;
			break;
		case MIPS:
			tmp = mipsHeap[ ind ];
			while (kthChild(ind, 1) < heapSize)
			{
				child = minChild(mode, ind);
				if (mipsHeap[child].mipsDiff < tmp.mipsDiff)
					mipsHeap[ind] = mipsHeap[child];
				else
					break;
				ind = child;
			}
			mipsHeap[ind] = tmp;
			break;
		default:
			break;
		}
        
    }
 
    /** Function to get smallest child **/
    private int minChild(HeapChoice mode, int ind) 
    {
    	int bestChild, k, pos;
    	switch (mode) { //Maintain BOTH heaps
		case Distance:
			bestChild = kthChild(ind, 1);
	        k = 2;
	        pos = kthChild(ind, k);
	        while ((k <= d) && (pos < heapSize)) 
	        {
	            if (latencyHeap[pos].latency < latencyHeap[bestChild].latency) 
	                bestChild = pos;
	            pos = kthChild(ind, k++);
	        }    
	        return bestChild;
		case Latency:
			bestChild = kthChild(ind, 1);
	        k = 2;
	        pos = kthChild(ind, k);
	        while ((k <= d) && (pos < heapSize)) 
	        {
	            if (latencyHeap[pos].latency < latencyHeap[bestChild].latency) 
	                bestChild = pos;
	            pos = kthChild(ind, k++);
	        }    
	        return bestChild;
		case CostPerSec:
			bestChild = kthChild(ind, 1);
			k = 2;
			pos = kthChild(ind, k);
			while ((k <= d) && (pos < heapSize))
			{
				if (costPerSecHeap[pos].costPerSecDiff < costPerSecHeap[bestChild].costPerSecDiff)
					bestChild = pos;
				pos = kthChild(ind, k++);
			}
			return bestChild;
		case MIPS:
			bestChild = kthChild(ind, 1);
			k = 2;
			pos = kthChild(ind, k);
			while ((k <= d) && (pos < heapSize))
			{
				if (mipsHeap[pos].mipsDiff < mipsHeap[bestChild].mipsDiff)
					bestChild = pos;
				pos = kthChild(ind, k++);
			}
			return bestChild;
		default:
			return -1;
		}
        
    }
 
    /** Function to print heap **/
    public void printHeap(HeapChoice mode)
    {
    	switch (mode) { //Maintain BOTH heaps
		case Distance:
			System.out.print("\nHeap = ");
	        for (int i = 0; i < heapSize; i++)
	            System.out.print(distanceHeap[i] +" ");
	        System.out.println();
			break;
		case Latency:
			System.out.print("\nHeap = ");
	        for (int i = 0; i < heapSize; i++)
	            System.out.print(latencyHeap[i] +" ");
	        System.out.println();
			break;
		case CostPerSec:
			System.out.print("\nHeap = ");
			for (int i = 0; i < heapSize; i++)
				System.out.print(costPerSecHeap[i] +" ");
			System.out.println();
			break;
		case MIPS:
			System.out.print("\nHeap = ");
			for (int i = 0; i < heapSize; i++)
				System.out.print(mipsHeap[i] +" ");
			System.out.println();
			break;
		default:
			break;
		}
        
    }
    
    public LinkedList<EdgeHost> getDistanceList() { //Compatibility with DistRadix code
    	LinkedList<EdgeHost> ret = new LinkedList<EdgeHost>();
    	for (int i = 0; i < distanceHeap.length; i++) {
			ret.add(distanceHeap[i].edgeHost);
		}
    	return ret;
    }
    
    public LinkedList<EdgeHost> getLatencyList() { //Compatibility with DistRadix code
    	LinkedList<EdgeHost> ret = new LinkedList<EdgeHost>();
    	for (int i = 0; i < latencyHeap.length; i++) {
			ret.add(latencyHeap[i].edgeHost);
		}
    	return ret;
    }

	public LinkedList<EdgeHost> getCostPerSecList() { //Compatibility with DistRadix code
		LinkedList<EdgeHost> ret = new LinkedList<EdgeHost>();
		for (int i = 0; i < costPerSecHeap.length; i++) {
			ret.add(costPerSecHeap[i].edgeHost);
		}
		return ret;
	}

	public LinkedList<EdgeHost> getMIPSList() { //Compatibility with DistRadix code
		LinkedList<EdgeHost> ret = new LinkedList<EdgeHost>();
		for (int i = 0; i < mipsHeap.length; i++) {
			ret.add(mipsHeap[i].edgeHost);
		}
		return ret;
	}
    
    public LinkedList<EdgeHost> sortNodes(){ //Compatibility with DistRadix code
    	return this.getDistanceList();
		//return this.getLatencyList();
    }

	public LinkedList<EdgeHost> sortNodesByCostPerSec(){ //Compatibility with DistRadix code
		return this.getCostPerSecList();
	}

	public LinkedList<EdgeHost> sortNodesByMIPS(){ //Compatibility with DistRadix code
		return this.getMIPSList();
	}
}
