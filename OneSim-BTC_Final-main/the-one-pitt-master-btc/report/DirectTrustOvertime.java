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

    private static Map<DTNHost, Map<DTNHost, ArrayList<Double>>> directTrustMap;
    private static Map<DTNHost, Map<DTNHost, ArrayList<Double>>> trustMap;
    private static Map<DTNHost, ArrayList<Double>> suspensionMap;
    private static List<Double> intervalTime;
    private double lastUpdated = 0;
    private double updateInterval = 21600;

    public DirectTrustOvertime() {
        directTrustMap = new HashMap<DTNHost, Map<DTNHost, ArrayList<Double>>>();
        trustMap = new HashMap<DTNHost, Map<DTNHost, ArrayList<Double>>>();
        suspensionMap = new HashMap<DTNHost, ArrayList<Double>>();
        intervalTime = new ArrayList<Double>();
        lastUpdated = 0;
        updateInterval = 21600;
        init();
    }

    @Override
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
                    Map<DTNHost, ArrayList<Double>> initTrust = new HashMap<DTNHost, ArrayList<Double>>();
                    ArrayList listTrust = new ArrayList();
                    initTrust.put(m, listTrust);

                    if (SimScenario.getInstance().mode == 2) {
                        if (!trustMap.containsKey(v)) {
                            trustMap.put(v, initTrust);
                        } else {
                            trustMap.get(v).put(m, listTrust);
                        }
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
                        listDT.add(new Double(QLearn.getQInstance().directTrust.get(ver).get(h)));
                        Map<DTNHost, ArrayList<Double>> curDT = directTrustMap.get(ver);
                        curDT.put(h, listDT);
                        directTrustMap.put(ver, curDT);

                        if (SimScenario.getInstance().mode == 2) {
                            SimScenario.getInstance().getFb().setVariable("directTrust", QLearn.getQInstance().directTrust.get(ver).get(h));
                            SimScenario.getInstance().getFb().setVariable("indirectTrust", QLearn.getQInstance().getAvgIT(h));
                            SimScenario.getInstance().getFb().setVariable("suspension", QLearn.getQInstance().suspension.get(h));
                            SimScenario.getInstance().getFb().evaluate();

                            double trust = SimScenario.getInstance().getFb().getVariable("trust").getValue();
                            ArrayList listTrust = trustMap.get(ver).get(h);
                            listTrust.add(new Double(trust));
                            Map<DTNHost, ArrayList<Double>> curTrust = trustMap.get(ver);
                            curTrust.put(h, listTrust);
                            trustMap.put(ver, curTrust);
                        }

                        ArrayList listSus = suspensionMap.get(h);
                        listSus.add(new Double(QLearn.getQInstance().suspension.get(h)));
                        suspensionMap.put(h, listSus);
                    }
                }
            }
//            System.out.println(directTrustMap);
        }

    }

    @Override
    public void done() {
        System.out.println(SimScenario.getInstance().getVerificator());
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
        for (Map.Entry<DTNHost, ArrayList<Double>> listIT : QLearn.getQInstance().indirectTrust.entrySet()) {
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

        if (SimScenario.getInstance().mode == 2) {
            write("Trust Overtime");
            for (Map.Entry<DTNHost, Map<DTNHost, ArrayList<Double>>> listHost : trustMap.entrySet()) {
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
        }
        super.done();
    }
}
