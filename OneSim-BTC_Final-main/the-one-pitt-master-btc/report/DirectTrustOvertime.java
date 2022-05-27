/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package report;

import core.DTNHost;
import core.SimClock;
import core.SimScenario;
import core.UpdateListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import rLearn.QLearn;

/**
 *
 * @author Icho
 */
public class DirectTrustOvertime extends Report implements UpdateListener {
    
    private static Map<DTNHost, Map<DTNHost, ArrayList<Double>>> directTrustMap = new HashMap<DTNHost, Map<DTNHost, ArrayList<Double>>>();
    private static Map<DTNHost, ArrayList<Double>> suspensionMap = new HashMap<DTNHost, ArrayList<Double>>();
    private static List<Double> intervalTime = new ArrayList<Double>();
    private double lastUpdated = 0;
    private double updateInterval = 21600;
    
    public DirectTrustOvertime() {
        init();
    }
    
    protected void init() {
        super.init();
        List<DTNHost> verificator = SimScenario.getInstance().getVerificator();
        List<DTNHost> hosts = SimScenario.getInstance().getHosts();
        for (DTNHost v : verificator) {
            for (DTNHost m : hosts) {
                if (m.toString().startsWith("Mis") || m.toString().startsWith("Mes")) {
                    Map<DTNHost, ArrayList<Double>> initDT = new HashMap<DTNHost, ArrayList<Double>>();
                    ArrayList listDT = new ArrayList();
                    initDT.put(m, listDT);
                    if (!directTrustMap.containsKey(v)) {
                        directTrustMap.put(v, initDT);
                    } else {
                        directTrustMap.get(v).put(m, listDT);
                    }
                    ArrayList listSus = new ArrayList();
                    suspensionMap.put(m, listSus);
                }
            }
        }
    }
    
    @Override
    public void updated(List<DTNHost> hosts) {
        if (SimClock.getTime() - lastUpdated >= updateInterval) {
            lastUpdated = SimClock.getTime();
            intervalTime.add(lastUpdated);
            List<DTNHost> verificators = SimScenario.getInstance().getVerificator();
            for (DTNHost ver : verificators) {
                for (DTNHost h : hosts) {
                    if (h.toString().startsWith("Mes") || h.toString().startsWith("Mis")) {
                        ArrayList listDT = directTrustMap.get(ver).get(h);
                        listDT.add(QLearn.directTrust.get(ver).get(h));
                        Map<DTNHost, ArrayList<Double>> curDT = directTrustMap.get(ver);
                        curDT.put(h, listDT);
                        directTrustMap.put(ver, curDT);
                        ArrayList listSus = suspensionMap.get(h);
                        listSus.add(QLearn.suspension.get(h));
                        suspensionMap.put(h, listSus);
                    }
                }
            }
//            System.out.println(directTrustMap);
        }
        
    }
    
    @Override
    public void done() {
        write("Direct Trust Overtime");
        for (Map.Entry<DTNHost, Map<DTNHost, ArrayList<Double>>> listHost : directTrustMap.entrySet()) {
            out.print("," + listHost.getKey());
            for (Double time : intervalTime) {
                out.print("," + time);
            }
            write("");
            for (Map.Entry<DTNHost, ArrayList<Double>> mapDT : listHost.getValue().entrySet()) {
                out.print(mapDT.getKey());
                for (Double DT : mapDT.getValue()) {
                    out.print("," + DT);
                }
                write("");
            }
        }
        write("Indirect Trust");
        for (Map.Entry<DTNHost, ArrayList<Double>> listIT : QLearn.indirectTrust.entrySet()) {
            out.print(listIT.getKey().toString());
            for (Double IT : listIT.getValue()) {
                out.print("," + IT);
            }
            write("");
        }
        write("Suspension");
        for (Map.Entry<DTNHost, ArrayList<Double>> listSus : suspensionMap.entrySet()) {
            out.print(listSus.getKey().toString());
            for (Double sus : listSus.getValue()) {
                out.print("," + sus);
            }
            write("");
        }
        super.done();
    }
}
