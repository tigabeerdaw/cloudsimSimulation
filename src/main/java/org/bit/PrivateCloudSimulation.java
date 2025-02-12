package org.bit;

// Import necessary CloudSim classes

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Calendar;
import java.util.List;

/**
 * A simple simulation of a private cloud environment with a single datacenter, a broker, VMs, and cloudlets.
 */
public class PrivateCloudSimulation {
    public static void main(String[] args) {
        // Initialize CloudSim
        CloudSim.init(1, Calendar.getInstance(), false);

        // Create a datacenter
        Datacenter datacenter = createDatacenter("DatacenterOne");

        // Create a broker to act as an intermediary between users and the datacenter
        DatacenterBroker broker = createBroker();
        int brokerId = broker.getId();

        // Create virtual machines (VMs)
        List<Vm> vmList = createVms(brokerId);

        // Create cloudlets (tasks) to be executed on the VMs
        List<Cloudlet> cloudletList = createCloudlets(brokerId);

        // Submit VMs and cloudlets to the broker
        broker.submitGuestList(vmList);
        broker.submitCloudletList(cloudletList);

        // Start the simulation
        CloudSim.startSimulation();

        // Print results
        List<Cloudlet> finishedCloudlets = broker.getCloudletReceivedList();
        CloudSim.stopSimulation();

        printCloudletResults(finishedCloudlets);
    }

    private static Datacenter createDatacenter(String name) {
        // Create a list of hosts
        List<Host> hostList = new ArrayList<>();

        // Define the specifications of a host

        int mips = 100000; // 100,000 MIPS
        int ram = 65536; // 64 GB
        long storage = 2000000; // 2 TB
        int bw = 10000; // 10 Gbps

        // Create PEs (Processing Elements) for the host
        List<Pe> peList = new ArrayList<>();
        peList.add(new Pe(0, new PeProvisionerSimple(mips)));

        // Create a host and add it to the host list
        hostList.add(new Host(
                0,
                new RamProvisionerSimple(ram),
                new BwProvisionerSimple(bw),
                storage,
                peList,
                new VmSchedulerTimeShared(peList)
        ));

        // Define datacenter characteristics
        String arch = "x64";
        String os = "Linux";
        String vmm = "Xen";
        double timeZone = 3.0;
        double cost = 3.0;
        double costPerMem = 0.05;
        double costPerStorage = 0.001;
        double costPerBw = 0.0;

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, timeZone, cost, costPerMem, costPerStorage, costPerBw
        );

        // Create the datacenter
        try {
            return new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), new LinkedList<>(), 0);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static DatacenterBroker createBroker() {
        try {
            return new DatacenterBroker("Broker");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static List<Vm> createVms(int brokerId) {
        List<Vm> vmList = new ArrayList<>();

        // Define VM specifications
        int mips = 10000; // 10,000 MIPS
        long size = 100000; // 100 GB
        int ram = 8192; // 8 GB
        long bw = 1000; // 1 Gbps
        int pesNumber = 4; // 4 CPUs
        String vmm = "Xen";

        // Create VMs and add them to the list
        for (int i = 0; i < 2; i++) {
            vmList.add(new Vm(i, brokerId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared()));
        }

        return vmList;
    }

    private static List<Cloudlet> createCloudlets(int brokerId) {
        List<Cloudlet> cloudletList = new ArrayList<>();

        // Define cloudlet specifications
        long length = 1000; // Number of instructions
        long fileSize = 300; // Input file size in MB
        long outputSize = 100; // Output file size in MB
        int pesNumber = 1; // Number of CPUs

        // Create cloudlets and add them to the list
        for (int i = 0; i < 5; i++) {
            Cloudlet cloudlet = new Cloudlet(i, length, pesNumber, fileSize, outputSize, new UtilizationModelFull(), new UtilizationModelFull(), new UtilizationModelFull());
            cloudlet.setUserId(brokerId);
            cloudletList.add(cloudlet);
        }

        return cloudletList;
    }

    private static void printCloudletResults(List<Cloudlet> cloudletList) {
        System.out.println("Cloudlet ID\tStatus\tDatacenter ID\tVM ID\tTime\tStart Time\tFinish Time");
        for (Cloudlet cloudlet : cloudletList) {
            System.out.println(
                    cloudlet.getCloudletId() + "\t\t" +
                            cloudlet.getStatus() + "\t\t" +
                            cloudlet.getResourceId() + "\t\t" +
                            cloudlet.getVmId() + "\t\t" +
                            cloudlet.getActualCPUTime() + "\t\t" +
                            cloudlet.getExecStartTime() + "\t\t" +
                            cloudlet.getFinishTime()
            );
        }
    }
}