//package com.vogella.junit5;

import edu.auburn.pFogSim.util.DataInterpreter;
import edu.boun.ConstantsClass;
import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.edge_server.EdgeVM;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.ResCloudlet;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EdgeVMTest {

    EdgeVM EV;

    @BeforeEach
    void setUp() throws IOException {
        int id = 1;
        int userId = 1;
        double mips = 2.0;
        int numberOfPes = 25;
        int ram = 1024;
        long bw = 10453267;
        long size = 853377553;
        String vmm = "testVM";
        CloudletScheduler cloudletScheduler = new CloudletScheduler() {
            @Override
            public double updateVmProcessing(double v, List<Double> list) {
                return 0;
            }

            @Override
            public double cloudletSubmit(Cloudlet cloudlet, double v) {
                return 0;
            }

            @Override
            public double cloudletSubmit(Cloudlet cloudlet) {
                return 0;
            }

            @Override
            public Cloudlet cloudletCancel(int i) {
                return null;
            }

            @Override
            public boolean cloudletPause(int i) {
                return false;
            }

            @Override
            public double cloudletResume(int i) {
                return 0;
            }

            @Override
            public void cloudletFinish(ResCloudlet resCloudlet) {

            }

            @Override
            public int getCloudletStatus(int i) {
                return 0;
            }

            @Override
            public boolean isFinishedCloudlets() {
                return false;
            }

            @Override
            public Cloudlet getNextFinishedCloudlet() {
                return null;
            }

            @Override
            public int runningCloudlets() {
                return 0;
            }

            @Override
            public Cloudlet migrateCloudlet() {
                return null;
            }

            @Override
            public double getTotalUtilizationOfCpu(double v) {
                return 0;
            }

            @Override
            public List<Double> getCurrentRequestedMips() {
                return null;
            }

            @Override
            public double getTotalCurrentAvailableMipsForCloudlet(ResCloudlet resCloudlet, List<Double> list) {
                return 0;
            }

            @Override
            public double getTotalCurrentRequestedMipsForCloudlet(ResCloudlet resCloudlet, double v) {
                return 0;
            }

            @Override
            public double getTotalCurrentAllocatedMipsForCloudlet(ResCloudlet resCloudlet, double v) {
                return 0;
            }

            @Override
            public double getCurrentRequestedUtilizationOfRam() {
                return 0;
            }

            @Override
            public double getCurrentRequestedUtilizationOfBw() {
                return 0;
            }
        };
        EV = new EdgeVM(id, userId, mips, numberOfPes, ram, bw, size, vmm, cloudletScheduler);
    }

    @Test
    @DisplayName("Get Set VM type")
    void testGetSetVmType() {
        SimSettings.VM_TYPES tempVM_Type = SimSettings.VM_TYPES.values()[0];
        SimSettings.VM_TYPES expectedValue = tempVM_Type;
        EV.setVmType(tempVM_Type);
        SimSettings.VM_TYPES actualValue = EV.getVmType();
        assertEquals(expectedValue, actualValue, "Values should be exactly equal");
    }

    @Test
    @DisplayName("Get Set type")
    void testGetSetType() {
        SimSettings.VM_TYPES tempType = SimSettings.VM_TYPES.values()[0];
        SimSettings.VM_TYPES expectedValue = tempType;
        EV.setType(tempType);
        SimSettings.VM_TYPES actualValue = EV.getType();
        assertEquals(expectedValue, actualValue, "Values should be exactly equal");
    }
}
